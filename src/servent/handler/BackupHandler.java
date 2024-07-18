package servent.handler;

import app.AppConfig;
import cli.command.files.FileData;
import servent.message.Message;
import servent.message.MessageType;

import java.io.File;

public class BackupHandler implements MessageHandler{

    private Message clientMessage;

    public BackupHandler(Message clientMessage) {
        this.clientMessage = clientMessage;
    }

    @Override
    public void run() {
        if (clientMessage.getMessageType() == MessageType.BACKUP) {
            String[] splitText = clientMessage.getMessageText().split(":");
            if (splitText.length == 3) {
                try {
                    int key = Integer.parseInt(splitText[0]);
                    String path = splitText[1];
                    String priv = splitText[2];
                    boolean privateOrPublic = priv.equalsIgnoreCase("false");
                    AppConfig.chordState.addBackupFile(clientMessage.getSenderPort(), key, new FileData(new File(path), privateOrPublic));
                } catch (NumberFormatException e) {
                    AppConfig.timestampedErrorPrint("Got backup message with bad text: " + clientMessage.getMessageText());
                }
            } else {
                AppConfig.timestampedErrorPrint("Got backup message with bad text: " + clientMessage.getMessageText());
            }


        } else {
            AppConfig.timestampedErrorPrint("Put handler got a message that is not PUT");
        }

    }
}
