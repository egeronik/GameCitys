package com.example.gamecitys.datamanager;

import java.util.ArrayList;
import java.util.List;

public class Session {
    public String hostId = null;
    public String hostName = "";
    public String guestId = "";
    public String guestName = "waiting";
    public String lastWord = "";
    public String currentChar = "";
    //True if host, false if guest
    public String turn = "";
    public List<String> usedLower= new ArrayList<>();


    public Session() {
    }

    public Session(String id, String hostName) {
        hostId = id;
        turn = hostId;
        this.hostName = hostName;
    }


}
