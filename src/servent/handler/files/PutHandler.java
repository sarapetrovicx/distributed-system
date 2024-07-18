package servent.handler.files;

import app.AppConfig;
import cli.command.files.FileData;
import servent.handler.MessageHandler;
import servent.message.Message;
import servent.message.MessageType;

import java.io.File;

public class PutHandler implements MessageHandler {

	private Message clientMessage;
	
	public PutHandler(Message clientMessage) {
		this.clientMessage = clientMessage;
	}
	
	@Override
	public void run() {
		if (clientMessage.getMessageType() == MessageType.PUT) {
			String[] splitText = clientMessage.getMessageText().split(":");
			if (splitText.length == 3) {
				int key = 0;
				
				try {
					key = Integer.parseInt(splitText[0]);
					String path = splitText[1];
					String priv = splitText[2];
					boolean privateOrPublic = true;
					if(priv.equalsIgnoreCase("false")){
						privateOrPublic = false;
					}
					
					AppConfig.chordState.putFile(key, new FileData(new File(path), privateOrPublic));
				} catch (NumberFormatException e) {
					AppConfig.timestampedErrorPrint("Got put message with bad text: " + clientMessage.getMessageText());
				}
			} else {
				AppConfig.timestampedErrorPrint("Got put message with bad text: " + clientMessage.getMessageText());
			}
			
			
		} else {
			AppConfig.timestampedErrorPrint("Put handler got a message that is not PUT");
		}

	}

}
