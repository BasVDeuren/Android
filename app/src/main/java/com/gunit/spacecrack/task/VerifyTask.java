package com.gunit.spacecrack.task;

import android.os.AsyncTask;

import com.gunit.spacecrack.application.SpaceCrackApplication;
import com.gunit.spacecrack.interfacerequest.IVerifyCallBack;
import com.gunit.spacecrack.restservice.RestService;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Tim on 20/03/14.
 */
public class VerifyTask extends AsyncTask<String, Void, Integer> {
    private String verificationTokenString;
    private IVerifyCallBack verifyCallBack;

    public VerifyTask(String verificationTokenString, IVerifyCallBack verifyCallBack) {
        this.verificationTokenString = verificationTokenString;
        this.verifyCallBack = verifyCallBack;
    }

    @Override
    protected Integer doInBackground(String... urls) {
        JSONObject verificationToken = new JSONObject();
        try {
            verificationToken.put("tokenValue", verificationTokenString);
        } catch (JSONException e) {
           return 400;
        }
        return RestService.postVerificationToken(SpaceCrackApplication.URL_VERIFY, verificationToken.toString());
    }

    @Override
    protected void onPostExecute(Integer statusCode) {
        verifyCallBack.verifyCallback(statusCode);
    }
}
