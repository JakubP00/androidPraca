package com.example.praca4.room.dto;

import com.example.praca4.sound.AudioDataBufferAndPlayer;

import java.nio.ByteBuffer;


public class AudioDataDto implements Comparable<AudioDataDto>{


    private final byte[] audioData;

    private final long timeStamp;

    private final long order;

    public AudioDataDto(byte[] audioData, long order) {
        this.order = order;
        this.audioData = audioData;
        timeStamp = System.currentTimeMillis();
    }

    public AudioDataDto(byte[] audioMessage){
        ByteBuffer byteBuffer = ByteBuffer.allocate(audioMessage.length);
        order = byteBuffer.getInt();
        timeStamp = byteBuffer.getInt();
        audioData = new byte [audioMessage.length - Long.BYTES * 2];
        byteBuffer.get(audioData, Long.BYTES * 2, audioMessage.length - Long.BYTES * 2);
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
