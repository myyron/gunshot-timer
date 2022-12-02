package test.timer;

import com.fazecast.jSerialComm.SerialPort;
import java.util.concurrent.TimeUnit;

public class StartApp {

    public static void main(String[] args) {
        final int START = 3;
        final int STOP = 4;

        long startTime = 0L;

        comTest();
    }
    
    private static void btTest() {
    }

    private static void comTest() {
        SerialPort comPort = SerialPort.getCommPort("COM4");
        comPort.openPort();
        comPort.setComPortTimeouts(SerialPort.TIMEOUT_READ_BLOCKING, 1000, 0);
        try {
            while (true) {
                byte[] readBuffer = new byte[1024];
                int numRead = comPort.readBytes(readBuffer, readBuffer.length);
                if (numRead == START) {
                    startTime = System.currentTimeMillis();
                    System.out.println("start timer");
                } else if (numRead == STOP) {
                    long duration = System.currentTimeMillis() - startTime;
                    long minutes = TimeUnit.MILLISECONDS.toMinutes(duration);
                    long seconds = TimeUnit.MILLISECONDS.toSeconds(duration);
                    System.out.println("stop timer");
                    System.out.format("time: %02d:%02d%n", minutes, seconds);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        comPort.closePort();
    }
}
