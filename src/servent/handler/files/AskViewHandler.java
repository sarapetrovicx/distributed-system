package servent.handler.files;

import app.AppConfig;
import cli.command.files.FileData;
import servent.handler.MessageHandler;
import servent.message.Message;
import servent.message.MessageType;
import servent.message.files.SendFilesMessage;
import servent.message.util.MessageUtil;

import java.util.ArrayList;
import java.util.List;

public class AskViewHandler implements MessageHandler {
    private Message clientMessage;

    public AskViewHandler(Message clientMessage) {
        this.clientMessage = clientMessage;
    }

    @Override
    public void run() {
        if (clientMessage.getMessageType() == MessageType.ASK_VIEW) {

            String address = "localhost:" + clientMessage.getSenderPort();

            boolean isFriend = AppConfig.chordState.isFriend(address);

            List<String> publicFiles = new ArrayList<>();
            List<String> privateFiles = new ArrayList<>();

            List<String> toSend = new ArrayList<>();

            for (FileData fileData : AppConfig.chordState.getAllFiles()) {
                if (fileData.isPriv()) {
                    privateFiles.add(fileData.toString());
                } else {
                    publicFiles.add(fileData.toString());
                }
            }

            if (!publicFiles.isEmpty()) {
                AppConfig.timestampedStandardPrint("Public files:");
                for (String file : publicFiles) {
                    toSend.add(file);
                }
            } else {
                AppConfig.timestampedStandardPrint("No public files found.");
            }

            if (isFriend) {
                if (!privateFiles.isEmpty()) {
                    AppConfig.timestampedStandardPrint("Private files:");
                    for (String file : privateFiles) {
                        toSend.add(file);
                    }
                } else {
                    AppConfig.timestampedStandardPrint("No private files found.");
                }
            } else {
                AppConfig.timestampedStandardPrint("You can only view public files from this node.");
            }

            SendFilesMessage sendFilesMessage = new SendFilesMessage(AppConfig.myServentInfo.getListenerPort(),
                    clientMessage.getSenderPort(), toSend.toString());
            MessageUtil.sendMessage(sendFilesMessage);
        }
    }
}
