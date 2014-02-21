package com.gunit.spacecrack.application;

import android.app.Application;
import android.content.Context;
import android.graphics.Bitmap;

import com.facebook.model.GraphUser;
import com.gunit.spacecrack.model.Profile;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * Created by Dimitri on 20/02/14.
 */
public class SpaceCrackApplication extends Application {

    //Logged in Profile
    public static Profile profile;
    public static Bitmap profilePicture;

    //Logged in Facebook user
    public static GraphUser graphUser;

    //Friends of logged in Facebook user
    private List<GraphUser> friends;

    public static String accessToken;

    public final static int NETWORK_TIMEOUT = 5000;
    //Localhost
    public final static String IP_ADDRESS = "10.0.2.2";
    //Network
//    public final static String IP_ADDRESS = "10.132.100.255";
//    public final static String IP_ADDRESS = "10.0.3.2";
//    public final static String IP_ADDRESS = "192.168.0.142";
//    public final static String IP_ADDRESS = "192.168.56.1";
    public final static String DOMAIN = "http://" + IP_ADDRESS + ":8080";
    public final static String URL_LOGIN = DOMAIN + "/api/accesstokens";
    public final static String URL_REGISTER = DOMAIN + "/api/user";
    public final static String URL_PROFILE = DOMAIN + "/api/auth/profile";


    public List<GraphUser> getFriends() {
        return friends;
    }

    public void setFriends(List<GraphUser> friends) {
        this.friends = friends;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }
}
