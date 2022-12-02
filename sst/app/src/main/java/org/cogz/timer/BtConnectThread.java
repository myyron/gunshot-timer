package org.cogz.timer;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.Handler;
import android.os.Message;

import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

public class BtConnectThread extends Thread {

    private static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    private final BluetoothAdapter mmAdapter;
    private final BluetoothSocket mmSocket;
    private final BluetoothDevice mmDevice;

    private Handler mmHandler;
    private InputStream mmInputStream;

    public BtConnectThread(BluetoothAdapter adapter, BluetoothDevice device, Handler handler) {
        // Use a temporary object that is later assigned to mmSocket
        // because mmSocket is final.
        mmAdapter = adapter;
        mmDevice = device;
        mmHandler = handler;

        BluetoothSocket tmp = null;
        try {
            // Get a BluetoothSocket to connect with the given BluetoothDevice.
            // MY_UUID is the app's UUID string, also used in the server code.
            tmp = device.createRfcommSocketToServiceRecord(MY_UUID);
        } catch (IOException e) {
            e.printStackTrace();
        }
        mmSocket = tmp;
    }

    public void run() {
        // Cancel discovery because it otherwise slows down the connection.
        mmAdapter.cancelDiscovery();

        int i = 0;
        do {
            try {
                // Connect to the remote device through the socket. This call blocks
                // until it succeeds or throws an exception.
                mmSocket.connect();
            } catch (IOException connectException) {
                connectException.printStackTrace();
            }
            i++;
        } while (!mmSocket.isConnected() && i < 3);

        if (!mmSocket.isConnected()) {
            // Unable to connect; close the socket and return.
            try {
                mmSocket.close();
            } catch (IOException closeException) {
                closeException.printStackTrace();
            }
            return;
        }

        // The connection attempt succeeded. Perform work associated with
        // the connection in a separate thread.
        inputListen();
    }

    // Closes the client socket and causes the thread to finish.
    public void cancel() {
        try {
            mmSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void inputListen() {
        byte[] mmBuffer = new byte[1024];

        try {
            mmInputStream = mmSocket.getInputStream();
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Keep listening to the InputStream until an exception occurs.
        while (true) {
            try {
                // Read from the InputStream.
                mmInputStream.read(mmBuffer);
                // Send the obtained bytes to the UI activity.
                Message readMsg = mmHandler.obtainMessage(0, mmBuffer);
                readMsg.sendToTarget();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }
}
