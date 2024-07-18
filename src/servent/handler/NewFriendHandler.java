package servent.handler;

import app.AppConfig;
import servent.message.Message;
import servent.message.MessageType;

public class NewFriendHandler implements MessageHandler {

    private Message clientMessage;

    public NewFriendHandler(Message clientMessage) {
        this.clientMessage = clientMessage;
    }

    @Override
    public void run() {
        if (clientMessage.getMessageType() == MessageType.ADD_FRIEND) {
            String address = "localhost:" + clientMessage.getSenderPort();

            boolean success = AppConfig.chordState.addFriend(address);

            if (success) {
                AppConfig.timestampedStandardPrint("Successfully added friend: " + address);
            } else {
                AppConfig.timestampedErrorPrint("Failed to add friend: " + address);
            }
        } else {
            AppConfig.timestampedErrorPrint("Invalid arguments for add_friend. Usage: add_friend [address:ip]");
        }
    }
}
