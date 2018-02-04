package gitlet;

import java.io.File;
import java.io.Serializable;

/** The rm command.
 *  @author Ryan Brill
 */
class RmCommand implements Command, Serializable {

    /** Remove the file named F.*/
    RmCommand(String F) {
        fileName = F;
    }

    /** Return true iff N is the proper number of operands for this command. */
    public boolean properNumOperands(int n) {
        return n == 2;
    }

    /** Execute this command, using TRACKER. */
    public void execute(Tracker tracker) {

        String headCommitReference = tracker.getCurrentBranchReference();
        Commit headCommit = Utils.readObject(
                new File(Utils.gitletPath(headCommitReference)), Commit.class);

        if (!tracker.isStaged(fileName) && !headCommit.isTracking(fileName)) {
            System.out.println("No reason to remove the file.");
            System.exit(0);
        }

        tracker.removeFromStage(fileName);

        if (headCommit.isTracking(fileName)) {
            tracker.remove(fileName);
            if (Utils.workingDirFileNames().contains(fileName)) {
                (new File(Utils.workingDirPath(fileName))).delete();
            }
        }
    }

    /** The name of the file to be removed. */
    private String fileName;
}
