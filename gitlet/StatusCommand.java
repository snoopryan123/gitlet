package gitlet;

import java.io.File;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Set;
import java.util.Collections;
import java.util.ArrayList;

/** The status command.
 *  @author Ryan Brill
 */
class StatusCommand implements Command, Serializable {

    /** Return true iff N is the proper number of operands for this command. */
    public boolean properNumOperands(int n) {
        return n == 1;
    }

    /** Execute this command, using TRACKER. */
    public void execute(Tracker tracker) {
        displayBranches(tracker);
        displayStaged(tracker);
        displayMarkedForUntracking(tracker);
        String headCommitReference = tracker.getCurrentBranchReference();
        Commit headCommit = Utils.readObject(
                new File(Utils.gitletPath(headCommitReference)),
                Commit.class
        );
        displayModificationsNotStaged(tracker, headCommit);
        displayUntracked(tracker, headCommit);
    }

    /** Using TRACKER, display what branches currently exist,
     *  and mark the current branch with a '*'. */
    private void displayBranches(Tracker tracker) {
        HashMap<String, String> branches = tracker.getBranches();
        Set<String> branchNamesSet = branches.keySet();
        ArrayList<String> branchNames = new ArrayList<>();
        branchNames.addAll(branchNamesSet);
        Collections.sort(branchNames);

        System.out.println("=== Branches ===");
        for (String branchName : branchNames) {
            if (branchName.equals(tracker.getCurrentBranchName())) {
                System.out.println("*" + branchName);
            } else {
                System.out.println(branchName);
            }
        }
        System.out.println();
    }

    /** Using TRACKER, display what files have been staged. */
    private void displayStaged(Tracker tracker) {
        HashMap<String, String> stage = tracker.getStagingArea();
        Set<String> stagedNamesSet = stage.keySet();
        ArrayList<String> stagedNames = new ArrayList<>();
        stagedNames.addAll(stagedNamesSet);
        Collections.sort(stagedNames);

        System.out.println("=== Staged Files ===");
        for (String fileName : stagedNames) {
            System.out.println(fileName);
        }
        System.out.println();
    }

    /** Using TRACKER, display what files have been marked for untracking. */
    private void displayMarkedForUntracking(Tracker tracker) {
        ArrayList<String> toBeRemoved = tracker.toBeRemoved();
        Collections.sort(toBeRemoved);

        System.out.println("=== Removed Files ===");
        for (String fileName : toBeRemoved) {
            System.out.println(fileName);
        }
        System.out.println();
    }

    /** Using TRACKER, Display files that have been modified
     *  in the HEADCOMMIT but not staged. */
    private void displayModificationsNotStaged(
            Tracker tracker, Commit headCommit) {
        ArrayList<String> modifiedNotStaged = new ArrayList<>();

        Set<String> workingDirFiles = Utils.workingDirFileNames();

        for (String fileName : workingDirFiles) {
            if (
                    headCommit.isTracking(fileName)
                    && tracker.changedFromCommit(fileName, headCommit)
                    && !tracker.isStaged(fileName)) {
                modifiedNotStaged.add(fileName + " (modified)");
            } else if (
                tracker.isStaged(fileName)
                && tracker.changedFromStaging(fileName)) {
                modifiedNotStaged.add(fileName + " (modified)");
            }
        }

        Set<String> staged = tracker.getStagingArea().keySet();
        for (String fileName : staged) {
            if (!workingDirFiles.contains(fileName)) {
                modifiedNotStaged.add(fileName + " (deleted)");
            }
        }

        Set<String> trackedInCurrentCommit = headCommit.blobMap().keySet();
        ArrayList<String> stagedForRemoval = tracker.toBeRemoved();

        for (String fileName : trackedInCurrentCommit) {
            if (!stagedForRemoval.contains(fileName)
                    && !workingDirFiles.contains(fileName)) {
                modifiedNotStaged.add(fileName + " (deleted)");
            }
        }

        Collections.sort(modifiedNotStaged);
        System.out.println("=== Modifications Not Staged For Commit ===");
        for (String fileName : modifiedNotStaged) {
            System.out.println(fileName);
        }
        System.out.println();
    }

    /** Using TRACKER, Display files that unTracked, i.e. present in the
     *  working directory but neither staged nor tracked by HEADCOMMIT. */
    private void displayUntracked(Tracker tracker, Commit headCommit) {
        ArrayList<String> untracked = new ArrayList<>();

        for (String fileName : Utils.workingDirFileNames()) {
            if ((!tracker.isStaged(fileName)
                    || tracker.isStagedForRemoval(fileName))
                    && !headCommit.isTracking(fileName)) {
                untracked.add(fileName);
            }
        }

        Collections.sort(untracked);
        System.out.println("=== Untracked Files ===");
        for (String fileName : untracked) {
            System.out.println(fileName);
        }
        System.out.println();
    }
}

