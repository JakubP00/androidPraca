package com.example.praca4.activities;

import static java.lang.Thread.setDefaultUncaughtExceptionHandler;


import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.praca4.R;
import com.example.praca4.managers.CallManager2;
import com.example.praca4.network.ClientTCP;
import com.example.praca4.network.Tags;
import com.example.praca4.room.Database;
import com.example.praca4.room.entities.CurrentCall;
import com.example.praca4.room.entities.LocalNetworkUsers;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class CallRequest extends AppCompatActivity {

    Button btnReject;
    Button btnAnswer;

    String ipAddress;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_call_request);

        setShowWhenLocked(true);
        setTurnScreenOn(true);

        setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
            @Override
            public void uncaughtException(@NonNull Thread t, @NonNull Throwable e) {
                Log.e("UncaughtExceptionHandler", t.getName() + (e.getMessage() != null ? e.getMessage() : "no message"));
            }
        });


        ipAddress = getIntent().getStringExtra(intentData.CALLING_PARTY_INET_ADDRESS.name());


        btnAnswer = findViewById(R.id.btnAnswer);
        btnReject = findViewById(R.id.btnReject);




        btnAnswer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                try {
                    ClientTCP.sendMessage(InetAddress.getByName(ipAddress), Tags.CALL_REQUEST_POSITIVE_ANSWER, null, new ClientTCP.ClientTCPListener() {
                        @Override
                        public void onMessageArrived(Tags tag, byte[] data) {
                            if(tag == Tags.OK){
                                Database.databaseExecutor.execute(() -> {
                                    Database database =  Database.getInstance(getApplicationContext());

                                    database.currentCallDao().clearTable();

                                    LocalNetworkUsers localNetworkUser =  database.localNetworkUsersDao().getByIpAddress(ipAddress);

                                    database.currentCallDao().insertAll(new CurrentCall(localNetworkUser.getUuid(), 1, CallManager2.CallMember.State.CONNECTED.getX()));
                                });

                                Intent intent = new Intent( getApplicationContext(), Call2.class);

                                startActivity(intent);
                                finish();
                            }else{
                                Log.e("Call", "unexpected");
                            }
                        }
                    });
                } catch (UnknownHostException e) {
                    throw new RuntimeException(e);
                }


            }
        });

        btnReject.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    ClientTCP.sendMessage(InetAddress.getByName(ipAddress), Tags.CALL_REQUEST_NEGATIVE_ANSWER, null, new ClientTCP.ClientTCPListener() {
                        @Override
                        public void onMessageArrived(Tags tag, byte[] data) {}
                    });
                } catch (UnknownHostException e) {
                    throw new RuntimeException(e);
                }
                finish();
            }
        });
    }
    public enum intentData{
        CALLING_PARTY_INET_ADDRESS;

    }

}
