package com.example.praca4.activities;

import static java.lang.Thread.setDefaultUncaughtExceptionHandler;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.praca4.R;
import com.example.praca4.managers.CallManager2;
import com.example.praca4.managers.PermissionManager;
import com.example.praca4.managers.SoundManager;


public class Call2 extends AppCompatActivity {


    private Button MuteButton;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.d("CallActivity", "opened");

        setContentView(R.layout.activity_call);

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

        setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
            @Override
            public void uncaughtException(@NonNull Thread t, @NonNull Throwable e) {
                Log.e("UncaughtExceptionHandler", t.getName() + (e.getMessage() != null ? e.getMessage() : "no message"));
            }
        });

        Log.d("CallActivity", "starting call");

        MuteButton = findViewById(R.id.btnTalk);
        CallManager2 callManager = new CallManager2(this);

        SoundManager soundManager = new SoundManager();

        PermissionManager permissionManager = new PermissionManager(Manifest.permission.RECORD_AUDIO, new PermissionManager.Handler() {
            @Override
            public void onPermissionGranted() {
                if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
                    return;
                }
                soundManager.InitializeRecording(getApplicationContext());
            }
        }, "This permission is necessary to be able to speak in a call", this, 100 );



        permissionManager.requestRuntimePermission();
        Log.d("CallActivity", "RECORD_AUDIO granted");
        soundManager.recordAudioData(callManager);
        Log.d("CallActivity", "RECORDing audio");
    }
}
