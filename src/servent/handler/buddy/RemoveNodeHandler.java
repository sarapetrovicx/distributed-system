package servent.handler.buddy;

import app.AppConfig;
import app.ServentInfo;
import cli.command.files.FileData;
import servent.handler.MessageHandler;
import servent.message.Message;
import servent.message.MessageType;
import servent.message.NewNodeMessage;
import servent.message.UpdateMessage;
import servent.message.buddy.RemoveNodeMessage;
import servent.message.util.MessageUtil;

import java.io.File;
import java.util.Map;

public class RemoveNodeHandler implements MessageHandler {

    private Message clientMessage;

    public RemoveNodeHandler(Message clientMessage) {
        this.clientMessage = clientMessage;
    }

    @Override
    public void run() {
        if (clientMessage.getMessageType() == MessageType.REMOVE_NODE) {
//            int nodeToDeletePort = clientMessage.getSenderPort();
//            if (nodeToDeletePort != AppConfig.myServentInfo.getListenerPort()) {
//                ServentInfo nodeToDeleteInfo = new ServentInfo("localhost", nodeToDeletePort);
//
//                //check if he is my predecessor
//                boolean isMyPred = AppConfig.chordState.getPredecessor().equals(nodeToDeleteInfo);
//                if (isMyPred) {
//                    Map<Integer, FileData> hisValues = AppConfig.chordState.getBackupFiles().get(nodeToDeletePort);
//                    Map<Integer, FileData> myValues = AppConfig.chordState.getFileValueMap();
//
//                    AppConfig.timestampedStandardPrint(hisValues.toString());
//
//                    myValues.putAll(hisValues);
//                    AppConfig.chordState.setFileValueMap(myValues);
//
//                    AppConfig.chordState.setPredecessor(AppConfig.chordState.getPredecessor().getPredecessor());
//
//                    // Notify other nodes about the removal
//                    RemoveNodeMessage rnm = new RemoveNodeMessage(AppConfig.myServentInfo.getListenerPort(),
//                            AppConfig.chordState.getNextNodePort(), "");
//                    MessageUtil.sendMessage(rnm);
//
//                } else {
//                    ServentInfo nextNode = AppConfig.chordState.getNextNodeForKey(nodeToDeleteInfo.getChordId());
//                    RemoveNodeMessage rnm = new RemoveNodeMessage(AppConfig.myServentInfo.getListenerPort(),
//                            AppConfig.chordState.getNextNodePort(), "");
//                    MessageUtil.sendMessage(rnm);
//                }
//            } else {
//                AppConfig.timestampedErrorPrint("Got a DELETE message to delete myself.");
//            }

            try {
                int nodeToDeletePort = Integer.parseInt(clientMessage.getMessageText());
                ServentInfo nodeToDeleteInfo = new ServentInfo("localhost", nodeToDeletePort);
                if (AppConfig.pinger.removeServent(nodeToDeleteInfo)) {
                    AppConfig.timestampedStandardPrint(String.format("Servent %s failed", nodeToDeleteInfo));
                }
            } catch (NumberFormatException e) {
                AppConfig.timestampedErrorPrint("Got put message with bad text: " + clientMessage.getMessageText());
            }


        } else {
            AppConfig.timestampedErrorPrint("DELETE_NODE handler got something that is not a delete node message.");
        }
    }
}

