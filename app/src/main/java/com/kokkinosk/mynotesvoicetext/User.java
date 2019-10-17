package com.kokkinosk.mynotesvoicetext;

public class User {

    static boolean  loginStatus;
    static String userName;
    static String userPass;

    public User(){
        loginStatus = false;
        userName = "";
        userPass = "";
    }

    public User(String name,String pass){
        loginStatus = true;
        userName = name;
        userPass = pass;

    }

    public static boolean isLoggedIn(){
        return loginStatus;
    }

    public  static String getUserName(){
        return userName;
    }

    public static String getUserPass(){
        return userPass;
    }




}
