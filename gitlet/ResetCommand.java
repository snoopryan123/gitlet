package gitlet;

import java.io.File;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.HashSet;

/** The reset command.
 *  @author Ryan Brill
 */
class ResetCommand implements Command, Serializable {

    /** Set C to be the commitSha of this checkout command. */
    ResetCommand(String C) {
        commitSha = Utils.fullSha(C);
    }

    /** Return true iff N is the proper number of operands for this command. */
    public boolean properNumOperands(int n) {
        return n == 2;
    }

    /** Execute this command, using TRACKER. */
    public void execute(Tracker tracker) {
        if (!tracker.commitShaExists(commitSha)) {
            System.out.println("No commit with that id exists.");
            System.exit(0);
        }


        Commit commit = Utils.readObject(
                new File(Utils.gitletPath(commitSha)),
                Commit.class
        );
        HashMap<String, String> blobs = commit.blobMap();

        HashSet<String> workingDirListing = Utils.workingDirFileNames();
        for (String fileName : workingDirListing) {
            if (!fileName.startsWith(".") && tracker.isUntracked(fileName)) {
                if (blobs.containsKey(fileName)) {
                    System.out.println(
                            "There is an untracked file in the way; "
                                    + "delete it or add it first.");
                    System.exit(0);
                }
            }
        }

        for (String fileName : blobs.keySet()) {
            Blob blob = Utils.readObject(
                    new File(Utils.gitletPath(blobs.get(fileName))),
                    Blob.class
            );
            Utils.writeContents(
                    new File(Utils.workingDirPath(fileName)),
                    new String(blob.getContents(), StandardCharsets.UTF_8)
            );
        }

        String headCommitSha = tracker.getCurrentBranchReference();
        Commit headCommit = Utils.readObject(
                new File(Utils.gitletPath(headCommitSha)),
                Commit.class
        );
        for (String fileName : headCommit.blobMap().keySet()) {
            if (!blobs.containsKey(fileName)) {

                if (Utils.workingDirFileNames().contains(fileName)) {
                    (new File(Utils.workingDirPath(fileName))).delete();
                }

            }
        }

        tracker.setCurrentBranchReference(commitSha);
        tracker.clearStage();
    }

    /** The SHA of the commit. */
    private String commitSha;

}
