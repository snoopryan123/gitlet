package gitlet;


import java.io.Serializable;
import java.io.File;

/** Driver class for Gitlet, the tiny stupid version-control system.
 *  @author Ryan Brill
 *  Collaborator: Adish Jain
 */
public class Main implements Serializable {

    /** Usage: java gitlet.Main ARGS, where ARGS contains
     *  <COMMAND> <OPERAND> .... */
    public static void main(String... args) {

        if (args.length == 0) {
            System.out.println("Please enter a command.");
            return;
        }

        String commandString = args[0];
        String args1 = (args.length > 1) ? args[1] : "";
        String args2 = (args.length > 2) ? args[2] : "";
        String args3 = (args.length > 3) ? args[3] : "";
        Command command = getCommand(commandString, args1, args2, args3);

        if (command == null) {
            System.out.println("No command with that name exists.");
            return;
        }
        if (!command.properNumOperands(args.length)) {
            System.out.println("Incorrect operands.");
            return;
        }
        if (!commandString.equals("init") && !repoExists()) {
            System.out.println("Not in an initialized Gitlet directory.");
            return;
        }
        if (commandString.equals("init") && repoExists()) {
            System.out.println(
                    "A Gitlet version-control system "
                    + "already exists in the current directory."
            );
            return;
        }

        if (!commandString.equals("init")) {
            tracker = Utils.readObject(
                    new File(Utils.gitletPath("tracker")), Tracker.class);
        } else {
            tracker = null;
        }

        command.execute(tracker);
        Utils.writeObject(new File(Utils.gitletPath("tracker")), tracker);

    }

    /** Return true iff a repository has been created. */
    private static boolean repoExists() {
        return (new File(Utils.gitletPath(""))).exists();
    }


    /** The tracker object. */
    private static Tracker tracker;

    /** Initialize the tracker object with commit INITIALCOMMIT. */
    static void initializeTracker(Commit initialCommit) {
        tracker = new Tracker("master", initialCommit);
        tracker.addCommit(Utils.sha1(Utils.serialize(initialCommit)));
    }

    /** Return the command with operands COMMANDSTRING, ARGS1, ARGS2, ARGS3. */
    private static Command getCommand(
            String commandString, String args1, String args2, String args3) {
        switch (commandString) {
        case "init": return new InitCommand();
        case "add": return new AddCommand(args1);
        case "commit": return new CommitCommand(args1);
        case "rm": return new RmCommand(args1);
        case "log": return new LogCommand();
        case "global-log": return new GlobalLogCommand();
        case "find": return new FindCommand(args1);
        case "status": return new StatusCommand();
        case "checkout": return new CheckoutCommand(args1, args2, args3);
        case "branch": return new BranchCommand(args1);
        case "rm-branch": return new RmBranchCommand(args1);
        case "reset": return new ResetCommand(args1);
        case "merge": return new MergeCommand(args1);
        default: return null;
        }
    }

}
