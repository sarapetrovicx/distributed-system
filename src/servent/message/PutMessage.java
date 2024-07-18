package servent.message;

import cli.command.files.FileData;

public class PutMessage extends BasicMessage {

	private static final long serialVersionUID = 5163039209888734276L;

	public PutMessage(int senderPort, int receiverPort, int key, FileData value) {
		super(MessageType.PUT, senderPort, receiverPort, key+":"+value.getFile().getAbsolutePath()+":"+value.isPriv());
	}
}
