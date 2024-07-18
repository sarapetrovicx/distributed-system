package servent.handler.buddy;

import app.AppConfig;
import servent.handler.MessageHandler;
import servent.message.Message;

public class ConfirmTellHandler implements MessageHandler {
    private final Message clientMessage;

    public ConfirmTellHandler(Message clientMessage) {
        this.clientMessage = clientMessage;
    }


    @Override
    public void run() {
        if (Boolean.getBoolean(clientMessage.getMessageText())) {
            AppConfig.pinger.pong(clientMessage.getReceiverPort());
        }
    }
}
