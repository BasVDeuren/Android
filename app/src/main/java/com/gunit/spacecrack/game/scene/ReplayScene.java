package com.gunit.spacecrack.game.scene;

import android.os.AsyncTask;
import android.os.Bundle;

import com.facebook.FacebookException;
import com.facebook.Session;
import com.facebook.widget.WebDialog;
import com.firebase.client.Firebase;
import com.google.gson.Gson;
import com.google.gson.JsonParseException;
import com.gunit.spacecrack.R;
import com.gunit.spacecrack.application.SpaceCrackApplication;
import com.gunit.spacecrack.game.GameActivity;
import com.gunit.spacecrack.game.manager.ResourcesManager;
import com.gunit.spacecrack.game.manager.SceneManager;
import com.gunit.spacecrack.json.RevisionListViewModel;
import com.gunit.spacecrack.model.Colony;
import com.gunit.spacecrack.model.Game;
import com.gunit.spacecrack.model.Planet;
import com.gunit.spacecrack.model.Player;
import com.gunit.spacecrack.model.Ship;
import com.gunit.spacecrack.restservice.RestService;

import org.andengine.engine.camera.Camera;
import org.andengine.engine.camera.hud.HUD;
import org.andengine.engine.handler.timer.ITimerCallback;
import org.andengine.engine.handler.timer.TimerHandler;
import org.andengine.entity.primitive.Line;
import org.andengine.entity.primitive.Rectangle;
import org.andengine.entity.scene.IOnSceneTouchListener;
import org.andengine.entity.scene.Scene;
import org.andengine.entity.sprite.ButtonSprite;
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

import javax.microedition.khronos.opengles.GL10;

/**
 * Created by Dimitri on 12/03/14.
 */
public class ReplayScene extends BaseScene implements IOnSceneTouchListener, PinchZoomDetector.IPinchZoomDetectorListener, ScrollDetector.IScrollDetectorListener {

    private Scene gameScene;
    private PinchZoomDetector pinchZoomDetector;
    private final float MIN_ZOOM_FACTOR = 1f;
    private final float MAX_ZOOM_FACTOR = 3f;
    private float zoomFactor;
    private SurfaceScrollDetector scrollDetector;
    private Map<String, TiledSprite> planetSprites;

    private List<Sprite> shipSprites;
    private List<Sprite> colonySprites;

    @Override
    public void createScene() {
        gameScene = this;

        shipSprites = new ArrayList<Sprite>();
        colonySprites = new ArrayList<Sprite>();

        //Draw the game
        createBackground();

        drawLines();
        drawPlanets();
        drawRevisions();

        //Detect touch controls
        this.setOnSceneTouchListener(this);
        scrollDetector = new SurfaceScrollDetector(this);
        pinchZoomDetector = new PinchZoomDetector(this);
        pinchZoomDetector.setEnabled(true);
        this.setOnSceneTouchListener(this);
        this.setTouchAreaBindingOnActionMoveEnabled(true);


    }

    @Override
    public void onBackKeyPressed() {
        SceneManager.getInstance().loadLoadingScene();
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
    }

    private void drawPlanets() {
        TiledSprite planetSprite;
        planetSprites = new HashMap<String, TiledSprite>();

        for (Planet planet : activity.spaceCrackMap.planets) {
            planetSprite = createTiledSprite((planet.x * GameActivity.SCALE_X) - (resourcesManager.planetRegion.getWidth() / 2), (planet.y * GameActivity.SCALE_Y) - (resourcesManager.planetRegion.getHeight() / 2), resourcesManager.planetRegion, vbom, 0);
            planetSprite.setX((planet.x * GameActivity.SCALE_X) - (resourcesManager.planetRegion.getWidth() / 2));
            planetSprite.setY((planet.y * GameActivity.SCALE_Y) - (resourcesManager.planetRegion.getHeight() / 2));
            planetSprite.setCurrentTileIndex(0);
            planetSprite.setScale(0.6f);
            planetSprite.setCullingEnabled(true);
            planetSprites.put(planet.name, planetSprite);
            attachChild(planetSprite);
        }
    }

    private void drawLines() {
        Line line;
        for (Planet planet : activity.spaceCrackMap.planets) {
            for (Planet connect : planet.connectedPlanets) {
                line = new Line(planet.x * GameActivity.SCALE_X, planet.y * GameActivity.SCALE_Y, connect.x * GameActivity.SCALE_X, connect.y * GameActivity.SCALE_Y, vbom);

                line.setLineWidth(3);
                line.setColor(0.6f, 0.6f, 0.6f);
                attachChild(line);
            }
        }
    }

    private void drawRevisions() {
        TimerHandler timerHandler = new TimerHandler(2f, true, new ITimerCallback() {
            int counter = 0;
            @Override
            public void onTimePassed(TimerHandler pTimerHandler) {
                if (counter < activity.revisions.size() - 1) {
                    new GetGameRevision().execute(SpaceCrackApplication.URL_REPLAY + "/" + activity.gameId + "/" + activity.revisions.get(counter));
                }
                counter++;
            }
        });
        this.registerUpdateHandler(timerHandler);
    }

