package com.gunit.spacecrack.game;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.KeyEvent;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.JsonParseException;
import com.gunit.spacecrack.application.SpaceCrackApplication;
import com.gunit.spacecrack.game.manager.ResourcesManager;
import com.gunit.spacecrack.game.manager.SceneManager;
import com.gunit.spacecrack.json.GameWrapper;
import com.gunit.spacecrack.model.Planet;
import com.gunit.spacecrack.model.SpaceCrackMap;
import com.gunit.spacecrack.restservice.RestService;

import org.andengine.engine.Engine;
import org.andengine.engine.LimitedFPSEngine;
import org.andengine.engine.camera.SmoothCamera;
import org.andengine.engine.handler.timer.ITimerCallback;
import org.andengine.engine.handler.timer.TimerHandler;
import org.andengine.engine.options.EngineOptions;
import org.andengine.engine.options.ScreenOrientation;
import org.andengine.engine.options.WakeLockOptions;
import org.andengine.engine.options.resolutionpolicy.IResolutionPolicy;
import org.andengine.engine.options.resolutionpolicy.RatioResolutionPolicy;
import org.andengine.entity.scene.Scene;
import org.andengine.input.touch.controller.MultiTouch;
import org.andengine.ui.activity.BaseGameActivity;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Dimitri on 24/02/14.
 */
public class GameActivity extends BaseGameActivity {

    private static final int WIDTH = 1600;
    private static final int HEIGHT = 1000;
    private SmoothCamera smoothCamera;
    public static final int CAMERA_WIDTH = 930;
    public static final int CAMERA_HEIGHT = 558;
    public static final float SCALE_X = 0.58125f;
    public static final float SCALE_Y = 0.558f;
    private static final int FPS_LIMIT = 60;
    private ResourcesManager resourceManager;

    public SpaceCrackMap spaceCrackMap;
    public GameWrapper gameWrapper;
    public Map<String, Planet> planets;

    // Camera movement speeds
    public final float maxVelocityX = 500;
    public final float maxVelocityY = 500;
    // Camera zoom speed
    public final float maxZoomFactorChange = 5;

    private boolean gameStarted;
    private boolean mapLoaded;


    @Override
    protected void onCreate(Bundle pSavedInstanceState) {
        super.onCreate(pSavedInstanceState);
        loadData();
    }

    @Override
    public synchronized void onResumeGame() {
        if (this.mEngine != null)
            super.onResumeGame();
    }

    private void loadData() {
        gameStarted = false;
        mapLoaded = false;
        planets = new HashMap<String, Planet>();
        new GetMap().execute(SpaceCrackApplication.URL_MAP);
        Intent intent = getIntent();
        if (intent.getStringExtra("gameName") != null && intent.getStringExtra("opponent") != null) {
            new StartGameTask(intent.getStringExtra("gameName"), intent.getStringExtra("opponent")).execute(SpaceCrackApplication.URL_GAME);
        } else {
            new StartGameTask("test", "2").execute(SpaceCrackApplication.URL_GAME);
        }
    }

    @Override
    public Engine onCreateEngine(EngineOptions pEngineOptions) {
        return new LimitedFPSEngine(pEngineOptions, FPS_LIMIT);
    }

    @Override
    public EngineOptions onCreateEngineOptions() {
        smoothCamera = new SmoothCamera(0, 0, CAMERA_WIDTH, CAMERA_HEIGHT, maxVelocityX, maxVelocityY, maxZoomFactorChange);
        smoothCamera.setBounds(0, 0, CAMERA_WIDTH, CAMERA_HEIGHT);
        smoothCamera.setBoundsEnabled(true);
        IResolutionPolicy resolutionPolicy = new RatioResolutionPolicy(CAMERA_WIDTH, CAMERA_HEIGHT);
        EngineOptions engineOptions = new EngineOptions(true, ScreenOrientation.LANDSCAPE_FIXED, resolutionPolicy, smoothCamera);
        engineOptions.getAudioOptions().setNeedsMusic(true).setNeedsSound(true);
        engineOptions.setWakeLockOptions(WakeLockOptions.SCREEN_ON);
        engineOptions.getTouchOptions().setNeedsMultiTouch(true);

        if (!MultiTouch.isSupported(this)) {
            Toast.makeText(this, "Your device doesn't support multi touch", Toast.LENGTH_SHORT).show();
            finish();
        }
        return engineOptions;
    }

    @Override
    public void onCreateResources(OnCreateResourcesCallback pOnCreateResourcesCallback) throws Exception {
        ResourcesManager.getInstance().init(this);
        ResourcesManager.getInstance().loadFonts();
        pOnCreateResourcesCallback.onCreateResourcesFinished();
    }

    @Override
    public void onCreateScene(OnCreateSceneCallback pOnCreateSceneCallback) throws Exception {
        SceneManager.getInstance().createSplashScene(pOnCreateSceneCallback);
    }

    @Override
    public void onPopulateScene(Scene pScene, OnPopulateSceneCallback pOnPopulateSceneCallback) throws Exception {
//        mEngine.registerUpdateHandler(new TimerHandler(10f, new ITimerCallback() {
//            @Override
//            public void onTimePassed(TimerHandler pTimerHandler) {
//                mEngine.unregisterUpdateHandler(pTimerHandler);
//                SceneManager.getInstance().loadMenuScene(mEngine);
//            }
//        }));
        pOnPopulateSceneCallback.onPopulateSceneFinished();
    }

//    @Override
//    protected void onDestroy() {
//        super.onDestroy();
//        System.exit(0);
//    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event)
    {
        if (keyCode == KeyEvent.KEYCODE_BACK)
        {
            SceneManager.getInstance().getCurrentScene().onBackKeyPressed();
        }
        return false;
    }

    private void startGame () {
        if (mapLoaded && gameStarted) {
            SceneManager.getInstance().loadMenuScene(mEngine);
        }
    }

    //GET request to map
    private class GetMap extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground (String...url)
        {
            return RestService.getRequest(url[0]);
        }

        @Override
        protected void onPostExecute (String result)
        {
            if (result != null) {
                try {
                    Gson gson = new Gson();
                    spaceCrackMap = gson.fromJson(result, SpaceCrackMap.class);
                    for (Planet planet : spaceCrackMap.planets) {
                        planets.put(planet.name, planet);
                    }
                    mapLoaded = true;
                    startGame();
                } catch (JsonParseException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    //POST request
    public class StartGameTask extends AsyncTask<String, Void, String> {

        private JSONObject game;

        public StartGameTask (String gameName, String opponentId)
        {
            super();
            game = new JSONObject();
            try {
                game.put("gameName", gameName);
                game.put("opponentProfileId", opponentId);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        @Override
        protected String doInBackground (String...url)
        {
            return RestService.postGame(url[0], game);
        }

        @Override
        protected void onPostExecute (String result)
        {
            if (result != null) {
                Toast.makeText(GameActivity.this, "Data received", Toast.LENGTH_SHORT).show();
                new GetActiveGame().execute(SpaceCrackApplication.URL_ACTIVEGAME + "/" + result);
            }
        }
    }

    //GET request to the active game
    private class GetActiveGame extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground (String...url)
        {
            return RestService.getRequest(url[0]);
        }

        @Override
        protected void onPostExecute (String result)
        {
            if (result != null) {
                try {
                    Gson gson = new Gson();
                    gameWrapper = gson.fromJson(result, GameWrapper.class);
                    gameStarted = true;
                    startGame();
                } catch (JsonParseException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
