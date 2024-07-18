package servent.handler.buddy;

import app.AppConfig;
import app.ServentInfo;
import servent.handler.MessageHandler;
import servent.message.Message;
import servent.message.MessageType;
import servent.message.buddy.PongMessage;
import servent.message.util.MessageUtil;

/**
 * Handler for the PING message - sends a PONG back to sender.
 * @author bmilojkovic
 *
 */
public class PingHandler implements MessageHandler {

    private final Message clientMessage;

    //ako dobijem ping vracam pong
    public PingHandler(Message clientMessage) {
        this.clientMessage = clientMessage;
    }

    @Override
    public void run() {
        if (clientMessage.getMessageType() == MessageType.PING) {
            AppConfig.pinger.addServent(new ServentInfo("localhost",clientMessage.getSenderPort()));
            MessageUtil.sendMessage(
                    new PongMessage(clientMessage.getReceiverPort(), clientMessage.getSenderPort(),
                            clientMessage.getMessageText()));

        } else {
            AppConfig.timestampedErrorPrint("PING handler got: " + clientMessage);
        }
    }

}
