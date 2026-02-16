package com.example.praca4.managers;


import android.media.*;
import java.net.*;

public class VoiceManager {

    private static final int AUDIO_PORT = 50001;
    private boolean talking = false;

    private final int sampleRate = 16000;
    private final int bufferSize;

    private final AudioRecord recorder;
    private final AudioTrack player;

    public VoiceManager() {
        bufferSize = AudioRecord.getMinBufferSize(
                sampleRate,
                AudioFormat.CHANNEL_IN_MONO,
                AudioFormat.ENCODING_PCM_16BIT
        );

        recorder = new AudioRecord(
                MediaRecorder.AudioSource.MIC,
                sampleRate,
                AudioFormat.CHANNEL_IN_MONO,
                AudioFormat.ENCODING_PCM_16BIT,
                bufferSize
        );

        player = new AudioTrack(
                AudioManager.STREAM_MUSIC,
                sampleRate,
                AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_16BIT,
                bufferSize,
                AudioTrack.MODE_STREAM
        );
    }

    public void startReceiving() {
        new Thread(() -> {
            try {
                DatagramSocket socket = new DatagramSocket(AUDIO_PORT);
                byte[] buffer = new byte[bufferSize];
                player.play();

                while (true) {
                    DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                    socket.receive(packet);
                    player.write(packet.getData(), 0, packet.getLength());
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    public void startTalking(String targetIp) {
        talking = true;
        new Thread(() -> {
            try {
                DatagramSocket socket = new DatagramSocket();
                InetAddress address = InetAddress.getByName(targetIp);
                byte[] buffer = new byte[bufferSize];

                recorder.startRecording();

                while (talking) {
                    int read = recorder.read(buffer, 0, buffer.length);
                    DatagramPacket packet = new DatagramPacket(
                            buffer, read, address, AUDIO_PORT
                    );
                    socket.send(packet);
                }

                recorder.stop();
                socket.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    public void stopTalking() {
        talking = false;
    }
}