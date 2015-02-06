package kobe.blueknobs.activity;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.graphics.Color;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.PopupMenu;
import android.widget.Toast;

import kobe.blueknobs.BluetoothMedian;
import kobe.blueknobs.Constants;
import kobe.blueknobs.R;

public class MainActivity extends ActionBarActivity {


    private BluetoothMedian mBlueMedian;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        mBlueMedian = new BluetoothMedian(BluetoothAdapter.getDefaultAdapter(), mBlueHandler);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Enable Connect Button
        TextView cb = (TextView)findViewById(R.id.connectButton);
        cb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mBlueMedian.cancel(); //close any existing sockets
                //check phone's bluetooth status
                if(!mBlueMedian.isEnabled()){
                    Toast.makeText(MainActivity.this, "Please turn on bluetooth", Toast.LENGTH_LONG);
                }
                //Create popup menu for connection options, anchor it to connectbutton
                PopupMenu popupMenu = new PopupMenu(MainActivity.this, v);
                //populate popup menu with paired devices
                int i = 0;
                for(BluetoothDevice dev : mBlueMedian.getPairedDevices()){
                    //getName and getAddress from each device and list
                    popupMenu.getMenu().add(Menu.NONE, i, Menu.NONE,
                            String.format("dev: %s", dev.getName()));
                    i++;
                }
                popupMenu.getMenuInflater().inflate(R.menu.popupmenu, popupMenu.getMenu());
                //Connect to bluetooth device
                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        mBlueMedian.open(mBlueMedian.getPairedDevices().get(item.getItemId()));
                        return true;
                    }
                });
                popupMenu.show();
                //initiate control seekbars
                new RCController(mBlueMedian);
            }
        });


    }

    private final Handler mBlueHandler = new Handler(){
        @Override
        public void handleMessage(Message msg){
            switch(msg.arg1){
                case Constants.BLUETOOTH_CONNECTED:
                    ((TextView)findViewById(R.id.connectButton)).setTextColor(Color.GREEN);
                    break;
                case Constants.BLUETOOTH_DISCONNECTED:
                    ((TextView)findViewById(R.id.connectButton)).setTextColor(Color.WHITE);
                    break;
            }
        }
    };

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
       // mBlueMedian.cancel();
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        if(mBlueMedian != null){
            mBlueMedian.cancel();
        }
    }


    private class RCController{
        //load seek bars for throttle and steering
        private SeekBar throttle; //send value between 0-255
        private SeekBar steering; //send value between 60-120
        private BluetoothMedian handler;

        private RCController(BluetoothMedian bh){
            handler = bh;
            throttle = (SeekBar)findViewById(R.id.accelerateBar);
            steering = (SeekBar)findViewById(R.id.steeringBar);
            throttle.setMax(255);
            steering.setMax(60);

            //create seekbar listener for events
            SeekBar.OnSeekBarChangeListener sbl = new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    switch(seekBar.getId()){
                        case R.id.accelerateBar:
                            ((TextView)findViewById(R.id.throttleView)).setText("Load: "+Integer.toString(progress));
                            break;
                        case R.id.steeringBar:
                            ((TextView)findViewById(R.id.steeringView)).setText("Angle: "+Integer.toString(progress - 30));
                            break;
                        default: break;
                    }
                }
                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {}
                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {
                    int progress = seekBar.getProgress();
                    switch(seekBar.getId()){
                        case R.id.accelerateBar:
                            accelerate(progress);
                            break;
                        case R.id.steeringBar:
                            turn(progress);
                            break;
                        default: break;
                    }
                }
            };
            throttle.setOnSeekBarChangeListener(sbl);
            steering.setOnSeekBarChangeListener(sbl);
        }

        private void turn(int angle){
            angle = angle + 60;
            handler.send("T" + Integer.toString(angle));
        }

        private void accelerate(int load){
            handler.send("A" + Integer.toString(load));
        }
    }
}
