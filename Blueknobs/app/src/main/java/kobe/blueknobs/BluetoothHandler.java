package kobe.blueknobs;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Set;
import java.util.UUID;

/**
 * Created by mkobe on 2/3/2015.
 */
public class BluetoothHandler {
    private BluetoothAdapter mBlueAdapter;
    private BluetoothDevice mBlueDevice;
    private BluetoothSocket mBlueSocket;
    private OutputStream mBlueOut;
    private InputStream mBlueIn;
    private final String MY_UUID = "1234";
    private ArrayList<BluetoothDevice> mPairedDevices;

    public BluetoothHandler(BluetoothAdapter ba){
        mBlueAdapter = ba;
        mPairedDevices = new ArrayList<BluetoothDevice>();
        for(BluetoothDevice dev : mBlueAdapter.getBondedDevices()){
            mPairedDevices.add(dev);
        }
    }

    public void open(BluetoothDevice dev){
        mBlueDevice = dev;
        try {
            mBlueSocket = dev.createRfcommSocketToServiceRecord(UUID.fromString(MY_UUID));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void connect(){
        mBlueAdapter.cancelDiscovery();
        //attempt to open connection. if failed close socket
        new Thread(new Runnable(){
            public void run(){
                try {
                    mBlueSocket.connect();
                } catch (IOException connectException) {
                    try {
                        mBlueSocket.close();
                    } catch (IOException e) {}
                }
            }
        }).start();
    }

    public void send(byte[] msg){
        try {
            mBlueOut.write(msg);
        } catch (IOException e) {}
    }

    public void cancel(){
        try {
            mBlueSocket.close();
        } catch (IOException e) {}
    }

    public ArrayList<BluetoothDevice> getPairedDevices(){
        return mPairedDevices;
    }
}

