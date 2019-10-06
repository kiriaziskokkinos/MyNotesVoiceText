package com.kokkinosk.mynotesvoicetext;

public class User {

    static boolean  isLoggedIn;
    static String userName;
    static String userPass;

    public User(){
        isLoggedIn = false;
        userName = "";
        userPass = "";
    }

    public User(String name,String pass){
        isLoggedIn = true;
        userName = name;
        userPass = pass;

    }

    public static boolean getLoginStatus(){
        return isLoggedIn;
    }

    public  static String getUserName(){
        return userName;
    }

    public static String getUserPass(){
        return userPass;
    }




}
