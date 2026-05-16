package com.total.overiden;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;

public class BluetoothSendThread extends Thread {
    private final BluetoothSocket socket;
    private final BluetoothDevice mmDevice;
    private BluetoothAdapter adapter;
    private List<String> data;
    private BufferedReader reader;
    private OutputStream outStream;
    @SuppressLint("MissingPermission")
    public BluetoothSendThread(BluetoothDevice device, BluetoothAdapter adpt, BluetoothSocket connection, List<String> sendData) {
        super();
        mmDevice = device; // do we need to persist the device to avoid the socket closing?
        adapter = adpt;
        data = sendData;
        if (connection == null) {
            BluetoothSocket tmp = null;
            try {
                // Get a BluetoothSocket to connect with the given BluetoothDevice.
                // MY_UUID is the app's UUID string, also used in the server code.
                tmp = device.createRfcommSocketToServiceRecord(UUID.fromString(FirstFragment.uuid));
            } catch (IOException e) {
//            Log.e(TAG, "Socket's create() method failed", e);
                System.out.println("Socket's create() method failed");
            }
            socket = tmp;
        } else {
            socket = connection;
        }
    }

    public BluetoothSocket getSocket() {
        return socket;
    }

    @SuppressLint("MissingPermission")
    public void run() {
        try {
            System.out.println("Cancel Discovery on adapter if available");
            if (adapter != null) {
                adapter.cancelDiscovery();

            }

            System.out.println("Check Socket is connected");
            if (!socket.isConnected()) {
                socket.connect();
                reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                outStream = socket.getOutputStream();
            }
            // endmessage tag only added on send so do it here
            data.add("ENDMESSAGE\n");
            System.out.println("Send packets via output stream");
            for (String msg : data) {

                socket.getOutputStream().write(msg.getBytes(StandardCharsets.UTF_8));

            }
            System.out.println("Send complete");
        } catch (IOException e) {
            try {
                System.out.println("Send thread failure: " + e.toString());
                socket.close();
//                Game.current.setSocket(null);

            } catch (IOException closeException) {
            }
        }
    }
}
