package gitlet;

import java.io.File;
import java.io.Serializable;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HashMap;

/** The commit command.
 *  @author Ryan Brill
 */
class CommitCommand implements Command, Serializable {

    /** The commit created by this command will have logMessage L. */
    CommitCommand(String L) {
        logMessage = L;
    }

    /** Return true iff N is the proper number of operands for this command. */
    public boolean properNumOperands(int n) {

        return n == 2;
    }

    /** Execute this command, using TRACKER. */
    public void execute(Tracker tracker) {
        HashMap<String, String> stage = tracker.getStagingArea();
        ArrayList<String> toBeRemoved = tracker.toBeRemoved();

        if (stage.isEmpty() && toBeRemoved.isEmpty()) {
            System.out.println("No changes added to the commit.");
            System.exit(0);
        }
        if (logMessage.equals("")) {
            System.out.println("Please enter a commit message.");
            System.exit(0);
        }

        String previousCommitReference = tracker.getCurrentBranchReference();
        Commit previousCommit = Utils.readObject(
                new File(Utils.gitletPath(previousCommitReference)),
                Commit.class
        );
        HashMap<String, String> previousBlobMap = previousCommit.blobMap();

        Commit currentCommit = new Commit(logMessage,
                LocalDateTime.ofInstant(
                        Instant.now(), ZoneId.of("America/Los_Angeles")),
                previousCommitReference);

        currentCommit.setBlobMap(previousBlobMap);

        for (String fileName : stage.keySet()) {
            currentCommit.addToBlobMap(fileName, stage.get(fileName));
        }
        tracker.clearStage();

        for (String fileName : toBeRemoved) {
            currentCommit.removeFromBlobMap(fileName);
        }
        tracker.clearRemoveThese();

        String currentCommitSha = Utils.sha1(Utils.serialize(currentCommit));
        tracker.setCurrentBranchReference(currentCommitSha);
        tracker.addCommit(currentCommitSha);

        Utils.writeObject(currentCommit);
    }


    /** The Log Message associated with this commit. */
    private String logMessage;
}
