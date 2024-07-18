package cli.command;

import app.AppConfig;
import app.PingerThread;
import cli.CLIParser;
import servent.SimpleServentListener;

public class StopCommand implements CLICommand {

	private CLIParser parser;
	private SimpleServentListener listener;
	private PingerThread pingerThread;
	
	public StopCommand(CLIParser parser, SimpleServentListener listener, PingerThread pingerThread) {
		this.parser = parser;
		this.listener = listener;
		this.pingerThread = pingerThread;
	}
	
	@Override
	public String commandName() {
		return "stop";
	}

	@Override
	public void execute(String args) {
		AppConfig.timestampedStandardPrint("Stopping...");
		parser.stop();
		listener.stop();
		pingerThread.stop();
	}

}
