package com.example.praca4.background;

import static com.example.praca4.network.NetworkUtilities.WILDCARD;

import android.content.Context;
import android.util.Log;

import com.example.praca4.managers.UserDataManager;
import com.example.praca4.network.ClientTCP;
import com.example.praca4.network.NetworkUtilities;
import com.example.praca4.network.Tags;
import com.example.praca4.room.Database;
import com.example.praca4.room.dto.UserDto;
import com.example.praca4.room.entities.LocalNetworkUsers;

import java.lang.ref.WeakReference;
import java.net.InetAddress;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

public class UDPMessagesMatches {
    private final Tags tag;
    private final InetAddress ipAddress;
    private final Handler handler;


    private static final Map<Tags, Map<InetAddress, WeakReference<Handler>>> handlers = new ConcurrentHashMap<>();

    private final static Handler defaultHandler = new Handler() {
        @Override
        public void onMatch(byte[] data, InetAddress inetAddress, Context context) {}
    };




    public void start(){
        handlers.computeIfAbsent(tag, t -> new ConcurrentHashMap<>())
                .put(ipAddress, new WeakReference<>(handler));

        Log.d("UDPMessagesMatches", "adding handler with tag: " + tag.name() + ", and address: " + ipAddress.getHostAddress());
    }


    public static Handler getHandler(Tags tag, InetAddress incomingAddress) {

        Log.d("UDPMessagesMatches", "matching handler with tag: " + tag.name() + ", and address: " + incomingAddress.getHostAddress());

        Map<InetAddress, WeakReference<Handler>> byAddress = handlers.get(tag);
        if (byAddress == null) {
            Log.d("UDPMessagesMatches", "no matcher fo tag " + tag.name());
            return defaultHandler;
        }

        WeakReference<Handler> ref = byAddress.get(incomingAddress);
        if (ref != null) {
            Handler handler = ref.get();
            if (handler != null) {
                Log.d("UDPMessagesMatches", "exact match");
                return handler;
            } else {
                byAddress.remove(incomingAddress); // cleanup
            }
        }

        ref = byAddress.get(WILDCARD);


        if (ref != null) {
            Handler handler = ref.get();
            if (handler != null) {
                Log.d("UDPMessagesMatches", "WILDCARD match");
                return handler;
            } else {
                byAddress.remove(WILDCARD); // cleanup
            }
        }

        if (byAddress.isEmpty()) {
            handlers.remove(tag);
        }

        Log.d("UDPMessagesMatches", "no ip match");
        return defaultHandler;
    }



    public void stop(){
        Map<InetAddress, WeakReference<Handler>> byAddress = handlers.get(tag);
        if (byAddress == null) return;

        byAddress.remove(ipAddress);

        if(byAddress.isEmpty())
            handlers.remove(tag);


        Log.d("UDPMessagesMatches", "removing handler with tag: " + tag.name() + ", and address: " + ipAddress.getHostAddress());
    }


    public UDPMessagesMatches(Tags tag, InetAddress ipAddress, Handler handler) {
        this.tag = tag;
        this.ipAddress = ipAddress;
        this.handler = handler;
    }


    public interface Handler{
        void onMatch(byte [] data, InetAddress inetAddress, Context context);
    }

    public static final UDPMessagesMatches DEVICE_INFO_EXCHANGE_REQUEST =
            new UDPMessagesMatches(Tags.DEVICE_INFO_EXCHANGE_REQUEST, WILDCARD, (data, inetAddress, context) -> {
                if(!inetAddress.getHostAddress().equals(Objects.requireNonNull(NetworkUtilities.getLocalIpAddress()).getHostAddress())){
                    UserDataManager userDataManager = new UserDataManager(context);
                    try {
                        UserDto meUserDto = userDataManager.getUserDto();
                        byte[] dataToSend = meUserDto.getJSON().toString().getBytes(StandardCharsets.UTF_8);
                        ClientTCP.sendMessage(inetAddress, Tags.DEVICE_INFO_EXCHANGE, dataToSend, new ClientTCP.ClientTCPListener() {
                            @Override
                            public void onMessageArrived(Tags tag, byte[] data) {
                                if (tag == Tags.DEVICE_INFO_EXCHANGE) {
                                    String jsonString = new String(data, StandardCharsets.UTF_8);
                                    UserDto userDto = new UserDto(jsonString);
                                    Database database = Database.getInstance(context);
                                    Database.databaseExecutor.execute(() -> {
                                        database.localNetworkUsersDao().insertAll(new LocalNetworkUsers(userDto));
                                    });
                                }
                            }
                        });
                    } catch (UserDataManager.NotInitialized e) {
                        Log.e("UDPMessageMatcher", "User uninitialized");
                    }
                }
            });
}
