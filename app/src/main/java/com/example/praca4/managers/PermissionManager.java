package com.example.praca4.managers;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.provider.Settings;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.praca4.activities.Call;

public class PermissionManager {



    private final String permission;
    private final Handler handler;
    private final String message;
    private final Activity activity;
    private final int requestPermissionCode;
    public interface Handler{

        void onPermissionGranted();
    }

    public PermissionManager(String permission, Handler handler, String message, Activity activity, int requestPermissionCode){

        this.permission = permission;
        this.handler = handler;
        this.message = message;
        this.activity = activity;
        this.requestPermissionCode = requestPermissionCode;
    }



    public void requestRuntimePermission(){
        if (ContextCompat.checkSelfPermission(
                activity, permission) ==
                PackageManager.PERMISSION_GRANTED) {
            Log.d("RuntimePermission", "Already have permission");
            handler.onPermissionGranted();
        } else if (ActivityCompat.shouldShowRequestPermissionRationale(
                activity, permission)) {
            AlertDialog.Builder builder = new AlertDialog.Builder(activity);
            builder.setTitle("Permission required");
            builder.setMessage(message);
            builder.setCancelable(false);
            builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    ActivityCompat.requestPermissions(activity, new String[]{permission}, requestPermissionCode);
                }
            });
            builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
            builder.show();

            Log.d("RuntimePermission", "Showing permission rationale");
        } else {
            ActivityCompat.requestPermissions(activity, new String[]{permission}, requestPermissionCode);

            Log.d("RuntimePermission", "Requesting permission");
        }
    }


    public void onRequestPermissionsResult(int requestCode,  int[] grantResults){
        if(requestCode == requestPermissionCode){
            if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                if (ActivityCompat.checkSelfPermission(activity, permission) != PackageManager.PERMISSION_GRANTED) {
                    handler.onPermissionGranted();
                }
            } else if (!ActivityCompat.shouldShowRequestPermissionRationale(activity, permission)) {
                AlertDialog.Builder builder = new AlertDialog.Builder(activity);
                builder.setTitle("Permission required");
                builder.setMessage(message);
                builder.setCancelable(false);
                builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                        Uri uri = Uri.fromParts("package", activity.getPackageName(), null);
                        intent.setData(uri);
                        activity.startActivity(intent);
                        dialog.dismiss();
                    }
                });
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                builder.show();
            }else
                requestRuntimePermission();
        }


    }

}
