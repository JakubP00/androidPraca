package com.example.praca4.activities;
import static java.lang.Thread.setDefaultUncaughtExceptionHandler;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.example.praca4.R;
import com.example.praca4.managers.UserDataManager;
import com.example.praca4.network.BackgroundTask;
import com.example.praca4.room.Database;
import com.example.praca4.room.dto.UserDto;

import java.util.List;

public class MainPage extends AppCompatActivity {


    Button myProfile;
    Button scanButton;

    //ServerTCP serverTCP;
    //ServerUDP serverUDP;









    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_page);


        setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
            @Override
            public void uncaughtException(@NonNull Thread t, @NonNull Throwable e) {
                Log.e("UncaughtExceptionHandler", t.getName() + (e.getMessage() != null ? e.getMessage() : "no message"));
            }
        });



        myProfile = findViewById(R.id.btnProfile);
        scanButton = findViewById(R.id.btnScan);

        Database database = Database.getInstance(this.getApplicationContext());



        Database.databaseExecutor.execute(() -> {
            database.localNetworkUsersDao().clearTable();
        });



        Intent intent = new Intent(this, BackgroundTask.class);
        startForegroundService( intent);




        //serverUDP = new ServerUDP();
        //serverTCP = new ServerTCP();
        //ClientTCP clientTCP = new ClientTCP();

        UserDataManager userDataManager = new UserDataManager(this);



        UserDto meUserDto;


        try{
            meUserDto = userDataManager.getUserDto();

//            if(!serverUDP.isListening())
//                serverUDP.startListening(new ServerUDP.ServerUDPListener() {
//                    @Override
//                    public void onDeviceInfoExchangeRequests(String ip) {
//                        clientTCP.sendMessage(ip, Tags.DEVICE_INFO_EXCHANGE, meUserDto.getJSON(), new ClientTCP.ClientTCPListener() {
//                            @Override
//                            public void onMassageArrived(String jsonString) {
//                                Database.databaseExecutor.execute(() -> {
//                                    database.localNetworkUsersDao().insertAll(new LocalNetworkUsers(new UserDto(jsonString)));
//                                });
//                            }
//                        });
//                    }
//
//                    @Override
//                    public void onAudioReceived(String ip, byte[] audioData) {
//
//
//                    }
//                });
//
//            if(!serverTCP.isServerRunning())
//                serverTCP.startServer(new ServerTCP.ServerTCPListener() {
//                    @Override
//                    public UserDto onDeviceInfoArrived(UserDto userDto) {
//                        Database.databaseExecutor.execute(() -> {
//                            database.localNetworkUsersDao().insertAll(new LocalNetworkUsers(userDto));
//                        });
//                        return meUserDto;
//                    }
//                });

        }catch (UserDataManager.NotInitialized e){

            Intent intent2 = new Intent(this, MyProfile.class);
            startActivity(intent2);
        }




        myProfile.setOnClickListener( (l) -> {
            Intent intent2 = new Intent(this, MyProfile.class);
            startActivity(intent2);
        });


        scanButton.setOnClickListener((l) -> {
            Intent intent2 = new Intent(this, UsersList.class);
            startActivity(intent2);
        });



    }

    @Override
    protected  void  onResume(){
        super.onResume();



    }


        @Override
    protected  void  onPause(){
        super.onPause();

    }


    @Override
    protected void onStop() {
        super.onStop();

    }


    @Override protected void onDestroy()
    {
        super.onDestroy();

        //serverUDP.stopListening();
        //serverTCP.stopServer();

    }
}
