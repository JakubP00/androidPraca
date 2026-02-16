package com.example.praca4.adapters;

import static androidx.core.content.ContextCompat.startActivity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.example.praca4.R;
import com.example.praca4.activities.Call;
import com.example.praca4.room.Database;
import com.example.praca4.room.dto.UserDto;
import com.example.praca4.room.entities.CurrentCall;
import com.example.praca4.room.entities.LocalNetworkUsers;

import java.util.List;

public class UserListAdapter extends ArrayAdapter<UserDto> {

    Context context;
    int layoutResourceId;
    List<UserDto> users = null;

    public UserListAdapter(@NonNull Context context, int layoutResourceId, List<UserDto> users) {
        super(context, layoutResourceId, users);
        this.context = context;
        this.layoutResourceId = layoutResourceId;
        this.users = users;
    }

    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        View row = convertView;
        UiElements uiElements = null;

        if(row == null)
        {
            LayoutInflater inflater = ((Activity)context).getLayoutInflater();
            row = inflater.inflate(layoutResourceId, parent, false);

            uiElements = new UiElements();
            uiElements.textView = row.findViewById(R.id.localUserName);
            uiElements.btnDetails = row.findViewById(R.id.btnDetails);
            uiElements.btnCall = row.findViewById(R.id.btnCall);

            row.setTag(uiElements);
        }
        else
        {
            uiElements = (UiElements) row.getTag();
        }

        UserDto userDto = users.get(position);
        uiElements.textView.setText(userDto.getUsername());
        uiElements.btnCall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getContext(), Call.class);

                Log.d("Test3", userDto.getIpAddress());
                intent.putExtra(Call.intentData.CALLING_PARTY_INET_ADDRESS.name(), userDto.getIpAddress());
                intent.putExtra(Call.intentData.CALLING_PARTY.name(), true);
                Database.databaseExecutor.execute(() -> {
                    Database database =  Database.getInstance(getContext());

                    database.currentCallDao().clearTable();

                    LocalNetworkUsers localNetworkUser =  database.localNetworkUsersDao().getByIpAddress(userDto.getIpAddress());

                    database.currentCallDao().insertAll(new CurrentCall(localNetworkUser.getUuid(), 1));
                });
                getContext().startActivity(intent);
            }
        });



        return row;
    }

    static class UiElements{
        TextView textView;
        Button btnDetails;
        Button btnCall;
    }
}
