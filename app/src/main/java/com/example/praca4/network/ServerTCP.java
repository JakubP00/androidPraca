package com.example.praca4.network;

import android.util.Log;

import com.example.praca4.background.TCPMessagesMatches;
import com.example.praca4.room.dto.UserDto;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;

public class ServerTCP {
    private volatile boolean serverRunning = false;
    private ServerSocket serverSocket;

    public interface ServerTCPListener {
        TCPMessagesMatches.TCPResponse onAnyMessageArrived(Tags tag, byte [] data, InetAddress inetAddress);

    }

    public void startServer(ServerTCPListener listener) {
        serverRunning = true;
        new Thread(() -> {
            Log.d("ServerTCP", "Running");
            try  {
                serverSocket = new ServerSocket(NetworkUtilities.PORT);
                while (serverRunning) {
                    Socket socket = serverSocket.accept();

                    byte[] buffer = new byte[1024];

                    int messageSize = socket.getInputStream().read(buffer);
                    if( messageSize == 1024)
                        throw new NetworkUtilities.BufferExcidedException();

                    Tags tagIn = Tags.fromInt(ByteBuffer.allocate(Integer.BYTES).put(buffer, 0, Integer.BYTES).getInt(0));

                    byte [] dataIn = null;

                    if(messageSize > Integer.BYTES)
                        dataIn = ByteBuffer.allocate(messageSize - Integer.BYTES).put(buffer, Integer.BYTES, messageSize - Integer.BYTES).array();


                    Log.d("ServerTCP", "Message arrived with tag: " + tagIn.name() );

                    TCPMessagesMatches.TCPResponse tcpResponse = listener.onAnyMessageArrived(tagIn, dataIn, socket.getInetAddress());

                    byte [] fullData;

                    if(tcpResponse.getData() != null)
                        fullData = ByteBuffer.allocate(Integer.BYTES + tcpResponse.getData().length)
                                .putInt(tcpResponse.getTag().getInt())
                                .put(tcpResponse.getData())
                                .array();
                    else
                        fullData = tcpResponse.getTag().getTag();

                    socket.getOutputStream().write(fullData);

                    Log.d("ServerTCP", "Responded with: " + tcpResponse.getTag().name());

                    socket.close();
                }
            } catch (Exception e) {
                Log.e("ServerTCP", e.getMessage() != null ? e.getMessage() : "Unknown ServerTCP error");
                serverRunning = false;
            }
        }).start();
    }
    public void stopServer() {
        if(!serverRunning)
            return;
        serverRunning = false;
        Log.d("ServerTCP", "Stoped");
        try {
            if(serverSocket != null)
                serverSocket.close();
        } catch (IOException e) {
            Log.e("ServerTCP", e.getMessage() != null ? e.getMessage() : "Unknown ServerTCP error");
        }
    }
    public boolean isServerRunning() {
        return serverRunning;
    }
}
