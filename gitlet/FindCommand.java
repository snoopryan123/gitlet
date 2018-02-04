package gitlet;

import java.io.Serializable;

/** The find command.
 *  @author Ryan Brill
 */
class FindCommand implements Command, Serializable {

    /** Find this logMessage L. */
    FindCommand(String L) {
        logMessage = L;
    }

    /** Return true iff N is the proper number of operands for this command. */
    public boolean properNumOperands(int n) {
        return n == 2;
    }

    /** Execute this command, using TRACKER. */
    public void execute(Tracker tracker) {
        boolean found = false;

        for (String commitRef : tracker.getAllCommits()) {
            Commit C = Utils.shaToCommit(commitRef);
            if (C.getLogMessage().equals(logMessage)) {
                found = true;
                System.out.println(commitRef);
            }
        }

        if (!found) {
            System.out.println("Found no commit with that message.");
            System.exit(0);
        }
    }

    /** The desired Log Message. */
    private String logMessage;
}
