package com.example.gamecitys.datamanager;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class FirebaseManager
{
    public DatabaseReference myRef;

    public FirebaseManager(String path) {
        FirebaseDatabase database = FirebaseDatabase.getInstance("https://citysgame-88ccf-default-rtdb.asia-southeast1.firebasedatabase.app");
        myRef = database.getReference(path);
    }

    public void writeData(String data){
        myRef.setValue(data);
    }

}
