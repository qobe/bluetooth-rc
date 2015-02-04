package kobe.blueknobs.activity;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.PopupMenu;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Set;
import java.util.UUID;

import kobe.blueknobs.BluetoothHandler;
import kobe.blueknobs.R;

public class MainActivity extends ActionBarActivity {

    private static final int REQUEST_ENABLE_BT = 87; //Request code for activity result
    private static final int CONNECTION_STATE_CHANGED = 8701; //request code for activity result to. check if dropped connection
    private static final int BLUETOOTH_MENU = 9; //group ID of popup menu list bluetooth devices
    private  UUID MY_UUID;
    private BluetoothDevice gSelectedDevice;
    private BluetoothHandler mBlueHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Enable Connect Button
        TextView cb = (TextView)findViewById(R.id.connectButton);
        cb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Create popup menu for connection options
                PopupMenu popupMenu = new PopupMenu(MainActivity.this, MainActivity.this.findViewById(R.id.connectButton));
                 //popupMenu.getMenuInflater().inflate(R.menu.popupmenu, popupMenu.getMenu());
                mBlueHandler = new BluetoothHandler(BluetoothAdapter.getDefaultAdapter());
                //Intent connState = new Intent(BluetoothAdapter.ACTION_CONNECTION_STATE_CHANGED);
                //startActivityForResult(connState, CONNECTION_STATE_CHANGED);

                //populate popup menu with paired devices
                int i = 0;
                for(BluetoothDevice dev : mBlueHandler.getPairedDevices()){
                    //getName and getAddress from each device and list
                    popupMenu.getMenu().add(BLUETOOTH_MENU, i, i,
                            String.format("dev: %s%nmac: %s", dev.getName(), dev.getAddress()));
                    i++;
                }
                popupMenu.show();
                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        //if connect to bluetooth chosen return true
                        mBlueHandler.open(mBlueHandler.getPairedDevices().get(item.getItemId()));
                        mBlueHandler.connect();
                        return true;
                    }
                });
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode, resultCode, data);
        if((resultCode == RESULT_OK) && (requestCode == CONNECTION_STATE_CHANGED)) {
            //get extras
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onStop(){
        super.onStop();
        mBlueHandler.cancel();
    }

}
