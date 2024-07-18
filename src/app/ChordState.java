package app;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.*;

import cli.command.files.FileData;
import servent.message.*;
import servent.message.files.AddFriendMessage;
import servent.message.files.BackupFileMessage;
import servent.message.files.RemoveMessage;
import servent.message.util.MessageUtil;

/**
 * This class implements all the logic required for Chord to function.
 * It has a static method <code>chordHash</code> which will calculate our chord ids.
 * It also has a static attribute <code>CHORD_SIZE</code> that tells us what the maximum
 * key is in our system.
 * 
 * Other public attributes and methods:
 * <ul>
 *   <li><code>chordLevel</code> - log_2(CHORD_SIZE) - size of <code>successorTable</code></li>
 *   <li><code>successorTable</code> - a map of shortcuts in the system.</li>
 *   <li><code>predecessorInfo</code> - who is our predecessor.</li>
 *   <li><code>valueMap</code> - DHT values stored on this node.</li>
 *   <li><code>init()</code> - should be invoked when we get the WELCOME message.</li>
 *   <li><code>isCollision(int chordId)</code> - checks if a servent with that Chord ID is already active.</li>
 *   <li><code>isKeyMine(int key)</code> - checks if we have a key locally.</li>
 *   <li><code>getNextNodeForKey(int key)</code> - if next node has this key, then return it, otherwise returns the nearest predecessor for this key from my successor table.</li>
 *   <li><code>addNodes(List<ServentInfo> nodes)</code> - updates the successor table.</li>
 *   <li><code>putValue(int key, int value)</code> - stores the value locally or sends it on further in the system.</li>
 *   <li><code>getValue(int key)</code> - gets the value locally, or sends a message to get it from somewhere else.</li>
 * </ul>
 * @author bmilojkovic
 *
 */
public class ChordState {

	public static int CHORD_SIZE;
	public static int chordHash(int value) {
		return 61 * value % CHORD_SIZE;
	}
	
	private int chordLevel; //log_2(CHORD_SIZE)
	
	private ServentInfo[] successorTable;
	private ServentInfo predecessorInfo;
	
	//we DO NOT use this to send messages, but only to construct the successor table
	private List<ServentInfo> allNodeInfo;
	
	private Map<Integer, Integer> valueMap;
	private Map<Integer, FileData> fileValueMap;

	private List<ServentInfo> friendList;
	private Map<Integer,  Map<Integer, FileData>> backupFiles;
	private Map<Integer, Integer> predecessorBackup;
	
	public ChordState() {
		this.chordLevel = 1;
		int tmp = CHORD_SIZE;
		while (tmp != 2) {
			if (tmp % 2 != 0) { //not a power of 2
				throw new NumberFormatException();
			}
			tmp /= 2;
			this.chordLevel++;
		}
		
		successorTable = new ServentInfo[chordLevel];
		for (int i = 0; i < chordLevel; i++) {
			successorTable[i] = null;
		}
		
		predecessorInfo = null;
		valueMap = new HashMap<>();
		fileValueMap = new HashMap<>();
		allNodeInfo = new ArrayList<>();

		friendList = new ArrayList<>();
		backupFiles = new HashMap<>();
		predecessorBackup = new HashMap<>();
	}
	
