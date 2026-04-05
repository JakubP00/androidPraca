package com.example.praca4.room.dto;

import android.util.Log;

import com.example.praca4.sound.AudioDataBufferAndPlayer;

import java.nio.ByteBuffer;


public class AudioDataDto implements Comparable<AudioDataDto>{


    private  byte[] audioData;

    private  long timeStamp;

    private  long order;

    public AudioDataDto(byte[] audioData, long order) {
        this.order = order;
        this.audioData = audioData;
        timeStamp = System.currentTimeMillis();
    }

    public AudioDataDto(byte[] audioMessage){
        try{
            Log.d("test3", "0");
            ByteBuffer byteBuffer = ByteBuffer.allocate(audioMessage.length).put(audioMessage);
            byteBuffer.rewind();
            Log.d("test3", "1");
            order = byteBuffer.getLong();
            Log.d("test3", "2");
            timeStamp = byteBuffer.getLong();
            Log.d("test3", "audioMessage size " + (audioMessage.length - Long.BYTES * 2) + "/ " + order + "/ " + timeStamp);
            audioData = ByteBuffer.allocate(audioMessage.length - Long.BYTES * 2).put(audioMessage, Long.BYTES * 2, audioMessage.length - Long.BYTES * 2).array();
        }catch (Exception e){
            Log.e("test3", e.getMessage() != null ? e.getMessage() + " " : "Unknown error");

        }

    }


    public byte[] getAudioMessage(){
        return ByteBuffer.allocate(Long.BYTES * 2 + audioData.length).putLong(order).putLong(timeStamp).put(audioData).array();
    }


    public int compareOrder(AudioDataDto audioDataDto){
        return (int) (order - audioDataDto.order);
    }


    public byte[] getAudioData() {return audioData;}
    public long getTimeStamp() {return timeStamp;}
    public long getOrder() {return order;}

    @Override
    public int compareTo(AudioDataDto o) {
        return (int) (order - o.getOrder());
    }
}
