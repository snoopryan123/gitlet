package gitlet;

import java.io.File;
import java.io.Serializable;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

/** The init command.
 *  @author Ryan Brill
 */
class InitCommand implements Command, Serializable {

    /** Return true iff N is the proper number of operands for this command. */
    public boolean properNumOperands(int n) {
        return n == 1;
    }

    /** Execute this command, using TRACKER. */
    public void execute(Tracker tracker) {
        (new File(Utils.gitletPath(""))).mkdir();

        Commit initialCommit = new Commit(
            "initial commit", LocalDateTime.ofInstant(
                Instant.EPOCH, ZoneId.of("America/Los_Angeles")), "");

        Utils.writeObject(initialCommit);

        Main.initializeTracker(initialCommit);
    }

}
