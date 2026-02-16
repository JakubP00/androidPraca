package com.example.praca4.managers;

import com.example.praca4.room.dto.UserDto;

import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Set;

public class DiscoveryManager {

    public static final int PORT = 50000;

    private final String myIp = getLocalIpAddress();
    private volatile boolean listening = false;
    private DatagramSocket socket;




    public interface Listener {

        void onDeviceFound(String ip);
    }



    public void startListening(Listener listener) {
        listening = true;
        new Thread(() -> {
            try {
                Set<String> devices = new HashSet<>();
                socket = new DatagramSocket(PORT);
                byte[] buffer = new byte[1024];

                while (listening) {
                    DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                    socket.receive(packet);

                    String msg = new String(packet.getData(), 0, packet.getLength(), StandardCharsets.UTF_8);

                    if (msg.equals("LAN_VOICE_HELLO")) {
                        String ip = packet.getAddress().getHostAddress();
                        if (ip.equals(myIp)) {
                            continue;
                        }
                        if (!devices.contains(ip)) {
                            devices.add(ip);
                            listener.onDeviceFound(ip);
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    public void stopListening() {
        listening = false;
        if (socket != null) {
            socket.close(); // unblocks receive()
        }
    }


    public void sendInformationShareRequest(){
        new Thread(() -> {
            try {
                DatagramSocket socket = new DatagramSocket();
                socket.setBroadcast(true);
                byte[] data = CommunicationTags.REQUEST_DEVICES_INFO.getBytes();
                DatagramPacket packet = new DatagramPacket(
                        data,
                        data.length,
                        InetAddress.getByName("255.255.255.255"),
                        PORT
                );
                socket.send(packet);
                socket.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    public void sendDeviceInfo(String ipAddress, UserDto userDto){
        new Thread(() -> {
            try {
                DatagramSocket socket = new DatagramSocket();

                byte[] data = CommunicationTags.DEVICES_INFO.getBytes();
                //byte[] data2 = Serializer.serialize(userDto);
                //byte[] combined = new byte[data.length + data2.length];

                //System.arraycopy(data, 0, combined, 0, data.length);
                //System.arraycopy(data2, 0, combined, data.length, data2.length);

                DatagramPacket packet = new DatagramPacket(
                        data,
                        data.length,
                        InetAddress.getByName(ipAddress),
                        PORT
                );
                socket.send(packet);
                socket.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }




    //do zmiany
    public void sendBroadcast() {
        new Thread(() -> {
            try {
                DatagramSocket socket = new DatagramSocket();
                socket.setBroadcast(true);

                byte[] data = "LAN_VOICE_HELLO".getBytes();
                DatagramPacket packet = new DatagramPacket(
                        data,
                        data.length,
                        InetAddress.getByName("255.255.255.255"),
                        PORT
                );

                socket.send(packet);
                socket.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    public static String getLocalIpAddress() {
        try {
            for (NetworkInterface ni : java.util.Collections.list(
                    NetworkInterface.getNetworkInterfaces())) {

                for (InetAddress addr : java.util.Collections.list(ni.getInetAddresses())) {
                    if (!addr.isLoopbackAddress() && addr instanceof Inet4Address) {
                        return addr.getHostAddress();
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private enum CommunicationTags {
        DEVICES_INFO("DEVICES_INFO"),
        REQUEST_DEVICES_INFO("REQUEST_DEVICES_INFO");

        private final String tag;

        CommunicationTags(String tag) {
            this.tag = tag;
        }

        byte [] getBytes(){
            return tag.getBytes();
        }
        public String tag() {
            return tag;
        }
    }

}