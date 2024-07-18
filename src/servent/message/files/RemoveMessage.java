package servent.message.files;

import servent.message.BasicMessage;
import servent.message.MessageType;

public class RemoveMessage extends BasicMessage {
    private static final long serialVersionUID = 4543119371396433913L;

    private final int key;

    public RemoveMessage(int senderPort, int receiverPort, int key) {
        super(MessageType.REMOVE, senderPort, receiverPort, String.valueOf(key));
        this.key = key;
    }

    public int getKey() {
        return key;
    }
}
