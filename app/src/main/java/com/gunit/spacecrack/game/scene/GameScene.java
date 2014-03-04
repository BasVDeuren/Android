package com.gunit.spacecrack.game.scene;

import android.content.Intent;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.ValueEventListener;
import com.google.gson.Gson;
import com.gunit.spacecrack.application.SpaceCrackApplication;
import com.gunit.spacecrack.chat.ChatActivity;
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
import org.andengine.entity.primitive.Rectangle;
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Dimitri on 24/02/14.
 */
public class GameScene extends BaseScene implements IOnSceneTouchListener, PinchZoomDetector.IPinchZoomDetectorListener, ScrollDetector.IScrollDetectorListener {

    private Scene gameScene;
    private HUD gameHUD;
    private Text commandText;
    private PinchZoomDetector pinchZoomDetector;
    private final float MIN_ZOOM_FACTOR = 1f;
    private final float MAX_ZOOM_FACTOR = 3f;
    private final int MOVECOST = 1;
    private final int COLONIZECOST = 2;
    private float zoomFactor;
    private SurfaceScrollDetector scrollDetector;
    private Map<String, TiledSprite> planetSprites;

    private List<Sprite> shipSprites;
    private List<Sprite> colonySprites;

    private Firebase ref;

    @Override
    public void createScene() {
        gameScene = this;

        shipSprites = new ArrayList<Sprite>();
        colonySprites = new ArrayList<Sprite>();

        //Register Firebase listener
        ref = new Firebase(activity.gameWrapper.firebaseGameURL);
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue() != null) {
                    Log.i("DataSnapshot", dataSnapshot.getValue().toString());
                    Gson gson = new Gson();
                    Game game = gson.fromJson(dataSnapshot.getValue().toString(), Game.class);
                    if (game != null) {
                        activity.gameWrapper.game = game;
                        resetPlayers();
                    }
                }
            }

            @Override
            public void onCancelled() {

            }
        });

        //Draw the game
        createBackground();
        createHUD();

        drawLines();
        drawPlanets();
        drawPlayers(activity.gameWrapper.game);

        //Detect touch controls
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
//        SceneManager.getInstance().loadMenuScene(engine);
        activity.finish();
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

        commandText = new Text(12, GameActivity.CAMERA_HEIGHT - 120, resourcesManager.font, "Commandpoints: 0123456789", new TextOptions(HorizontalAlign.LEFT), vbom);
        commandText.setHorizontalAlign(HorizontalAlign.LEFT);
        commandText.setText("Commandpoints: " + activity.gameWrapper.game.player1.commandPoints);
        gameHUD.attachChild(commandText);
        Rectangle chat = new Rectangle(20, GameActivity.CAMERA_HEIGHT - 70, 50, 50, vbom)
        {
            @Override
            public boolean onAreaTouched(TouchEvent pSceneTouchEvent, float pTouchAreaLocalX, float pTouchAreaLocalY) {
                if (pSceneTouchEvent.isActionUp()) {
                    Intent intent = new Intent(activity, ChatActivity.class);
                    intent.putExtra("gameId", String.valueOf(activity.gameWrapper.game.gameId));
                    activity.startActivity(intent);
                }
                return true;
            }
        };
        gameHUD.registerTouchArea(chat);
        gameHUD.attachChild(chat);
        Rectangle endTurn = new Rectangle(90, GameActivity.CAMERA_HEIGHT - 70, 50, 50, vbom)
        {
            @Override
            public boolean onAreaTouched(TouchEvent pSceneTouchEvent, float pTouchAreaLocalX, float pTouchAreaLocalY) {
                if (pSceneTouchEvent.isActionUp() && !activity.gameWrapper.game.player1.turnEnded) {
                    new ActionTask(new Action("ENDTURN", activity.gameWrapper.game.gameId, activity.gameWrapper.activePlayerId), null, null).execute(SpaceCrackApplication.URL_ACTION);
                }
                return true;
            }
        };
        gameHUD.registerTouchArea(endTurn);
        gameHUD.attachChild(endTurn);
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
        updateCommandPoints();
    }

    private void drawColonies(Player player, int index) {
        Sprite colonySprite;

        for (Colony colony : player.colonies) {
            Planet planet = activity.planets.get(colony.planetName);
            Sprite planetSprite = planetSprites.get(colony.planetName);
//            colonySprite = createTiledSprite((planet.x * GameActivity.SCALE_X) - 10, (planet.y * GameActivity.SCALE_Y) - 25, resourcesManager.colonyFlagRegion, vbom, index);
            colonySprite = createTiledSprite(10, -15, resourcesManager.colonyFlagRegion, vbom, index);
//            colonySprite.setScale(0.5f);
            colonySprites.add(colonySprite);
            planetSprite.attachChild(colonySprite);
//            attachChild(colonySprite);
        }
    }

    private void drawShips(Player player) {
        Sprite shipSprite;

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
                                if (checkCommandPoints(newPlanet.name)) {
                                    placeShip(this, planet, newPlanet, finalShip);
                                } else {
//                                    Toast.makeText(activity, activity.getResources().getString(R.string.commandpoints_fail), Toast.LENGTH_SHORT).show();
                                    this.setPosition((planet.x * GameActivity.SCALE_X) - 10, (planet.y * GameActivity.SCALE_Y) - 10);
                                }
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
            if (player.playerId == activity.gameWrapper.activePlayerId) {
                registerTouchArea(shipSprite);
            }
            setTouchAreaBindingOnActionMoveEnabled(true);
            shipSprites.add(shipSprite);
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

    private boolean checkCommandPoints(String planetName) {
        boolean colony = false;
        Sprite planetSprite = planetSprites.get(planetName);
        if (planetSprite.getChildCount() > 0) {
            colony = true;
        }
        return (colony && activity.gameWrapper.game.player1.commandPoints >= MOVECOST) || (!colony && activity.gameWrapper.game.player1.commandPoints >= COLONIZECOST);
    }

    private void placeShip(Sprite shipSprite, Planet oldPlanet,Planet newPlanet, Ship ship) {
        new ActionTask(new Action("MOVESHIP", newPlanet.name, activity.gameWrapper.game.gameId, activity.gameWrapper.activePlayerId, ship), shipSprite, oldPlanet).execute(SpaceCrackApplication.URL_ACTION);
    }

    private void resetPlayers() {
        activity.runOnUpdateThread(new Runnable() {
            @Override
            public void run() {
//                for (Map.Entry<Integer, Sprite> entry : shipSprites.entrySet()) {
//                    gameScene.unregisterTouchArea(entry.getValue());
//                    gameScene.detachChild(entry.getValue());
//                    entry.getValue().clearEntityModifiers();
//                    entry.getValue().clearUpdateHandlers();
//                    if (!entry.getValue().isDisposed()) {
//                        entry.getValue().dispose();
//                    }
//                    entry.getValue().detachSelf();
//                }
//                for (Map.Entry<String, Sprite> entry : colonySprites.entrySet()) {
//                    gameScene.detachChild(entry.getValue());
//                    entry.getValue().dispose();
//                }
                for (Sprite sprite : shipSprites) {
                    gameScene.unregisterTouchArea(sprite);
                    sprite.clearEntityModifiers();
                    sprite.clearUpdateHandlers();
                    sprite.dispose();
                    detachChild(sprite);
                }
                for (Sprite sprite : colonySprites) {
                    sprite.clearEntityModifiers();
                    sprite.clearUpdateHandlers();
                    sprite.dispose();
                    detachChild(sprite);
                }
                shipSprites.clear();
                colonySprites.clear();
                drawPlayers(activity.gameWrapper.game);
            }
        });
    }


    private void updateCommandPoints()
    {
        commandText.setText("Commandpoints: " + activity.gameWrapper.game.player1.commandPoints);
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
            if (result != null || !result.equals("")) {
                if (shipSprite != null && planet !=null) {
                    if (result.equals("406")) {
                        Toast.makeText(activity, "Move is not valid", Toast.LENGTH_SHORT).show();
                        shipSprite.setPosition((planet.x * GameActivity.SCALE_X) - 10, (planet.y * GameActivity.SCALE_Y) - 10);
                    } else {
                        Toast.makeText(activity, "Move valid", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(activity, "Turn ended", Toast.LENGTH_SHORT).show();
                    Log.i("Result", result);
                }

            } else {
                Toast.makeText(activity, "Something went wrong...", Toast.LENGTH_SHORT).show();
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
