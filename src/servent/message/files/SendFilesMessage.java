package servent.message.files;

import servent.message.BasicMessage;
import servent.message.MessageType;

public class SendFilesMessage extends BasicMessage {

    private static final long serialVersionUID = 5726915016688148663L;

    public SendFilesMessage(int senderPort, int receiverPort, String messageText) {
        super(MessageType.SEND_FILES, senderPort, receiverPort, messageText);
    }
}
