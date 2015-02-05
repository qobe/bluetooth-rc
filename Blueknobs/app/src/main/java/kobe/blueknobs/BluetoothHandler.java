package kobe.blueknobs;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.UUID;

/**
 * Created by mkobe on 2/3/2015.
 */
public class BluetoothHandler {
    private BluetoothAdapter mBlueAdapter;
    private ConnectThread mConnectThread;

    static final String MY_UUID = "00001101-0000-1000-8000-00805F9B34FB";
    private ArrayList<BluetoothDevice> mPairedDevices;

    public BluetoothHandler(BluetoothAdapter ba){
        mBlueAdapter = ba;
        mPairedDevices = new ArrayList<BluetoothDevice>();
        for(BluetoothDevice dev : mBlueAdapter.getBondedDevices()){
            mPairedDevices.add(dev);
        }
    }

    public void open(BluetoothDevice dev){
        mBlueAdapter.cancelDiscovery();
        mConnectThread =  new ConnectThread(dev);
    }


    public void send(String data) {
        byte msg[] = new byte[4];
        //convert string into byte array
        //T or A is 2 bytes.
        for (int i = 0; i < data.length(); i++) {
            msg[i] = (byte) data.charAt(i);
        }
    }
    public void cancel(){
        mConnectThread.close();
    }


    public ArrayList<BluetoothDevice> getPairedDevices(){
        return mPairedDevices;
    }

    //attempt with private class.
    private class ConnectThread extends Thread{
        private final BluetoothSocket mmSocket;
        private final BluetoothDevice mmDevice;
        private final OutputStream mBlueOut;
        private final InputStream mBlueIn;

        private ConnectThread(BluetoothDevice dev){
            mmDevice = dev;
            BluetoothSocket tmp = null;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            // Get a BluetoothSocket to connect with the given BluetoothDevice
            try {
                // MY_UUID is the app's UUID string, also used by the server code
                tmp = dev.createRfcommSocketToServiceRecord(UUID.fromString(MY_UUID));
                tmpOut = tmp.getOutputStream();
                tmpIn = tmp.getInputStream();
            } catch (IOException e) { }
            mmSocket = tmp;
            mBlueOut = tmpOut;
            mBlueIn = tmpIn;
        }

        public void run(){
            try {
                mmSocket.connect();
            } catch (IOException connectException) {
                try {
                   mmSocket.close();
                } catch (IOException e) {}
            }
        }

        private void write(byte[] bytes){
            try {
                mBlueOut.write(bytes);
            } catch (IOException e) { }
        }

        private void close(){
            try {
                mmSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}

