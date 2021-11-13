package com.example.tesis.Model;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.firebase.Timestamp;

import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Locale;

public class Event implements Parcelable, Serializable {
    private String creator, zone, title, description, id, url;
    private Timestamp start, finish;
    private final SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm");
    private final Locale locale = new Locale("es", "ES");
    private final SimpleDateFormat format = new SimpleDateFormat("EE d 'de' MMM 'del' yyyy 'a las' HH:mm 'Hrs.'", locale);

    public Event(String id, String creator, String zone, String title, String description, Timestamp start, Timestamp finish, String url) {
        this.id = id;
        this.creator = creator;
        this.zone = zone;
        this.title = title;
        this.description = description;
        this.start = start;
        this.finish = finish;
        this.url = url;
    }

    public String getUrl(){ return url;}

    public String getZone(){
        return zone;
    }

    public String getCreator() {
        return creator;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public Timestamp getStart() {
        return start;
    }

    public Timestamp getFinish() {
        return finish;
    }

    public String converStartDateAndHour() throws ParseException {
        String date = format.format(this.start.toDate());
        return date;
    }

    public String converFinishDateAndHour() throws ParseException {
        String date = format.format(this.finish.toDate());
        return date;
    }

    public String getHourStart(){
        String auxStart[] = formatter.format(this.start.toDate()).split(" ");
        return auxStart[1];
    }

    public String getHourEnd(){
        String auxFinish[] = formatter.format(this.finish.toDate()).split(" ");
        return auxFinish[1];
    }

    public String getDateStart(){
        String auxStart[] = formatter.format(this.start.toDate()).split(" ");
        return auxStart[0];
    }

    public String getDateEnd(){
        String auxFinish[] = formatter.format(this.finish.toDate()).split(" ");
        return auxFinish[0];
    }

    public String getId() {
        return id;
    }

    public void setCreator(String creator) {
        this.creator = creator;
    }

    public void setZone(String zone) {
        this.zone = zone;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {

    }
}
