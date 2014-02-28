package com.gunit.spacecrack.game.manager;

import android.os.AsyncTask;
import android.widget.Toast;

import com.gunit.spacecrack.application.SpaceCrackApplication;
import com.gunit.spacecrack.game.GameActivity;
import com.gunit.spacecrack.model.Ship;
import com.gunit.spacecrack.restservice.RestService;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Dimitri on 25/02/14.
 */
public class GameManager {

    private static GameManager INSTANCE;

    private GameManager () {

    }

    public static GameManager getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new GameManager();
        }
        return INSTANCE;
    }


}
