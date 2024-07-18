package servent.message.buddy;

import servent.message.BasicMessage;
import servent.message.MessageType;

public class RemoveNodeMessage extends BasicMessage {
    private static final long serialVersionUID = -3333331125380908318L;

    public RemoveNodeMessage(int senderPort, int receiverPort, String message) {
        super(MessageType.REMOVE_NODE, senderPort, receiverPort, message);
    }
}
