package com.example.contacts2csv;

import java.util.ArrayList;

public class ContactInfo {
    public String lastName;
    public String firstName;
    public String displayName;
    public ArrayList<String> mobileNum;
    public ArrayList<String> homeNum;
    public ArrayList<String> Email;
    public ArrayList<String> Im;

    public ContactInfo(
            String displayName, String lastName, String firstName,
            ArrayList<String> mobileNum, ArrayList<String> homeNum,
            ArrayList<String> Email, ArrayList<String> Im) {
        this.displayName = displayName;
        this.lastName = lastName;
        this.firstName = firstName;
        this.mobileNum = mobileNum;
        this.homeNum = homeNum;
        this.Email = Email;
        this.Im = Im;
    }

    public ContactInfo() {
        this.displayName = "";
        this.lastName = "";
        this.firstName = "";
        this.mobileNum = new ArrayList<String>();
        this.homeNum = new ArrayList<String>();
        this.Email = new ArrayList<String>();
        this.Im = new ArrayList<String>();
    }
}
