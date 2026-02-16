package com.example.praca4.activities;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

import com.example.praca4.R;
import com.example.praca4.managers.UserDataManager;



public class MyProfile extends AppCompatActivity {
    String userName = "";
    UserDataManager userDataManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.my_profile);


        EditText etUserName = findViewById(R.id.etUserName);
        Button btnSave = findViewById(R.id.btnSave);
        userDataManager = new UserDataManager(this);


//        Database db = Database.getInstance(this.getApplicationContext());
//
//        Database.databaseExecutor.execute(() -> {
//            List<KnownUsers> knownUsersList = db.knownUsersDao().getAll();
//
//            for(KnownUsers knownUsers : knownUsersList){
//                Log.e("test","2 " + knownUsers.getUuid() );
//            }
//        });


            etUserName.addTextChangedListener(new TextWatcher() {
                @Override
                public void afterTextChanged(Editable s) {
                    userName = s.toString();
                    if(!UserDataManager.validUsername(userName)){
                        //tu coś ma być co da informacje o ilośći znaków
                    }
                }

                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {

                }
            });


        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    userDataManager.setUserName(userName);
                    userDataManager.generateUUID();
                    userDataManager.setAppWasInitialized();
                    finish();
                } catch (UserDataManager.IncorrectUsername e) {
                    Log.e("asdads", userName);
                    //tu coś ma być co da informacje o ilośći znaków
                }
            }
        });
    }
}
