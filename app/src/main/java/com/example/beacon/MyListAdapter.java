package com.example.beacon;

import android.bluetooth.BluetoothSocket;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;


public class MyListAdapter extends RecyclerView.Adapter<MyListAdapter.ViewHolder> {
    private ArrayList<Data_send> listdata;

    public Button allow;
    public Button deny;
    Security_Guard security_guard;

    private final BluetoothSocket mmSocket;
    private lightOn btt = null;


    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference myRef;

    // RecyclerView recyclerView;
    public MyListAdapter(ArrayList<Data_send> listdata, Security_Guard security_guard, BluetoothSocket socket) {
        this.listdata = listdata;
        this.security_guard = security_guard;
        mmSocket = socket;
        btt = new lightOn(mmSocket);

    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View listItem = layoutInflater.inflate(R.layout.list_item, parent, false);
        allow = (Button) listItem.findViewById(R.id.allow);
        deny = (Button) listItem.findViewById(R.id.deny);
        ViewHolder viewHolder = new ViewHolder(listItem);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Data_send data_send = listdata.get(position);
        holder.veh_num.setText(data_send.getVeh_num());
        Date date = new Date((Long) (data_send.date));
        SimpleDateFormat sfd = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
        holder.date_time.setText(sfd.format(date));
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        myRef = mFirebaseDatabase.getReference().child("Data");

        allow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                myRef.child(listdata.get(position).getFirebaseId()).child("status").setValue("allow");
                listdata.remove(listdata.get(position));
                stopRingtone();
                notifyDataSetChanged();


                Log.i("[BLUETOOTH]", "Attempting to send data");
                if (mmSocket.isConnected() && btt != null) {

                    String sendtxt = "RY";
                    btt.write(sendtxt.getBytes());

                } else {
                    Toast.makeText(security_guard, "bluetooth not connected to module", Toast.LENGTH_LONG).show();
                }
            }
        });

        deny.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                myRef.child(listdata.get(position).getFirebaseId()).child("status").setValue("deny");
                listdata.remove(listdata.get(position));
                stopRingtone();
                notifyDataSetChanged();
            }
        });
    }


    @Override
    public int getItemCount() {
        return listdata.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        public TextView veh_num;
        public TextView date_time;
        public Button allow;
        public Button deny;

        public ViewHolder(View itemView) {
            super(itemView);
            this.veh_num = (TextView) itemView.findViewById(R.id.veh_num);
            this.date_time = (TextView) itemView.findViewById(R.id.date_time);
            this.allow = (Button) itemView.findViewById(R.id.allow);
            this.deny = (Button) itemView.findViewById(R.id.deny);
        }
    }

    private void stopRingtone() {
        if (security_guard != null && security_guard.getMediaPlayer() != null && security_guard.getMediaPlayer().isPlaying()) {
            security_guard.getMediaPlayer().stop();
            try {
                security_guard.getMediaPlayer().prepare();
            } catch (IOException E) {
            }

        }
    }
}