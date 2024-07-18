package app;


public class PingerThread implements Runnable {

    private volatile boolean working = true;

    @Override
    public void run() {
        while (working) {
//            AppConfig.timestampedStandardPrint("Pokrenuto");
            AppConfig.pinger.ping();
        }
    }

    public void stop() {
        working = false;
    }
}
