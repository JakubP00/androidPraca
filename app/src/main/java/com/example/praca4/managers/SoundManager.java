package com.example.praca4.managers;

import android.Manifest;
import android.media.AudioAttributes;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaRecorder;

import androidx.annotation.RequiresPermission;

public class SoundManager {

    private static final int sampleRate = 16000;
    private static final int bufferSize = AudioRecord.getMinBufferSize(
            sampleRate,
            AudioFormat.CHANNEL_IN_MONO,
            AudioFormat.ENCODING_PCM_16BIT
    );


    @RequiresPermission(Manifest.permission.RECORD_AUDIO)
    public static AudioRecord getNewRecorder() {

        return new AudioRecord(
                MediaRecorder.AudioSource.MIC,
                sampleRate,
                AudioFormat.CHANNEL_IN_MONO,
                AudioFormat.ENCODING_PCM_16BIT,
                bufferSize
        );


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
}
