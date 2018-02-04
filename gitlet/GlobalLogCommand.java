package gitlet;

import java.io.File;
import java.io.Serializable;

/** The global-log command.
 *  @author Ryan Brill
 */
class GlobalLogCommand implements Command, Serializable {

    /** Return true iff N is the proper number of operands for this command. */
    public boolean properNumOperands(int n) {
        return n == 1;
    }

    /** Execute this command, using TRACKER. */
    public void execute(Tracker tracker) {
        for (String commitRef : tracker.getAllCommits()) {
            Commit C = Utils.readObject(
                    new File(Utils.gitletPath(commitRef)), Commit.class);
            System.out.println(C);
        }
    }
}
