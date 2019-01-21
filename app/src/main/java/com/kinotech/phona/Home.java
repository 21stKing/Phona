package com.kinotech.phona;

import android.support.v7.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class Home extends AppCompatActivity {

    private Button startService;
    private Button stopService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        startService = findViewById(R.id.button);
        stopService  = findViewById(R.id.button2);

        startService.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){
                startNewService(v);
            }
        });

        stopService.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){
                //check contact, sms, location, call log and internet access permissions



                stopNewService(v);
            }
        });
    }

    public void startNewService(View view){
        startService(new Intent(this, KService.class));
    }

    public void stopNewService(View view){
        stopService(new Intent(this, KService.class));
    }
}
