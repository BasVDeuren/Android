package com.gunit.spacecrack.game;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import com.firebase.client.Firebase;
import com.google.gson.Gson;
import com.google.gson.JsonParseException;
import com.gunit.spacecrack.R;
import com.gunit.spacecrack.application.SpaceCrackApplication;
import com.gunit.spacecrack.game.manager.ResourcesManager;
import com.gunit.spacecrack.game.manager.SceneManager;
import com.gunit.spacecrack.json.GameActivePlayerWrapper;
import com.gunit.spacecrack.json.PlayerViewModel;
import com.gunit.spacecrack.json.RevisionListViewModel;
import com.gunit.spacecrack.model.Invite;
import com.gunit.spacecrack.model.Planet;
import com.gunit.spacecrack.model.SpaceCrackMap;
import com.gunit.spacecrack.restservice.RestService;
import com.gunit.spacecrack.service.SpaceCrackService;

import org.andengine.engine.Engine;
import org.andengine.engine.LimitedFPSEngine;
import org.andengine.engine.camera.SmoothCamera;
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
import java.util.List;
import java.util.Map;

/**
 * Created by Dimitri on 24/02/14.
 */

/**
 * GameActivity will handle the whole Game
 */
public class GameActivity extends BaseGameActivity {

    private SmoothCamera smoothCamera;
    public static final int CAMERA_WIDTH = 930;
    public static final int CAMERA_HEIGHT = 558;
    public static final float SCALE_X = 0.58125f;
    public static final float SCALE_Y = 0.558f;
    private static final int FPS_LIMIT = 60;
    private ResourcesManager resourceManager;

    public SpaceCrackMap spaceCrackMap;
    public GameActivePlayerWrapper gameActivePlayerWrapper;
    public PlayerViewModel player;
    public Map<String, Planet> planets;
    public List<Integer> revisions;
    // Camera movement speeds
    public final float maxVelocityX = 500;
    public final float maxVelocityY = 500;
    // Camera zoom speed
    public final float maxZoomFactorChange = 5;

    private boolean gameStarted;
    private boolean mapLoaded;
    private boolean firstPLayer;
    public boolean replay;
    public int gameId;
    private String opponentUsername;

    private SpaceCrackService spaceCrackService;
    private boolean boundToService = false;

