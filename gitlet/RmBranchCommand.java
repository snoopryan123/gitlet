package gitlet;

import java.io.Serializable;

/** The rm-branch command.
 *  @author Ryan Brill
 */
class RmBranchCommand implements Command, Serializable {

    /** Set B to be the branchName of this checkout command. */
    RmBranchCommand(String B) {
        branchName = B;
    }

    /** Return true iff N is the proper number of operands for this command. */
    public boolean properNumOperands(int n) {
        return n == 2;
    }

    /** Execute this command, using TRACKER. */
    public void execute(Tracker tracker) {
        if (!tracker.branchNameExists(branchName)) {
            System.out.println("A branch with that name does not exist.");
            System.exit(0);
        }
        if (tracker.getCurrentBranchName().equals(branchName)) {
            System.out.println("Cannot remove the current branch.");
            System.exit(0);
        }
        tracker.deleteBranch(branchName);
    }

    /** The name of the branch to be added with this command. */
    private String branchName;
}