    private void drawPlayers(Game game) {
        drawColonies(game.player1, 0);
        drawColonies(game.player2, 1);
        drawShips(game.player1, 0);
        drawShips(game.player2, 1);
        if (game.loserPlayerId > 0) {
            endGame(game);
        }
    }

    private void drawColonies(Player player, int index) {
        TiledSprite colonySprite;
        Sprite miniSpaceshipSprite;

        if (player.colonies != null) {
            for (Colony colony : player.colonies) {
                Sprite planetSprite = planetSprites.get(colony.planetName);

                colonySprite = new TiledSprite(-2, -25, resourcesManager.castleRegion, vbom)
                {
                    @Override
                    protected void preDraw(GLState glState, Camera camera1) {
                        super.preDraw(glState, camera1);
                        glState.enableDither();
                    }
                };
                colonySprite.setCullingEnabled(true);
                colonySprite.setCurrentTileIndex(index);

                colonySprites.add(colonySprite);
                planetSprite.attachChild(colonySprite);
            }
        }
    }

    private void drawShips(Player player, int index) {
        TiledSprite shipSprite;
        Text txtStrength;

        if (player.ships != null) {
            for (Ship ship : player.ships) {
                final Planet planet = activity.planets.get(ship.planetName);
                final Ship finalShip = ship;
                shipSprite = createTiledSprite((planet.x * GameActivity.SCALE_X) - 10, (planet.y * GameActivity.SCALE_Y) - 10, resourcesManager.spaceshipRegion, vbom, 0);
                shipSprite.setScale(0.5f);
                shipSprite.setCullingEnabled(true);

                //Ship strength
                txtStrength = new Text(50, -10, resourcesManager.font, "0123456789", new TextOptions(HorizontalAlign.LEFT), vbom);
                txtStrength.setText(String.valueOf(ship.strength));
                shipSprite.attachChild(txtStrength);

                shipSprites.add(shipSprite);
                attachChild(shipSprite);
            }
        }
    }

    private void resetPlayers(final Game game) {
        activity.runOnUpdateThread(new Runnable() {
            @Override
            public void run() {
                for (Sprite sprite : shipSprites) {
                    gameScene.unregisterTouchArea(sprite);
                    sprite.clearEntityModifiers();
                    sprite.clearUpdateHandlers();
                    sprite.dispose();
                    detachChild(sprite);
                }
                for (Sprite sprite : colonySprites) {
                    gameScene.unregisterTouchArea(sprite);
                    sprite.clearEntityModifiers();
                    sprite.clearUpdateHandlers();
                    sprite.dispose();
                    detachChild(sprite);
                }
                shipSprites.clear();
                colonySprites.clear();
                drawPlayers(game);
            }
        });
    }

    private void endGame(final Game game) {
        camera.setHUD(null);
        camera.setCenterDirect(GameActivity.CAMERA_WIDTH / 2, GameActivity.CAMERA_HEIGHT / 2);
        camera.setZoomFactor(1f);

        activity.runOnUpdateThread(new Runnable() {
            @Override
            public void run() {
                for (Sprite sprite : shipSprites) {
                    gameScene.unregisterTouchArea(sprite);
                    sprite.clearEntityModifiers();
                    sprite.clearUpdateHandlers();
                }
                for (Sprite sprite : colonySprites) {
                    gameScene.unregisterTouchArea(sprite);
                    sprite.clearEntityModifiers();
                    sprite.clearUpdateHandlers();
                }
            }
        });

        Rectangle endScreen = new Rectangle((activity.CAMERA_WIDTH / 2) - 200, (activity.CAMERA_HEIGHT / 2) - 100, 400, 200, vbom);
        endScreen.setColor(0.24f, 0.27f, 0.3f);
        endScreen.setBlendFunction(GL10.GL_SRC_ALPHA, GL10.GL_ONE_MINUS_SRC_ALPHA);
        endScreen.setAlpha(0.2f);

        Sprite monkey = new Sprite(((endScreen.getWidth() - resourcesManager.monkeyRegion.getWidth())/2),  endScreen.getHeight() / 2 - 60, resourcesManager.monkeyRegion, vbom);
        monkey.setScale(1.5f);
        Text endStatus;
        if (game.loserPlayerId == activity.player.playerId) {
            endStatus = new Text(0, 0, resourcesManager.font, activity.getText(R.string.you_lost), vbom);
        } else {
            endStatus = new Text(0, 0, resourcesManager.font, activity.getText(R.string.you_won), vbom);
        }
        endStatus.setPosition((endScreen.getWidth() - endStatus.getWidth()) / 2, (endScreen.getHeight() - endStatus.getHeight()) / 2);
        endScreen.attachChild(endStatus);
        endScreen.attachChild(monkey);
        this.attachChild(endScreen);
    }

    @Override
    public boolean onSceneTouchEvent(Scene pScene, TouchEvent pSceneTouchEvent) {

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

    private class GetGameRevision extends AsyncTask<String, Void, String> {
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
                    Game game = gson.fromJson(result, Game.class);
                    resetPlayers(game);
                } catch (JsonParseException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}