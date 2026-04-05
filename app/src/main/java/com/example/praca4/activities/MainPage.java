package com.example.praca4.activities;
import static java.lang.Thread.setDefaultUncaughtExceptionHandler;

import android.Manifest;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.example.praca4.R;
import com.example.praca4.managers.PermissionManager;
import com.example.praca4.managers.UserDataManager;
import com.example.praca4.network.BackgroundTask;
import com.example.praca4.room.Database;
import com.example.praca4.room.dto.UserDto;

import java.util.List;

public class MainPage extends AppCompatActivity {


    private Button myProfile;
    private Button scanButton;
    private PermissionManager permissionManager = null;






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





        UserDataManager userDataManager = new UserDataManager(this);



        UserDto meUserDto;


        try{
            meUserDto = userDataManager.getUserDto();
        }catch (UserDataManager.NotInitialized e){

            Intent intent2 = new Intent(this, MyProfile.class);
            startActivity(intent2);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissionManager = new PermissionManager(Manifest.permission.POST_NOTIFICATIONS, new PermissionManager.Handler() {
                @Override
                public void onPermissionGranted() {
                }
            }, "To be able to answer a call application needs this permission", this, 101);
            permissionManager.requestRuntimePermission();
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

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults, int deviceId) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults, deviceId);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && permissionManager != null)
            permissionManager.onRequestPermissionsResult(requestCode,  grantResults);
    }

}
