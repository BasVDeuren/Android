package com.gunit.spacecrack.task;

import android.os.AsyncTask;

import com.gunit.spacecrack.interfacerequest.ILoginRequest;
import com.gunit.spacecrack.application.SpaceCrackApplication;
import com.gunit.spacecrack.restservice.RestService;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Dimitri on 7/03/14.
 */

/**
 * LoginTask sends a login request with an email and password as parameters
 */
public class LoginTask extends AsyncTask<String, Void, String> {

    private JSONObject user;
    private ILoginRequest activity;

    public LoginTask (String email, String password, ILoginRequest activity)
    {
        super();
        //Create an user to log in
        user = new JSONObject();
        try {
            user.put("email", email);
            user.put("password", password);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        this.activity = activity;
    }

    @Override
    protected void onPreExecute() {
        activity.startTask();
    }

    @Override
    protected String doInBackground(String... params) {
        return RestService.postRequestWithoutAccessToken(SpaceCrackApplication.URL_LOGIN, user);
    }

    @Override
    protected void onPostExecute(String result) {
        activity.loginCallback(result);
    }
}
