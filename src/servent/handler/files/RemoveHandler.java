package servent.handler.files;

import app.AppConfig;
import servent.handler.MessageHandler;
import servent.message.Message;
import servent.message.MessageType;

public class RemoveHandler implements MessageHandler {

    private Message clientMessage;

    public RemoveHandler(Message clientMessage) {
        this.clientMessage = clientMessage;
    }

    @Override
    public void run() {
        if (clientMessage.getMessageType() == MessageType.REMOVE) {
            int key = Integer.parseInt(clientMessage.getMessageText());
            boolean result = AppConfig.chordState.removeFile(key);

            if (result) {
                AppConfig.timestampedStandardPrint("File with key " + key + " removed.");
            } else {
                AppConfig.timestampedStandardPrint("File with key " + key + " not found locally.");
            }
        } else {
            AppConfig.timestampedErrorPrint("Invalid arguments for REMOVE. Usage: remove [path]");
        }
    }
}
