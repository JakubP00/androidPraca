package com.example.praca4.network;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.example.praca4.R;
import com.example.praca4.activities.Call;
import com.example.praca4.activities.CallRequest;
import com.example.praca4.background.TCPMessagesMatches;
import com.example.praca4.background.UDPMessagesMatches;
import com.example.praca4.managers.UserDataManager;
import com.example.praca4.room.Database;
import com.example.praca4.room.dto.UserDto;
import com.example.praca4.room.entities.LocalNetworkUsers;

import java.lang.ref.WeakReference;
import java.lang.reflect.Array;
import java.net.InetAddress;
import java.util.HashSet;
import java.util.Set;


public class BackgroundTask extends Service {

    private ServerTCP serverTCP;
    private ServerUDP serverUDP;
    private TCPMessagesMatches callRequest;

    private volatile boolean ansewerAllCalls = true; //volatile może być nie potrzebne sprawdzić ma zaczytać z UserDataMenager

    private final IBinder backgroundTaskBinder = new BackgroundTaskBinder();

    public class BackgroundTaskBinder extends Binder {
        public BackgroundTask getService() {
            return BackgroundTask.this;
        }
    }


    @Override
    public void onCreate() {
        super.onCreate();

        startForeground(1, createDefoultNotification());

        serverTCP = new ServerTCP();
        serverUDP = new ServerUDP();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        callRequest = new TCPMessagesMatches(Tags.CALL_REQUEST, NetworkUtilities.WILDCARD, new TCPMessagesMatches.Handler() {
            @Override
            public TCPMessagesMatches.TCPResponse onMatch(byte[] data, InetAddress inetAddress, Context context) {
                if(ansewerAllCalls){
                    if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                        // TODO: Consider calling
                        //    ActivityCompat#requestPermissions
                        // here to request the missing permissions, and then overriding
                        //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                        //                                          int[] grantResults)
                        // to handle the case where the user grants the permission. See the documentation
                        // for ActivityCompat#requestPermissions for more details.
                    }
                    NotificationManagerCompat.from(getApplicationContext())
                            .notify(2, createCallNotification(inetAddress.getHostAddress()));
                    return new TCPMessagesMatches.TCPResponse(Tags.CALL_REQUEST);
                }else
                    return new TCPMessagesMatches.TCPResponse(Tags.REFUSED);
            }
        });


        callRequest.start();
        TCPMessagesMatches.DEVICE_INFO_EXCHANGE.start();
        UDPMessagesMatches.DEVICE_INFO_EXCHANGE_REQUEST.start();
        startServices();

        return START_STICKY;
    }

    private void startServices() {
            if(!serverUDP.isListening())
                serverUDP.startListening(new ServerUDP.ServerUDPListener() {


                    @Override
                    public void onAnyMessageArrived(Tags tag, byte[] data, InetAddress inetAddress) {
                        UDPMessagesMatches.getHandler(tag, inetAddress).onMatch(data, inetAddress, getApplicationContext());
                    }

                });

            if(!serverTCP.isServerRunning())
                serverTCP.startServer(new ServerTCP.ServerTCPListener() {
                    @Override
                    public TCPMessagesMatches.TCPResponse onAnyMessageArrived(Tags tag, byte[] data, InetAddress inetAddress) {
                        TCPMessagesMatches.Handler handler = TCPMessagesMatches.getHandler(tag, inetAddress);
                        return handler.onMatch(data, inetAddress, getApplicationContext());
                    }
                });
    }

    private Notification createDefoultNotification() {
        String channelId = "service_channel";
            NotificationChannel channel = new NotificationChannel(
                    channelId,
                    "Background Service",
                    NotificationManager.IMPORTANCE_HIGH
            );
            channel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
            getSystemService(NotificationManager.class)
                    .createNotificationChannel(channel);


        return new NotificationCompat.Builder(this, channelId)
                .setContentTitle("Service Running")
                .setContentText("Listening for connections...")
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .build();
    }

    private Notification createCallNotification(String ipAddress){

        Intent intent = new Intent(getApplicationContext(), CallRequest.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra(CallRequest.intentData.CALLING_PARTY_INET_ADDRESS.name(), ipAddress);

        PendingIntent pendingIntent = PendingIntent.getActivity(
                getApplicationContext(),
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        Notification notification = new NotificationCompat.Builder(getApplicationContext(), "service_channel")
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle("Incoming Call")
                .setContentText("User is calling...")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setCategory(NotificationCompat.CATEGORY_CALL)
                .setFullScreenIntent(pendingIntent, true)
                .setContentIntent(pendingIntent)
                .build();

        return notification;
    }




        @Override
    public void onDestroy() {
        super.onDestroy();

        TCPMessagesMatches.DEVICE_INFO_EXCHANGE.stop();
        UDPMessagesMatches.DEVICE_INFO_EXCHANGE_REQUEST.stop();
        callRequest.stop();


        serverUDP.stopListening();
        serverTCP.stopServer();

    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.d( "binding", "on bind invocked" );
        return backgroundTaskBinder;
    }

}
