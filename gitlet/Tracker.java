package gitlet;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

/** The tracker - tracks all things under the hood in gitlet,
 *  including branches, blobs, commits, etc.
 *  @author Ryan Brill
 */
class Tracker implements Serializable {

    /** The tracker is created with INITIALBRANCHNAME and INITIALCOMMIT. */
    Tracker(String initialBranchName, Commit initialCommit) {
        stagingArea = new HashMap<>();
        removeThese = new ArrayList<>();
        currentBranchName = initialBranchName;
        branches = new HashMap<>();
        branches.put(initialBranchName,
                Utils.sha1(Utils.serialize(initialCommit)));
        allCommits = new ArrayList<>();
    }

    /* BRANCHES. */

    /** Key: Branch name // Value: Branch head commit SHA1 reference. */
    private HashMap<String, String> branches;

    /** Add a branch with name BRANCHNAME to branches,
     *  and make its head reference the head of the current active branch. */
    void addBranch(String branchName) {
        branches.put(branchName, getCurrentBranchReference());
    }

    /** Delete the branch with name BRANCHNAME, and its associated pointer,
     *  from branches. */
    void deleteBranch(String branchName) {
        branches.remove(branchName);
    }

    /** Return the HashMap containing info about all the branches. */
    HashMap<String, String> getBranches() {
        return branches;
    }

    /** Branch name of the current active branch. */
    private String currentBranchName;

    /** Return true iff a branch with name BRANCHNAME already exists. */
    boolean branchNameExists(String branchName) {
        return branches.containsKey(branchName);
    }

    /** Return the reference of the branch named BRANCHNAME. */
    String getBranchReference(String branchName) {
        return branches.get(branchName);
    }

    /** Change the name of the current active branch to BRANCHNAME. */
    void changeCurrentBranchName(String branchName) {
        currentBranchName = branchName;
    }

    /** Return the active branch name. */
    String getCurrentBranchName() {
        return currentBranchName;
    }

    /** Return the SHA reference of the head commit of the current branch. */
    String getCurrentBranchReference() {
        return branches.get(currentBranchName);
    }

    /** Change the SHA of the head commit of the current branch to REF. */
    void setCurrentBranchReference(String ref) {
        branches.put(currentBranchName, ref);
    }

    /** Return true iff FILENAME is not tracked in the current branch. */
    boolean isUntracked(String fileName) {
        String headCommitSha = getCurrentBranchReference();
        Commit headCommit = Utils.readObject(
                new File(Utils.gitletPath(headCommitSha)), Commit.class);
        HashMap<String, String> blobs = headCommit.blobMap();
        return !blobs.containsKey(fileName) && !isStaged(fileName);
    }

    /* THE REMOVAL QUEUE. */

    /** List of fileNames to be removed in the next commit. */
    private ArrayList<String> removeThese;

    /** Return the list of fileNames to be removed. */
    ArrayList<String> getRemoveThese() {
        return removeThese;
    }

    /** Allow FILENAME to be removed in the next commit. */
    void remove(String fileName) {
        removeThese.add(fileName);
    }

    /** After a commit, there is nothing left to be removed. */
    void clearRemoveThese() {
        removeThese.clear();
    }

    /** Return the list of fileNames to be removed from the upcoming commit. */
    ArrayList<String> toBeRemoved() {
        return removeThese;
    }

    /** Return true if FILENAME is staged for removal. */
    boolean isStagedForRemoval(String fileName) {
        return removeThese.contains(fileName);
    }

    /** Cancel the removal of FILENAME by deleting it from removeThese. */
    void cancelRemoval(String fileName) {
        removeThese.remove(fileName);
    }

    /* THE STAGING AREA (STAGE). */

    /** Key: File name of staged files // Value: SHA reference of file. */
    private HashMap<String, String> stagingArea;

    /** Add the Blob with name FILENAME and sha SHA to the Staging Area. */
    void stage(String fileName, String sha) {
        stagingArea.put(fileName, sha);
    }

    /** Return true if FILENAME is staged. */
    boolean isStaged(String fileName) {
        return stagingArea.containsKey(fileName);
    }

    /** Clear the staging area (After a commit, or checkout case3). */
    void clearStage() {
        stagingArea = new HashMap<>();
    }

    /** Remove fileName from the staging area (unStage FILENAME). */
    void removeFromStage(String fileName) {
        stagingArea.remove(fileName);
    }

    /** Return the staging area. */
    HashMap<String, String> getStagingArea() {
        return stagingArea;
    }

    /* STUFF DEALING WITH COMMITS. */

    /** A list containing references to every commit in .gitlet repo. */
    private ArrayList<String> allCommits;

    /** Add REF, a reference to a commit, to allCommits. */
    void addCommit(String ref) {
        allCommits.add(ref);
    }

    /** Return the list containing references to every commit. */
    ArrayList<String> getAllCommits() {
        return allCommits;
    }

    /** Return true iff a commit with Sha COMMITSHA exists. */
    boolean commitShaExists(String commitSha) {
        return allCommits.contains(commitSha);
    }

    /* REMOTE DIRECTORIES. */
    /** Key: Name of remote repo. // Value: Path to remote directory. */
    private HashMap<String, String> remotes = new HashMap<>();

    /** Add remote repo with name NAME and directory PATH. */
    void addRemote(String name, String path) {
        remotes.put(name, path);
    }

    /* Miscellaneous. */

    /** If a working file is untracked in the current branch,
     *  and would be overwritten by checkout, reset, or merge,
     *  end the program and print an error. */
    void checkUntrackedFileInTheWay() {
        File[] workingDirectoryListing = (
                new File(Utils.workingDirPath("."))).listFiles();
        for (File F : workingDirectoryListing) {
            if (!F.getName().startsWith(".") && isUntracked(F.getName())) {
                System.out.println(
                    "There is an untracked file in the way; "
                    + "delete it or add it first.");
                System.exit(0);
            }
        }
    }

    /** Return true if FILENAME in the working directory
     *  has changed from its contents in COMMIT. */
    boolean changedFromCommit(String fileName, Commit commit) {
        byte[] currentContents = Utils.readContents(
                new File(Utils.workingDirPath(fileName)));
        Blob currentBlob = new Blob(currentContents);
        String currentBlobSha = Utils.sha1(Utils.serialize(currentBlob));
        String previousBlobSha = commit.getBlobReference(fileName);
        return !currentBlobSha.equals(previousBlobSha);
    }

    /** Return true if FILENAME in the working directory
     *  has changed from its contents when it was staged. */
    boolean changedFromStaging(String fileName) {
        byte[] currentContents = Utils.readContents(
                new File(Utils.workingDirPath(fileName)));
        Blob currentBlob = new Blob(currentContents);
        String currentBlobSha = Utils.sha1(Utils.serialize(currentBlob));
        String previousBlobSha = stagingArea.get(fileName);
        return !currentBlobSha.equals(previousBlobSha);
    }


}
