package com.example.praca4.activities;

import static java.lang.Thread.setDefaultUncaughtExceptionHandler;

import android.Manifest;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.AudioRecord;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresPermission;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.example.praca4.R;
import com.example.praca4.managers.CallManager;
import com.example.praca4.managers.SoundManager;
import com.example.praca4.managers.VoiceManager;
import com.example.praca4.network.BackgroundTask;
import com.example.praca4.network.ClientTCP;
import com.example.praca4.room.Database;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.util.Objects;


public class Call extends AppCompatActivity {


    private BackgroundTask backgroundTask;
    private boolean callingParty = true;

    private volatile boolean talking = true;
    private final CallManager callManager = new CallManager();
    InetAddress callingPartyInetAddress;

    @RequiresPermission(Manifest.permission.RECORD_AUDIO)
    private AudioRecord audioRecord;

    Database database;

    Thread sendSound;
    private Button returnButton;

    private Button talkButton;
    private TextView tvCallMessage;


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


        tvCallMessage = findViewById(R.id.tvCallMessage);
        returnButton = findViewById(R.id.btnCallReturn);
        talkButton = findViewById(R.id.btnTalk);



        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {

            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }


        byte [] audioBuffer = new byte[SoundManager.getBufferSize()];








        Intent myIntent = getIntent();
        callingParty = myIntent.getBooleanExtra( intentData.CALLING_PARTY.name(), false);
        try {
            callingPartyInetAddress = InetAddress.getByName(myIntent.getStringExtra(intentData.CALLING_PARTY_INET_ADDRESS.name()));
        } catch (UnknownHostException e) {
            callingPartyInetAddress = null;
        }

        Log.d("Test3", "czy ten address ma sens?" + callingPartyInetAddress.getHostAddress());

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
            });


        tvCallMessage.setText("waiting for response");

        returnButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                talking = false;

                callManager.stopPlayingSound();
                callManager.stopCall();

                finish();

            }
        });


        talkButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    talking = true;
                    sendSound = new Thread(() -> {
                        AudioRecord audioRecord = SoundManager.getNewRecorder();
                        audioRecord.startRecording();
                        while (talking) {

                            int read = audioRecord.read(audioBuffer, 0, audioBuffer.length);



                            if(read > 0){
                                byte [] soundData = ByteBuffer.allocate(read).put(audioBuffer, 0, read).array();

                                double sum = 0.0;
                                int sampleCount = read / 2;

                                for (int i = 0; i < read - 1; i += 2) {
                                    short sample = (short) (
                                            (soundData[i] & 0xFF) | (soundData[i + 1] << 8)
                                    );
                                    sum += sample * sample;
                                }

                                double rms = Math.sqrt(sum / sampleCount);

                                double SPEECH_THRESHOLD = 1000;

                                if(rms > SPEECH_THRESHOLD )
                                    callManager.sendSound(soundData);
                            }

                        }
                        audioRecord.stop();
                    });
                    sendSound.start();
                    callManager.stopPlayingSound();
                } else if (event.getAction() == MotionEvent.ACTION_UP) {
                    talking = false;
                    callManager.playSound();
                    v.performClick();
                }
                return true;
            }

        });



    }







    @Override
    protected void onStop() {
        super.onStop();
    }







    public enum intentData{

        CALLING_PARTY,
        CALLING_PARTY_INET_ADDRESS;

    }


}
