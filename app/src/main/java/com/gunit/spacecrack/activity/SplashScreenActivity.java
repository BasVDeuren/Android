package com.gunit.spacecrack.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.widget.Toast;

import com.facebook.Request;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.model.GraphUser;
import com.google.gson.Gson;
import com.google.gson.JsonParseException;
import com.gunit.spacecrack.interfacerequest.ILoginRequest;
import com.gunit.spacecrack.interfacerequest.IUserRequest;
import com.gunit.spacecrack.R;
import com.gunit.spacecrack.application.SpaceCrackApplication;
import com.gunit.spacecrack.chat.ChatActivity;
import com.gunit.spacecrack.game.GameActivity;
import com.gunit.spacecrack.model.User;
import com.gunit.spacecrack.service.SpaceCrackService;
import com.gunit.spacecrack.task.LoginTask;
import com.gunit.spacecrack.task.UserTask;

public class SplashScreenActivity extends Activity implements ILoginRequest, IUserRequest {

    private SharedPreferences sharedPreferences;
    private String email;
    private String password;

    private final String TAG = "SplashScreenActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);

        //Check the network connection
        final ConnectivityManager conMgr =  (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        final NetworkInfo activeNetwork = conMgr.getActiveNetworkInfo();
        if (activeNetwork != null && activeNetwork.isConnected()) {
            sharedPreferences = getSharedPreferences("Login", 0);
            startApp();
        } else {
            Toast.makeText(SplashScreenActivity.this, getResources().getString(R.string.no_connection), Toast.LENGTH_LONG).show();
            System.exit(0);
            finish();
        }
    }

    private void startApp() {
        if (isLoggedIn()) {
            email = sharedPreferences.getString("email", null);
            password = sharedPreferences.getString("password", null);
            new LoginTask(email, password, this).execute();
        } else {
            goLogin();
        }
    }

    private boolean isLoggedIn() {
        //Facebook check
        Session session = Session.getActiveSession();
        if (session == null) {
            session = Session.openActiveSessionFromCache(SplashScreenActivity.this);
        }
        if (session != null && session.isOpened()) {
            final Session finalSession = session;
            Request request = Request.newMeRequest(session, new Request.GraphUserCallback() {
                @Override
                public void onCompleted(GraphUser user, Response response) {
                    if (user != null && finalSession == Session.getActiveSession()) {
                        SpaceCrackApplication.graphUser = user;
                    }
                }
            });
            request.executeAsync();
            return true;
        }
        //SpaceCrack user check
        return sharedPreferences.getString("accessToken", null) != null;
    }

    private void proceedApp() {
        Intent service = new Intent(this, SpaceCrackService.class);
        service.putExtra("username", SpaceCrackApplication.user.username);
        startService(service);
        Intent intent = getIntent();
        Intent startIntent = new Intent(SplashScreenActivity.this, HomeActivity.class);
        if (intent.getStringExtra("task") != null) {
            if (intent.getStringExtra("task").equals("chat")) {
                startIntent = new Intent(SplashScreenActivity.this, ChatActivity.class);
                startIntent.putExtra("gameId", intent.getStringExtra("gameId"));
                startIntent.putExtra("username", intent.getStringExtra("username"));
            } else if (intent.getStringExtra("task").equals("game")) {
                startIntent = new Intent(SplashScreenActivity.this, GameActivity.class);
                startIntent.putExtra("gameId", intent.getIntExtra("gameId", 0));
            } else if (intent.getStringExtra("task").equals("invite")) {
                startIntent = new Intent(SplashScreenActivity.this, HomeActivity.class);
                startIntent.putExtra("invite", true);
            } else if (intent.getStringExtra("task").equals("accepted")) {
                startIntent = new Intent(SplashScreenActivity.this, HomeActivity.class);
                startIntent.putExtra("accepted", true);
            }
        }

        startActivity(startIntent);
        //Close this activity so it won't show up again
        finish();
    }

    private void goLogin () {
        if (Session.getActiveSession() != null) {
            Session.getActiveSession().closeAndClearTokenInformation();
        }
        Intent intent = new Intent(SplashScreenActivity.this, LoginActivity.class);
        startActivity(intent);
        //Close this activity so it won't show up again
        finish();
    }

    @Override
    public void startTask() {

    }

    @Override
    public void userCallback(String result) {
        if (result != null) {
            try {
                Gson gson = new Gson();
                SpaceCrackApplication.user = gson.fromJson(result, User.class);
                //Get the image from the Data URI
                if (SpaceCrackApplication.user.profile.image != null) {
                    String image = SpaceCrackApplication.user.profile.image.substring(SpaceCrackApplication.user.profile.image.indexOf(",") + 1);
                    try {
                        byte[] decodedString = Base64.decode(image, 0);
                        SpaceCrackApplication.profilePicture = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                    } catch (IllegalArgumentException e) {
                        Log.i(TAG, "Image could not be rendered from Base64");
                        e.printStackTrace();
                    }
                }
                proceedApp();
            } catch (JsonParseException e) {
                e.printStackTrace();
            }
        } else {
            Toast.makeText(SplashScreenActivity.this, getResources().getText(R.string.user_fail), Toast.LENGTH_SHORT).show();
            goLogin();
        }
    }

    @Override
    public void loginCallback(String result) {
        SpaceCrackApplication.accessToken = result;
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("accessToken", result);
        editor.putString("email", email);
        editor.putString("password", password);
        editor.commit();
        if (result != null) {
            //Get the profile of the user
            new UserTask(this).execute(SpaceCrackApplication.URL_USER);
        } else {
            Toast.makeText(SplashScreenActivity.this, getResources().getText(R.string.login_fail), Toast.LENGTH_SHORT).show();
            goLogin();
        }
    }
}
