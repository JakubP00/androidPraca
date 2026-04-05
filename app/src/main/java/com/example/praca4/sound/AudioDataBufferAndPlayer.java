package com.example.praca4.sound;

import android.media.AudioTrack;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;

import com.example.praca4.managers.SoundManager;
import com.example.praca4.room.dto.AudioDataDto;


public class AudioDataBufferAndPlayer {

    private final AudioDataDtoWrapper highestOrder = new AudioDataDtoWrapper(false);
    private final AudioDataDtoWrapper lowestOrder = new AudioDataDtoWrapper(true);
    private final int myAudioId;
    private final AudioTrack audioTrack;

    private int numberOfSamples = 0;
    private final Object queueLock = new Object();
    private final HandlerThread audioThread;
    private final Handler handler;

    private final Runnable playTask = new Runnable() {
        @Override
        public void run() {

            AudioDataDto current;

            synchronized (queueLock) {
                current = getLowest();
                if (current == null) return;
                removeLowest();
            }

            byte[] audio = current.getAudioData();
            audioTrack.write(audio, 0, audio.length);

            AudioDataDto next;
            synchronized (queueLock) {
                 next = getLowest();
            }

            if (next != null) {

                long delayMs =  next.getTimeStamp() - System.currentTimeMillis()
                        + delayGlobalMs + extraDelay - 10;

                handler.postDelayed(this, Math.max(0, delayMs));
            }
        }
    };
    private long extraDelay = 0;
    private static int delayGlobalMs = 50;

    public AudioDataBufferAndPlayer(int audioId){
        this.myAudioId = audioId;
        audioTrack = SoundManager.getNewPlayer(audioId);
        audioTrack.play();
        audioThread = new HandlerThread("AudioPlayerThread: " + audioId);
        audioThread.start();
        handler = new Handler(audioThread.getLooper());
        highestOrder.setLowerOrder(lowestOrder);
    }
    public void insertToTheQueue(AudioDataDto audioDataDto){
        Log.d("AudioDataBufferAndPlayer", "inserting to the Queue" );
        long offsetSample = System.currentTimeMillis() - audioDataDto.getTimeStamp();
        boolean shouldSchedule = false;

        if(numberOfSamples < 5){
            extraDelay = (extraDelay * numberOfSamples + offsetSample)/ (numberOfSamples + 1);
            numberOfSamples++;
        }else if (Math.abs(offsetSample - extraDelay) < 50) {
            extraDelay = (extraDelay * 4 + offsetSample) / 5;
        }else {
            extraDelay = (extraDelay * 9 + offsetSample) / 10;
        }

        synchronized (queueLock) {
            AudioDataDtoWrapper audioDataDtoWrapper = new AudioDataDtoWrapper(audioDataDto);
            AudioDataDtoWrapper needsTesting = highestOrder;

            while (needsTesting.isHigherThan(audioDataDtoWrapper)) {
                needsTesting = needsTesting.getLowerOrder();
            }
            needsTesting.insertAfter(audioDataDtoWrapper);

            if (needsTesting == lowestOrder) {
                shouldSchedule = true;
            }
        }
        if(shouldSchedule){
            schedule();
        }
    }
    private AudioDataDto getLowest(){
        return lowestOrder.getHigherOrder().getAudioDataDto();
    }
    private AudioDataDto removeLowest(){
        AudioDataDto audioDataDto = lowestOrder.getHigherOrder().getAudioDataDto();

        if(audioDataDto != null){
            lowestOrder.setHigherOrder(lowestOrder.getHigherOrder().getHigherOrder());
        }

        return audioDataDto;
    }

    private void schedule() {
        handler.removeCallbacks(playTask);
        if(lowestOrder.higherOrder == highestOrder)
            return;
        long delayMs = Math.max(0, getLowest().getTimeStamp() - System.currentTimeMillis() + delayGlobalMs + extraDelay);
        handler.postDelayed(playTask, delayMs);
    }
    public void cancel() {
        handler.removeCallbacks(playTask);
    }
    public int getMyAudioId() {
        return myAudioId;
    }

    private static class AudioDataDtoWrapper {

        private final AudioDataDto audioDataDto;
        private AudioDataDtoWrapper higherOrder = null;
        private AudioDataDtoWrapper lowerOrder = null;
        private boolean lowest = false;
        private boolean highest = false;
        AudioDataDtoWrapper(AudioDataDto audioDataDto){
            this.audioDataDto = audioDataDto;
        }
        AudioDataDtoWrapper(boolean lowest){
            audioDataDto = null;
            this.lowest = lowest;
            this.highest = !lowest;

        }
        public AudioDataDto getAudioDataDto() {return audioDataDto;}
        public AudioDataDtoWrapper getHigherOrder() {return higherOrder;}
        public void setHigherOrder(AudioDataDtoWrapper higherOrder) {
            this.higherOrder = higherOrder;
            if(higherOrder != null)
                higherOrder.lowerOrder = this;
        }
        public boolean isLowerThan(AudioDataDtoWrapper audioDataDtoWrapper){
            if(lowest)
                return true;
            if(highest)
                return false;
            return getAudioDataDto().getOrder() - audioDataDtoWrapper.getAudioDataDto().getOrder() < 0;
        }
        public boolean isHigherThan(AudioDataDtoWrapper audioDataDtoWrapper){
            if(highest)
                return true;
            if(lowest)
                return false;
            return getAudioDataDto().getOrder() - audioDataDtoWrapper.getAudioDataDto().getOrder() > 0;
        }
        public void insertAfter (AudioDataDtoWrapper audioDataDtoWrapper){
            audioDataDtoWrapper.setHigherOrder(this.getHigherOrder());
            audioDataDtoWrapper.setLowerOrder(this);
        }
        public AudioDataDtoWrapper getLowerOrder() {
            return lowerOrder;
        }
        public void setLowerOrder(AudioDataDtoWrapper lowerOrder) {
            this.lowerOrder = lowerOrder;
            if(lowerOrder != null)
                lowerOrder.higherOrder = this;
        }

    }
}
