package com.example.praca4.activities;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;


import android.Manifest;
import android.view.MotionEvent;
import android.widget.*;

import androidx.core.app.ActivityCompat;

import com.example.praca4.managers.DiscoveryManager;
import com.example.praca4.R;
import com.example.praca4.managers.VoiceManager;

import java.util.ArrayList;

public class VisibleUsersActivity extends AppCompatActivity {

    private final ArrayList<String> devices = new ArrayList<>();
    private String selectedIp = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.RECORD_AUDIO}, 1);

        ListView listView = findViewById(R.id.listDevices);
        Button btnDiscover = findViewById(R.id.btnDiscover);
        Button btnTalk = findViewById(R.id.btnTalk);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this, android.R.layout.simple_list_item_1, devices);
        listView.setAdapter(adapter);

        DiscoveryManager discovery = new DiscoveryManager();
        VoiceManager voice = new VoiceManager();
        voice.startReceiving();


        discovery.startListening(new DiscoveryManager.Listener() {


            @Override
            public void onDeviceFound(String ip) {
                devices.add(ip);
                adapter.notifyDataSetChanged();
            }
        });


        btnDiscover.setOnClickListener(v -> discovery.sendBroadcast());

        listView.setOnItemClickListener((a, v, pos, id) ->
                selectedIp = devices.get(pos));

        btnTalk.setOnTouchListener((v, event) -> {
            if (selectedIp == null) return false;

            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                voice.startTalking(selectedIp);
            } else if (event.getAction() == MotionEvent.ACTION_UP) {
                voice.stopTalking();
            }
            return true;
        });
    }
}