package servent.message.buddy;

import app.ServentInfo;
import servent.message.BasicMessage;
import servent.message.MessageType;

public class PingMessage extends BasicMessage {

    private static final long serialVersionUID = -1934709147043909111L;

    public PingMessage(int senderInfo, int receiverInfo, String check) {
        super(MessageType.PING, senderInfo, receiverInfo, check);
    }
}
