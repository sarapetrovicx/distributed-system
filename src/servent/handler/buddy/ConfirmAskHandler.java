package servent.handler.buddy;

import app.AppConfig;
import app.ServentInfo;
import servent.handler.MessageHandler;
import servent.message.Message;
import servent.message.buddy.ConfirmTellMessage;
import servent.message.buddy.PingMessage;
import servent.message.util.MessageUtil;

public class ConfirmAskHandler implements MessageHandler {
    private final Message clientMessage;

    public ConfirmAskHandler(Message clientMessage) {
        this.clientMessage = clientMessage;
    }

    @Override
    public void run() {

        PingMessage pingMessage = new PingMessage(clientMessage.getSenderPort(),
                clientMessage.getReceiverPort(), "CONFIRM");
        MessageUtil.sendMessage(pingMessage);

        AppConfig.pinger.check(clientMessage.getReceiverPort());

        ServentInfo si = new ServentInfo("localhost",clientMessage.getReceiverPort());

        boolean active = AppConfig.pinger.isChecked(si);

        if (!active) {
            AppConfig.pinger.removeServent(si);
        }

        ConfirmTellMessage msg = new ConfirmTellMessage(clientMessage.getSenderPort(), clientMessage.getReceiverPort(), active);
        MessageUtil.sendMessage(msg);
    }
}
