package gitlet;

import ucb.junit.textui;
import org.junit.Test;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

import static org.junit.Assert.*;

/** The suite of all JUnit tests for the gitlet package.
 *  @author Ryan Brill
 */
public class UnitTest {

    /** Run the JUnit tests in the loa package. Add xxxTest.class entries to
     *  the arguments of runClasses to run other JUnit tests. */
    public static void main(String[] ignored) {
        textui.runClasses(UnitTest.class);
    }

    /** A dummy test to avoid complaint. */
    @Test
    public void placeholderTest() {

    }

    /** Test the toString() method of the Commit object. */
    @Test
    public void commitToStringTest() {
        Commit initialCommit = new Commit("initial commit",
                LocalDateTime.ofInstant(
                    Instant.EPOCH, ZoneId.of("America/Los_Angeles")),
                "");


        StringBuilder expected = new StringBuilder();
        expected.append("===\ncommit "
                + Utils.sha1(Utils.serialize(initialCommit)) + "\n");
        expected.append("Date: Wed Dec 31 16:00:00 1969 -0800"
                + "\ninitial commit\n");

        assertEquals(initialCommit.toString(), expected.toString());
    }

}


