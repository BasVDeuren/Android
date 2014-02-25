package com.gunit.spacecrack.game.scene;

import com.gunit.spacecrack.game.GameActivity;
import com.gunit.spacecrack.game.manager.SceneManager;

import org.andengine.engine.camera.Camera;
import org.andengine.engine.camera.hud.HUD;
import org.andengine.engine.handler.IUpdateHandler;
import org.andengine.entity.scene.IOnSceneTouchListener;
import org.andengine.entity.scene.Scene;
import org.andengine.entity.scene.background.Background;
import org.andengine.entity.sprite.Sprite;
import org.andengine.entity.text.Text;
import org.andengine.entity.text.TextOptions;
import org.andengine.input.touch.TouchEvent;
import org.andengine.input.touch.controller.MultiTouch;
import org.andengine.input.touch.detector.PinchZoomDetector;
import org.andengine.input.touch.detector.ScrollDetector;
import org.andengine.input.touch.detector.SurfaceScrollDetector;
import org.andengine.opengl.util.GLState;
import org.andengine.util.HorizontalAlign;
import org.andengine.util.color.Color;

/**
 * Created by Dimitri on 24/02/14.
 */
public class GameScene extends BaseScene implements IOnSceneTouchListener, PinchZoomDetector.IPinchZoomDetectorListener, ScrollDetector.IScrollDetectorListener {

    private HUD gameHUD;
    private Text scoreText;
    private int score;
    private PinchZoomDetector pinchZoomDetector;
    private final float MIN_ZOOM_FACTOR = 1f;
    private final float MAX_ZOOM_FACTOR = 2f;
    private float zoomFactor;
    private SurfaceScrollDetector scrollDetector;

    @Override
    public void createScene() {
        createBackground();
        createHUD();
        createPlanets();
        this.setOnSceneTouchListener(this);
        scrollDetector = new SurfaceScrollDetector(this);
        pinchZoomDetector = new PinchZoomDetector(this);
        pinchZoomDetector.setEnabled(true);
        this.setOnSceneTouchListener(this);
        this.setTouchAreaBindingOnActionMoveEnabled(true);
        camera.setZoomFactor(1.5f);

    }

    @Override
    public void onBackKeyPressed() {
        SceneManager.getInstance().loadMenuScene(engine);
    }

    @Override
    public SceneManager.SceneType getSceneType() {
        return SceneManager.SceneType.SCENE_GAME;
    }

    @Override
    public void disposeScene() {
        camera.setHUD(null);
        camera.setCenterDirect(GameActivity.CAMERA_WIDTH / 2, GameActivity.CAMERA_HEIGHT / 2);
        camera.setZoomFactor(1f);
        this.detachSelf();
        this.dispose();
    }

    private void createBackground() {
        attachChild(new Sprite(0, 0, resourcesManager.gameBackgroundRegion, vbom) {
            @Override
            protected void preDraw(GLState pGLState, Camera pCamera) {
                super.preDraw(pGLState, pCamera);
                pGLState.enableDither();
            }
        });
//        this.setBackground(new Background(Color.BLACK));
    }

    private void createHUD() {
        gameHUD = new HUD();

        scoreText = new Text(20, GameActivity.CAMERA_HEIGHT - 70, resourcesManager.font, "Score: 0123456789", new TextOptions(HorizontalAlign.LEFT), vbom);
        scoreText.setHorizontalAlign(HorizontalAlign.LEFT);
        scoreText.setText("Score: 0");
        gameHUD.attachChild(scoreText);

        camera.setHUD(gameHUD);
    }

    private void createPlanets() {
        Sprite planet;
        for (int i = 0; i < 5; i++) {
            planet = new Sprite(0, 0, resourcesManager.planetRegion, vbom)
            {
                @Override
                protected void preDraw(GLState pGLState, Camera pCamera)
                {
                    super.preDraw(pGLState, pCamera);
                    pGLState.enableDither();
                }
            };
            planet.setPosition(i*100, i*50);
            planet.setScale(0.5f);
            attachChild(planet);
        }
    }

    private void addToScore(int i)
    {
        score += i;
        scoreText.setText("Score: " + score);
    }

    @Override
    public boolean onSceneTouchEvent(Scene pScene, TouchEvent pSceneTouchEvent) {

//        if (pSceneTouchEvent.isActionMove()) {
////            float xDistance = Math.abs(camera.getCenterX() - pSceneTouchEvent.getX());
////            float yDistance = Math.abs(camera.getCenterY() - pSceneTouchEvent.getY());
////            float distance = xDistance + yDistance;
////            float CAMERA_SPEED = 100;
////            camera.setMaxVelocity((xDistance / distance) * CAMERA_SPEED, (yDistance / distance) * CAMERA_SPEED);
////            camera.setCenter(pSceneTouchEvent.getX(), pSceneTouchEvent.getY());
//            return true;
//
//        }
        if(this.pinchZoomDetector != null) {
            this.pinchZoomDetector.onTouchEvent(pSceneTouchEvent);

            if(this.pinchZoomDetector.isZooming()) {
                this.scrollDetector.setEnabled(false);
            } else {
                if(pSceneTouchEvent.isActionDown()) {
                    this.scrollDetector.setEnabled(true);
                }
                this.scrollDetector.onTouchEvent(pSceneTouchEvent);
            }
        } else {
            this.scrollDetector.onTouchEvent(pSceneTouchEvent);
        }

        return true;

//        return false;

    }

    @Override
    public void onPinchZoomStarted(PinchZoomDetector pPinchZoomDetector, TouchEvent pSceneTouchEvent) {
        zoomFactor = camera.getZoomFactor();
    }

    @Override
    public void onPinchZoom(PinchZoomDetector pPinchZoomDetector, TouchEvent pTouchEvent, float pZoomFactor) {
        if (pZoomFactor != 1)
        {
            // check bounds
            float newZoomFactor = zoomFactor * pZoomFactor;
            if (newZoomFactor <= MIN_ZOOM_FACTOR)
                camera.setZoomFactor(MIN_ZOOM_FACTOR);
            else if (newZoomFactor >= MAX_ZOOM_FACTOR)
                camera.setZoomFactor(MAX_ZOOM_FACTOR);
            else
                camera.setZoomFactor(newZoomFactor);
        }
    }

    @Override
    public void onPinchZoomFinished(PinchZoomDetector pPinchZoomDetector, TouchEvent pTouchEvent, float pZoomFactor) {

    }

    @Override
    public void onScrollStarted(ScrollDetector pScollDetector, int pPointerID, float pDistanceX, float pDistanceY) {

    }

    @Override
    public void onScroll(ScrollDetector pScollDetector, int pPointerID, float pDistanceX, float pDistanceY) {
        final float zoomFactor = camera.getZoomFactor();
        camera.offsetCenter(-pDistanceX/zoomFactor, -pDistanceY/zoomFactor);
    }

    @Override
    public void onScrollFinished(ScrollDetector pScollDetector, int pPointerID, float pDistanceX, float pDistanceY) {

    }
}
