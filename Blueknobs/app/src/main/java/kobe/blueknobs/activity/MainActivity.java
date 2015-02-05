package kobe.blueknobs.activity;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.graphics.Color;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.PopupMenu;
import android.widget.Toast;

import kobe.blueknobs.BluetoothHandler;
import kobe.blueknobs.R;

public class MainActivity extends ActionBarActivity {

    private static final int CONNECTION_STATE_CHANGED = 8701; //request code for activity result to. check if dropped connection
   // private static final int BLUETOOTH_MENU = 9; //group ID of popup menu list bluetooth devices
    private BluetoothDevice gSelectedDevice;
    private BluetoothHandler mBlueHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        //startActivityForResult(new Intent(BluetoothAdapter.ACTION_CONNECTION_STATE_CHANGED), CONNECTION_STATE_CHANGED);

        //Enable Connect Button
        TextView cb = (TextView)findViewById(R.id.connectButton);
        cb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mBlueHandler = new BluetoothHandler(BluetoothAdapter.getDefaultAdapter());
                //Create popup menu for connection options, anchor it to connectbutton
                PopupMenu popupMenu = new PopupMenu(MainActivity.this, v);
                //populate popup menu with paired devices
                int i = 0;
                for(BluetoothDevice dev : mBlueHandler.getPairedDevices()){
                    //getName and getAddress from each device and list
                    popupMenu.getMenu().add(Menu.NONE, i, Menu.NONE,
                            String.format("dev: %s", dev.getName()));
                    i++;
                }
                popupMenu.getMenuInflater().inflate(R.menu.popupmenu, popupMenu.getMenu());
                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        //if connect to bluetooth chosen return true
                        mBlueHandler.open(mBlueHandler.getPairedDevices().get(item.getItemId()));
                        return true;
                    }
                });
                popupMenu.show();
                new RCController(mBlueHandler);
            }
        });
        //initiate control seekbars



    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode, resultCode, data);
        if((resultCode == RESULT_OK) && (requestCode == CONNECTION_STATE_CHANGED)) {
            //get extras
            int state = data.getIntExtra(BluetoothAdapter.EXTRA_CONNECTION_STATE, -1);
            TextView tv = (TextView)findViewById(R.id.connectButton);
            switch (state){
            //state connected: make button blue
                case BluetoothAdapter.STATE_CONNECTED:
                    tv.setBackgroundColor(Color.BLUE);
                    Toast.makeText(MainActivity.this, "Connected..", Toast.LENGTH_SHORT);
                    break;
            //state disconnected: make button clear
                case BluetoothAdapter.STATE_DISCONNECTED:
                    tv.setBackgroundColor(Color.YELLOW);
                    Toast.makeText(MainActivity.this, "Disconnected..", Toast.LENGTH_SHORT);
                    break;
                default: break;
            }
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
       // mBlueHandler.cancel();
    }


    private class RCController{
        //load seek bars for throttle and steering
        private SeekBar throttle; //send value between 0-255
        private SeekBar steering; //send value between 60-120
        private BluetoothHandler blue;

        private RCController(BluetoothHandler bh){
            blue = bh;
            throttle = (SeekBar)findViewById(R.id.accelerateBar);
            steering = (SeekBar)findViewById(R.id.steeringBar);
            throttle.setMax(255);
            steering.setMax(60);

            //create seekbar listener for events
            SeekBar.OnSeekBarChangeListener sbl = new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    if(fromUser){
                        String str = Integer.toString(progress);
                        switch(seekBar.getId()){
                            case R.id.accelerateBar:
                                //accelerate(progress);
                                ((TextView)findViewById(R.id.throttleView)).setText("Load: "+str);
                                break;
                            case R.id.steeringBar:
                                //turn(progress);
                                ((TextView)findViewById(R.id.steeringView)).setText("Angle: "+str);
                                break;
                            default: break;
                        }
                    }
                }
                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {}

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {}
            };
            throttle.setOnSeekBarChangeListener(sbl);
            steering.setOnSeekBarChangeListener(sbl);
        }

        private void turn(int angle){
            angle = angle + 60;
            blue.send("T"+Integer.toString(angle));
        }

        private void accelerate(int load){
            blue.send("A"+Integer.toString(load));
        }
    }
}
