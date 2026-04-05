package com.example.praca4.managers;

import android.Manifest;
import android.content.Context;
import android.media.AudioAttributes;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaRecorder;
import android.media.audiofx.AcousticEchoCanceler;
import android.media.audiofx.NoiseSuppressor;
import android.util.Log;

import androidx.annotation.RequiresPermission;

import com.example.praca4.room.dto.AudioDataDto;

import java.nio.ByteBuffer;

public class SoundManager {

    public static final int sampleRate = 16000;
    public static final double minRms = 2000;

    long order = 0;

    public static volatile  boolean sendingSound;
    public static final int bufferSize = AudioRecord.getMinBufferSize(
            sampleRate,
            AudioFormat.CHANNEL_IN_MONO,
            AudioFormat.ENCODING_PCM_16BIT
    );

    private AudioRecord audioRecord = null;


    @RequiresPermission(Manifest.permission.RECORD_AUDIO)
    public static AudioRecord getNewRecorder(Context context) {

        AudioManager audioManager =
                (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);

        audioManager.setMode(AudioManager.MODE_IN_COMMUNICATION);
        audioManager.setSpeakerphoneOn(true);



        AudioRecord audioRecord = new AudioRecord(
                MediaRecorder.AudioSource.VOICE_COMMUNICATION,
                sampleRate,
                AudioFormat.CHANNEL_IN_MONO,
                AudioFormat.ENCODING_PCM_16BIT,
                bufferSize*2
        );

        if (AcousticEchoCanceler.isAvailable()) {
            AcousticEchoCanceler aec =
                    AcousticEchoCanceler.create(audioRecord.getAudioSessionId());

            if (aec != null) {
                aec.setEnabled(true);
            }
        }

        if (NoiseSuppressor.isAvailable()) {
            NoiseSuppressor ns =
                    NoiseSuppressor.create(audioRecord.getAudioSessionId());

            if (ns != null) {
                ns.setEnabled(true);
            }
        }


        return audioRecord;
    }

    @RequiresPermission(Manifest.permission.RECORD_AUDIO)
    public void InitializeRecording(Context context){
        audioRecord = getNewRecorder(context);
        Log.d("SoundManager", "recording initialized");
    }

    public void StopRecording(){
        sendingSound = false;
    }

//    public void RecordVoiceAndSendIt(CallManager callManager){
//
//        sendingSound = true;
//        if(audioRecord != null){
//            Thread sendSound = new Thread(() -> {
//                byte [] audioBuffer = new byte[getBufferSize()];
//                audioRecord.startRecording();
//                while (sendingSound) {
//
//                    int read = audioRecord.read(audioBuffer, 0, 320);
//
//                    if(read > 0){
//                        byte [] soundData = ByteBuffer.allocate(read).put(audioBuffer, 0, read).array();
//
//                        //asdasdasdsaddsads
//
//
//                        if( calculateRms(soundData, read) > minRms){
//                            AudioDataDto audioDataDto = new AudioDataDto(soundData, order);
//                            callManager.sendSound(audioDataDto);
//                        }
//                    }
//                }
//                audioRecord.stop();
//            });
//            sendSound.start();
//        }
//    }


    public void recordAudioData(CallManager2 callManager){

        sendingSound = true;
        if(audioRecord != null){
            Thread sendSound = new Thread(() -> {
                byte [] audioBuffer = new byte[getBufferSize()];
                audioRecord.startRecording();
                while (sendingSound) {

                    int read = audioRecord.read(audioBuffer, 0, 320);

                    if(read > 0){
                        byte [] soundData = ByteBuffer.allocate(read).put(audioBuffer, 0, read).array();

                        //asdasdasdsaddsads


                        if( calculateRms(soundData, read) > minRms){
                            AudioDataDto audioDataDto = new AudioDataDto(soundData, order);
                            callManager.sendAudio(audioDataDto.getAudioMessage());
                        }
                    }
                }
                audioRecord.stop();
            });
            sendSound.start();
        }
    }


    public static AudioTrack getNewPlayer(int audioSessionId) {
        AudioAttributes audioAttributes = new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_MEDIA)
                .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                .build();

        AudioFormat audioFormat = new AudioFormat.Builder()
                .setSampleRate(sampleRate)
                .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                .build();


        return new AudioTrack(audioAttributes,
                audioFormat,
                bufferSize,
                AudioTrack.MODE_STREAM, // to może być do zmiany
                audioSessionId);
    }
    public static int getBufferSize(){
        return bufferSize;
    }

    public static int getSampleRate(){
        return sampleRate;
    }



    private static double calculateRms(byte[] buffer, int readSize) {
        long sum = 0;
        int sampleCount = readSize / 2;

        for (int i = 0; i < readSize - 1; i += 2) {
            short sample = (short) (
                    (buffer[i] & 0xFF) |
                            (buffer[i + 1] << 8)
            );

            sum += sample * sample;
        }

        return Math.sqrt(sum / (double) sampleCount);
    }
}
