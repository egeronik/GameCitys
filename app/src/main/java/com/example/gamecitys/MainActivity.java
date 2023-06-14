package com.example.gamecitys;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.*;

import com.example.gamecitys.datamanager.FirebaseManager;
import com.example.gamecitys.datamanager.Session;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

public class MainActivity extends AppCompatActivity {

    private TextView nicknameTextView;
    private TextView emailTextView;
    private TextView ratingTextView;
    private Button matchmakingButton;
    private Button logoutButton;

    private String TAG = "MainActivity";

    FirebaseManager manager;
    FirebaseUser user;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

//        Assigns
        nicknameTextView = (TextView) findViewById(R.id.nicknameTextView);
        emailTextView = (TextView) findViewById(R.id.emailTextView);
        ratingTextView = (TextView) findViewById(R.id.ratingTextView);
        matchmakingButton = (Button) findViewById(R.id.matchmakingButton);
        logoutButton = (Button) findViewById(R.id.logoutButton);
        user = FirebaseAuth.getInstance().getCurrentUser();
        manager = new FirebaseManager("");

//        Information filling
        if (user != null) {
            String name = user.getDisplayName();
            String email = user.getEmail();
            nicknameTextView.setText(name);
            emailTextView.setText(email);
            manager.myRef.child("Users").child(user.getUid()).get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DataSnapshot> task) {
                    if (!task.isSuccessful()) {
                        Log.e("firebase", "Error getting data", task.getException());
                    }
                    else {
                        ratingTextView.setText(String.valueOf(task.getResult().getValue()));
                    }
                }
            });
        }


//        Button events
        matchmakingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                StartMatchMaking();
            }
        });


        logoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FirebaseAuth.getInstance().signOut();
                Intent myIntent = new Intent(MainActivity.this, LoginActivity.class);
                MainActivity.this.startActivity(myIntent);
            }
        });
    }


    private void StartMatchMaking(){
        manager.myRef.child("Queue").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    String sessionId = snapshot.getValue(String.class);
                    assert sessionId != null;
                    manager.myRef.child("Sessions").child(sessionId).child("guestId").setValue(user.getUid());
                    manager.myRef.child("Sessions").child(sessionId).child("guestName").setValue(user.getDisplayName());
                    Intent myIntent = new Intent(MainActivity.this, GameActivity.class);
                    myIntent.putExtra("SessionID", sessionId);
                    manager.myRef.child("Queue").removeValue();
                    MainActivity.this.startActivity(myIntent);

                }
                else{
                    manager.myRef.child("Queue").setValue(user.getUid());
                    manager.myRef.child("Sessions").child(user.getUid()).setValue(new Session(user.getUid(),user.getDisplayName()));
                    Intent myIntent = new Intent(MainActivity.this, GameActivity.class);
                    myIntent.putExtra("SessionID", user.getUid());
                    MainActivity.this.startActivity(myIntent);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });



    }


}