	/**
	 * This should be called once after we get <code>WELCOME</code> message.
	 * It sets up our initial value map and our first successor so we can send <code>UPDATE</code>.
	 * It also lets bootstrap know that we did not collide.
	 */
	public void init(WelcomeMessage welcomeMsg) {
		//set a temporary pointer to next node, for sending of update message
		successorTable[0] = new ServentInfo("localhost", welcomeMsg.getSenderPort());
//		this.valueMap = welcomeMsg.getValues();
		this.fileValueMap = welcomeMsg.getValues();
		
		//tell bootstrap this node is not a collider
		try {
			Socket bsSocket = new Socket("localhost", AppConfig.BOOTSTRAP_PORT);
			
			PrintWriter bsWriter = new PrintWriter(bsSocket.getOutputStream());
			bsWriter.write("New\n" + AppConfig.myServentInfo.getListenerPort() + "\n");
			
			bsWriter.flush();
			bsSocket.close();
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public int getChordLevel() {
		return chordLevel;
	}
	
	public ServentInfo[] getSuccessorTable() {
		return successorTable;
	}
	
	public int getNextNodePort() {
		return successorTable[0].getListenerPort();
	}
	
	public ServentInfo getPredecessor() {
		return predecessorInfo;
	}
	
	public void setPredecessor(ServentInfo newNodeInfo) {
		AppConfig.myServentInfo.setPredecessor(newNodeInfo);
		this.predecessorInfo = newNodeInfo;
	}

	public Map<Integer, Integer> getValueMap() {
		return valueMap;
	}

	public void setValueMap(Map<Integer, Integer> valueMap) {
		this.valueMap = valueMap;
	}

	public Map<Integer, FileData> getFileValueMap() {
		return fileValueMap;
	}

	public Map<Integer, Map<Integer, FileData>> getBackupFiles() {
		return backupFiles;
	}

	public void setFileValueMap(Map<Integer, FileData> fileValueMap) {
		this.fileValueMap = fileValueMap;
	}

	public boolean isCollision(int chordId) {
		if (chordId == AppConfig.myServentInfo.getChordId()) {
			return true;
		}
		for (ServentInfo serventInfo : allNodeInfo) {
			if (serventInfo.getChordId() == chordId) {
				return true;
			}
		}
		return false;
	}

//	private Map<Ser, Map<String, List<String>>> nodeFiles = new ConcurrentHashMap<>();

//	private Set<String> friends = ConcurrentHashMap.newKeySet();


	private ServentInfo getServentInfoByAddress(String address) {
		for (ServentInfo serventInfo : allNodeInfo) {
			String serventAddress = serventInfo.getIpAddress() + ":" + serventInfo.getListenerPort();
			if (serventAddress.equals(address)) {
				return serventInfo;
			}
		}
		return null;
	}

	public void removeFriend(ServentInfo friendInfo) {
		friendList.remove(friendInfo);
	}

	public List<ServentInfo> getFriends() {
		return new ArrayList<>(friendList);
	}


	public boolean addFriend(String address) {
		ServentInfo friendInfo = getServentInfoByAddress(address);
		if (!friendList.contains(friendInfo)) {

			friendList.add(friendInfo);
			System.out.println(friendList);
			return true;
		}
		return false;
	}

	public void sendFriendRequest(String address){
		AddFriendMessage addFriendMessage = new AddFriendMessage(
				AppConfig.myServentInfo.getListenerPort(),
				Integer.parseInt(address.split(":")[1]));

		MessageUtil.sendMessage(addFriendMessage);
	}

	public boolean isFriend(String address) {
		return friendList.contains(getServentInfoByAddress(address));
	}


	/**
	 * Returns true if we are the owner of the specified key.
	 */
	public boolean isKeyMine(int key) {
		if (predecessorInfo == null) {
			return true;
		}
		
		int predecessorChordId = predecessorInfo.getChordId();
		int myChordId = AppConfig.myServentInfo.getChordId();
		
		if (predecessorChordId < myChordId) { //no overflow
			if (key <= myChordId && key > predecessorChordId) {
				return true;
			}
		} else { //overflow
			if (key <= myChordId || key > predecessorChordId) {
				return true;
			}
		}
		
		return false;
	}
	
	/**
	 * Main chord operation - find the nearest node to hop to to find a specific key.
	 * We have to take a value that is smaller than required to make sure we don't overshoot.
	 * We can only be certain we have found the required node when it is our first next node.
	 */
	public ServentInfo getNextNodeForKey(int key) {
		if (isKeyMine(key)) {
			return AppConfig.myServentInfo;
		}
		
		//normally we start the search from our first successor
		int startInd = 0;
		
		//if the key is smaller than us, and we are not the owner,
		//then all nodes up to CHORD_SIZE will never be the owner,
		//so we start the search from the first item in our table after CHORD_SIZE
		//we know that such a node must exist, because otherwise we would own this key
		if (key < AppConfig.myServentInfo.getChordId()) {
			int skip = 1;
			while (successorTable[skip].getChordId() > successorTable[startInd].getChordId()) {
				startInd++;
				skip++;
			}
		}
		
		int previousId = successorTable[startInd].getChordId();
		
		for (int i = startInd + 1; i < successorTable.length; i++) {
			if (successorTable[i] == null) {
				AppConfig.timestampedErrorPrint("Couldn't find successor for " + key);
				break;
			}
			
			int successorId = successorTable[i].getChordId();
			
			if (successorId >= key) {
				return successorTable[i-1];
			}
			if (key > previousId && successorId < previousId) { //overflow
				return successorTable[i-1];
			}
			previousId = successorId;
		}
		//if we have only one node in all slots in the table, we might get here
		//then we can return any item
		return successorTable[0];
	}

	private void updateSuccessorTable() {
		//first node after me has to be successorTable[0]
		
		int currentNodeIndex = 0;
		ServentInfo currentNode = allNodeInfo.get(currentNodeIndex);
		successorTable[0] = currentNode;
		
		int currentIncrement = 2; //(1 2 4 8 16..)
		
		ServentInfo previousNode = AppConfig.myServentInfo;
		
		//i is successorTable index
		for(int i = 1; i < chordLevel; i++, currentIncrement *= 2) {
			//we are looking for the node that has larger chordId than this
			int currentValue = (AppConfig.myServentInfo.getChordId() + currentIncrement) % CHORD_SIZE; // 2
			
			int currentId = currentNode.getChordId(); // 1
			int previousId = previousNode.getChordId(); // 15
			
			//this loop needs to skip all nodes that have smaller chordId than currentValue
			while (true) {
				if (currentValue > currentId) {
					//before skipping, check for overflow
					if (currentId > previousId || currentValue < previousId) { //skipuj, daj drugom cvoru
						//try same value with the next node
						previousId = currentId;
						currentNodeIndex = (currentNodeIndex + 1) % allNodeInfo.size();
						currentNode = allNodeInfo.get(currentNodeIndex);
						currentId = currentNode.getChordId();
					} else { //kada smo 1, a dodajemo 15
						successorTable[i] = currentNode;
						break;
					}
				} else { //node id is larger - ISTO KAO GORE SAMO OBRNUTO
					ServentInfo nextNode = allNodeInfo.get((currentNodeIndex + 1) % allNodeInfo.size());
					int nextNodeId = nextNode.getChordId();
					//check for overflow
					if (nextNodeId < currentId && currentValue <= nextNodeId) {
						//try same value with the next node
						previousId = currentId;
						currentNodeIndex = (currentNodeIndex + 1) % allNodeInfo.size();
						currentNode = allNodeInfo.get(currentNodeIndex);
						currentId = currentNode.getChordId();
					} else {
						successorTable[i] = currentNode;
						break;
					}
				}
			}
		}
		
	}

	/**
	 * This method constructs an ordered list of all nodes. They are ordered by chordId, starting from this node.
	 * Once the list is created, we invoke <code>updateSuccessorTable()</code> to do the rest of the work.
	 * 
	 */
	public void addNodes(List<ServentInfo> newNodes) {
		allNodeInfo.addAll(newNodes);
		
		allNodeInfo.sort(new Comparator<ServentInfo>() {
			
			@Override
			public int compare(ServentInfo o1, ServentInfo o2) {
				return o1.getChordId() - o2.getChordId();
			}
			
		});
		
		List<ServentInfo> newList = new ArrayList<>();
		List<ServentInfo> newList2 = new ArrayList<>();
		
		int myId = AppConfig.myServentInfo.getChordId();
		for (ServentInfo serventInfo : allNodeInfo) {
			if (serventInfo.getChordId() < myId) {
				newList2.add(serventInfo);//svi cvorovi manji od mene
			} else {
				newList.add(serventInfo);//veci od mene
			}
		}
		
		allNodeInfo.clear();
		allNodeInfo.addAll(newList); //prvo dodajemo sortirane vece cvorove (8 9 12)
		allNodeInfo.addAll(newList2); //pa manje (1 4)
		if (newList2.size() > 0) {//ako imamo nesto ovde - znaci da imamo direktnog prethodnika (4)
			predecessorInfo = newList2.get(newList2.size()-1);
		} else { //inace je onaj poslednji najveci broj pre nas (12)
			predecessorInfo = newList.get(newList.size()-1);
		}
		
		updateSuccessorTable();
	}

	public boolean addBackupFile(int chordPort, int fileKey, FileData file) {
		Map<Integer, FileData> fileMap = backupFiles.getOrDefault(chordPort, new HashMap<>());

		if (!fileMap.containsKey(fileKey) || !fileMap.get(fileKey).equals(file)) {
			fileMap.put(fileKey, file);
			backupFiles.put(chordPort, fileMap);
			return true;
		}
		return false;
	}

	private void sendFileToReserveNodes(int key, FileData value) {
		if (getSuccessorTable().length > 0) {
			BackupFileMessage backupFileMessage = new BackupFileMessage(AppConfig.myServentInfo.getListenerPort(),
					getSuccessorTable()[0].getListenerPort(), key+":"+value.getFile().getAbsolutePath() + ":" + value.isPriv());
			MessageUtil.sendMessage(backupFileMessage);
		}
		if (getPredecessor() != null) {
			BackupFileMessage backupFileMessage = new BackupFileMessage(AppConfig.myServentInfo.getListenerPort(),
					getPredecessor().getListenerPort(), key+":"+value.getFile().getAbsolutePath() + ":" + value.isPriv());
			MessageUtil.sendMessage(backupFileMessage);
		}
	}


	/**
	 * The Chord put operation. Stores locally if key is ours, otherwise sends it on.
	 */

	public void putFile(int key, FileData value) {
		if (isKeyMine(key)) {
			AppConfig.timestampedErrorPrint("Key is mine.");
			fileValueMap.put(key, value);
			sendFileToReserveNodes(key, value);
		} else {
			ServentInfo nextNode = getNextNodeForKey(key);
			PutMessage pm = new PutMessage(AppConfig.myServentInfo.getListenerPort(), nextNode.getListenerPort(), key, value);
			MessageUtil.sendMessage(pm);
		}
	}
	
	/**
	 * The chord get operation. Gets the value locally if key is ours, otherwise asks someone else to give us the value.
	 * @return <ul>
	 *			<li>The value, if we have it</li>
	 *			<li>-1 if we own the key, but there is nothing there</li>
	 *			<li>-2 if we asked someone else</li>
	 *		   </ul>
	 */
	public String getValue(int key) {
		if (isKeyMine(key)) {
			if (fileValueMap.containsKey(key)) {
				return fileValueMap.get(key).getFile().getAbsolutePath();
			} else {
				return String.valueOf(-1);
			}
		}
		
		ServentInfo nextNode = getNextNodeForKey(key);
		AskGetMessage agm = new AskGetMessage(AppConfig.myServentInfo.getListenerPort(), nextNode.getListenerPort(), String.valueOf(key));
		MessageUtil.sendMessage(agm);
		
		return String.valueOf(-2);
	}

	public boolean removeFile(int key) {
		if (isKeyMine(key)) {
			if (fileValueMap.containsKey(key)) {
				fileValueMap.remove(key);
				AppConfig.timestampedErrorPrint("Key is mine. File removed.");
				return true;
			} else {
				AppConfig.timestampedErrorPrint("Key is mine, but no file found.");
				return false;
			}
		} else {
			ServentInfo nextNode = getNextNodeForKey(key);
			AppConfig.timestampedErrorPrint("Key is not mine. Forwarding to node with Chord ID: " + nextNode.getChordId());
			RemoveMessage rm = new RemoveMessage(AppConfig.myServentInfo.getListenerPort(), nextNode.getListenerPort(), key);
			MessageUtil.sendMessage(rm);
			return false;
		}
	}

	public List<FileData> getAllFiles() {
		List<FileData> filePaths = new ArrayList<>();
		for (Map.Entry<Integer, FileData> entry : fileValueMap.entrySet()) {
			if (isKeyMine(entry.getKey())) {
				filePaths.add(entry.getValue());
			}
		}
		return filePaths;
	}

}
