package cli.command.files;

import app.AppConfig;
import cli.command.CLICommand;
import mutex.DistributedMutex;
import mutex.TokenMutex;

import java.io.File;
import java.io.IOException;

public class AddFileCommand implements CLICommand {
    private TokenMutex mutex;

    public AddFileCommand(DistributedMutex mutex) {
        this.mutex = (TokenMutex) mutex;
    }

    @Override
    public String commandName() {
        return "add_file";
    }

//    add_file [path] [private/public] - Dodavanje nove datoteke u mrežu sa opcijom privatnog ili javnog deljenja.
    @Override
    public void execute(String args) {
        String[] splitArgs = args.split(" ");

        if (splitArgs.length == 2) {
            String path = splitArgs[0];
            String privateOrPublic = splitArgs[1];

            if(!checkArgs(privateOrPublic))
                AppConfig.timestampedErrorPrint("Invalid arguments for add_file 1");

            boolean isPrivate = privateOrPublic.equalsIgnoreCase("private");

            this.mutex.lock();
            add(path, isPrivate);
            this.mutex.unlock();

        } else {
            AppConfig.timestampedErrorPrint("Invalid arguments for add_file");
        }
    }

    public boolean checkArgs(String privateOrPublic){
        if (!privateOrPublic.equalsIgnoreCase("private") && !privateOrPublic.equalsIgnoreCase("public")) {
            AppConfig.timestampedErrorPrint("The second argument must be 'private' or 'public'.");
            return false;
        }
        return true;
    }

    public void add(String fileName, boolean isPrivate) {
        File file = new File(AppConfig.ROOT_DIR + "/" + (isPrivate ? "private_" : "public_") + fileName);
        if (!file.exists()) {
            try {
                boolean isNewFileCreated = file.createNewFile();

                if (isNewFileCreated) {
                    int hashedFilePath = AppConfig.sha1HashToChordKey(file.getAbsolutePath());
                    AppConfig.chordState.putFile(hashedFilePath,  new FileData(file, isPrivate));
                    AppConfig.timestampedStandardPrint("File added successfully with " + isPrivate + " sharing option.");
                } else {
                    AppConfig.timestampedStandardPrint("Adding file: " + fileName + " has failed." );
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        } else {
            AppConfig.timestampedStandardPrint("File with name : " + fileName + " already exists.");
            int hashedFilePath = AppConfig.sha1HashToChordKey(file.getAbsolutePath());
            AppConfig.chordState.putFile(hashedFilePath, new FileData(file, isPrivate));
        }
    }


}
