package servent.message.files;

import servent.message.BasicMessage;
import servent.message.MessageType;

public class AskViewFilesMessage extends BasicMessage {

    private static final long serialVersionUID = -5526548540908760049L;

    public AskViewFilesMessage(int senderPort, int receiverPort) {
        super(MessageType.ASK_VIEW, senderPort, receiverPort);
    }
}
