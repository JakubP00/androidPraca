package com.example.praca4.network;


import static java.lang.Integer.min;

import android.util.Log;

import com.example.praca4.managers.SoundManager;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.util.Objects;

public class ServerUDP {

    private volatile boolean listening = false;

    private DatagramSocket socket;
    public interface ServerUDPListener {
        void onAnyMessageArrived(Tags tag, byte [] data, InetAddress inetAddress);

    }


    public void startListening(ServerUDPListener listener) {
        listening = true;
        new Thread(() -> {
            try {
                Log.d("ServerUDP", "Running");

                socket = new DatagramSocket(NetworkUtilities.PORT);
                byte[] buffer = new byte[SoundManager.getBufferSize() + Integer.BYTES];

                while (listening) {


                    DatagramPacket packet = new DatagramPacket(buffer, buffer.length);

                    socket.receive(packet);



                    Tags tag = Tags.fromInt(ByteBuffer.allocate(Integer.BYTES).put(packet.getData(), 0, Integer.BYTES).getInt(0));
                    byte [] data = null;
                    if(packet.getLength() > Integer.BYTES)
                        data = ByteBuffer.allocate(packet.getLength() - Integer.BYTES).put(buffer, Integer.BYTES, packet.getLength() - Integer.BYTES).array();



                    Log.d("ServerUDP", "Message received tag: " + tag.name());


                    listener.onAnyMessageArrived(tag, data, packet.getAddress());

                }

            } catch (Exception e) {
                Log.e("ServerUDP", e.getMessage() != null ? e.getMessage() + " " : "Unknown ServerUDP error");
                listening = false;
            }
        }).start();
    }

    public void stopListening() {
        if(!listening)
            return;
        listening = false;
        if (socket != null) {
            socket.close(); // unblocks receive()
        }
        Log.d("ServerUDP", "Stoping");
    }


    public boolean isListening() {
        return listening;
    }
}
