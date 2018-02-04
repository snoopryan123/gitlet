package gitlet;

import java.io.File;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashSet;

/** Merges files from the given branch into the current branch.
 *  @author Ryan Brill
 */
class MergeCommand implements Command, Serializable {

    /** Set B to be the branchName of this checkout command. */
    MergeCommand(String B) {
        branchName = B;
    }

    /** Return true iff N is the proper number of operands for this command. */
    public boolean properNumOperands(int n) {
        return n == 2;
    }

    /** Execute this command, using TRACKER. */
    public void execute(Tracker tracker) {
        performErrorChecks(tracker);

        String givenBranchSha = tracker.getBranchReference(branchName);
        Commit givenBranch = Utils.shaToCommit(givenBranchSha);
        String currentBranchName = tracker.getCurrentBranchName();
        String currentBranchSha = tracker.getCurrentBranchReference();
        Commit currentBranch = Utils.shaToCommit(currentBranchSha);
        String splitPointSha = findSplitPoint(givenBranchSha, currentBranchSha);
        Commit splitPoint = Utils.shaToCommit(splitPointSha);
        if (splitPointSha.equals(givenBranchSha)) {
            System.out.println(
                    "Given branch is an ancestor of the current branch."
            );
            System.exit(0);
        }
        if (splitPointSha.equals(currentBranchSha)) {
            tracker.setCurrentBranchReference(
                    tracker.getBranchReference(branchName)
            );
            System.out.println("Current branch fast-forwarded.");
            System.exit(0);
        }
        HashSet<String> presentAtSplitPoint =
                new HashSet<>(splitPoint.blobMap().keySet());
        HashSet<String> presentAtGivenBranch =
                new HashSet<>(givenBranch.blobMap().keySet());
        HashSet<String> presentAtCurrentBranch =
                new HashSet<>(currentBranch.blobMap().keySet());
        HashSet<String> modifiedInGivenBranch =
                modifiedSinceSplitPoint(givenBranch, splitPoint, true);
        HashSet<String> modifiedInCurrentBranch =
                modifiedSinceSplitPoint(currentBranch, splitPoint, true);
        HashSet<String> notModifiedInCurrentBranch =
                modifiedSinceSplitPoint(currentBranch, splitPoint, false);
        helper(modifiedInGivenBranch, notModifiedInCurrentBranch, tracker,
                givenBranchSha, givenBranch, presentAtGivenBranch,
                presentAtSplitPoint, presentAtCurrentBranch);
        HashSet<String> modifiedInBoth =
                new HashSet<>(modifiedInCurrentBranch);
        modifiedInBoth.retainAll(modifiedInGivenBranch);
        HashSet<String> modifiedInSameWay =
            modifiedInTheSameWay(modifiedInBoth, currentBranch, givenBranch);
        HashSet<String> inConflict =
                presentInBoth(presentAtCurrentBranch, presentAtGivenBranch);
        inConflict.removeAll(modifiedInSameWay);
        HashSet<String> absentInCurrentChangedInGiven =
                new HashSet<>(modifiedInGivenBranch);
        absentInCurrentChangedInGiven.removeAll(presentAtCurrentBranch);
        inConflict.addAll(absentInCurrentChangedInGiven);
        HashSet<String> absentInGivenChangedInCurrent =
                new HashSet<>(modifiedInCurrentBranch);
        absentInGivenChangedInCurrent.removeAll(presentAtGivenBranch);
        inConflict.addAll(absentInGivenChangedInCurrent);
        handleMergeConflicts(inConflict, currentBranch,
                givenBranch, currentBranchName, tracker);
    }

