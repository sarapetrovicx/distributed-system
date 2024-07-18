package app;

import servent.message.UpdateMessage;
import servent.message.buddy.ConfirmAskMessage;
import servent.message.buddy.PingMessage;
import servent.message.buddy.RemoveNodeMessage;
import servent.message.util.MessageUtil;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class Pinger {
    private final Set<ServentInfo> allServents;
    private final Set<ServentInfo> suspicious;
    private final Set<ServentInfo> checked;

    public Pinger() {
        this.allServents = Collections.newSetFromMap(new ConcurrentHashMap<>());
        this.suspicious = Collections.newSetFromMap(new ConcurrentHashMap<>());
        this.checked = Collections.newSetFromMap(new ConcurrentHashMap<>());
    }


    public void ping() {
        suspicious.clear();
        suspicious.addAll(allServents);
        if(allServents.size()>0){
            AppConfig.timestampedStandardPrint("Uslo");
            for (ServentInfo servent : allServents) {
                AppConfig.timestampedErrorPrint(allServents.toString());
                PingMessage pingMessage = new PingMessage(AppConfig.myServentInfo.getListenerPort(),
                        servent.getListenerPort(), "FIRST");
                MessageUtil.sendMessage(pingMessage);
            }

            try {
                Thread.sleep(AppConfig.WEAK);
                AppConfig.timestampedStandardPrint("WAITING 4 SECONDS");
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            //ostavljamo samo cvorove koju si aktivni - koji su odgovorili sa PONG
            List<ServentInfo> active = new LinkedList<>(allServents);
            active.removeAll(suspicious);

            if (!active.isEmpty()) {
                for (ServentInfo pinged : suspicious) {
                    //SALJEMO NAJBLIZEM AKTIVNOM CVORU DA PINGUJE SVE NESIGURNE
                    active.sort((s1, s2) -> {
                        int keyDifference1 = Math.abs(s1.getChordId() - pinged.getChordId());
                        int keyDifference2 = Math.abs(s2.getChordId() - pinged.getChordId());
                        return Integer.compare(keyDifference1, keyDifference2);
                    });
                    ConfirmAskMessage msg = new ConfirmAskMessage(AppConfig.myServentInfo.getListenerPort(),
                            active.get(0).getListenerPort(), String.valueOf(pinged.getListenerPort()));
                    MessageUtil.sendMessage(msg);
                }
            }

            try {
                Thread.sleep(AppConfig.STRONG);
                AppConfig.timestampedStandardPrint("WAITING 10 SECONDS");
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }

            for (ServentInfo servent : suspicious) {
                if(active.size() > 0) {
                    for (ServentInfo activeNode: active) {
                        RemoveNodeMessage removeNodeMessage = new RemoveNodeMessage(AppConfig.myServentInfo.getListenerPort(),
                                activeNode.getListenerPort(), String.valueOf(servent.getListenerPort()));
                        MessageUtil.sendMessage(removeNodeMessage);
                    }
                }
                tellBootstrap(servent.getListenerPort());
                allServents.remove(servent);

                UpdateMessage um = new UpdateMessage(AppConfig.myServentInfo.getListenerPort(), AppConfig.chordState.getNextNodePort(), "");
                MessageUtil.sendMessage(um);
            }
        }
    }

    public void check(int servent) {
        ServentInfo serventInfo = new ServentInfo("localhost", servent);
        checked.add(serventInfo);
        try {
            Thread.sleep(AppConfig.WEAK);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    public void pong(int servent) {
        ServentInfo serventInfo = new ServentInfo("localhost", servent);
        suspicious.remove(serventInfo);
    }

    public void uncheck(int servent) {
        ServentInfo serventInfo = new ServentInfo("localhost", servent);
        checked.remove(serventInfo);
    }
    public boolean isChecked(ServentInfo servent) {
        return checked.contains(servent);
    }

    public void addServent(ServentInfo servent) {
        AppConfig.timestampedStandardPrint("Dodat "+servent );
        allServents.add(servent);
    }

    public boolean removeServent(ServentInfo servent) {
        return allServents.remove(servent);
    }

    public void tellBootstrap(int port){
        try {
            Socket bsSocket = new Socket("localhost", AppConfig.BOOTSTRAP_PORT);

            PrintWriter bsWriter = new PrintWriter(bsSocket.getOutputStream());
            bsWriter.write("Remove\n" + port + "\n");

            bsWriter.flush();
            bsSocket.close();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
