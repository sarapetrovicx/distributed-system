package cli.command.files;

import app.AppConfig;
import cli.command.CLICommand;
import mutex.TokenMutex;

public class RemoveFileCommand implements CLICommand {

    private TokenMutex mutex;

    @Override
    public String commandName() {
        return "remove_file";
    }

    @Override
    public void execute(String args) {
        String[] splitArgs = args.split(" ");

        if (splitArgs.length == 1) {
            String filePath = splitArgs[0];
            int key = AppConfig.sha1HashToChordKey(filePath);
            AppConfig.chordState.removeFile(key);
        } else {
            AppConfig.timestampedErrorPrint("Invalid arguments for remove_file. Usage: remove_file [path]");
        }
    }
}