    /** Perform part of the merge command, using
     *  MODIFIEDINGIVENBRANCH, NOTMODIFIEDINCURRENTBRANCH,
     *  TRACKER, GIVENBRANCHSHA, GIVENBRANCH,
     *  PRESENTATGIVENBRANCH, PRESENTATSPLITPOINT,
     *  PRESENTATCURRENTBRANCH. */
    private void helper(
            HashSet<String> modifiedInGivenBranch,
            HashSet<String> notModifiedInCurrentBranch,
            Tracker tracker, String givenBranchSha,
            Commit givenBranch, HashSet<String> presentAtGivenBranch,
            HashSet<String> presentAtSplitPoint,
            HashSet<String> presentAtCurrentBranch) {

        HashSet<String> modifiedInGivenNotCurrent =
                new HashSet<>(modifiedInGivenBranch);
        modifiedInGivenNotCurrent.retainAll(notModifiedInCurrentBranch);
        for (String fileName : modifiedInGivenNotCurrent) {
            (new CheckoutCommand(givenBranchSha, "--", fileName))
                    .execute(tracker);
            tracker.stage(fileName, givenBranch.getBlobReference(fileName));
        }

        HashSet<String> presentOnlyInGivenBranch =
                new HashSet<>(presentAtGivenBranch);
        presentOnlyInGivenBranch.removeAll(presentAtSplitPoint);
        presentOnlyInGivenBranch.removeAll(presentAtCurrentBranch);
        for (String fileName : presentOnlyInGivenBranch) {
            (new CheckoutCommand(givenBranchSha, "--", fileName))
                    .execute(tracker);
            tracker.stage(fileName, givenBranch.getBlobReference(fileName));
        }

        HashSet<String> removeThese =
                new HashSet<>(presentAtSplitPoint);
        removeThese.retainAll(notModifiedInCurrentBranch);
        removeThese.removeAll(presentAtGivenBranch);
        for (String fileName : removeThese) {
            (new RmCommand(fileName)).execute(tracker);
        }
    }

    /** Using TRACKER, CURRENTBRANCH, GIVENBRANCH, CURRENTBRANCHNAME,
     *  handle the merge conflicts of the fileNames in INCONFLICT. */
    private void handleMergeConflicts(HashSet<String> inConflict,
          Commit currentBranch, Commit givenBranch,
          String currentBranchName, Tracker tracker) {
        for (String fileName : inConflict) {
            String contentsInCurrentBranch;
            try {
                String blobShaInCurrentBranch =
                        currentBranch.getBlobReference(fileName);
                byte[] currentBranchBtyeContents =
                    Utils.shaToBlob(blobShaInCurrentBranch).getContents();
                contentsInCurrentBranch =
                            new String(currentBranchBtyeContents,
                            StandardCharsets.UTF_8
                    );
            } catch (NullPointerException | IllegalArgumentException e) {
                contentsInCurrentBranch = "";
            }

            String contentsInGivenBranch;
            try {
                String blobShaInGivenBranch =
                    givenBranch.getBlobReference(fileName);
                byte[] givenBranchBtyeContents =
                    Utils.shaToBlob(blobShaInGivenBranch).getContents();
                contentsInGivenBranch =
                    new String(givenBranchBtyeContents, StandardCharsets.UTF_8);
            } catch (NullPointerException | IllegalArgumentException e) {
                contentsInGivenBranch = "";
            }

            String contents =
                    "<<<<<<< HEAD\n" + contentsInCurrentBranch
                    + "=======\n" + contentsInGivenBranch + ">>>>>>>\n";
            Utils.writeContents(
                    new File(Utils.workingDirPath(fileName)),
                    contents
            );
            (new AddCommand(fileName)).execute(tracker);
        }

        String logMessage = "Merged " + branchName
                            + " into " + currentBranchName + ".";
        (new CommitCommand(logMessage)).execute(tracker);

        if (!inConflict.isEmpty()) {
            System.out.println("Encountered a merge conflict.");
        }

    }

    /** Return the filenames that are
     *  PRESENTATCURRENTBRANCH and PRESENTATGIVENBRANCH. */
    private HashSet<String> presentInBoth(
            HashSet<String> presentAtCurrentBranch,
            HashSet<String> presentAtGivenBranch) {
        HashSet<String> presentInBoth = new HashSet<>();
        for (String fileName : presentAtCurrentBranch) {
            if (presentAtGivenBranch.contains(fileName)) {
                presentInBoth.add(fileName);
            }
        }
        return presentInBoth;
    }

