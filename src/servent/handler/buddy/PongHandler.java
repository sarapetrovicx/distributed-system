package servent.handler.buddy;

import app.AppConfig;
import servent.handler.MessageHandler;
import servent.message.Message;
import servent.message.MessageType;


public class PongHandler implements MessageHandler {

    private Message clientMessage;

    public PongHandler(Message clientMessage) {
        this.clientMessage = clientMessage;
    }

    @Override
    public void run() {
        if (clientMessage.getMessageType() == MessageType.PONG) {
            if (clientMessage.getMessageText().equals("CHECK")) {
                AppConfig.pinger.uncheck(clientMessage.getSenderPort());
            } else {
                AppConfig.pinger.pong(clientMessage.getSenderPort());
            }
        }
    }
}