    @Override
    protected void onCreate(Bundle pSavedInstanceState) {
        super.onCreate(pSavedInstanceState);
        Log.d("GameActivity", "Create");
        loadData();
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d("GameActivity", "Start");
        //Bind to the service
        Intent intent = new Intent(this, SpaceCrackService.class);
        intent.putExtra("username", SpaceCrackApplication.user.username);
        bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onStop() {
        if (boundToService) {
            unbindService(serviceConnection);
        }
        Log.d("GameActivity", "Stop");

        super.onStop();
    }

    @Override
    protected synchronized void onResume() {
        super.onResume();
        if (mEngine != null && !mEngine.isRunning()) {
            mEngine.start();
        }
    }


    /** Defines callbacks for service binding, passed to bindService() */
    private ServiceConnection serviceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            SpaceCrackService.LocalBinder binder = (SpaceCrackService.LocalBinder) service;
            spaceCrackService = binder.getService();
            boundToService = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            boundToService = false;
        }
    };

    @Override
    public synchronized void onGameCreated() {
        super.onGameCreated();
        Log.d("GameActivity", "Game created");
    }

    @Override
    public synchronized void onResumeGame() {
        Log.d("GameActivity", "Resume Game");
        if (this.mEngine != null)
            super.onResumeGame();
    }

    /**
     * Load the data containing the Game its state
     */
    private void loadData() {
        gameStarted = false;
        mapLoaded = false;
        planets = new HashMap<String, Planet>();
        new MapTask().execute(SpaceCrackApplication.URL_MAP);
        Intent intent = getIntent();
        if (intent.getStringExtra("gameName") != null && intent.getIntExtra("opponentId", 0) != 0) {
            opponentUsername = intent.getStringExtra("opponentName");
            new GameTask(intent.getStringExtra("gameName"), intent.getIntExtra("opponentId", 0)).execute(SpaceCrackApplication.URL_GAME);
        } else if (intent.getIntExtra("gameId", 0) != 0 ) {
            if (intent.getBooleanExtra("replay", false)) {
                replay = true;
                gameId = intent.getIntExtra("gameId", 0);
                new RevisionsTask().execute(SpaceCrackApplication.URL_REPLAY + "/" + intent.getIntExtra("gameId", 0));
            } else {
                new ActiveGameTask().execute(SpaceCrackApplication.URL_ACTIVEGAME + "/" + intent.getIntExtra("gameId", 0));
            }
        } else {
            Toast.makeText(this, getString(R.string.game_not_loaded), Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    @Override
    public Engine onCreateEngine(EngineOptions pEngineOptions) {
        Log.d("GameActivity", "Engine");
        return new LimitedFPSEngine(pEngineOptions, FPS_LIMIT);
    }

    /**
     * Defines the options of the engine, such as the type of camera and screen orientation
     * @return
     */
    @Override
    public EngineOptions onCreateEngineOptions() {
        smoothCamera = new SmoothCamera(0, 0, CAMERA_WIDTH, CAMERA_HEIGHT, maxVelocityX, maxVelocityY, maxZoomFactorChange);
        smoothCamera.setBounds(0, 0, CAMERA_WIDTH, CAMERA_HEIGHT);
        smoothCamera.setBoundsEnabled(true);
        IResolutionPolicy resolutionPolicy = new RatioResolutionPolicy(CAMERA_WIDTH, CAMERA_HEIGHT);
        EngineOptions engineOptions = new EngineOptions(true, ScreenOrientation.LANDSCAPE_FIXED, resolutionPolicy, smoothCamera);
        engineOptions.setWakeLockOptions(WakeLockOptions.SCREEN_ON);
        engineOptions.getTouchOptions().setNeedsMultiTouch(true);

        //Check for MultiTouch support
        if (!MultiTouch.isSupported(this)) {
            Toast.makeText(this, getResources().getString(R.string.multitouch_support), Toast.LENGTH_SHORT).show();
            finish();
        }
        return engineOptions;
    }

    @Override
    public void onCreateResources(OnCreateResourcesCallback pOnCreateResourcesCallback) throws Exception {
        ResourcesManager.getInstance().init(this);
        ResourcesManager.getInstance().loadFonts();
        pOnCreateResourcesCallback.onCreateResourcesFinished();
        Log.d("GameActivity", "Create Resources");

    }

    @Override
    public void onCreateScene(OnCreateSceneCallback pOnCreateSceneCallback) throws Exception {
//        SceneManager.getInstance().createSplashScene(pOnCreateSceneCallback);
        SceneManager.getInstance().createLoadingScene(pOnCreateSceneCallback);
        Log.d("GameActivity", "Create Scene");

    }

    @Override
    public void onPopulateScene(Scene pScene, OnPopulateSceneCallback pOnPopulateSceneCallback) throws Exception {
//        mEngine.registerUpdateHandler(new TimerHandler(10f, new ITimerCallback() {
//            @Override
//            public void onTimePassed(TimerHandler pTimerHandler) {
//                mEngine.unregisterUpdateHandler(pTimerHandler);
//                SceneManager.getInstance().loadGameScene(mEngine);
//            }
//        }));
        pOnPopulateSceneCallback.onPopulateSceneFinished();
    }

    @Override
    protected void onDestroy() {
        Log.d("Gameactivity", "Destroy");
//        if (this.isGameLoaded()) {
//            System.exit(0);
//        }
        super.onDestroy();
    }

    @Override
    protected void onPause() {
        Log.i("Gameactivity", "Pause");
        super.onPause();
        if (mEngine != null && mEngine.isRunning()) {
            mEngine.stop();
        }
    }
    //    @Override
//    public boolean onKeyDown(int keyCode, KeyEvent event)
//    {
//        if (keyCode == KeyEvent.KEYCODE_BACK)
//        {
//            SceneManager.getInstance().getCurrentScene().onBackKeyPressed();
//        }
//        return false;
//    }

    /**
     * Override the default action of the Back button, and let the current Scene decide what to do
     */
    @Override
    public void onBackPressed() {
        SceneManager.getInstance().getCurrentScene().onBackKeyPressed();
    }

    private void startGame () {
        if (mapLoaded && gameStarted) {
            mapLoaded = false;
            gameStarted = false;
            if (replay) {
                SceneManager.getInstance().loadReplayScene(mEngine);
            } else {
                spaceCrackService.addChatListener(SpaceCrackApplication.URL_FIREBASE_CHAT, String.valueOf(gameActivePlayerWrapper.game.gameId));
                spaceCrackService.addGameListener(gameActivePlayerWrapper.firebaseGameURL, player.playerId, firstPLayer);
                SceneManager.getInstance().loadGameScene(mEngine);
            }
        }
    }

    private void pushInvite (String gameId, String opponentId) {
        opponentUsername = opponentUsername.replaceAll("\\s","");
        Invite invite = new Invite(gameId, SpaceCrackApplication.user.username, false);
        Firebase firebase = new Firebase(SpaceCrackApplication.URL_FIREBASE_INVITES + "/" + opponentUsername + opponentId);
        firebase.push().setValue(invite);
    }

    /**
     * GET request to game map
     */
    private class MapTask extends AsyncTask<String, Void, String> {

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

    /**
     * POST request to create the game
     */
    private class GameTask extends AsyncTask<String, Void, String> {

        private JSONObject game;
        private int opponentId;

        public GameTask(String gameName, int opponentId)
        {
            super();
            game = new JSONObject();
            this.opponentId = opponentId;
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
                pushInvite(result, String.valueOf(opponentId));
                new ActiveGameTask().execute(SpaceCrackApplication.URL_ACTIVEGAME + "/" + result);
            } else {
                Toast.makeText(GameActivity.this, getString(R.string.game_not_created), Toast.LENGTH_SHORT).show();
            }
        }
    }

    /**
     * GET request to the active game
     */
    private class ActiveGameTask extends AsyncTask<String, Void, String> {

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
                    Log.i("Game JSON", result);
                    gameActivePlayerWrapper = gson.fromJson(result, GameActivePlayerWrapper.class);
                    if (gameActivePlayerWrapper.activePlayerId == gameActivePlayerWrapper.game.player1.playerId) {
                        player = gameActivePlayerWrapper.game.player1;
                        firstPLayer = true;
                    } else {
                        player = gameActivePlayerWrapper.game.player2;
                        firstPLayer = false;
                    }
                    gameStarted = true;
                    startGame();
                } catch (JsonParseException e) {
                    e.printStackTrace();
                }
            } else {
                Toast.makeText(GameActivity.this, getString(R.string.game_not_found), Toast.LENGTH_SHORT).show();
            }
        }
    }

    /**
     * GET request to the Revisions
     */
    private class RevisionsTask extends AsyncTask<String, Void, String> {
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
                    RevisionListViewModel revisionListViewModel = gson.fromJson(result, RevisionListViewModel.class);
                    revisions = revisionListViewModel.revisions;
                    gameStarted = true;
                    startGame();
                } catch (JsonParseException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
