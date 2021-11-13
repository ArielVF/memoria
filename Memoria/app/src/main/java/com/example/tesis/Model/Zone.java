package com.example.tesis.Model;

import java.io.Serializable;

public class Zone implements Serializable {
    private String id, name, description;
    private double longitude, latitude;
    private int floor;
    private String[] photo;
    private boolean permanent;

    public Zone(String id, String name, String description, double longitude, double latitude, int floor, boolean permanent){
        this.id = id;
        this.description = description;
        this.name = name;
        this.longitude = longitude;
        this.latitude = latitude;
        this.floor = floor;
        this.permanent = permanent;
    }

    public boolean getPermanent(){return permanent;}

    public int getFloor(){return floor;}

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public String getDescription() {
        return description;
    }

    public void convertPhotoToList(String auxPhoto){
        auxPhoto = auxPhoto.replace("["," ");
        auxPhoto = auxPhoto.replace( "]", " ");
        this.photo = auxPhoto.split(",");
    }

    public String getLastPhotoIndex(){
        int lastindex = this.photo.length-1;
        return this.photo[lastindex];
    }

    public String[] getPhotos(){ return this.photo; }

    public String[] getPhoto() {
        return photo;
    }
}
