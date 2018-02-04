package gitlet;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;

/** The commit object.
 *  @author Ryan Brill
 */
public class Commit implements Serializable  {

    /** This commit has logMessage L, timeStamp T,
     *  and parentReference P.*/
    Commit(String L, LocalDateTime T, String P) {
        logMessage = L;
        timeStamp = T;
        parentReference = P;
    }

    /* BLOBS TRACKED BY THIS COMMIT. */

    /** HashMap of Blobs tracked by this Commit:
     *  Key: File name // Value: Blob reference (SHA value). */
    private HashMap<String, String> blobMap = new HashMap<>();

    /** Return blobMap. */
    HashMap<String, String> blobMap() {
        return blobMap;
    }

    /** Set blobMap to B. */
    void setBlobMap(HashMap<String, String> B) {
        blobMap = B;
    }

    /** Add a (FILENAME, REFERENCE) entry to the blobMap. */
    void addToBlobMap(String fileName, String reference) {
        blobMap.put(fileName, reference);
    }

    /** Untrack FILENAME by removing it from the blobMap. */
    void removeFromBlobMap(String fileName) {
        blobMap.remove(fileName);
    }

    /** Return the Sha reference of the blob named BLOBNAME. */
    String getBlobReference(String blobName) {
        return blobMap.get(blobName);
    }

    /** Return true if this commit is tracking the blob named FILENAME. */
    boolean isTracking(String fileName) {
        return blobMap.containsKey(fileName);
    }

    /* PARENT COMMIT. */

    /** Reference to the parent commit of this commit, as a SHA value. */
    private String parentReference;

    /** Return the reference to the parent commit of this commit. */
    String getParentReference() {
        return parentReference;
    }

    /** The timestamp of this commit. */
    private LocalDateTime timeStamp;

    /** The log message of this commit. */
    private String logMessage;

    /** Return the log message of this commit. */
    String getLogMessage() {
        return logMessage;
    }


    @Override
    public String toString() {
        StringBuilder S = new StringBuilder();
        S.append("===\n");
        S.append("commit " + Utils.sha1(Utils.serialize(this)) + "\n");
        DateTimeFormatter F =
                DateTimeFormatter.ofPattern("EEE MMM d HH:mm:ss yyyy");
        S.append("Date: " + timeStamp.format(F) + " -0800" + "\n");
        S.append(logMessage + "\n");
        return S.toString();
    }

}
