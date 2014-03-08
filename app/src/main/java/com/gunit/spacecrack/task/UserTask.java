package com.gunit.spacecrack.task;

import android.os.AsyncTask;

import com.gunit.spacecrack.interfacerequest.IUserRequest;
import com.gunit.spacecrack.restservice.RestService;

/**
 * Created by Dimitri on 8/03/14.
 */

/**
 * UsertTask sends a request to get the current user
 */
public class UserTask extends AsyncTask<String, Void, String> {

    private IUserRequest userCall;

    public UserTask (IUserRequest userCall) {
        this.userCall = userCall;
    }

    @Override
    protected String doInBackground (String...url)
    {
        return RestService.getRequest(url[0]);
    }

    @Override
    protected void onPostExecute (String result)
    {
        userCall.userCallback(result);
    }
}
