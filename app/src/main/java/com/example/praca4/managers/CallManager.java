package com.example.praca4.managers;

import android.content.Context;
import android.media.AudioTrack;
import android.util.Log;

import com.example.praca4.adapters.CallAdapter;
import com.example.praca4.background.TCPMessagesMatches;
import com.example.praca4.background.UDPMessagesMatches;
import com.example.praca4.network.ClientTCP;
import com.example.praca4.network.ClientUDP;
import com.example.praca4.network.Tags;
import com.example.praca4.room.dto.AudioDataDto;
import com.example.praca4.room.dto.CurrentCallDto;
import com.example.praca4.room.dto.UserDto;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class CallManager {

    private final Map<InetAddress, ConnectedUser>  connectedUsers;


    private final Handler handler;
    public interface Handler{
        void onStatusChange();
    }

    public CallManager(Handler handler){
        this.handler = handler;
        connectedUsers = new HashMap<>();
    }


    public List<CallAdapter.ConnectedUser> getConnectedUsersList() {
        List<CallAdapter.ConnectedUser> connectedUsersList = new ArrayList<>();
        connectedUsers.forEach(((inetAddress, connectedUser) ->{
            connectedUsersList.add(new CallAdapter.ConnectedUser("Needs finishing", connectedUser.getState()));
        } ));
        Log.d("Call", "amount of users: "  + connectedUsersList.size());
        return  connectedUsersList;
    }

    public boolean addUser(ConnectedUser connectedUser){



        try {
            InetAddress inetAddress = InetAddress.getByName(connectedUser.currentCallDto.getIpAddress());
            connectedUser.setHandler(new ConnectedUser.Handler() {
                @Override
                public void onStatusChange() {
                    handler.onStatusChange();
                }
            });
            if(connectedUsers.containsKey(inetAddress)){
                Objects.requireNonNull(connectedUsers.get(inetAddress)).setStillInCall(true);
                return false;
            }else{
                connectedUsers.put(inetAddress, connectedUser);
                connectedUser.setStillInCall(true);
                return true;
            }

        } catch (UnknownHostException e) {
            Log.e("CallManager", "IpAddress can not be interpreted " + connectedUser.currentCallDto.getIpAddress());
            return false;
        }
    }

    public void resetStillInCallFlag(){
        connectedUsers.forEach( (inetAddress, connectedUser) -> {
            connectedUser.setStillInCall(false);
        });
    }

    public void removeUsersNotInCall(){
        connectedUsers.entrySet().removeIf(mapObject -> {
            if (!mapObject.getValue().isStillInCall()){
                mapObject.getValue().stop();
                return true;
            }
            return false;
        });
    }

    public void callUsersNotYetCalled(){
        connectedUsers.forEach(((inetAddress, connectedUser) -> {
            if(connectedUser.getState() == ConnectedUser.State.UN_CALLED){

                connectedUser.generateTCPResponses();
                connectedUser.getTcpMessagesMatchPOSITIVE().start();
                connectedUser.getTcpMessagesMatchNEGATIVE().start();

                ClientTCP.sendMessage(inetAddress, Tags.CALL_REQUEST, null, new ClientTCP.ClientTCPListener() {
                    @Override
                    public void onMessageArrived(Tags tag, byte[] data) {
                        if(tag == Tags.CALL_REQUEST){
                            connectedUser.setState(ConnectedUser.State.CAllED);
                        }else{
                            connectedUser.setState(ConnectedUser.State.UN_ANSWERED);
                            connectedUser.getTcpMessagesMatchPOSITIVE().stop();
                            connectedUser.getTcpMessagesMatchNEGATIVE().stop();
                        }
                    }
                });
            }
        }));


    }

    public void sendSound(AudioDataDto audioDataDto){
        connectedUsers.forEach( ((inetAddress, connectedUser) -> {

            if(connectedUser.getState() == ConnectedUser.State.CONNECTED_INITIALIZED)
                connectedUser.sendSound(audioDataDto.getAudioMessage());

        }));
    }

    public void playSound(){
        ConnectedUser.setPlaySound(true);
    }

    public void stopPlayingSound(){
        ConnectedUser.setPlaySound(false);
    }
    public void stopCall(){
        connectedUsers.forEach(((inetAddress, connectedUser) -> {

            ConnectedUser.State state = connectedUser.getState();

            switch (state){
//                case UN_CALLED:
//                    break;

                case CONNECTED_INITIALIZED:
                    connectedUser.stop();
                case CAllED:
                case CONNECTED:
                    ClientTCP.sendMessage(inetAddress, Tags.CALL_ENDED, null, new ClientTCP.ClientTCPListener() {
                        @Override
                        public void onMessageArrived(Tags tag, byte[] data) {}
                    });
                    break;
//                case REFUSED:
//                    break;

//                case UN_ANSWERED:
//                    break;

                default:
            }

        }));


    }




    public static class ConnectedUser{

        private static boolean playSound = false;
        private final AudioTrack audioTrack;
        private final CurrentCallDto currentCallDto;
        private TCPMessagesMatches tcpMessagesMatchPOSITIVE = null;
        private TCPMessagesMatches tcpMessagesMatchNEGATIVE = null;

        private ConnectedUser.Handler handler;
        private interface Handler{
            void onStatusChange();
        }
        private UDPMessagesMatches udpMessagesMatchesReceivedSound;
        private State state;
        public ConnectedUser(int audioId, CurrentCallDto currentCallDto, State state) {
            this.audioTrack = SoundManager.getNewPlayer(audioId);
            this.currentCallDto = currentCallDto;
            this.state = state;
            if(state == State.CONNECTED)
                start();
        }

        public static void setPlaySound(boolean playSound) {
            ConnectedUser.playSound = playSound;
        }

        public void setHandler(Handler handler){
            this.handler = handler;
        }
        public void stop(){
            if(udpMessagesMatchesReceivedSound != null)
                udpMessagesMatchesReceivedSound.stop();
            audioTrack.stop();
        }

        public CurrentCallDto getCurrentCallDto() {return currentCallDto;}
        public AudioTrack getAudioTrack() {return audioTrack;}

        public State getState() {
            return state;
        }

        public void setState(State state) {
            this.state = state;
            if(handler != null)
                handler.onStatusChange();
        }

        public TCPMessagesMatches getTcpMessagesMatchPOSITIVE() {
            return tcpMessagesMatchPOSITIVE;
        }


        public TCPMessagesMatches getTcpMessagesMatchNEGATIVE() {
            return tcpMessagesMatchNEGATIVE;
        }

        public void start() {

            try {
                InetAddress inetAddress = InetAddress.getByName(currentCallDto.getIpAddress());
                audioTrack.play();
                udpMessagesMatchesReceivedSound  = new UDPMessagesMatches(Tags.AUDIO, inetAddress, new UDPMessagesMatches.Handler() {
                    @Override
                    public void onMatch(byte[] data, InetAddress inetAddress, Context context) {
                        if(playSound)
                            audioTrack.write(data, 0, data.length);
                    }

                });

                udpMessagesMatchesReceivedSound.start();
                setState(State.CONNECTED_INITIALIZED);

            } catch (UnknownHostException e) {
                throw new RuntimeException(e);
            }
        }

        public void sendSound(byte [] soundData){

            try {
                InetAddress inetAddress = InetAddress.getByName(currentCallDto.getIpAddress());
                ClientUDP.sendMessage(inetAddress, Tags.AUDIO, soundData);
            } catch (UnknownHostException e) {
                throw new RuntimeException(e);
            }

        }

        public void generateTCPResponses(){

            try {
                InetAddress inetAddress = InetAddress.getByName(currentCallDto.getIpAddress());
                tcpMessagesMatchPOSITIVE = new TCPMessagesMatches(Tags.CALL_REQUEST_POSITIVE_ANSWER, inetAddress, new TCPMessagesMatches.Handler() {
                    @Override
                    public TCPMessagesMatches.TCPResponse onMatch(byte[] data, InetAddress inetAddress, Context context) {
                        tcpMessagesMatchPOSITIVE.stop();
                        tcpMessagesMatchNEGATIVE.stop();
                        setState( State.CONNECTED);
                        start();

                        return new TCPMessagesMatches.TCPResponse(Tags.OK);
                    }
                });
                tcpMessagesMatchNEGATIVE = new TCPMessagesMatches(Tags.CALL_REQUEST_NEGATIVE_ANSWER, inetAddress, new TCPMessagesMatches.Handler() {
                    @Override
                    public TCPMessagesMatches.TCPResponse onMatch(byte[] data, InetAddress inetAddress, Context context) {
                        tcpMessagesMatchPOSITIVE.stop();
                        tcpMessagesMatchNEGATIVE.stop();
                        setState( State.REFUSED);
                        return new TCPMessagesMatches.TCPResponse(Tags.OK);
                    }
                });
            } catch (UnknownHostException e) {
                Log.e("Test2", e.getMessage() != null ? e.getMessage() : "inetAddress error"); //poprawić pewnie dodać InetAddress do zmiennych
            }



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
            public int getX() {return x;}
        }
    }
}
