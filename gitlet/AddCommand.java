package gitlet;

import java.io.Serializable;
import java.io.File;

/** The add command.
 *  @author Ryan Brill
 */
class AddCommand implements Command, Serializable {

    /** Add the file with name F. */
    AddCommand(String F) {
        fileName = F;
    }

    /** Return true iff N is the proper number of operands for this command. */
    public boolean properNumOperands(int n) {
        return n == 2;
    }

    /** Execute this command, using TRACKER. */
    public void execute(Tracker tracker) {
        if (!(new File(Utils.workingDirPath(fileName))).exists()) {
            System.out.println("File does not exist.");
            System.exit(0);
        }

        String currentCommitSha = tracker.getCurrentBranchReference();
        Commit currentCommit = Utils.readObject(
                new File(Utils.gitletPath(currentCommitSha)),
                Commit.class);

        byte[] currentContents = Utils.readContents(
                new File(Utils.workingDirPath(fileName)));

        Blob currentBlob = new Blob(currentContents);
        String currentBlobSha = Utils.sha1(Utils.serialize(currentBlob));
        String previousBlobSha = currentCommit.getBlobReference(fileName);
        if (currentBlobSha.equals(previousBlobSha)) {
            tracker.removeFromStage(fileName);
            if (tracker.isStagedForRemoval(fileName)) {
                tracker.cancelRemoval(fileName);
            }
            return;
        }

        Utils.writeObject(currentBlob);

        if (tracker.isStagedForRemoval(fileName)) {
            tracker.cancelRemoval(fileName);
        } else {
            tracker.stage(fileName, currentBlobSha);
        }

    }

    /** The name of the file to be added. */
    private String fileName;
}
