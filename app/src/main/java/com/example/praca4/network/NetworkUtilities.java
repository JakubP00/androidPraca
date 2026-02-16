package com.example.praca4.network;

import android.util.Log;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;

public class NetworkUtilities {
    NetworkUtilities(){}

    public static final InetAddress WILDCARD;


    static {
        try {
            WILDCARD = InetAddress.getByName("255.255.255.255");
        } catch (UnknownHostException e) {
            throw new RuntimeException(e);
        }
    }


    public static final int PORT = 50000;
    public static InetAddress getLocalIpAddress() {
        try {
            for (NetworkInterface ni : java.util.Collections.list(
                    NetworkInterface.getNetworkInterfaces())) {

                for (InetAddress addr : java.util.Collections.list(ni.getInetAddresses())) {
                    if (!addr.isLoopbackAddress() && addr instanceof Inet4Address) {
                        return addr;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


    public static boolean checkTag(byte[] buffer, Tags tag){
        int tagCode;
        try {
            tagCode = ByteBuffer.allocate(Integer.BYTES).put(buffer).getInt(0);
            return tagCode == tag.getInt();
        }catch (Error e){
            Log.e("NetworkUtilities", e.getMessage() != null ? e.getMessage() : "Unknown");
        }
        return false;
    }



    public static class BufferExcidedException extends Exception{
        BufferExcidedException(){
            super("BufferExcidedException");
        }
    }
}
