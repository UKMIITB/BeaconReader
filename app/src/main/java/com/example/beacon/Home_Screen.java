package com.example.beacon;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Button;

public class Home_Screen extends AppCompatActivity {

    public static final String SHARED_PREFS = "sharedPrefs";
    public static final String TEXT = "text";
    private String s=null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        loadData();
        setContentView(R.layout.activity_home__screen);
        Button resident = (Button)findViewById(R.id.resident);
        Button security = (Button)findViewById(R.id.security);

        resident.setOnClickListener((v) -> {
            Intent intent =new Intent(getApplicationContext(), MainActivity.class);
            s = "resident";
            saveData(s);
            startActivity(intent);
            finish();
        });

        security.setOnClickListener(v -> {
            Intent intent = new Intent(getApplicationContext(), Security_Guard.class);
            s = "security";
            saveData(s);
            startActivity(intent);
            finish();
        });
    }

    public void saveData(String s) {
        if(TEXT.equalsIgnoreCase("text")){
        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS,MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(TEXT,s);
        editor.apply();}
    }

    public void loadData() {
        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS,MODE_PRIVATE);
        s = sharedPreferences.getString(TEXT, "");

        if(s.equalsIgnoreCase("resident"))
        {
            Intent intent =new Intent(getApplicationContext(), MainActivity.class);
            startActivity(intent);
            finish();
        }
        if(s.equalsIgnoreCase("security"))
        {
            Intent intent = new Intent(getApplicationContext(), Security_Guard.class);
            startActivity(intent);
            finish();
        }

    }


}
