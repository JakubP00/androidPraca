package com.example.praca4.adapters;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.example.praca4.R;
import com.example.praca4.managers.CallManager;

import java.util.List;

public class CallAdapter extends ArrayAdapter<CallAdapter.ConnectedUser> {
    Context context;
    int layoutResourceId;
    List<ConnectedUser> users = null;

    public CallAdapter(@NonNull Context context, int layoutResourceId, List<ConnectedUser> users) {
        super(context, layoutResourceId, users);
        this.context = context;
        this.layoutResourceId = layoutResourceId;
        this.users = users;
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        View row = convertView;
        CallAdapter.UiElements uiElements = null;

        if(row == null)
        {
            LayoutInflater inflater = ((Activity)context).getLayoutInflater();
            row = inflater.inflate(layoutResourceId, parent, false);

            uiElements = new CallAdapter.UiElements();
            uiElements.userName = row.findViewById(R.id.userName);
            uiElements.status = row.findViewById(R.id.status);


            row.setTag(uiElements);
        }
        else
        {
            uiElements = (CallAdapter.UiElements) row.getTag();
        }

        ConnectedUser connectedUser = users.get(position);
        uiElements.status.setText(connectedUser.getUserName());

        String status = "unknown";

        switch (connectedUser.getState()){
            case UN_CALLED: //was not called yet
            case CAllED://was called account is set to answer
                status = "CALLED";
                break;
            case CONNECTED: // is consider ready to receive sound may be already sending sound
            case CONNECTED_INITIALIZED:// is consider ready to receive sound and both receiving and sending sound is already setup
                status = "CONNECTED";
                break;
            case REFUSED: //user REFUSED
            case UN_ANSWERED://was called but user account is set to not answer
                status = "REFUSED";
                break;
            case STOPED: // user disconnected from the call:
                status = "LEFT";
        }

        uiElements.status.setText(status);

        return row;
    }

    static class UiElements{
        TextView userName;
        TextView status;
    }

    public static class ConnectedUser {
        private final String userName;
        private final CallManager.ConnectedUser.State state;

        public ConnectedUser(String userName, CallManager.ConnectedUser.State state) {
            this.userName = userName;
            this.state = state;
        }

        public String getUserName() {return userName;}
        public CallManager.ConnectedUser.State getState() {return state;}
    }
}
