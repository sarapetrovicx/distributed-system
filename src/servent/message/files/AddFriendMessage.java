package servent.message.files;

import servent.message.BasicMessage;
import servent.message.MessageType;

public class AddFriendMessage extends BasicMessage {

    private static final long serialVersionUID = -4907329255932997644L;

    public AddFriendMessage(int senderPort, int receiverPort) {
        super(MessageType.ADD_FRIEND, senderPort, receiverPort);
    }
}
