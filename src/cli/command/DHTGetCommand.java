package cli.command;

import app.AppConfig;

public class DHTGetCommand implements CLICommand {

	@Override
	public String commandName() {
		return "dht_get";
	}

	@Override
	public void execute(String args) {
		try {
			int key = Integer.parseInt(args);
			
			String val = AppConfig.chordState.getValue(key);
			
			if (Integer.parseInt(val) == -2) {
				AppConfig.timestampedStandardPrint("Please wait...");
			} else if (Integer.parseInt(val) == -1) {
				AppConfig.timestampedStandardPrint("No such key: " + key);
			} else {
				AppConfig.timestampedStandardPrint(key + ": " + val);
			}
		} catch (NumberFormatException e) {
			AppConfig.timestampedErrorPrint("Invalid argument for dht_get: " + args + ". Should be key, which is an int.");
		}
	}

}
