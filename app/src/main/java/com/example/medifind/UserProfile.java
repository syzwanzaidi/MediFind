package com.example.medifind;

public class UserProfile {
    public String name, age, gender, phone;

    public UserProfile() {
        // Required for Firebase
    }

    public UserProfile(String name, String age, String gender, String phone) {
        this.name = name;
        this.age = age;
        this.gender = gender;
        this.phone = phone;
    }
}
