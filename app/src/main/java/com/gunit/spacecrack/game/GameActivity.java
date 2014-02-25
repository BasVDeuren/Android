package com.gunit.spacecrack.game;

import android.view.KeyEvent;

import com.gunit.spacecrack.game.scene.SceneManager;

import org.andengine.engine.Engine;
import org.andengine.engine.LimitedFPSEngine;
import org.andengine.engine.camera.BoundCamera;
import org.andengine.engine.camera.Camera;
import org.andengine.engine.handler.timer.ITimerCallback;
import org.andengine.engine.handler.timer.TimerHandler;
import org.andengine.engine.options.EngineOptions;
import org.andengine.engine.options.ScreenOrientation;
import org.andengine.engine.options.WakeLockOptions;
import org.andengine.engine.options.resolutionpolicy.IResolutionPolicy;
import org.andengine.engine.options.resolutionpolicy.RatioResolutionPolicy;
import org.andengine.entity.scene.IOnSceneTouchListener;
import org.andengine.entity.scene.Scene;
import org.andengine.input.touch.TouchEvent;
import org.andengine.input.touch.detector.PinchZoomDetector;
import org.andengine.ui.activity.BaseGameActivity;

/**
 * Created by Dimitri on 24/02/14.
 */
public class GameActivity extends BaseGameActivity implements IOnSceneTouchListener, PinchZoomDetector.IPinchZoomDetectorListener {

    private static final int WIDTH = 1600;
    private static final int HEIGHT = 1000;
    private Camera camera;
    private BoundCamera boundCamera;
    public static final int CAMERA_WIDTH = 930;
    public static final int CAMERA_HEIGHT = 558;
    private static final int FPS_LIMIT = 60;
    private ResourcesManager resourceManager;

    @Override
    public Engine onCreateEngine(EngineOptions pEngineOptions) {
        Engine engine = new LimitedFPSEngine(pEngineOptions, FPS_LIMIT);
        return engine;
    }

    @Override
    public EngineOptions onCreateEngineOptions() {
        camera = new Camera(0, 0, WIDTH, HEIGHT);
        boundCamera = new BoundCamera(0, 0, CAMERA_WIDTH, CAMERA_HEIGHT, 0, WIDTH, 0, HEIGHT);
        boundCamera.setBoundsEnabled(true);
        IResolutionPolicy resolutionPolicy = new RatioResolutionPolicy(CAMERA_WIDTH, CAMERA_HEIGHT);
        EngineOptions engineOptions = new EngineOptions(true, ScreenOrientation.LANDSCAPE_FIXED, resolutionPolicy, boundCamera);
        engineOptions.getAudioOptions().setNeedsMusic(true).setNeedsSound(true);
        engineOptions.setWakeLockOptions(WakeLockOptions.SCREEN_ON);
        return engineOptions;
    }

    @Override
    public void onCreateResources(OnCreateResourcesCallback pOnCreateResourcesCallback) throws Exception {
        ResourcesManager.getInstance().init(this);
        pOnCreateResourcesCallback.onCreateResourcesFinished();
    }

    @Override
    public void onCreateScene(OnCreateSceneCallback pOnCreateSceneCallback) throws Exception {
        SceneManager.getInstance().createSplashScene(pOnCreateSceneCallback);
    }

    @Override
    public void onPopulateScene(Scene pScene, OnPopulateSceneCallback pOnPopulateSceneCallback) throws Exception {
        mEngine.registerUpdateHandler(new TimerHandler(2f, new ITimerCallback() {
            @Override
            public void onTimePassed(TimerHandler pTimerHandler) {
                mEngine.unregisterUpdateHandler(pTimerHandler);
                SceneManager.getInstance().createMenuScene();
            }
        }));
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

    @Override
    public boolean onSceneTouchEvent(Scene pScene, TouchEvent pSceneTouchEvent) {
        return false;
    }

    @Override
    public void onPinchZoomStarted(PinchZoomDetector pPinchZoomDetector, TouchEvent pSceneTouchEvent) {

    }

    @Override
    public void onPinchZoom(PinchZoomDetector pPinchZoomDetector, TouchEvent pTouchEvent, float pZoomFactor) {

    }

    @Override
    public void onPinchZoomFinished(PinchZoomDetector pPinchZoomDetector, TouchEvent pTouchEvent, float pZoomFactor) {

    }
}
