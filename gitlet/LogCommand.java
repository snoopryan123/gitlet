package gitlet;

import java.io.File;
import java.io.Serializable;

/** The init command.
 *  @author Ryan Brill
 */
class LogCommand implements Command, Serializable {

    /** Return true iff N is the proper number of operands for this command. */
    public boolean properNumOperands(int n) {
        return n == 1;
    }

    /** Execute this command, using TRACKER. */
    public void execute(Tracker tracker) {
        String headCommitSha = tracker.getCurrentBranchReference();
        Commit headCommit = Utils.readObject(
                new File(Utils.gitletPath(headCommitSha)), Commit.class);
        if (headCommit == null) {
            return;
        }
        Commit current = headCommit;
        while (true) {
            System.out.println(current);
            String parentSha = current.getParentReference();
            if (parentSha.equals("")) {
                break;
            }
            current = Utils.readObject(
                    new File(Utils.gitletPath(parentSha)), Commit.class);
        }
    }

}
