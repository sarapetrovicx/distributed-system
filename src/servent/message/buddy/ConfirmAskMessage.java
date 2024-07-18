package servent.message.buddy;

import servent.message.BasicMessage;
import servent.message.MessageType;

public class ConfirmAskMessage extends BasicMessage {
    private static final long serialVersionUID = 4076770276119670201L;

    public ConfirmAskMessage(int senderPort, int receiverPort, String msg) {
        super(MessageType.CHECK_ASK, senderPort, receiverPort, msg);
    }
}
