package zachary.m.com.mentalalertnesscheckerapp;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Switch;

public class Bluetooth extends AppCompatActivity {
    private static final String TAG = "Bluetooth device"; //this will be changed later to reflect the device connected

    BluetoothAdapter mBluetoothAdapter;

    //create a broadcastReciever for ACTION_FOUND
    private final BroadcastReceiver mBroadcastReceiver1 = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            //whe discovery finds a device
            if(action.equals(mBluetoothAdapter.ACTION_STATE_CHANGED)){
                //do something
                final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, mBluetoothAdapter.ERROR);

                switch(state){ //catches all state changes
                    case BluetoothAdapter.STATE_OFF:
                        Log.d(TAG, "onReceive: State Off");
                        break;
                    case BluetoothAdapter.STATE_TURNING_OFF:
                        Log.d(TAG, "mBroadcastReceiver1: State Turning Off");
                        break;
                    case BluetoothAdapter.STATE_ON:
                        Log.d(TAG, "mBroadcastReceiver1: State On");
                        break;
                    case BluetoothAdapter.STATE_TURNING_ON:
                        Log.d(TAG,"mBroadcastReceiver1: State Turning On");
                        break;
                }
           // if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                //get Bluetooth device  object from the intent
               // BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                //add the name an address to an array adapter to show in a listView
               // mArryAdapter.add(device.getName() + "\n" + device.getAddress());
            }
        }
    };

    @Override
    protected void onDestroy(){
        Log.d(TAG, "onDestroy is called" );
        super.onDestroy();
        unregisterReceiver(mBroadcastReceiver1);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message); //activity_message app page used to connect to bluetooth

        Button blueSwitch = (Button) findViewById(R.id.blueONOFF); // switch used to turn bluetooth on an off.

        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        blueSwitch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG,"onClick: enabling/disabling bluetooth.");
                enabledDisableBT();
            }
        });
    }

    public void enabledDisableBT() {
        if(mBluetoothAdapter == null) {
            Log.d(TAG,"Does not have Bluetooth capabilities (or BT is off).");
        }
        if(!mBluetoothAdapter.isEnabled()) {
            Log.d(TAG,"Enabling Bluetooth");
            Intent enableBTIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivity(enableBTIntent);

            IntentFilter BTIntent = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
            registerReceiver(mBroadcastReceiver1, BTIntent);
        }
        if(mBluetoothAdapter.isEnabled()) {
            Log.d(TAG, "Disabling Bluetooth");
            mBluetoothAdapter.disable();

            IntentFilter BTIntent = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
            registerReceiver(mBroadcastReceiver1, BTIntent);
        }
    }
}

