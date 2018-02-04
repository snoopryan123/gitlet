package gitlet;

import java.io.Serializable;

/** The branch command.
 *  @author Ryan Brill
 */
class BranchCommand implements Command, Serializable {

    /** Set B to be the branchName of this checkout command. */
    BranchCommand(String B) {
        branchName = B;
    }

    /** Return true iff N is the proper number of operands for this command. */
    public boolean properNumOperands(int n) {
        return n == 2;
    }

    /** Execute this command, using TRACKER. */
    public void execute(Tracker tracker) {
        if (tracker.branchNameExists(branchName)) {
            System.out.println("A branch with that name already exists.");
            System.exit(0);
        }
        tracker.addBranch(branchName);
    }

    /** The name of the branch to be added with this command. */
    private String branchName;
}
