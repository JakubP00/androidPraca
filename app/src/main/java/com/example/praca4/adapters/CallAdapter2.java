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
import com.example.praca4.managers.CallManager2;

import java.util.List;

public class CallAdapter2 extends ArrayAdapter<CallManager2.CallMember> {


    private final Context context;
    private final int layoutResourceId;
    private final List<CallManager2.CallMember> callMembers;


    public CallAdapter2(@NonNull Context context, int layoutResourceId, @NonNull List<CallManager2.CallMember> callMembers) {
        super(context, layoutResourceId, callMembers);
        this.context = context;
        this.layoutResourceId = layoutResourceId;
        this.callMembers = callMembers;
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        View row = convertView;
        CallAdapter2.UiElements uiElements = null;

        if(row == null)
        {
            LayoutInflater inflater = ((Activity)context).getLayoutInflater();
            row = inflater.inflate(layoutResourceId, parent, false);

            uiElements = new CallAdapter2.UiElements();
            uiElements.userName = row.findViewById(R.id.userName);
            uiElements.status = row.findViewById(R.id.status);


            row.setTag(uiElements);
        }
        else
        {
            uiElements = (CallAdapter2.UiElements) row.getTag();
        }

        CallManager2.CallMember callMember = callMembers.get(position);
        uiElements.status.setText(callMember.getUserName());

        String status = "unknown";

        switch (callMember.getState()){
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

}
