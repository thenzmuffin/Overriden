package com.total.overiden;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;

public class BluetoothClientAccept extends Thread{
    private final BluetoothSocket mmSocket;
//    private final BluetoothDevice mmDevice;
    private final BluetoothAdapter adapter;
//    private final IBluetoothMessage callback;
    private final ForceList list;
    @SuppressLint("MissingPermission")
    public BluetoothClientAccept(BluetoothDevice device, BluetoothAdapter adpt, ForceList list/*, IBluetoothMessage btMsg*/) {
        // Use a temporary object that is later assigned to mmSocket
        // because mmSocket is final.
        BluetoothSocket tmp = null;
//        mmDevice = device;
        adapter = adpt;
//        callback = btMsg;
        this.list = list;
        try {
            // Get a BluetoothSocket to connect with the given BluetoothDevice.
            // MY_UUID is the app's UUID string, also used in the server code.
            tmp = device.createRfcommSocketToServiceRecord(UUID.fromString(FirstFragment.uuid));
        } catch (IOException e) {
//            Log.e(TAG, "Socket's create() method failed", e);
            System.out.println("Socket's create() method failed");
        }
        mmSocket = tmp;
    }
//    public BluetoothSocket getSocket(){return mmSocket;}
    @SuppressLint("MissingPermission")
    public void run() {

        // Cancel discovery because it otherwise slows down the connection.
        adapter.cancelDiscovery();

        try {
            // Connect to the remote device through the socket. This call blocks
            // until it succeeds or throws an exception.
            mmSocket.connect();

//            String send = "Hello There";
            List<String> send = list.getStreamValue();
            send.add("ENDMESSAGE\n");
            byte[] message;
            OutputStream stream = mmSocket.getOutputStream();
            System.out.println("Bluetooth Out : total lines " + send.size());
            for (String msg : send) {
                System.out.println("Bluetooth Out : " + msg);
                message = msg.getBytes(StandardCharsets.UTF_8);
                stream.write(message);
            }
//            mmSocket.close();
            // go straight into receiving a force back
//            boolean stop = false;
//            List<String> input = new ArrayList<>();
//
//            BufferedReader reader = new BufferedReader(new InputStreamReader(mmSocket.getInputStream()));
//            while (!stop) {
//                String temp = reader.readLine();
//                input.add(temp);
//                stop = temp.equals("ENDMESSAGE");
//            }
//            System.out.println("Lines Generated: " + input.size());
//
//            callback.processMessage(input, mmSocket.getRemoteDevice().getName());

        } catch (IOException connectException) {
            // Unable to connect; close the socket and return.
            try {
                mmSocket.close();
                System.out.println("BT Client Accept socket closed unexpectedly");
            } catch (IOException closeException) {
            }
//            return;
        }

        // The connection attempt succeeded. Perform work associated with
        // the connection in a separate thread.
//        manageMyConnectedSocket(mmSocket);
    }

    // Closes the client socket and causes the thread to finish.
//    public void cancel() {
//        try {
//            mmSocket.close();
//        } catch (IOException e) {
////            Log.e(TAG, "Could not close the client socket", e);
//        }
//    }
}
