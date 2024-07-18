package servent.message.mutex;

import app.ServentInfo;
import servent.message.BasicMessage;
import servent.message.MessageType;

public class TokenMessage extends BasicMessage {

	private static final long serialVersionUID = 2084490973699262440L;

	public TokenMessage(int sender, int receiver) {
		super(MessageType.TOKEN, sender, receiver);
	}
}
