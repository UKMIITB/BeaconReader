package com.example.beacon;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.ufobeaconsdk.callback.OnFailureListener;
import com.ufobeaconsdk.callback.OnScanSuccessListener;
import com.ufobeaconsdk.callback.OnSuccessListener;
import com.ufobeaconsdk.main.UFOBeaconManager;
import com.ufobeaconsdk.main.UFODevice;


public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    DatabaseReference datasend;
    private UFOBeaconManager ufoBeaconManager;
    private TextView vehicleNumberEditText;
    private Button startStopButton;
    public static final String SHARED_PREFS = "sharedPrefs";
    public static final String VEH_NUM = "veh_num";
    private ImageView loadingImageView;
    private String vehnum;
    private TextView mainScreenMsg;


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        startStopButton = (Button) findViewById(R.id.button);
        mainScreenMsg = (TextView) findViewById(R.id.mainscreenmsg);
        startStopButton.setOnClickListener(this);
        datasend = FirebaseDatabase.getInstance().getReference("Data");
        loadingImageView = findViewById(R.id.loadingImageView);
        loadingImageView.setImageResource(R.drawable.nobroker_icon);
        vehicleNumberEditText = (TextView) findViewById(R.id.vehicle_num);

        int REQUEST_LOCATION = 1;
        checkAndRequestBluetooth();
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION);
        }

        startScan();
        loadData();
        updateData();
    }

    private boolean checkAndRequestBluetooth() {
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        int REQUEST_ENABLE_BT = 2;

        if (!bluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            return false;
        } else
            //Toast.makeText(this, "Bluetooth is ON", Toast.LENGTH_LONG).show();
            return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (grantResults[0] == PackageManager.PERMISSION_GRANTED && requestCode == 1) {
            //Toast.makeText(MainActivity.this, "Location permission granted", Toast.LENGTH_LONG).show();
            startScan();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 2 && resultCode == RESULT_OK) {
            // Toast.makeText(MainActivity.this, "Bluetooth is ON", Toast.LENGTH_LONG).show();
            startScan();
        }
    }

    void startScan() {

        Log.e("TAG", "In start scan ");
        if (ufoBeaconManager == null)
            ufoBeaconManager = new UFOBeaconManager(this);
        ufoBeaconManager.startScan(new OnScanSuccessListener() {
            @Override
            public void onSuccess(final UFODevice ufodevice) {

                Log.e("TAG", "UUID " + ufodevice.getProximityUUID());
                TextView t3 = (TextView) findViewById(R.id.textView14);
                String uuid = ufodevice.getProximityUUID().toString();
                t3.setText("UUID : " + (uuid));


                Log.e("TAG", "Major " + ufodevice.getMajor());
                // TextView t4 = (TextView) findViewById(R.id.textView15);
                int major = ufodevice.getMajor();
                //t4.setText("Major : " + Integer.toString(major));

                Log.e("TAG", "Minor " + ufodevice.getMinor());
                // TextView t5 = (TextView) findViewById(R.id.textView16);
                int minor = ufodevice.getMinor();

                stopScan();
                String id = datasend.push().getKey();
                Object date = ServerValue.TIMESTAMP;
                Data_send data = new Data_send(minor, major, uuid, "KA 07 JQ 5234", date);

                datasend.child(id).setValue(data).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {

                            Toast.makeText(MainActivity.this, "Entry Successful!", Toast.LENGTH_LONG).show();
                            mainScreenMsg.setText("Entry Updated in Server. \n Thank You!");

                            new Handler().postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    finish();
                                    System.exit(0);
                                }
                            }, 5000);

                            saveData();


                        } else {
                            Log.e("TAG", "try again ");
                            Toast.makeText(MainActivity.this, "Please try again", Toast.LENGTH_LONG).show();


                        }
                    }
                });

            }
        }, new OnFailureListener() {
            @Override
            public void onFailure(final int code, final String message) {
                Log.e("TAG", "failed " + message);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                    }
                });
            }
        });
    }


    @Override
    public void onClick(View v) {

        switch (v.getId()) {
            case R.id.button:
                if (vehicleNumberEditText == null || TextUtils.isEmpty(vehicleNumberEditText.getText().toString().trim())) {
                    Toast.makeText(this, "Vehicle number required", Toast.LENGTH_LONG).show();
                    return;
                }
                if (!checkAndRequestBluetooth()) {
                    return;
                }
                if (startStopButton.getText().toString().trim().equalsIgnoreCase(getResources().getString(R.string.start_scan))) {
                    startScan();
                    startStopButton.setText(getResources().getString(R.string.stop_scan));
                } else {
                    stopScan();
                    startStopButton.setText(getResources().getString(R.string.start_scan));
                }
                break;
        }
    }

    private void stopScan() {
        if (ufoBeaconManager != null)
            ufoBeaconManager.stopScan(new OnSuccessListener() {
                @Override
                public void onSuccess(boolean b) {
                    //  Toast.makeText(MainActivity.this, "stopped successfully", Toast.LENGTH_SHORT).show();
                    startStopButton.setText(getResources().getString(R.string.start_scan));
                }
            }, new OnFailureListener() {
                @Override
                public void onFailure(int i, String s) {
                    Toast.makeText(MainActivity.this, "stopped failed", Toast.LENGTH_SHORT).show();
                }
            });
    }

    public void saveData() {
        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        editor.putString(VEH_NUM, vehicleNumberEditText.getText().toString());
        editor.apply();
    }

    public void loadData() {
        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
        vehnum = sharedPreferences.getString(VEH_NUM, "");
    }

    public void updateData() {
        vehicleNumberEditText.setText(vehnum);
    }

}
