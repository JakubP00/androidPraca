package com.example.praca4.network;

import android.util.Log;

import org.json.JSONObject;

import java.net.InetAddress;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

public class ClientTCP {

    public ClientTCP(){}

    public interface ClientTCPListener {
        void onMessageArrived(Tags tag, byte [] data);

    }

    public static void sendMessage(InetAddress inetAddress, Tags tag, byte [] dataToSend, ClientTCPListener listener ) {
        new Thread(() -> {
            try (Socket socket = new Socket(inetAddress, NetworkUtilities.PORT)) {

                if(dataToSend != null) {
                    socket.getOutputStream().write(
                            ByteBuffer.allocate(Integer.BYTES + dataToSend.length)
                                    .putInt(tag.getInt())
                                    .put(dataToSend)
                                    .array()
                    );
                }else{
                    socket.getOutputStream().write(tag.getTag());
                }

                Log.d("ClientTCP", "Message send with tag: " + tag.name());

                byte[] buffer = new byte[1024];
                int messageSize = socket.getInputStream().read(buffer);

                if(messageSize == 1024)
                    throw new NetworkUtilities.BufferExcidedException();

                int tagCode = ByteBuffer.allocate(Integer.BYTES).put(buffer, 0, Integer.BYTES).getInt(0);


                byte [] data = null;
                if(messageSize > Integer.BYTES)
                    data = ByteBuffer.allocate(messageSize - Integer.BYTES).put(buffer, Integer.BYTES, messageSize - Integer.BYTES).array();


                listener.onMessageArrived(Tags.fromInt(tagCode), data);


                Log.d("ClientTCP", "Response received with tag: " + Tags.fromInt(tagCode).name());


            } catch (Exception e) {
                Log.e("ClientTCP", e.getMessage() != null ? e.getMessage() : "Unknown exception");
            }
        }).start();
    }
}
