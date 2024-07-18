package servent.handler;

import java.util.ArrayList;
import java.util.List;

import app.AppConfig;
import app.ServentInfo;
import servent.message.Message;
import servent.message.MessageType;
import servent.message.UpdateMessage;
import servent.message.util.MessageUtil;

public class UpdateHandler implements MessageHandler {

	private Message clientMessage;
	
	public UpdateHandler(Message clientMessage) {
		this.clientMessage = clientMessage;
	}
	
	@Override
	public void run() {
		if (clientMessage.getMessageType() == MessageType.UPDATE) {
			//Onaj ko je poslao poruku nije ja(Kada se UPDATE poruka vraca do mene)
			if (clientMessage.getSenderPort() != AppConfig.myServentInfo.getListenerPort()) {
				ServentInfo newNodInfo = new ServentInfo("localhost", clientMessage.getSenderPort());
				List<ServentInfo> newNodes = new ArrayList<>();
				newNodes.add(newNodInfo);


				AppConfig.chordState.addNodes(newNodes);

				ServentInfo[] successorTable = AppConfig.chordState.getSuccessorTable();
				AppConfig.pinger.addServent(successorTable[0]);
				AppConfig.pinger.addServent(AppConfig.chordState.getPredecessor());
//				AppConfig.pinger.setPredecessor(AppConfig.chordState.getPredecessor());

				String newMessageText = "";
				if (clientMessage.getMessageText().equals("")) { //dopisi sebe u poruci
					newMessageText = String.valueOf(AppConfig.myServentInfo.getListenerPort());
				} else {//ako vec nesto ima, dodaj zarez
					newMessageText = clientMessage.getMessageText() + "," + AppConfig.myServentInfo.getListenerPort();
				}
				Message nextUpdate = new UpdateMessage(clientMessage.getSenderPort(), AppConfig.chordState.getNextNodePort(),
						newMessageText);
				MessageUtil.sendMessage(nextUpdate);
			} else { //Vratila mi se poruka
				String messageText = clientMessage.getMessageText();
				String[] ports = messageText.split(",");
				
				List<ServentInfo> allNodes = new ArrayList<>();
				for (String port : ports) {
					allNodes.add(new ServentInfo("localhost", Integer.parseInt(port)));
				}
				//dodaje sve dobijene nodove u chordState
				AppConfig.chordState.addNodes(allNodes);
				ServentInfo[] successorTable = AppConfig.chordState.getSuccessorTable();

				AppConfig.pinger.addServent(successorTable[0]);
				AppConfig.pinger.addServent(AppConfig.chordState.getPredecessor());
			}
		} else {
			AppConfig.timestampedErrorPrint("Update message handler got message that is not UPDATE");
		}
	}

}
