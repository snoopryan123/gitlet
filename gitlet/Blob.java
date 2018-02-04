package gitlet;

import java.io.Serializable;

/** Blob: the byte representation of a file.
 *  @author Ryan Brill*/

public class Blob implements Serializable {

    /** This blob contains the contents in BYTESTREAM. */
    Blob(byte[] byteStream) {
        contents = byteStream;
    }

    /** The contents of the files represented by this blob. */
    private byte[] contents;

    /** Return the contents of the files represented by this blob. */
    byte[] getContents() {
        return contents;
    }

}
