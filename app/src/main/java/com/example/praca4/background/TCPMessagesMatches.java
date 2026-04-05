package com.example.praca4.background;


import android.content.Context;
import android.util.Log;

import com.example.praca4.managers.UserDataManager;
import com.example.praca4.network.Tags;
import com.example.praca4.room.Database;
import com.example.praca4.room.dto.UserDto;
import com.example.praca4.room.entities.LocalNetworkUsers;

import java.lang.ref.WeakReference;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class TCPMessagesMatches {

    private final Tags tag;
    private final InetAddress ipAddress;
    private final Handler handler;


    private final static Handler defaultHandler = new Handler() {
        @Override
        public TCPResponse onMatch(byte[] data, InetAddress inetAddress, Context context) {

            Log.d("TCPMessagesMatches", "responding with defaultHandler");
            return new TCPResponse(Tags.BAD_PATH);
        }
    };
    private final  static Map<Tags, Map<InetAddress, WeakReference<Handler>>> handlers = new ConcurrentHashMap<>();

    public static void logTagMap() {

        if (handlers == null || handlers.isEmpty()) {
            Log.d("TCPMessagesMatches","Map is empty.");
            return;
        }

        for (Map.Entry<Tags, Map<InetAddress, WeakReference<Handler>>> tagEntry : handlers.entrySet()) {
            Tags tag = tagEntry.getKey();
            Map<InetAddress, WeakReference<Handler>> innerMap = tagEntry.getValue();

            Log.d("TCPMessagesMatches","Tag: " + tag.name());

            if (innerMap == null || innerMap.isEmpty()) {
                Log.d("TCPMessagesMatches","  (no addresses)");
                continue;
            }

            for (Map.Entry<InetAddress, WeakReference<Handler>> entry : innerMap.entrySet()) {
                InetAddress address = entry.getKey();
                WeakReference<Handler> weakRef = entry.getValue();

                boolean alive = weakRef != null && weakRef.get() != null;

                Log.d("TCPMessagesMatches","  " + address.getHostAddress() +
                        " -> alive=" + alive);
            }
        }
    }

    public static final InetAddress WILDCARD;

    static {
        try {
            WILDCARD = InetAddress.getByName("255.255.255.255");
        } catch (UnknownHostException e) {
            throw new RuntimeException(e);
        }
    }

    public TCPMessagesMatches(Tags tag, InetAddress ipAddress, Handler handler) {
        this.tag = tag;
        this.ipAddress = ipAddress;
        this.handler = handler;
    }

    public void start(){
        handlers.computeIfAbsent(tag, t -> new ConcurrentHashMap<>())
                .put(ipAddress, new WeakReference<>(handler));

        Log.d("TCPMessagesMatches", "added handler with tag: " + tag.name() + ", and address: " + ipAddress.getHostAddress());
    }

    public void stop(){
        Map<InetAddress, WeakReference<Handler>> byAddress = handlers.get(tag);
        if (byAddress == null) return;

        byAddress.remove(ipAddress);

        if(byAddress.isEmpty())
            handlers.remove(tag);


        Log.d("TCPMessagesMatches", "removed handler with tag: " + tag.name() + ", and address: " + ipAddress.getHostAddress());
    }
    public static Handler getHandler(Tags tag, InetAddress incomingAddress) {
        logTagMap();

        Log.d("TCPMessagesMatches", "matching handler with tag: " + tag.name() + ", and address: " + incomingAddress.getHostAddress());
        Map<InetAddress, WeakReference<Handler>> byAddress = handlers.get(tag);
        if (byAddress == null) {
            return defaultHandler;
        }
        WeakReference<Handler> ref = byAddress.get(incomingAddress);
        if (ref != null) {
            Log.d("TCPMessagesMatches", "found exact match");
            Handler handler = ref.get();
            if (handler != null) {
                return handler;
            } else {
                byAddress.remove(incomingAddress); // cleanup
            }
        }
        ref = byAddress.get(WILDCARD);
        if (ref != null) {
            Handler handler = ref.get();
            if (handler != null) {
                return handler;
            } else {
                byAddress.remove(WILDCARD); // cleanup
            }
        }
        if (byAddress.isEmpty()) {
            handlers.remove(tag);
        }

        return defaultHandler;
    }



    public static class TCPResponse{
        private final Tags tag;
        private final byte [] data;
        TCPResponse(Tags tag, byte[] data){
            this.tag = tag;
            this.data = data;
        }
        public TCPResponse(Tags tag){
            this.tag = tag;
            this.data = null;
        }

        public Tags getTag() {return tag;}
        public byte[] getData() {return data;}
    }


    public interface Handler{
        TCPResponse onMatch(byte [] data, InetAddress inetAddress, Context context);
    }

    public static final TCPMessagesMatches DEVICE_INFO_EXCHANGE =
            new TCPMessagesMatches(Tags.DEVICE_INFO_EXCHANGE, WILDCARD, new Handler() {
                @Override
                public TCPResponse onMatch(byte[] data, InetAddress inetAddress, Context context) {
                    UserDataManager userDataManager = new UserDataManager(context);
                    try {
                        UserDto meUserDto = userDataManager.getUserDto();
                        String jsonString = new String(data, StandardCharsets.UTF_8);
                        UserDto userDto = new UserDto(jsonString);
                        Database database = Database.getInstance(context);
                        Database.databaseExecutor.execute(()->{
                            database.localNetworkUsersDao().insertAll(new LocalNetworkUsers(userDto));
                        });
                        return new TCPResponse(Tags.DEVICE_INFO_EXCHANGE, meUserDto.getJSON().toString().getBytes(StandardCharsets.UTF_8));
                    } catch (UserDataManager.NotInitialized e) {
                        return new TCPResponse(Tags.ERROR);
                    }
                }
            });
}
