package com.example.beacon;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.MediaPlayer;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.IOException;
import java.util.ArrayList;
import java.util.UUID;

public class Security_Guard extends AppCompatActivity {

    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference myRef;
    private MyListAdapter adapter;
    private RecyclerView recyclerView;
    private Ringtone ringtone;

    MediaPlayer mediaPlayer;

    public final static String MODULE_MAC = "00:18:E4:40:00:06";
    public final static int REQUEST_ENABLE_BT = 1;
    private static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb");
    BluetoothAdapter bta;                 //bluetooth stuff
    BluetoothSocket mmSocket;             //bluetooth stuff
    BluetoothDevice mmDevice;             //bluetooth stuff
    private BroadcastReceiver broadcastReceiver;
    ArrayList<Data_send> datasend;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_security__guard);

        mFirebaseDatabase = FirebaseDatabase.getInstance();
        myRef = mFirebaseDatabase.getReference().child("Data");
        mediaPlayer = MediaPlayer.create(getApplicationContext(), R.raw.kabir_singh_ringtone);


        datasend = new ArrayList<>();

        Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE);
        ringtone = RingtoneManager.getRingtone(getApplicationContext(), notification);

        myRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot ds, @Nullable String s) {

                //for (DataSnapshot ds : dataSnapshot.getChildren()) {
                try {
                    Data_send data_send = ds.getValue(Data_send.class);
                    data_send.setFirebaseId(ds.getKey());
                    if (data_send.getStatus().equals("")) {
                        datasend.add(0, data_send);
                        adapter.notifyDataSetChanged();
                        new Handler().postDelayed(new Runnable() {
                            @Override

                            public void run() {
                                recyclerView.scrollToPosition(0);

                            }
                        }, 200);
                       // mediaPlayer = MediaPlayer.create(getApplicationContext(), R.raw.kabir_singh_ringtone);
                        if (!mediaPlayer.isPlaying())
                            mediaPlayer.start();
                       /* if (!ringtone.isPlaying())
                            ringtone.play();*/
                        //adapter.notifyDataSetChanged();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot ds, @Nullable String s) {

               /* try {
                    Data_send data_send = ds.getValue(Data_send.class);
                    data_send.setFirebaseId(ds.getKey());
                    if (data_send.getStatus().equals("")) {
                        datasend.add(0, data_send);
                        adapter.notifyItemInserted(0);
                        new Handler().postDelayed(new Runnable() {
                            @Override

                            public void run() {
                                recyclerView.scrollToPosition(0);

                            }
                        }, 200);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }*/

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                final String action = intent.getAction();
                Log.i("BroadCast reciever","in recieved method");
                if (action.equals(BluetoothAdapter.ACTION_STATE_CHANGED)) {
                    final int bluetoothState = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE,
                            BluetoothAdapter.ERROR);

                    Log.i("BroadCast reciever","Adapter state changed");
                    switch (bluetoothState) {
                        case BluetoothAdapter.STATE_OFF: {
                            Log.i("BroadCast reciever","Adapter state off");
                            BluetoothConnect();
                            break;
                        }

                    }
                }
                if(action.equals(BluetoothDevice.ACTION_BOND_STATE_CHANGED))
                {
                    Log.i("BroadCast reciever","bond state changed");
                    final int ConnectionModuleState = intent.getIntExtra(BluetoothDevice.EXTRA_BOND_STATE,BluetoothDevice.ERROR);
                    final String RemoteDevice = intent.getStringExtra(BluetoothDevice.EXTRA_NAME);
                    switch (ConnectionModuleState) {
                        case BluetoothDevice.BOND_NONE:
                            Log.i("BroadCast reciever","device not bonded");
                            BluetoothConnect();

                    }



                }


            }
        };

        /*recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        adapter = new MyListAdapter(datasend, this);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);*/

        BluetoothConnect();

    }

    public Ringtone getRingtone() {
        return ringtone;
    }

    public MediaPlayer getMediaPlayer() {
        return mediaPlayer;
    }

    private void  BluetoothConnect()
    {
        bta = BluetoothAdapter.getDefaultAdapter();
        if (!bta.isEnabled()) {
            Intent enableBTIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBTIntent, REQUEST_ENABLE_BT);
        } else {
            initiateBluetoothProcess();
        }
    }
    private void initiateBluetoothProcess() {
        if (bta.isEnabled()) {
            //attempt to connect to bluetooth module;

            Log.i("bluetooth enabled","entered into inititate");
            mmDevice = bta.getRemoteDevice(MODULE_MAC);
            //Log.i("device name",mmDevice.getName());
            //creating socket and connecting to it

            try {
                mmSocket = mmDevice.createRfcommSocketToServiceRecord(MY_UUID);
                Log.i("mmDevice ", "got the socket");
                mmSocket.connect();
                Log.i("[BLUETOOTH]", "Connected to: " + mmDevice.getName());

            } catch (IOException e) {
                Log.e("creating socket", "error in creating socket");
                Log.e("creating socket", e.getMessage());
                initiateBluetoothProcess();
//                try {
//                    mmSocket.close();
//                } catch (IOException c) {
//                    return;
//                }

            }

            recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
            adapter = new MyListAdapter(datasend,this, mmSocket);
            recyclerView.setHasFixedSize(true);
            recyclerView.setLayoutManager(new LinearLayoutManager(this));
            recyclerView.setAdapter(adapter);
            // Log.i("[BLUETOOTH]", "Creating and running Thread");


        }
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_ENABLE_BT)
            initiateBluetoothProcess();

    }
    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(broadcastReceiver,new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED));

        registerReceiver(broadcastReceiver,new IntentFilter(BluetoothDevice.ACTION_BOND_STATE_CHANGED));
    }
    @Override
    protected void onDestroy() {
        unregisterReceiver(broadcastReceiver);
        super.onDestroy();

    }


}
