package servent.handler.files;

import app.AppConfig;
import servent.handler.MessageHandler;
import servent.message.Message;
import servent.message.MessageType;

public class SendFilesHandler implements MessageHandler {

    private Message clientMessage;

    public SendFilesHandler(Message clientMessage) {
        this.clientMessage = clientMessage;
    }

    @Override
    public void run() {
        if (clientMessage.getMessageType() == MessageType.SEND_FILES) {
            AppConfig.timestampedStandardPrint("Files: " + clientMessage.getMessageText());
        }
    }
}
