package servent.handler;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import app.AppConfig;
import app.ServentInfo;
import cli.command.files.FileData;
import mutex.DistributedMutex;
import mutex.TokenMutex;
import servent.message.Message;
import servent.message.MessageType;
import servent.message.NewNodeMessage;
import servent.message.SorryMessage;
import servent.message.WelcomeMessage;
import servent.message.util.MessageUtil;

public class NewNodeHandler implements MessageHandler {

	private Message clientMessage;
	private TokenMutex mutex;
	
	public NewNodeHandler(Message clientMessage, DistributedMutex tokenMutex) {
		this.clientMessage = clientMessage;
		this.mutex = (TokenMutex) tokenMutex;

	}
	
	@Override
	public void run() {
		if (clientMessage.getMessageType() == MessageType.NEW_NODE) {
			//KREIRAM NOVI CVOR(24)

			this.mutex.lock();
			int newNodePort = clientMessage.getSenderPort();
			ServentInfo newNodeInfo = new ServentInfo("localhost", newNodePort);
			
			//check if the new node collides with another existing node.
			if (AppConfig.chordState.isCollision(newNodeInfo.getChordId())) {
				Message sry = new SorryMessage(AppConfig.myServentInfo.getListenerPort(), clientMessage.getSenderPort());
				MessageUtil.sendMessage(sry);
				return;
			}
			
			//check if he is my predecessor(24 28ici)
			boolean isMyPred = AppConfig.chordState.isKeyMine(newNodeInfo.getChordId());
			if (isMyPred) { //if yes, prepare and send welcome message
				//NJEGOV PRETHODNIK JE SAD MOJ PRETHODNIK(4)
				ServentInfo hisPred = AppConfig.chordState.getPredecessor();
				if (hisPred == null) { //situacija kad je i prethodnik i sledbenik
					hisPred = AppConfig.myServentInfo;
				}

				//moj prethodnik je sad taj novi node(24 28ici)
				AppConfig.chordState.setPredecessor(newNodeInfo);
				
				Map<Integer, FileData> myValues = AppConfig.chordState.getFileValueMap();
				Map<Integer, FileData> hisValues = new HashMap<>();
				
				int myId = AppConfig.myServentInfo.getChordId();//28
				int hisPredId = hisPred.getChordId();//4
				int newNodeId = newNodeInfo.getChordId();//24


				//CELA FOR PETLJA JE SAD DELJENJE CVOROVA
				for (Entry<Integer, FileData> valueEntry : myValues.entrySet()) {
					if (hisPredId == myId) { //i am first and he is second(postoje samo ta 2 cvora)
						if (myId < newNodeId) {
							if (valueEntry.getKey() <= newNodeId && valueEntry.getKey() > myId) {
								hisValues.put(valueEntry.getKey(), valueEntry.getValue());
							}
						} else {
							if (valueEntry.getKey() <= newNodeId || valueEntry.getKey() > myId) {
								hisValues.put(valueEntry.getKey(), valueEntry.getValue());
							}
						}
					}
					if (hisPredId < myId) { //my old predecesor was before me(regularan slucaj)
						if (valueEntry.getKey() <= newNodeId) {
							hisValues.put(valueEntry.getKey(), valueEntry.getValue());
						}
					} else { //my old predecesor was after me
						if (hisPredId > newNodeId) { //new node overflow
							if (valueEntry.getKey() <= newNodeId || valueEntry.getKey() > hisPredId) {
								hisValues.put(valueEntry.getKey(), valueEntry.getValue());
							}
						} else { //no new node overflow
							if (valueEntry.getKey() <= newNodeId && valueEntry.getKey() > hisPredId) {
								hisValues.put(valueEntry.getKey(), valueEntry.getValue());
							}
						}
						
					}
					
				}
				//iz mojih vrednosti(28) izbrisi sve njegove(24)
				for (Integer key : hisValues.keySet()) { //remove his values from my map
					myValues.remove(key);
				}
				AppConfig.chordState.setFileValueMap(myValues);

				this.mutex.unlock();
				//welocomeHandler
				WelcomeMessage wm = new WelcomeMessage(AppConfig.myServentInfo.getListenerPort(), newNodePort, hisValues);
				MessageUtil.sendMessage(wm);
			} else { //if he is not my predecessor, let someone else take care of it
				ServentInfo nextNode = AppConfig.chordState.getNextNodeForKey(newNodeInfo.getChordId());
				NewNodeMessage nnm = new NewNodeMessage(newNodePort, nextNode.getListenerPort());
				MessageUtil.sendMessage(nnm);
			}
			
		} else {
			AppConfig.timestampedErrorPrint("NEW_NODE handler got something that is not new node message.");
		}

	}

}
