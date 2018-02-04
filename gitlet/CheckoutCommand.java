package gitlet;

import java.io.File;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.HashSet;

/** The checkout command.
 *  @author Ryan Brill
 */
class CheckoutCommand implements Command, Serializable {

    /** Set (A1, A2, A3) to be the arguments of this checkout command. */
    CheckoutCommand(String A1, String A2, String A3) {
        args1 = A1;
        args2 = A2;
        args3 = A3;
    }

    /** Return true iff N is the proper number of operands for this command. */
    public boolean properNumOperands(int n) {
        return n == 2 || n == 3 || n == 4;
    }

    /** Execute this command, using TRACKER. */
    public void execute(Tracker tracker) {
        if (args1.equals("--") && !args2.equals("") && args3.equals("")) {
            executeCase1(tracker, args2);
        } else if (!args1.equals("")
                && args2.equals("--")
                && !args3.equals("")) {
            executeCase2(tracker, args1, args3);
        } else if (!args1.equals("") && args2.equals("") && args3.equals("")) {
            executeCase3(tracker, args1);
        } else {
            System.out.println("Incorrect operands.");
            System.exit(0);
        }
    }

    /** Execute Case 1: checkout FILENAME, using TRACKER. */
    private void executeCase1(Tracker tracker, String fileName) {
        String headCommitSha = tracker.getCurrentBranchReference();
        Commit headCommit = Utils.readObject(
                new File(Utils.gitletPath(headCommitSha)),
                Commit.class
        );
        String fileInHeadSha = headCommit.blobMap().get(fileName);
        if (fileInHeadSha == null || fileInHeadSha.equals("")) {
            System.out.println("File does not exist in that commit.");
            System.exit(0);
        }
        Blob headFileBlob = Utils.shaToBlob(fileInHeadSha);
        Utils.writeContents(
                new File(Utils.workingDirPath(fileName)),
                new String(headFileBlob.getContents(), StandardCharsets.UTF_8)
        );
    }

    /** Execute Case 2: checkout FILENAME from COMMITSHA using TRACKER. */
    private void executeCase2(
            Tracker tracker, String commitSha, String fileName) {
        commitSha = Utils.fullSha(commitSha);
        File commitFile = new File(Utils.gitletPath(commitSha));
        if (!commitFile.exists()) {
            System.out.println("No commit with that id exists.");
            System.exit(0);
        }
        Commit commit = Utils.readObject(commitFile, Commit.class);
        String fileInCommitSha = commit.blobMap().get(fileName);
        if (fileInCommitSha == null || fileInCommitSha.equals("")) {
            System.out.println("File does not exist in that commit.");
            System.exit(0);
        }
        Blob fileInCommitBlob = Utils.readObject(
                new File(Utils.gitletPath(fileInCommitSha)),
                Blob.class
        );
        Utils.writeContents(
                new File(Utils.workingDirPath(fileName)),
                new String(fileInCommitBlob.getContents(),
                        StandardCharsets.UTF_8)
        );
    }

    /** Execute Case 3, checkout BRANCHNAME, using TRACKER. */
    private void executeCase3(Tracker tracker, String branchName) {

        if (!tracker.getBranches().containsKey(branchName)) {
            System.out.println("No such branch exists.");
            System.exit(0);
        }
        if (branchName.equals(tracker.getCurrentBranchName())) {
            System.out.println("No need to checkout the current branch.");
            System.exit(0);
        }

        String checkedOutBranchHeadCommitSha =
                tracker.getBranches().get(branchName);
        Commit checkedOutBranchHeadCommit =
                Utils.readObject(
                    new File(Utils.gitletPath(checkedOutBranchHeadCommitSha)),
                    Commit.class
                );
        HashMap<String, String> checkedOutBlobs =
                checkedOutBranchHeadCommit.blobMap();

        HashSet<String> workingDirListing = Utils.workingDirFileNames();
        for (String fileName : workingDirListing) {
            if (!fileName.startsWith(".") && tracker.isUntracked(fileName)) {
                if (checkedOutBlobs.containsKey(fileName)) {
                    System.out.println(
                            "There is an untracked file in the way; "
                            + "delete it or add it first.");
                    System.exit(0);
                }
            }
        }

        for (String fileName : checkedOutBlobs.keySet()) {
            Blob blob = Utils.readObject(
                    new File(Utils.gitletPath(checkedOutBlobs.get(fileName))),
                    Blob.class
            );
            Utils.writeContents(
                    new File(Utils.workingDirPath(fileName)),
                    new String(blob.getContents(), StandardCharsets.UTF_8)
            );
        }

        String headCommitSha = tracker.getCurrentBranchReference();
        Commit headCommit = Utils.readObject(
                new File(Utils.gitletPath(headCommitSha)), Commit.class);
        HashMap<String, String> trackedBlobs = headCommit.blobMap();
        for (String fileName : trackedBlobs.keySet()) {
            if (!checkedOutBlobs.containsKey(fileName)) {
                (new File(Utils.workingDirPath(fileName))).delete();
            }
        }

        tracker.clearStage();
        tracker.changeCurrentBranchName(branchName);
    }

    /** The operand in index 1 of args in the Main method.
     *  Case 1: "--" // Case 2: [commit id] // Case 3: [branch name] .*/
    private String args1;

    /** The operand in index 2 of args in the Main method.
     *  Case 1: [file name] // Case 2: "--" // Case 3: "" .*/
    private String args2;

    /** The operand in index 3 of args in the Main method.
     *  Case 1: ""// Case 2: "[file name]" // Case 3: "" .*/
    private String args3;

}
