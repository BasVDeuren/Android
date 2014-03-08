package com.gunit.spacecrack.interfacerequest;

/**
 * Created by Dimitri on 8/03/14.
 */

/**
 * Methods used by the AsyncTask to notify their progress to the component who started the AsyncTask
 */
public interface IUserRequest {
    void startTask();
    void userCallback(String result);
}
