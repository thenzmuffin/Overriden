package com.total.overiden;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class BluetoothInputThread extends Thread{
    private final BluetoothSocket socket;
    private final BluetoothDevice device;
    private IBluetoothMessage callback;

    private BluetoothAdapter adapter;
    private BufferedReader reader = null;

    private boolean connected = false;
    private OutputStream outStream = null;

    @SuppressLint("MissingPermission")
    public BluetoothInputThread(BluetoothDevice device, BluetoothAdapter adpt, BluetoothSocket connection) {
        super();
        this.device = device; // do we need to persist the device to avoid the socket closing?
        adapter = adpt;
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
            try {
                if (adapter != null) {
                    adapter.cancelDiscovery();
                }
                socket.connect();

                reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                outStream = socket.getOutputStream();
            } catch (IOException closeException) {
                System.out.println("READER CREATE FAILED - NO SOCKET");
            }
        } else {
            socket = connection;
            try {
                reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                outStream = socket.getOutputStream();
            } catch (IOException closeException) {
                System.out.println("READER CREATE FAILED - SOCKET PROVIDED");
            }
        }
    }
//    public BluetoothInputThread(BluetoothDevice device, BluetoothSocket connection, IBluetoothMessage btMsg){
//        super();
//        socket = connection;
//        callback = btMsg;
//        this.device = device;
//    }
    @SuppressLint("MissingPermission")
    public void run() {
        if (reader==null)return; // can't read if the reader failed to create
        boolean stop = false;
        List<String> input = new ArrayList<>();
        try {
//            if (!socket.isConnected()) {
//                socket.connect();
//            }
//            if (reader == null){
//                reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
//                outStream = socket.getOutputStream();
//            }
//            BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            do { // in game loop, this will always run in the background waiting for external input
                while (!stop) {
                    String temp = reader.readLine();
                    System.out.println("Input Thread : " + temp);
                    input.add(temp);
                    stop = temp.equals("ENDMESSAGE");
                }
                System.out.println("Lines Generated: " + input.size());
                Game.current.inGameUpdate(input, socket.getRemoteDevice().getName());
                stop = false;
                input.clear();
//            callback.processMessage(input, socket.getRemoteDevice().getName());
            } while (true);
        } catch (IOException e) {
            try {
                System.out.println("Input Thread : ***************** THREAD STOPPED *****************");
                socket.close();
            } catch (IOException closeException) {
            }
        }
    }

    @SuppressLint("MissingPermission")
    public void write(List<String> data){
        try {
            System.out.println("Check Socket is connected");
            if (!socket.isConnected()) {
                System.out.println("Socket says it isn't connected");
            }
//            if (reader == null){
//                reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
//                outStream = socket.getOutputStream();
//            }
            System.out.println("Check the output stream is created");
            if (outStream==null)return;
            System.out.println("Cancel Discovery on adapter if available");
            if (adapter != null) {
                adapter.cancelDiscovery();

            }


            // endmessage tag only added on send so do it here
            data.add("ENDMESSAGE\n");
            System.out.println("Send packets via output stream");
            for (String msg : data) {
                System.out.println("Sending: " + msg);
//                socket.getOutputStream().write(msg.getBytes(StandardCharsets.UTF_8));
                outStream.write(msg.getBytes(StandardCharsets.UTF_8));
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
