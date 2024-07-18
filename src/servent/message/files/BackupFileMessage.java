package servent.message.files;

import servent.message.BasicMessage;
import servent.message.MessageType;

public class BackupFileMessage extends BasicMessage {

    private static final long serialVersionUID = -1107046058152382577L;

    public BackupFileMessage(int senderPort, int receiverPort, String messageText) {
        super(MessageType.BACKUP, senderPort, receiverPort, messageText);
    }
}
