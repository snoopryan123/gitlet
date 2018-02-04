package gitlet;

/** The command interface.
 *  @author Ryan Brill
 */
interface Command {

    /** Return true iff N is the proper number of operands for this command. */
    boolean properNumOperands(int n);

    /** Execute this command, using TRACKER. */
    void execute(Tracker tracker);

}
