package com.example.praca4.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.praca4.R;
import com.example.praca4.adapters.UserListAdapter;
import com.example.praca4.network.ClientUDP;
import com.example.praca4.managers.UserDataManager;
import com.example.praca4.network.NetworkUtilities;
import com.example.praca4.network.Tags;
import com.example.praca4.room.Database;
import com.example.praca4.room.dto.UserDto;
import com.example.praca4.room.entities.LocalNetworkUsers;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;


public class UsersList extends AppCompatActivity {

    UserDataManager userDataManager;
    ListView listView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_users_list);


        View root = findViewById(R.id.rootLayout);

        ViewCompat.setOnApplyWindowInsetsListener(root, (view, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            view.setPadding(
                    view.getPaddingLeft(),
                    view.getPaddingTop(),
                    view.getPaddingRight(),
                    systemBars.bottom
            );
            return insets;
        });






        userDataManager = new UserDataManager(this);

        Database database = Database.getInstance(this.getApplicationContext());

        listView = findViewById(R.id.localUsersList);
        Button btnScan = findViewById(R.id.scan);


        btnScan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ClientUDP.sendMessage(NetworkUtilities.WILDCARD, Tags.DEVICE_INFO_EXCHANGE_REQUEST, null);
            }
        });

        database.localNetworkUsersDao().observeUsers().observe(this, users -> {
            List<UserDto> usersDtoList= new ArrayList<UserDto>();
            for(LocalNetworkUsers localNetworkUser : users){
                usersDtoList.add(new UserDto(localNetworkUser));
            }
            listView.setAdapter(new UserListAdapter(this, R.layout.users_list_row, usersDtoList));
        });

    }
}
