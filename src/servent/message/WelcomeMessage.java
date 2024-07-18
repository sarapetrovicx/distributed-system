package servent.message;

import cli.command.files.FileData;

import java.util.Map;

public class WelcomeMessage extends BasicMessage {

	private static final long serialVersionUID = -8981406250652693908L;

	private Map<Integer, FileData> values;
	
	public WelcomeMessage(int senderPort, int receiverPort, Map<Integer, FileData> values) {
		super(MessageType.WELCOME, senderPort, receiverPort);
		
		this.values = values;
	}
	
	public Map<Integer, FileData> getValues() {
		return values;
	}
}
