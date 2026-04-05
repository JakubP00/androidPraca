package com.example.praca4.managers;

import android.content.Context;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModel;

import com.example.praca4.background.TCPMessagesMatches;
import com.example.praca4.background.UDPMessagesMatches;
import com.example.praca4.network.ClientTCP;
import com.example.praca4.network.ClientUDP;
import com.example.praca4.network.Tags;
import com.example.praca4.room.Database;
import com.example.praca4.room.dto.AudioDataDto;
import com.example.praca4.room.dto.CurrentCallDto;
import com.example.praca4.sound.AudioDataBufferAndPlayer;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public class CallManager2 {



    private CallMembers callMembers;
    private final AppCompatActivity appCompatActivity;

    public CallManager2( AppCompatActivity appCompatActivity) {
        this.appCompatActivity = appCompatActivity;
        Database database = Database.getInstance(appCompatActivity);

        database.currentCallDao().observeUsersInCall().observe(appCompatActivity, users -> {
            for(String uuid : users){
                if (!callMembers.map.containsKey(uuid))
                    CallMember.create(uuid, database, (callMember) -> {
                        callMembers.map.put(uuid, callMember);
                    });
            }
        });
    }

    public void sendAudio(byte [] audioData){
        callMembers.map.forEach((uuid, callMember  ) -> {
            callMember.sendSound(audioData);
        });
    }

    private static class CallMembers extends ViewModel {
        public Map<String, CallMember> map = new HashMap<>();
    }

    public static class CallMember{

        private State state;
        private InetAddress userIpAddress;
        private String userUuid;
        private String userName;
        private int audioId;
        private boolean saved;
        private boolean playSound = false;
        private boolean sendSound = false;
        private TCPMessagesMatches callRequestPositiveAnswer;
        private TCPMessagesMatches callRequestNegativeAnswer;
        private TCPMessagesMatches callEnd;
        private UDPMessagesMatches audioDataReceived;
        private AudioDataBufferAndPlayer audioDataBufferAndPlayer;

        private CallMember(){}

        public static void create(String uuid, Database database, Consumer<CallMember> callback) {
            Database.databaseExecutor.execute(() -> {

                CurrentCallDto dto = database.currentCallDao().getUserData(uuid);


                CallMember member = new CallMember();

                try {
                    member.userIpAddress = InetAddress.getByName(dto.getIpAddress());
                } catch (UnknownHostException e) {
                    throw new RuntimeException(e);
                }
                member.userUuid = dto.getUuid();
                member.userName = dto.getUserName();
                member.audioId = dto.getAudioId();
                member.saved = dto.isSaved();
                member.audioDataBufferAndPlayer = new AudioDataBufferAndPlayer(member.audioId);
                member.callRequestPositiveAnswer = new TCPMessagesMatches(Tags.CALL_REQUEST_POSITIVE_ANSWER, member.userIpAddress, new TCPMessagesMatches.Handler() {
                    @Override
                    public TCPMessagesMatches.TCPResponse onMatch(byte[] data, InetAddress inetAddress, Context context) {
                        member.updateState(State.CONNECTED);
                        return new TCPMessagesMatches.TCPResponse(Tags.OK);
                    }
                });
                member.callRequestNegativeAnswer = new TCPMessagesMatches(Tags.CALL_REQUEST_NEGATIVE_ANSWER, member.userIpAddress, new TCPMessagesMatches.Handler() {
                    @Override
                    public TCPMessagesMatches.TCPResponse onMatch(byte[] data, InetAddress inetAddress, Context context) {
                        member.updateState(State.REFUSED);
                        return new TCPMessagesMatches.TCPResponse(Tags.OK);
                    }
                });
                member.callEnd = new TCPMessagesMatches(Tags.CALL_END, member.userIpAddress, new TCPMessagesMatches.Handler() {
                    @Override
                    public TCPMessagesMatches.TCPResponse onMatch(byte[] data, InetAddress inetAddress, Context context) {
                        member.updateState(State.STOPED);
                        return new TCPMessagesMatches.TCPResponse(Tags.OK);
                    }
                });
                member.audioDataReceived = new UDPMessagesMatches(Tags.AUDIO, member.userIpAddress, new UDPMessagesMatches.Handler() {
                    @Override
                    public void onMatch(byte[] data, InetAddress inetAddress, Context context) {
                        if(member.playSound)
                            member.audioDataBufferAndPlayer.insertToTheQueue(new AudioDataDto(data));
                    }
                });

                member.updateState(State.fromInt(dto.getState()));
                callback.accept(member);
            });
        }


        public void updateState(State state){
            //w przypadku gdym implementował powrut po kraszu to każdy matcher musi być ustawiony dla każdego tagu
            switch (state){
                case UN_CALLED:
                    callRequestPositiveAnswer.start();
                    callRequestNegativeAnswer.start();
                    ClientTCP.sendMessage(userIpAddress, Tags.CALL_REQUEST, null, new ClientTCP.ClientTCPListener() {
                        @Override
                        public void onMessageArrived(Tags tag, byte[] data) {
                            if(tag == Tags.CALL_REQUEST){
                                updateState(State.CAllED);
                            }else{
                                updateState(State.UN_ANSWERED);
                            }
                        }
                    });
                    break;
                case CAllED:
                    break;
                case CONNECTED:
                    audioDataReceived.start();
                    callRequestPositiveAnswer.stop();
                    callRequestNegativeAnswer.stop();
                    sendSound = true;
                    playSound = true;
                    updateState(State.CONNECTED_INITIALIZED);
                    break;
                case CONNECTED_INITIALIZED:
                    break;
                case REFUSED:
                    callRequestPositiveAnswer.stop();
                    callRequestNegativeAnswer.stop();
                    break;
                case UN_ANSWERED:
                    break;
                case STOPED:
                    sendSound = true;
                    playSound = true;
                    break;
            }

        }

        public void sendSound(byte [] audioData){
            ClientUDP.sendMessage(userIpAddress, Tags.AUDIO, audioData);
        }

        public State getState() {
            return state;
        }

        public String getUserName() {
            return userName;
        }

        public enum State{
            UN_CALLED(0), //was not called yet
            CAllED(1), //was called account is set to answer
            CONNECTED(2), // is consider ready to receive sound may be already sending sound
            CONNECTED_INITIALIZED(3), // is consider ready to receive sound and both receiving and sending sound is already setup
            REFUSED(4), //user REFUSED
            UN_ANSWERED(5),//was called but user account is set to not answer
            STOPED(6); // user disconnected from the call
            private int x;
            State(int x){
                this.x = x;
            }


            public static State fromInt(int value) {
                for (State state : State.values()) {
                    if (state.x == value) {
                        return state;
                    }
                }
                throw new IllegalArgumentException("Unknown tag value: " + value);
            }
            public int getX() {return x;}
        }
    }

}
