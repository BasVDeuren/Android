package com.gunit.spacecrack.game.scene;

import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import com.google.gson.Gson;
import com.gunit.spacecrack.application.SpaceCrackApplication;
import com.gunit.spacecrack.game.GameActivity;
import com.gunit.spacecrack.game.manager.ResourcesManager;
import com.gunit.spacecrack.game.manager.SceneManager;
import com.gunit.spacecrack.json.Action;
import com.gunit.spacecrack.model.Colony;
import com.gunit.spacecrack.model.Game;
import com.gunit.spacecrack.model.Planet;
import com.gunit.spacecrack.model.Player;
import com.gunit.spacecrack.model.Ship;
import com.gunit.spacecrack.restservice.RestService;

import org.andengine.engine.camera.Camera;
import org.andengine.engine.camera.hud.HUD;
import org.andengine.entity.primitive.Line;
import org.andengine.entity.scene.IOnSceneTouchListener;
import org.andengine.entity.scene.Scene;
import org.andengine.entity.sprite.Sprite;
import org.andengine.entity.sprite.TiledSprite;
import org.andengine.entity.text.Text;
import org.andengine.entity.text.TextOptions;
import org.andengine.input.touch.TouchEvent;
import org.andengine.input.touch.detector.PinchZoomDetector;
import org.andengine.input.touch.detector.ScrollDetector;
import org.andengine.input.touch.detector.SurfaceScrollDetector;
import org.andengine.opengl.util.GLState;
import org.andengine.util.HorizontalAlign;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Dimitri on 24/02/14.
 */
public class GameScene extends BaseScene implements IOnSceneTouchListener, PinchZoomDetector.IPinchZoomDetectorListener, ScrollDetector.IScrollDetectorListener {

    private Scene gameScene;
    private HUD gameHUD;
    private Text scoreText;
    private int score;
    private PinchZoomDetector pinchZoomDetector;
    private final float MIN_ZOOM_FACTOR = 1f;
    private final float MAX_ZOOM_FACTOR = 3f;
    private float zoomFactor;
    private SurfaceScrollDetector scrollDetector;
    private Map<String, TiledSprite> planetSprites;
    private Map<Integer, Sprite> shipSprites;
    private Map<String, Sprite> colonySprites;

