package com.gunit.spacecrack.application;

import android.app.Application;
import android.graphics.Bitmap;

import com.facebook.model.GraphUser;
import com.gunit.spacecrack.model.User;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Pattern;

/**
 * Created by Dimitri on 20/02/14.
 */

/**
 * Application class which holds several global variables and methods
 */
public class SpaceCrackApplication extends Application {

    //Logged in User
    public static User user;
    public static Bitmap profilePicture;

    //Logged in Facebook user
    public static GraphUser graphUser;

    public static String accessToken;

    public static Pattern emailRegex = Pattern.compile("^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,6}$", Pattern.CASE_INSENSITIVE);
    public static Pattern plainTextRegex = Pattern.compile("^[0-9A-Z]+$", Pattern.CASE_INSENSITIVE);

    public final static int NETWORK_TIMEOUT = 12000;
    //Localhost emulator
//    public final static String IP_ADDRESS = "10.0.2.2";
    //Network (IP address pc)
//    public static final String IP_ADDRESS = "192.168.0.143";
//    public final static String IP_ADDRESS = "192.168.56.1";
    public final static String IP_ADDRESS = "spacecrack-groepg.rhcloud.com";

//    public static final String DOMAIN = "http://" + IP_ADDRESS + ":8080";
    public static final String DOMAIN = "http://" + IP_ADDRESS;
    public static final String URL_LOGIN = DOMAIN + "/api/accesstokens";
    public static final String URL_REGISTER = DOMAIN + "/api/user";
    public static final String URL_FBREGISTER = DOMAIN + "/api/fbuser";
    public static final String URL_USER = DOMAIN + "/api/auth/user";
    public static final String URL_VERIFY = DOMAIN + "/api/verifiedUser";
    public static final String URL_PROFILE = DOMAIN + "/api/auth/profile";
    public static final String URL_MAP = DOMAIN + "/api/map";
    public static final String URL_GAME = DOMAIN + "/api/auth/game";
    public static final String URL_ACTIVEGAME = DOMAIN + "/api/auth/game/specificGame";
    public static final String URL_ACTION = DOMAIN + "/api/auth/action";
    public static final String URL_FIND_USERNAME = DOMAIN + "/api/auth/findusersbyusername";
    public static final String URL_FIND_EMAIL = DOMAIN + "/api/auth/findusersbyemail";
    public static final String URL_FIND_USERID = DOMAIN + "/api/auth/findUserByUserId";
    public static final String URL_REPLAY = DOMAIN + "/api/auth/replay";
    public static final String URL_STATISTICS = DOMAIN + "/api/auth/statistics";
    public static final String URL_GAMEINVITE = DOMAIN + "/api/auth/game/invite";
    public static final String URL_INVITATION = DOMAIN + "/api/auth/invitation";


    public static final String URL_FIREBASE_CHAT = "https://amber-fire-3394.firebaseio.com";
    public static final String URL_FIREBASE_INVITES = "https://vivid-fire-9476.firebaseio.com/invites";

    public static String getFacebookPictureUrl () {
        if (graphUser != null) {
            return "http://graph.facebook.com/" + graphUser.getId() + "/picture?type=large&redirect=false";
        }
        return null;
    }

    public static void logout() {
        user = null;
        profilePicture = null;
        graphUser = null;
        accessToken = null;
    }

    public static boolean isValidEmail (String email) {
        return emailRegex.matcher(email).matches();
    }

    public static boolean isPlainText (String text) {
        return plainTextRegex.matcher(text).matches();
    }

    /**
     * Hash the password with the MD5 algorithm
     * @param password
     * @return
     */
    public static String hashPassword(String password) {
        String hashedString = null;
        try {
            MessageDigest messageDigest = MessageDigest.getInstance("MD5");
            messageDigest.update(password.getBytes(), 0, password.length());
            byte[] hashedBytes = messageDigest.digest();
            StringBuffer sb = new StringBuffer();
            for (int i = 0; i < hashedBytes.length; i++) {
                sb.append(Integer.toString((hashedBytes[i] & 0xff) + 0x100, 16).substring(1));
            }
            hashedString = sb.toString();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }

        return hashedString;
    }

    public static String getLocalDate (String dateString) {
        SimpleDateFormat format = new SimpleDateFormat("MM/dd/yyyy");
        SimpleDateFormat localFormat = new SimpleDateFormat("dd-MM-yyyy");
        try {
            Date date = format.parse(dateString);
            return localFormat.format(date);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return dateString;

    }
}
