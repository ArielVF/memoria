package com.example.tesis.Model;

import java.io.Serializable;

public class User implements Serializable {
    private String id, name, lastname, email, rol, rut;
    private boolean enable;

    public User(String id, String name, String lastname, String email, String rol, String rut, boolean enable){
        this.id = id;
        this.name = name;
        this.lastname = lastname;
        this.email = email;
        this.rol = rol;
        this.rut = rut;
        this.enable = enable;
    }

    public String getId() { return id; }

    public void setId(String id) { this.id = id; }

    public String getName() { return name; }

    public void setName(String name) { this.name = name; }

    public String getLastname() { return lastname; }

    public void setLastname(String lastname) { this.lastname = lastname; }

    public String getEmail() { return email; }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getRol() {
        return rol;
    }

    public void setRol(String rol) {
        this.rol = rol;
    }

    public String getRut() {
        return rut;
    }

    public void setRut(String rut) {
        this.rut = rut;
    }

    public boolean isEnable() {
        return enable;
    }

    public void setEnable(boolean enable) {
        this.enable = enable;
    }

    public String formatRut(){
        String auxRut = this.rut.trim();
        String formatRut = "";
        String sectionRut[] = auxRut.split("-");
        auxRut = sectionRut[0];
        if(auxRut.length() == 8){
            formatRut += auxRut.substring(0,2)+"."+auxRut.substring(2,5)+"."+auxRut.substring(5,8)+"-"+sectionRut[1];
        }
        else{
            formatRut += auxRut.substring(0,1)+"."+auxRut.substring(1,4)+"."+auxRut.substring(4,7)+"-"+sectionRut[1];
        }
        return formatRut;
    }
}
