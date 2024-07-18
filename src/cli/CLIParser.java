package cli;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import app.AppConfig;
import app.Cancellable;
import app.Pinger;
import app.PingerThread;
import cli.command.CLICommand;
import cli.command.DHTGetCommand;
import cli.command.DHTPutCommand;
import cli.command.InfoCommand;
import cli.command.PauseCommand;
import cli.command.StopCommand;
import cli.command.SuccessorInfo;
import cli.command.files.AddFileCommand;
import cli.command.files.AddFriendCommand;
import cli.command.files.RemoveFileCommand;
import cli.command.files.ViewFilesCommand;
import cli.command.mutex.DistributedLockCommand;
import cli.command.mutex.DistributedUnlockCommand;
import cli.command.mutex.InitTokenMutexCommand;
import mutex.DistributedMutex;
import servent.SimpleServentListener;

/**
 * A simple CLI parser. Each command has a name and arbitrary arguments.
 * 
 * Currently supported commands:
 * 
 * <ul>
 * <li><code>info</code> - prints information about the current node</li>
 * <li><code>pause [ms]</code> - pauses exection given number of ms - useful when scripting</li>
 * <li><code>ping [id]</code> - sends a PING message to node [id] </li>
 * <li><code>broadcast [text]</code> - broadcasts the given text to all nodes</li>
 * <li><code>causal_broadcast [text]</code> - causally broadcasts the given text to all nodes</li>
 * <li><code>print_causal</code> - prints all received causal broadcast messages</li>
 * <li><code>stop</code> - stops the servent and program finishes</li>
 * </ul>
 * 
 * @author bmilojkovic
 *
 */
public class CLIParser implements Runnable, Cancellable {

	private volatile boolean working = true;
	
	private final List<CLICommand> commandList;
	
	public CLIParser(SimpleServentListener listener, PingerThread pinger, DistributedMutex mutex) {
		this.commandList = new ArrayList<>();
		
		commandList.add(new InfoCommand());
		commandList.add(new PauseCommand());
		commandList.add(new SuccessorInfo());
		commandList.add(new DHTGetCommand());
		commandList.add(new DHTPutCommand());
		commandList.add(new AddFriendCommand());
		commandList.add(new AddFileCommand(mutex));
		commandList.add(new RemoveFileCommand());
		commandList.add(new ViewFilesCommand(mutex));
		commandList.add(new StopCommand(this, listener, pinger));
		commandList.add(new DistributedLockCommand(mutex));
		commandList.add(new DistributedUnlockCommand(mutex));
		commandList.add(new InitTokenMutexCommand(mutex));
	}
	
	@Override
	public void run() {
		Scanner sc = new Scanner(System.in);
		
		while (working) {
			String commandLine = sc.nextLine();
			
			int spacePos = commandLine.indexOf(" ");
			
			String commandName = null;
			String commandArgs = null;
			if (spacePos != -1) {
				commandName = commandLine.substring(0, spacePos);
				commandArgs = commandLine.substring(spacePos+1, commandLine.length());
			} else {
				commandName = commandLine;
			}
			
			boolean found = false;
			
			for (CLICommand cliCommand : commandList) {
				if (cliCommand.commandName().equals(commandName)) {
					cliCommand.execute(commandArgs);
					found = true;
					break;
				}
			}
			
			if (!found) {
				AppConfig.timestampedErrorPrint("Unknown command: " + commandName);
			}
		}
		
		sc.close();
	}
	
	@Override
	public void stop() {
		this.working = false;
		
	}
}
