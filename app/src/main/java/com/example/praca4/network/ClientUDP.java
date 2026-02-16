package com.example.praca4.network;

import android.util.Log;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.ByteBuffer;

public class ClientUDP {

    public static void sendMessage(InetAddress inetAddress, Tags tag, byte [] data){
        new Thread(() -> {
            try {
                DatagramSocket socket = new DatagramSocket();

                if(inetAddress == NetworkUtilities.WILDCARD)
                    socket.setBroadcast(true);

                byte [] dataToSend;
                if(data != null)
                    dataToSend = ByteBuffer.allocate(Integer.BYTES + data.length).putInt(tag.getInt()).put(data).array();
                else
                    dataToSend = tag.getTag();

                DatagramPacket packet = new DatagramPacket(
                        dataToSend,
                        dataToSend.length,
                        inetAddress,
                        NetworkUtilities.PORT
                );

                socket.send(packet);
                socket.close();

                Log.d("ClientUDP", "sent message with tag " + tag.name() + " and address: " + inetAddress.getHostAddress());
            } catch (Exception e) {
                Log.e("ClientUDP", e.getMessage() != null? e.getMessage() : "Unknown");
            }
        }).start();
    }

}
