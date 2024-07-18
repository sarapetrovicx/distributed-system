package servent.message.buddy;

import app.ServentInfo;
import servent.message.BasicMessage;
import servent.message.MessageType;

public class PongMessage extends BasicMessage {

    private static final long serialVersionUID = -71866183898007085L;

    public PongMessage(int senderInfo, int receiverInfo, String check) {
        super(MessageType.PONG, senderInfo, receiverInfo, check);
    }
}
