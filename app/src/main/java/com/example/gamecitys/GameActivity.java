package com.example.gamecitys;

import static android.app.PendingIntent.getActivity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.gamecitys.datamanager.FirebaseManager;
import com.example.gamecitys.datamanager.Session;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.*;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;

public class GameActivity extends AppCompatActivity {

    String TAG = "GameActivity";

    private TextView enemyTextView;
    private TextView selfTextView;
    private EditText wordTextEdit;
    private Button submitButton;
    private Button surrenderButton;

    private TextView wordTextView;
    private ImageView arrowImageView;
    private TextView letterTextView;

    private FirebaseManager manager;
    FirebaseUser user;
    Session session = null;
    String sessionID;

    JSONArray citys;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

//        Assigns
        enemyTextView = (TextView) findViewById(R.id.enemyTextView);
        selfTextView = (TextView) findViewById(R.id.selfTextView);
        wordTextEdit = (EditText) findViewById(R.id.wordTextEdit);
        submitButton = (Button) findViewById(R.id.submitButton);
        surrenderButton = (Button) findViewById(R.id.surrenderButton);
        wordTextView = (TextView) findViewById(R.id.wordTextView);
        arrowImageView = (ImageView) findViewById(R.id.arrowImageView);
        letterTextView = (TextView) findViewById(R.id.letterTextView);
        manager = new FirebaseManager("Sessions");
        user = FirebaseAuth.getInstance().getCurrentUser();
        sessionID = getIntent().getStringExtra("SessionID");


//        Information filling
        try {
            JSONObject obj = new JSONObject(loadJSONFromAsset());
            citys = obj.getJSONArray("city");
        } catch (JSONException e) {
            e.printStackTrace();
        }


        manager.myRef.child(sessionID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()) {
                    session = snapshot.getValue(Session.class);
                    updateUI();
                }else {
//                    Display current mmr
                    finish();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

//        Button events
        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SumbitWord();
            }
        });

        surrenderButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FirebaseManager userManager = new FirebaseManager("Users");
                userManager.myRef.child(user.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        userManager.myRef.child(user.getUid()).setValue(snapshot.getValue(Integer.class)-20);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
                String target = session.hostId;
                if(user.getUid().equals(sessionID))
                    target=session.guestId;
                String finalTarget = target;
                Log.d(TAG, "onClick: "+finalTarget);
                userManager.myRef.child(target).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        userManager.myRef.child(finalTarget).setValue(snapshot.getValue(Integer.class)+20);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
                manager.myRef.child(sessionID).removeValue();
                finish();
            }
        });


    }
    private void SumbitWord() {
        boolean correct = false;
        String city = wordTextEdit.getText().toString().trim();
        String cityLower = wordTextEdit.getText().toString().toLowerCase().trim();
        String lastWordLower = session.lastWord.toLowerCase();
        if(!session.usedLower.contains(cityLower)){
            if(session.lastWord.equals("") || cityLower.charAt(0)==lastWordLower.charAt(lastWordLower.length()-1)){
                for (int i = 0; i < citys.length(); i++) {
                    try {
                        if (citys.getJSONObject(i).getString("name").toLowerCase().equals(cityLower)) {
                            correct = true;
                            break;
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        }else{
            Toast.makeText(GameActivity.this, "City already used",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        if (correct){
            session.usedLower.add(cityLower);
            if(user.getUid().equals(sessionID)) {
                session.turn = session.guestId;
            }else {
                session.turn = session.hostId;
            }
            session.lastWord = city;
            session.currentChar = session.lastWord.substring(session.lastWord.length() - 1);
            manager.myRef.child(sessionID).setValue(session);
            updateUI();
        }
        else{
            Toast.makeText(GameActivity.this, "City not exist",
                    Toast.LENGTH_SHORT).show();
        }

    }

    public void updateUI(){
        wordTextEdit.setText("");
        if(user.getUid().equals(sessionID)){
            enemyTextView.setText(session.guestName);
            selfTextView.setText(session.hostName);
        }else {
            enemyTextView.setText(session.hostName);
            selfTextView.setText(session.guestName);
        }

        wordTextView.setText(session.lastWord);
        letterTextView.setText(session.currentChar);
        if(session.turn.equals(user.getUid())){
            arrowImageView.setImageDrawable(getDrawable(R.drawable.green_arrow));
            submitButton.setClickable(true);
        }else{
            arrowImageView.setImageDrawable(getDrawable(R.drawable.red_arrow));
            submitButton.setClickable(false);
        }
        if(session.guestId.equals("waiting")){
            submitButton.setClickable(false);
        }
    }


    public String loadJSONFromAsset() {
        String json = null;
        try {
            InputStream is = this.getAssets().open("cities.json");
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            json = new String(buffer, "UTF-8");
        } catch (IOException ex) {
            ex.printStackTrace();
            return null;
        }
        return json;
    }
}
