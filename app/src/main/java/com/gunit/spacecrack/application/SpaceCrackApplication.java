package com.gunit.spacecrack.application;

import android.app.Application;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;

import com.facebook.FacebookException;
import com.facebook.Session;
import com.facebook.model.GraphUser;
import com.facebook.widget.WebDialog;
import com.gunit.spacecrack.model.Profile;
import com.gunit.spacecrack.model.User;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * Created by Dimitri on 20/02/14.
 */
public class SpaceCrackApplication extends Application {

    //Logged in User
    public static User user;
    public static Bitmap profilePicture;

    //Logged in Facebook user
    public static GraphUser graphUser;

    //Friends of logged in Facebook user
    public static List<GraphUser> friends;

    public static String accessToken;

    public final static int NETWORK_TIMEOUT = 12000;
    //Localhost emulator
//    public final static String IP_ADDRESS = "10.0.2.2";
    //Network (IP address pc)
//    public final static String IP_ADDRESS = "10.132.100.255";
//    public final static String IP_ADDRESS = "10.0.3.2";
//    public static final String IP_ADDRESS = "192.168.0.143";
    public final static String IP_ADDRESS = "192.168.56.1";
    public static final String DOMAIN = "http://" + IP_ADDRESS + ":8080";
    public static final String URL_LOGIN = DOMAIN + "/api/accesstokens";
    public static final String URL_REGISTER = DOMAIN + "/api/user";
    public static final String URL_USER = DOMAIN + "/api/auth/user";
    public static final String URL_PROFILE = DOMAIN + "/api/auth/profile";
    public static final String URL_MAP = DOMAIN + "/api/map";
    public static final String URL_GAME = DOMAIN + "/api/auth/game";
    public static final String URL_ACTIVEGAME = DOMAIN + "/api/auth/game/specificGame";
    public static final String URL_ACTION = DOMAIN + "/api/auth/action";
    public static final String URL_FIND_USERNAME = DOMAIN + "/api/auth/findusersbyusername";
    public static final String URL_FIND_EMAIL = DOMAIN + "/api/auth/findusersbyemail";
    public static final String URL_FIND_USERID = DOMAIN + "/api/auth/findUserByUserId";

    public static final String URL_FIREBASE_CHAT = "https://amber-fire-3394.firebaseio.com";

    public static void logout() {
        user = null;
        profilePicture = null;
        graphUser = null;
        friends = null;
        accessToken = null;
    }


    public List<GraphUser> getFriends() {
        return friends;
    }

    public void setFriends(List<GraphUser> friends) {
        this.friends = friends;
    }


//    Bundle params = new Bundle();
//    params.putString("name", "Space Crack");
//    params.putString("caption", "Space Crack");
//    params.putString("description", "I've captured all the planets!");
//    params.putString("picture", "http://www.italieinbedrijf.nl/wp-content/uploads/2012/12/Lamborghini-Logo.jpg");
//
//    WebDialog feedDialog = (new WebDialog.FeedDialogBuilder(getActivity(),
//            Session.getActiveSession(), params))
//            .setOnCompleteListener(new WebDialog.OnCompleteListener() {
//                @Override
//                public void onComplete(Bundle values, FacebookException error) {
//
//                }
//            }).build();
//    feedDialog.show();
}