    @Override
    public void createScene() {
        gameScene = this;
        createBackground();
        createHUD();

        drawLines();
        drawPlanets();
        drawPlayers(activity.gameWrapper.game);

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

    private void drawPlanets() {
        TiledSprite planetSprite;
        planetSprites = new HashMap<String, TiledSprite>();

        for (Planet planet : activity.spaceCrackMap.planets) {
            planetSprite = createTiledSprite((planet.x * GameActivity.SCALE_X) - (resourcesManager.planetRegion.getWidth() / 2), (planet.y * GameActivity.SCALE_Y) - (resourcesManager.planetRegion.getHeight() / 2), resourcesManager.planetRegion, vbom, 0);
            planetSprite.setScale(0.5f);
            planetSprites.put(planet.name, planetSprite);
            attachChild(planetSprite);
        }
    }

    private void drawLines() {
        Line line;
        for (Planet planet : activity.spaceCrackMap.planets) {
            for (Planet connect : planet.connectedPlanets) {
                line = new Line(planet.x * GameActivity.SCALE_X, planet.y * GameActivity.SCALE_Y, connect.x * GameActivity.SCALE_X, connect.y * GameActivity.SCALE_Y, vbom);

                line.setLineWidth(5);
                line.setColor(0.6f, 0.6f, 0.6f);
                attachChild(line);
            }
        }
    }

    private void drawPlayers(Game game) {
        drawColonies(game.player1, 0);
        drawColonies(game.player2, 1);
        drawShips(game.player1);
        drawShips(game.player2);
    }

    private void drawColonies(Player player, int index) {
        Sprite colonySprite;
        colonySprites = new HashMap<String, Sprite>();

        for (Colony colony : player.colonies) {
            Planet planet = activity.planets.get(colony.planetName);
            colonySprite = createTiledSprite((planet.x * GameActivity.SCALE_X) - 10, (planet.y * GameActivity.SCALE_Y) - 25, resourcesManager.colonyFlagRegion, vbom, index);
            colonySprite.setScale(0.5f);
            colonySprites.put(colony.planetName, colonySprite);
            attachChild(colonySprite);
        }
    }

    private void drawShips(Player player) {
        Sprite shipSprite;
        shipSprites = new HashMap<Integer, Sprite>();

        for (Ship ship : player.ships) {
            final Planet planet = activity.planets.get(ship.planetName);
            final Ship finalShip = ship;
            shipSprite = new Sprite((planet.x * GameActivity.SCALE_X) - 10, (planet.y * GameActivity.SCALE_Y) - 10, resourcesManager.spaceshipRegion, vbom)
            {
                @Override
                protected void preDraw(GLState glState, Camera camera1) {
                    super.preDraw(glState, camera1);
                    glState.enableDither();
                }
                @Override
                public boolean onAreaTouched(TouchEvent pSceneTouchEvent, float pTouchAreaLocalX, float pTouchAreaLocalY) {
                    switch (pSceneTouchEvent.getAction()) {
                        case TouchEvent.ACTION_DOWN:
                            this.setScale(0.8f);
                            highlightConnectedPlantes(finalShip.planetName, 1);
                            break;
                        case TouchEvent.ACTION_MOVE:
                            this.setPosition(pSceneTouchEvent.getX() - this.getWidth() / 2, pSceneTouchEvent.getY() - this.getHeight() / 2);
                            break;
                        case TouchEvent.ACTION_UP:
                            this.setScale(0.5f);
                            Planet newPlanet = checkValidPosition(this, finalShip.planetName);
                            if (newPlanet != null) {
                                placeShip(this, planet, newPlanet, finalShip);
                            } else {
                                this.setPosition((planet.x * GameActivity.SCALE_X) - 10, (planet.y * GameActivity.SCALE_Y) - 10);
                            }
                            highlightConnectedPlantes(finalShip.planetName, 0);
                            break;
                    }
                    return true;
                }
            };
            shipSprite.setScale(0.5f);
            registerTouchArea(shipSprite);
            setTouchAreaBindingOnActionMoveEnabled(true);
            shipSprites.put(ship.shipId, shipSprite);
            attachChild(shipSprite);
        }
    }

    private void highlightConnectedPlantes(String planetName, int index) {
        for (Planet planet : activity.planets.get(planetName).connectedPlanets) {
            TiledSprite connectedSprite = planetSprites.get(planet.name);
            connectedSprite.setCurrentTileIndex(index);
        }
    }

    private Planet checkValidPosition(Sprite shipSprite, String planetName) {
        if (shipSprite != null) {
            for (Planet planet : activity.planets.get(planetName).connectedPlanets) {
                Sprite planetSprite = planetSprites.get(planet.name);
                if (shipSprite.collidesWith(planetSprite)) {
                    Log.i("New planet", planet.name);
                    return planet;
                }
            }
        }
        return null;
    }

    private void placeShip(Sprite shipSprite, Planet oldPlanet,Planet newPlanet, Ship ship) {
        new ActionTask(new Action("MOVESHIP", newPlanet.name, activity.gameWrapper.game.gameId, activity.gameWrapper.activePlayerId, ship), shipSprite, oldPlanet).execute(SpaceCrackApplication.URL_ACTION);
    }

    private void resetPlayers() {
        activity.runOnUpdateThread(new Runnable() {
            @Override
            public void run() {
                for (Map.Entry<Integer, Sprite> entry : shipSprites.entrySet()) {
                    gameScene.detachChild(entry.getValue());
                }
                for (Map.Entry<String, Sprite> entry : colonySprites.entrySet()) {
                    gameScene.detachChild(entry.getValue());
                }
            }
        });

    }

    private void addToScore(int i)
    {
        score += i;
        scoreText.setText("Score: " + score);
    }

    //POST request
    public class ActionTask extends AsyncTask<String, Void, String> {

        private String actionString;
        private Gson gson;
        private Sprite shipSprite;
        private Planet planet;

        public ActionTask (Action action, Sprite shipSprite, Planet planet) {
            super();
            //Create an user to log in
            gson = new Gson();
            actionString = gson.toJson(action);
            this.shipSprite = shipSprite;
            this.planet = planet;
        }

        @Override
        protected String doInBackground (String...url)
        {
            return RestService.postAction(url[0], actionString);
        }

        @Override
        protected void onPostExecute (String result)
        {
            if (result != null) {
                if (result.equals("406")) {
                    Toast.makeText(activity, "Move is not valid", Toast.LENGTH_SHORT).show();
                    shipSprite.setPosition((planet.x * GameActivity.SCALE_X) - 10, (planet.y * GameActivity.SCALE_Y) - 10);
                } else {
                    Gson gson = new Gson();
                    Game game = gson.fromJson(result, Game.class);
                    resetPlayers();
                    drawPlayers(game);
                }
            }
        }
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
