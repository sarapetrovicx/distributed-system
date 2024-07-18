package servent.message.buddy;

import servent.message.BasicMessage;
import servent.message.MessageType;

public class ConfirmTellMessage extends BasicMessage {

    private static final long serialVersionUID = -7367535306651160361L;

    public ConfirmTellMessage(int senderPort, int receiverPort, boolean messageText) {
        super(MessageType.CHECK_TELL, senderPort, receiverPort, String.valueOf(messageText));
    }
}
