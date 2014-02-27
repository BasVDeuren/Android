package com.gunit.spacecrack.game.scene;

import com.gunit.spacecrack.game.GameActivity;
import com.gunit.spacecrack.game.manager.ResourcesManager;
import com.gunit.spacecrack.game.manager.SceneManager;
import com.gunit.spacecrack.model.Colony;
import com.gunit.spacecrack.model.Planet;
import com.gunit.spacecrack.model.Player;
import com.gunit.spacecrack.model.Ship;

import org.andengine.engine.camera.Camera;
import org.andengine.engine.camera.hud.HUD;
import org.andengine.entity.primitive.Line;
import org.andengine.entity.scene.IOnSceneTouchListener;
import org.andengine.entity.scene.Scene;
import org.andengine.entity.sprite.Sprite;
import org.andengine.entity.text.Text;
import org.andengine.entity.text.TextOptions;
import org.andengine.input.touch.TouchEvent;
import org.andengine.input.touch.detector.PinchZoomDetector;
import org.andengine.input.touch.detector.ScrollDetector;
import org.andengine.input.touch.detector.SurfaceScrollDetector;
import org.andengine.opengl.util.GLState;
import org.andengine.util.HorizontalAlign;

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
        drawLines();
        createPlanets();
        drawShips();
        drawColonies();
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
        ResourcesManager.getInstance().unloadGameResources();
        this.detachSelf();
        this.dispose();
    }

    private void createBackground() {
        Sprite background = createSprite(0, 0, resourcesManager.gameBackgroundRegion, vbom);
        attachChild(background);
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
        Sprite planetSprite;

        for (Planet planet : activity.spaceCrackMap.planets) {
            planetSprite = new Sprite(0, 0, resourcesManager.planetRegion, vbom)
            {
                @Override
                protected void preDraw(GLState pGLState, Camera pCamera)
                {
                    super.preDraw(pGLState, pCamera);
                    pGLState.enableDither();
                }
            };
//            planetSprite.setScale(0.15f);
            planetSprite.setPosition((planet.x * 0.58125f) - (resourcesManager.planetRegion.getWidth()/2), (planet.y * 0.558f) - (resourcesManager.planetRegion.getHeight()/2));
            attachChild(planetSprite);
        }
    }

    private void drawLines() {
        Line line;
        for (Planet planet : activity.spaceCrackMap.planets) {
            for (Planet connect : planet.connectedPlanets) {
                line = new Line(planet.x * 0.58125f, planet.y * 0.558f, connect.x * 0.58125f, connect.y * 0.558f, vbom);

                line.setLineWidth(5);
                line.setColor(0.6f, 0.6f, 0.6f);
                attachChild(line);
            }
        }
    }

    private void drawShips() {
        Player player = activity.gameWrapper.game.player1;
        Sprite shipSprite;

        for (Ship ship : player.ships) {
            Planet planet = activity.planets.get(ship.planetName);
            shipSprite = createSprite((planet.x * 0.58125f), ((planet.y - 15) * 0.558f), resourcesManager.spaceshipRegion, vbom);
//            shipSprite.setScale(0.15f);
            attachChild(shipSprite);
        }
    }

    private void drawColonies() {
        Player player = activity.gameWrapper.game.player1;
        Sprite colonySprite;

        for (Colony colony : player.colonies) {
            Planet planet = activity.planets.get(colony.planetName);
            colonySprite = createSprite(((planet.x - 5) * 0.58125f), ((planet.y - 32) * 0.558f), resourcesManager.colonyFlagRegion, vbom);
//            colonySprite.setScale(0.15f);
            attachChild(colonySprite);
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
