package com.example.praca4.activities;

import static java.lang.Thread.setDefaultUncaughtExceptionHandler;

import android.Manifest;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.annotation.UiThread;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.praca4.R;
import com.example.praca4.adapters.CallAdapter;
import com.example.praca4.managers.CallManager;
import com.example.praca4.managers.PermissionManager;
import com.example.praca4.managers.SoundManager;
import com.example.praca4.room.Database;

import java.io.Console;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Objects;


public class Call extends AppCompatActivity {

    private boolean callingParty = true;
    private CallManager callManager;
    InetAddress callingPartyInetAddress;
    Database database;
    private boolean record = false;
    private PermissionManager permissionManager;
    private Button returnButton;
    private Button talkButton;
    private ListView listView;

    private SoundManager soundManager;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_call);

        setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
            @Override
            public void uncaughtException(@NonNull Thread t, @NonNull Throwable e) {
                StringWriter errors = new StringWriter();
                e.printStackTrace(new PrintWriter(errors));
                Log.e("UncaughtExceptionHandler", t.getName() + " " + (e.getMessage() != null ? e.getMessage() : "no message") + " " + errors.toString());
            }
        });

        soundManager = new SoundManager();
        permissionManager = new PermissionManager(Manifest.permission.RECORD_AUDIO, new PermissionManager.Handler() {
            @Override
            public void onPermissionGranted() {
                if (ActivityCompat.checkSelfPermission(Call.this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
                    return;
                }
                soundManager.InitializeRecording(Call.this);
            }
        }, "This permission is necessary to speak in a call", this, 100 );


        View root = findViewById(R.id.callRootLayout);

        ViewCompat.setOnApplyWindowInsetsListener(root, (view, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            view.setPadding(
                    view.getPaddingLeft(),
                    view.getPaddingTop(),
                    view.getPaddingRight(),
                    systemBars.bottom
            );
            return insets;
        });



        listView = findViewById(R.id.callList);
        returnButton = findViewById(R.id.btnCallReturn);
        talkButton = findViewById(R.id.btnTalk);


        callManager = new CallManager(new CallManager.Handler() {
            @Override
            public void onStatusChange() {
                Call.this.runOnUiThread(
                    new Thread(() ->
                        listView.setAdapter(new CallAdapter(Call.this, R.layout.call_row, callManager.getConnectedUsersList()))
                    )
                );
            }
        });


        Intent myIntent = getIntent();
        callingParty = myIntent.getBooleanExtra( intentData.CALLING_PARTY.name(), false);
        try {
            callingPartyInetAddress = InetAddress.getByName(myIntent.getStringExtra(intentData.CALLING_PARTY_INET_ADDRESS.name()));
        } catch (UnknownHostException e) {
            callingPartyInetAddress = null;
        }



        database = Database.getInstance(this.getApplicationContext());
            database.currentCallDao().observeUsers().observe(this, users -> {
                callManager.resetStillInCallFlag();
                users.forEach(user -> {

                    boolean considerConnectedFromTheStart =
                            Objects.equals(user.getIpAddress(), callingPartyInetAddress.getHostAddress()) && !callingParty;



                    CallManager.ConnectedUser connectedUser =
                            new CallManager.ConnectedUser(user.getAudioId(), user, considerConnectedFromTheStart ? CallManager.ConnectedUser.State.CONNECTED : CallManager.ConnectedUser.State.UN_CALLED);

                    callManager.addUser(connectedUser);
                });
                callManager.removeUsersNotInCall();

                callManager.callUsersNotYetCalled();
                callManager.playSound();
                listView.setAdapter(new CallAdapter(this, R.layout.call_row, callManager.getConnectedUsersList()));
            });


        returnButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                callManager.stopPlayingSound();
                callManager.stopCall();

                finish();

            }
        });



        permissionManager.requestRuntimePermission();



        talkButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(record){
                    soundManager.StopRecording();
                    record = false;
                    talkButton.setText("Muted");
                }else{
                    soundManager.RecordVoiceAndSendIt(callManager);
                    record = true;
                    talkButton.setText("Recording");
                }
            }
        });


    }




    @Override
    protected void onStop() {
        super.onStop();
    }



    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults, int deviceId) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults, deviceId);
        permissionManager.onRequestPermissionsResult(requestCode,  grantResults);
    }

    public enum intentData{

        CALLING_PARTY,
        CALLING_PARTY_INET_ADDRESS;

    }


}
