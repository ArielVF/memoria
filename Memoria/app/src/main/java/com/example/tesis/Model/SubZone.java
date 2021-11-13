package com.example.tesis.Model;

import java.io.Serializable;

public class SubZone implements Serializable {
    private String id;
    private String name;

    public SubZone(String id, String name){
        this.id = id;
        this.name = name;
    }

    public String getName(){ return this.name; }

    public String getId(){ return this.id; }
}