    /** Return a list of the FILENAMES that have been MODIFIEDINBOTH,
     *  i.e. modified in the same way in the CURRENTBRANCH & GIVENBRANCH. */
    private HashSet<String> modifiedInTheSameWay(HashSet<String> modifiedInBoth,
                     Commit currentBranch, Commit givenBranch) {
        HashSet<String> modifiedInSameWay = new HashSet<>(modifiedInBoth);
        for (String fileName : modifiedInBoth) {
            String shaInCurrentBranch =
                    currentBranch.getBlobReference(fileName);
            String shaInGivenBranch = givenBranch.getBlobReference(fileName);
            if (!shaInCurrentBranch.equals(shaInGivenBranch)) {
                modifiedInSameWay.remove(fileName);
            }
        }
        return modifiedInSameWay;
    }

    /** Return a set of fileNames that have (if MODIFIED is true),
     *  or have not (if MODIFIED is false),
     *  been modified in the commit BRANCHHEAD since the commit SPLITPOINT. */
    private HashSet<String> modifiedSinceSplitPoint(
            Commit branchHead, Commit splitPoint, boolean modified) {
        HashSet<String> fileNamesInBranchHead =
                new HashSet<>(branchHead.blobMap().keySet());
        HashSet<String> fileNamesInSplitPoint =
                new HashSet<>(splitPoint.blobMap().keySet());
        fileNamesInBranchHead.retainAll(fileNamesInSplitPoint);

        HashSet<String> modifiedFileNames = new HashSet<>();
        for (String fileName : fileNamesInBranchHead) {
            String shaInSplitPoint = splitPoint.getBlobReference(fileName);
            String shaInBranchHead = branchHead.getBlobReference(fileName);
            if (modified && !shaInSplitPoint.equals(shaInBranchHead)) {
                modifiedFileNames.add(fileName);
            } else if (!modified && shaInSplitPoint.equals(shaInBranchHead)) {
                modifiedFileNames.add(fileName);
            }
        }
        return modifiedFileNames;
    }


    /** Return the split point of the commits associated with
     *  COMMIT1SHA and COMMIT2SHA, using TRACKER. */
    private String findSplitPoint(String commit1Sha, String commit2Sha) {
        ArrayList<String> commit1History =
                new ArrayList<>(getCommitHistory(commit1Sha));
        ArrayList<String> commit2History =
                new ArrayList<>(getCommitHistory(commit2Sha));
        commit1History.retainAll(commit2History);
        return (commit1History.isEmpty()) ? "" : commit1History.get(0);
    }

    /** Return the commit history of the commit associated with COMMITSHA,
     * all the way back to the initial commit. */
    private ArrayList<String> getCommitHistory(String commitSha) {
        ArrayList<String> history = new ArrayList<>();
        Commit commit = Utils.shaToCommit(commitSha);
        while (!commitSha.equals("")) {
            history.add(commitSha);
            commitSha = commit.getParentReference();
            if (commitSha.equals("")) {
                break;
            }
            commit = Utils.shaToCommit(commitSha);
        }
        return history;
    }


    /** Using TRACKER, perform the error checks for this merge command. */
    private void performErrorChecks(Tracker tracker) {
        if (!tracker.getStagingArea().isEmpty()
                || !tracker.getRemoveThese().isEmpty()) {
            System.out.println("You have uncommitted changes.");
            System.exit(0);
        }

        if (!tracker.branchNameExists(branchName)) {
            System.out.println("A branch with that name does not exist.");
            System.exit(0);
        }

        if (tracker.getCurrentBranchName().equals(branchName)) {
            System.out.println("Cannot merge a branch with itself.");
            System.exit(0);
        }

        tracker.checkUntrackedFileInTheWay();
    }

    /** The name of the branch to be merged with this command. */
    private String branchName;
}
