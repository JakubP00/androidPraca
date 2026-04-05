package com.example.praca4.network;

import java.nio.ByteBuffer;

public enum Tags {

    DEVICE_INFO_EXCHANGE(0),
    DEVICE_INFO_EXCHANGE_REQUEST(1),

    AUDIO(2),
    CALL_REQUEST(3),
    CALL_REQUEST_NEGATIVE_ANSWER(4),
    CALL_REQUEST_POSITIVE_ANSWER(5),

    REFUSED(6),
    OK(7),
    BAD_PATH(8),

    ERROR(9),
    CALL_END(10);


    private final int x;
    Tags(int x) {
        this.x = x;
    }

    public static Tags fromInt(int value) {
        for (Tags tag : Tags.values()) {
            if (tag.x == value) {
                return tag;
            }
        }
        throw new IllegalArgumentException("Unknown tag value: " + value);
    }
    public byte [] getTag(){
        return  ByteBuffer.allocate(Integer.BYTES).putInt(x).array();
    }
    public int getInt() {return x;}
}