package com.gunit.spacecrack.game.scene;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.facebook.FacebookException;
import com.facebook.Session;
import com.facebook.widget.WebDialog;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.ValueEventListener;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.gunit.spacecrack.R;
import com.gunit.spacecrack.activity.HomeActivity;
import com.gunit.spacecrack.application.SpaceCrackApplication;
import com.gunit.spacecrack.chat.ChatActivity;
import com.gunit.spacecrack.game.GameActivity;
import com.gunit.spacecrack.game.manager.ResourcesManager;
import com.gunit.spacecrack.json.Action;
import com.gunit.spacecrack.json.ColonyViewModel;
import com.gunit.spacecrack.json.GameViewModel;
import com.gunit.spacecrack.json.PlayerViewModel;
import com.gunit.spacecrack.json.ShipViewModel;
import com.gunit.spacecrack.model.Planet;
import com.gunit.spacecrack.restservice.RestService;

import org.andengine.engine.camera.Camera;
import org.andengine.engine.camera.hud.HUD;
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
import org.andengine.opengl.texture.region.ITextureRegion;
import org.andengine.opengl.util.GLState;
import org.andengine.util.HorizontalAlign;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.microedition.khronos.opengles.GL10;

/**
 * Created by Dimitri on 24/02/14.
 */

/**
 * GameScene will display an active game, and allows user input on the game itself
 */
public class GameScene extends BaseScene implements IOnSceneTouchListener, PinchZoomDetector.IPinchZoomDetectorListener, ScrollDetector.IScrollDetectorListener {

    private Scene gameScene;
    private HUD gameHUD;
    private Text txtCommandPlayer1;
    private Text txtCommandPlayer2;
    private Text txtStatePlayer1;
    private Text txtStatePlayer2;
    private PinchZoomDetector pinchZoomDetector;
    private final float MIN_ZOOM_FACTOR = 1f;
    private final float MAX_ZOOM_FACTOR = 3f;
    private final int MOVECOST = 1;
    private final int COLONIZECOST = 2;
    private final int SHIPCOST = 3;
    private float zoomFactor;
    private SurfaceScrollDetector scrollDetector;
    private Map<String, TiledSprite> planetSprites;

    private List<Sprite> shipSprites;
    private List<Sprite> colonySprites;

    private Rectangle exitScene;
    private boolean backPressed = true;
    private boolean dragging = false;

    private ButtonSprite btnEndTurn;

    private Firebase ref;

    @Override
    public void createScene() {
        gameScene = this;

        shipSprites = new ArrayList<Sprite>();
        colonySprites = new ArrayList<Sprite>();

        //Register Firebase listener
        registerFirebase();

        //Draw the game
        createBackground();
        createHUD();

        Log.d("Engine runnning", String.valueOf(engine.isRunning()));

        drawLines();
        drawPlanets();
        drawPlayers(activity.gameActivePlayerWrapper.game);

        //Detect touch controls
        this.setOnSceneTouchListener(this);
        scrollDetector = new SurfaceScrollDetector(this);
        pinchZoomDetector = new PinchZoomDetector(this);
        pinchZoomDetector.setEnabled(true);
        this.setOnSceneTouchListener(this);
        this.setTouchAreaBindingOnActionMoveEnabled(true);

    }

    /**
     * Displays an overlay asking for the confirmation of the user
     */
    @Override
    public void onBackKeyPressed() {
        camera.setCenterDirect(GameActivity.CAMERA_WIDTH / 2, GameActivity.CAMERA_HEIGHT / 2);
        camera.setZoomFactor(1f);
        if (backPressed) {
            //Create the overlay and show it
            exitScene = new Rectangle((activity.CAMERA_WIDTH / 2) - 200, (activity.CAMERA_HEIGHT / 2) - 100, 400, 200, vbom);
            exitScene.setColor(0.24f, 0.27f, 0.3f);
            exitScene.setBlendFunction(GL10.GL_SRC_ALPHA, GL10.GL_ONE_MINUS_SRC_ALPHA);
            exitScene.setAlpha(0.8f);

            Text text = new Text(0, 0, resourcesManager.font, activity.getText(R.string.close_game), vbom);
            text.setPosition(((exitScene.getWidth() - text.getWidth()) / 2), (exitScene.getHeight() - text.getHeight()) / 2);
            exitScene.attachChild(text);

            ButtonSprite btnConfirm = new ButtonSprite(0, 0, resourcesManager.turnRegion, vbom);
            btnConfirm.setPosition(((exitScene.getWidth() - btnConfirm.getWidth()) / 2) - 50, exitScene.getHeight() - 50);
            btnConfirm.setOnClickListener(new ButtonSprite.OnClickListener() {
                @Override
                public void onClick(ButtonSprite pButtonSprite, float pTouchAreaLocalX, float pTouchAreaLocalY) {
                    Intent intent = new Intent(activity, HomeActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    intent.putExtra("EXIT", true);
                    activity.startActivity(intent);
                    activity.finish();
                }
            });
            ButtonSprite btnCancel = new ButtonSprite(0, 0, resourcesManager.cancelRegion, vbom);
            btnCancel.setPosition(((exitScene.getWidth() - btnCancel.getWidth()) / 2) + 50, exitScene.getHeight() - 50);
            btnCancel.setOnClickListener(new ButtonSprite.OnClickListener() {
                @Override
                public void onClick(ButtonSprite pButtonSprite, float pTouchAreaLocalX, float pTouchAreaLocalY) {
                    activity.runOnUpdateThread(new Runnable() {
                        @Override
                        public void run() {
                            if (!exitScene.isDisposed()) {
                                exitScene.dispose();
                            }
                            gameScene.detachChild(exitScene);
                        }
                    });
                }
            });

            this.registerTouchArea(btnConfirm);
            this.registerTouchArea(btnCancel);
            exitScene.attachChild(btnConfirm);
            exitScene.attachChild(btnCancel);
            this.attachChild(exitScene);
        } else {
            //Hide the overlay
            activity.runOnUpdateThread(new Runnable() {
                @Override
                public void run() {
                    if (!exitScene.isDisposed()) {
                        exitScene.dispose();
                    }
                    gameScene.detachChild(exitScene);
                }
            });
        }

        backPressed = !backPressed;
    }

    /**
     * Unload the resources and reset the camera
     */
    @Override
    public void disposeScene() {
        camera.setHUD(null);
        camera.setCenterDirect(GameActivity.CAMERA_WIDTH / 2, GameActivity.CAMERA_HEIGHT / 2);
        camera.setZoomFactor(1f);
        ResourcesManager.getInstance().unloadGameResources();
        this.detachSelf();
        this.dispose();
    }

    private void registerFirebase() {
        ref = new Firebase(activity.gameActivePlayerWrapper.firebaseGameURL);
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue() != null) {
                    Log.i("DataSnapshot", dataSnapshot.getValue().toString());
                    Gson gson = new Gson();
                    try {
                        GameViewModel game = gson.fromJson(dataSnapshot.getValue().toString(), GameViewModel.class);
                        if (game != null) {
                            activity.gameActivePlayerWrapper.game = game;
                            if (activity.gameActivePlayerWrapper.activePlayerId == game.player1.playerId) {
                                activity.player = game.player1;
                            } else {
                                activity.player = game.player2;
                            }
                            resetPlayers();
                        }
                    } catch (JsonSyntaxException e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onCancelled() {

            }
        });
    }

    private void createBackground() {
        Sprite background = createSprite(0, 0, resourcesManager.gameBackgroundRegion, vbom);
        attachChild(background);
    }

    private void createHUD() {
        gameHUD = new HUD();

        txtCommandPlayer1 = new Text(20, 20, resourcesManager.font, "Commandpoints: 0123456789", new TextOptions(HorizontalAlign.LEFT), vbom);
        txtCommandPlayer1.setText("Commandpoints: " + activity.player.commandPoints);
        gameHUD.attachChild(txtCommandPlayer1);
        txtCommandPlayer2 = new Text(0, 0, resourcesManager.font, "Commandpoints: 0123456789", new TextOptions(HorizontalAlign.RIGHT), vbom);
        txtCommandPlayer2.setPosition(GameActivity.CAMERA_WIDTH - txtCommandPlayer2.getWidth() - 20, 20);
        txtCommandPlayer2.setText("Commandpoints: " + activity.player.commandPoints);
        gameHUD.attachChild(txtCommandPlayer2);
        txtStatePlayer1 = new Text(20, 50, resourcesManager.font, "State: busy", new TextOptions(HorizontalAlign.LEFT), vbom);
        gameHUD.attachChild(txtStatePlayer1);
        txtStatePlayer2 = new Text(0, 0, resourcesManager.font, "State: busy", new TextOptions(HorizontalAlign.LEFT), vbom);
        txtStatePlayer2.setPosition(GameActivity.CAMERA_WIDTH - txtStatePlayer2.getWidth() - 20, 50);
        gameHUD.attachChild(txtStatePlayer2);
        ButtonSprite btnChat = new ButtonSprite(20, GameActivity.CAMERA_HEIGHT - 70, resourcesManager.chatRegion, resourcesManager.chatPressedRegion,vbom)
        {
            @Override
            public boolean onAreaTouched(TouchEvent pSceneTouchEvent, float pTouchAreaLocalX, float pTouchAreaLocalY) {
                if (pSceneTouchEvent.isActionUp()) {
                    Intent intent = new Intent(activity, ChatActivity.class);
                    intent.putExtra("gameId", String.valueOf(activity.gameActivePlayerWrapper.game.gameId));
                    intent.putExtra("username", SpaceCrackApplication.user.username);
                    activity.startActivity(intent);
                }
                return true;
            }
        };
        gameHUD.registerTouchArea(btnChat);
        gameHUD.attachChild(btnChat);
        btnEndTurn = new ButtonSprite(90, GameActivity.CAMERA_HEIGHT - 70, resourcesManager.turnRegion, resourcesManager.turnPressedRegion,vbom)
        {
            @Override
            public boolean onAreaTouched(TouchEvent pSceneTouchEvent, float pTouchAreaLocalX, float pTouchAreaLocalY) {
                if (pSceneTouchEvent.isActionUp() && !activity.player.turnEnded) {
                    new ActionTask(new Action("ENDTURN", activity.gameActivePlayerWrapper.game.gameId, activity.gameActivePlayerWrapper.activePlayerId), null, null).execute(SpaceCrackApplication.URL_ACTION);
                    this.setVisible(false);
                }
                return true;
            }
        };
        if (activity.player.turnEnded) {
            btnEndTurn.setVisible(false);
        }
        gameHUD.registerTouchArea(btnEndTurn);
        gameHUD.attachChild(btnEndTurn);
        camera.setHUD(gameHUD);
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
            this.attachChild(planetSprite);
        }
    }

    private void drawLines() {
        Line line;
        for (Planet planet : activity.spaceCrackMap.planets) {
            for (Planet connect : planet.connectedPlanets) {
                line = new Line(planet.x * GameActivity.SCALE_X, planet.y * GameActivity.SCALE_Y, connect.x * GameActivity.SCALE_X, connect.y * GameActivity.SCALE_Y, vbom);

                line.setLineWidth(3);
                line.setColor(0.6f, 0.6f, 0.6f);
                this.attachChild(line);
            }
        }
    }

    private void drawPlayers(GameViewModel game) {
        drawColonies(game.player1, 0);
        drawColonies(game.player2, 1);
        drawShips(game.player1, 0);
        drawShips(game.player2, 1);
        updateHud();
        if (!activity.player.turnEnded && !btnEndTurn.isVisible()) {
            btnEndTurn.setVisible(true);
        }
        if (game.loserPlayerId > 0) {
            endGame(game);
        }
    }

    private void drawColonies(PlayerViewModel player, int index) {
        TiledSprite colonySprite;
        Sprite miniSpaceshipSprite;

        if (player.colonies != null) {
            for (ColonyViewModel colony : player.colonies) {
                Planet planet = activity.planets.get(colony.planetName);
                Sprite planetSprite = planetSprites.get(colony.planetName);

                //Mini spaceship
                miniSpaceshipSprite = drawMiniShip(index, colony);
                if (player.playerId == activity.gameActivePlayerWrapper.activePlayerId) {
                    registerTouchArea(miniSpaceshipSprite);
                }

                final Sprite finalMiniSpaceshipSprite = miniSpaceshipSprite;
                colonySprite = new TiledSprite(-2, -25, resourcesManager.castleRegion, vbom)
                {
                    @Override
                    protected void preDraw(GLState glState, Camera camera1) {
                        super.preDraw(glState, camera1);
                        glState.enableDither();
                    }

                    @Override
                    public boolean onAreaTouched(TouchEvent pSceneTouchEvent, float pTouchAreaLocalX, float pTouchAreaLocalY) {
                        if (!dragging) {
                            switch (pSceneTouchEvent.getAction()) {
                                case TouchEvent.ACTION_UP:
                                    finalMiniSpaceshipSprite.setVisible(!finalMiniSpaceshipSprite.isVisible());
                            }
                            return true;
                        }
                        return false;
                    }
                };
                colonySprite.setCullingEnabled(true);
                colonySprite.setCurrentTileIndex(index);
                if (player.playerId == activity.gameActivePlayerWrapper.activePlayerId) {
                    registerTouchArea(colonySprite);
                }


                colonySprite.attachChild(miniSpaceshipSprite);
                colonySprites.add(colonySprite);
                planetSprite.attachChild(colonySprite);
            }
        }
    }

    private Sprite drawMiniShip(int index, final ColonyViewModel colony) {
        final Sprite miniSpaceshipSprite;
        ITextureRegion miniSpaceShip;
        if (index == 0) {
            miniSpaceShip = resourcesManager.miniSpaceshipPlayer1Region;
        } else {
            miniSpaceShip = resourcesManager.miniSpaceshipPLayer2Region;
        }
        miniSpaceshipSprite = new Sprite(-20, -30, miniSpaceShip, vbom)
        {
            @Override
            protected void preDraw(GLState glState, Camera camera1) {
                super.preDraw(glState, camera1);
                glState.enableDither();
            }

            @Override
            public boolean onAreaTouched(TouchEvent pSceneTouchEvent, float pTouchAreaLocalX, float pTouchAreaLocalY) {
                if (!dragging) {
                    switch (pSceneTouchEvent.getAction()) {
                        case TouchEvent.ACTION_UP:
                            if (!activity.player.turnEnded) {
                                if (activity.player.commandPoints >= SHIPCOST) {
                                    new ActionTask(new Action("BUILDSHIP", colony, activity.gameActivePlayerWrapper.game.gameId, activity.player.playerId), null, null).execute(SpaceCrackApplication.URL_ACTION);
                                    this.setVisible(false);
                                }
                            }
                    }
                    return true;
                }
                return false;
            }
        };
        miniSpaceshipSprite.setVisible(false);
        miniSpaceshipSprite.setCullingEnabled(true);
        return miniSpaceshipSprite;
    }

    private void drawShips(PlayerViewModel player, int index) {
        TiledSprite shipSprite;
        Text txtStrength;

        if (player.ships != null) {
            for (ShipViewModel ship : player.ships) {
                final Planet planet = activity.planets.get(ship.planetName);
                final ShipViewModel finalShip = ship;
                shipSprite = new TiledSprite((planet.x * GameActivity.SCALE_X) - 10, (planet.y * GameActivity.SCALE_Y) - 10, resourcesManager.spaceshipRegion, vbom)
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
                                dragging = true;
                                break;
                            case TouchEvent.ACTION_MOVE:
                                this.setPosition(pSceneTouchEvent.getX() - this.getWidth() / 2, pSceneTouchEvent.getY() - this.getHeight() / 2);
                                break;
                            case TouchEvent.ACTION_UP:
                                this.setScale(0.5f);
                                Planet newPlanet = checkValidPosition(this, finalShip.planetName);
                                if (newPlanet != null) {
                                    if (!activity.player.turnEnded) {
                                        if (checkCommandPoints(newPlanet.name)) {
                                            placeShip(this, planet, newPlanet, finalShip);

                                        } else {
                                            activity.runOnUiThread(new Runnable() {
                                                @Override
                                                public void run() {
                                                    Toast.makeText(activity, activity.getResources().getString(R.string.commandpoints_fail), Toast.LENGTH_SHORT).show();
                                                }
                                            });
                                            this.setPosition((planet.x * GameActivity.SCALE_X) - 10, (planet.y * GameActivity.SCALE_Y) - 10);
                                        }
                                    } else {
                                        activity.runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                Toast.makeText(activity, activity.getResources().getString(R.string.turn_ended), Toast.LENGTH_SHORT).show();
                                            }
                                        });
                                        this.setPosition((planet.x * GameActivity.SCALE_X) - 10, (planet.y * GameActivity.SCALE_Y) - 10);
                                    }
                                } else {
                                    this.setPosition((planet.x * GameActivity.SCALE_X) - 10, (planet.y * GameActivity.SCALE_Y) - 10);
                                }
                                highlightConnectedPlantes(finalShip.planetName, 0);
                                dragging = false;
                                break;
                        }
                        return true;
                    }
                };
                shipSprite.setCurrentTileIndex(index);
                shipSprite.setScale(0.5f);
                shipSprite.setCullingEnabled(true);
                if (player.playerId == activity.gameActivePlayerWrapper.activePlayerId) {
                    registerTouchArea(shipSprite);
                }
                setTouchAreaBindingOnActionMoveEnabled(true);

                //Ship strength
                txtStrength = new Text(50, -10, resourcesManager.font, "0123456789", new TextOptions(HorizontalAlign.LEFT), vbom);
                txtStrength.setText(String.valueOf(ship.strength));
                shipSprite.attachChild(txtStrength);

                shipSprites.add(shipSprite);
                attachChild(shipSprite);
            }
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
        return (colony && activity.player.commandPoints >= MOVECOST) || (!colony && activity.player.commandPoints >= COLONIZECOST);
    }

    private void placeShip(Sprite shipSprite, Planet oldPlanet, Planet newPlanet, ShipViewModel ship) {
        new ActionTask(new Action("MOVESHIP", newPlanet.name, activity.gameActivePlayerWrapper.game.gameId, activity.gameActivePlayerWrapper.activePlayerId, ship), shipSprite, oldPlanet).execute(SpaceCrackApplication.URL_ACTION);
    }

    /**
     * After each move, redraw the game with the new state
     */
    private void resetPlayers() {
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
                drawPlayers(activity.gameActivePlayerWrapper.game);
            }
        });
    }

    private void updateHud()
    {
        txtCommandPlayer1.setText(activity.getString(R.string.commandpoints) + ": " + activity.gameActivePlayerWrapper.game.player1.commandPoints);
        txtCommandPlayer2.setText(activity.getString(R.string.commandpoints) + ": " + activity.gameActivePlayerWrapper.game.player2.commandPoints);
        txtCommandPlayer2.setPosition(GameActivity.CAMERA_WIDTH - txtCommandPlayer2.getWidth() - 20, 20);
        if (activity.gameActivePlayerWrapper.game.player1.turnEnded) {
            txtStatePlayer1.setText(activity.getString(R.string.ended));
        } else {
            txtStatePlayer1.setText(activity.getString(R.string.busy));
        }
        if (activity.gameActivePlayerWrapper.game.player2.turnEnded) {
            txtStatePlayer2.setText(activity.getString(R.string.ended));
        } else {
            txtStatePlayer2.setText(activity.getString(R.string.busy));
        }
        txtStatePlayer2.setPosition(GameActivity.CAMERA_WIDTH - txtStatePlayer2.getWidth() - 20, 50);
    }

    /**
     * End of the game, overlay will be created to informate the user about the winner
     * @param game
     */
    private void endGame(final GameViewModel game) {
        camera.setHUD(null);
        camera.setCenterDirect(GameActivity.CAMERA_WIDTH / 2, GameActivity.CAMERA_HEIGHT / 2);
        camera.setZoomFactor(1f);

        //Make sure te user can't control the elements of the game anymore
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

        //Create the overlay
        drawEndOverlay(game);
    }

    private void drawEndOverlay(final GameViewModel game) {
        Rectangle endScreen = new Rectangle((activity.CAMERA_WIDTH / 2) - 200, (activity.CAMERA_HEIGHT / 2) - 100, 400, 200, vbom);
        endScreen.setColor(0.24f, 0.27f, 0.3f);
        endScreen.setBlendFunction(GL10.GL_SRC_ALPHA, GL10.GL_ONE_MINUS_SRC_ALPHA);
        endScreen.setAlpha(0.8f);

        if (Session.getActiveSession() != null) {
            ButtonSprite facebook = new ButtonSprite(((endScreen.getWidth() / 2) - 75), (endScreen.getHeight() / 2 + 30), resourcesManager.facebookRegion, resourcesManager.facebookPressedRegion, vbom);
            facebook.setOnClickListener(new ButtonSprite.OnClickListener() {
                @Override
                public void onClick(ButtonSprite pButtonSprite, float pTouchAreaLocalX, float pTouchAreaLocalY) {
                    activity.runOnUiThread(new Runnable() {
                        public void run() {
                            Bundle params = new Bundle();
                            params.putString("name", activity.getString(R.string.app_name));
                            if (game.loserPlayerId == activity.player.playerId) {
                                params.putString("caption", activity.getString(R.string.defeated));
                                params.putString("description", activity.getString(R.string.lost_all_planets));
                            } else {
                                params.putString("caption", activity.getString(R.string.victory));
                                params.putString("description", activity.getString(R.string.captured_all_planets));
                            }
                            params.putString("picture", "http://lunar.lostgarden.com/uploaded_images/SpaceCrack2D-Mockup2-716382.jpg");

                            WebDialog feedDialog = (new WebDialog.FeedDialogBuilder(activity,
                                    Session.getActiveSession(), params))
                                    .setOnCompleteListener(new WebDialog.OnCompleteListener() {
                                        @Override
                                        public void onComplete(Bundle values, FacebookException error) {

                                        }
                                    }).build();
                            feedDialog.show();
                        }
                    });
                }
            });
            endScreen.attachChild(facebook);
            this.registerTouchArea(facebook);
        }

        ButtonSprite quit = new ButtonSprite(((endScreen.getWidth() / 2) + 25), (endScreen.getHeight() / 2 + 30), resourcesManager.quitRegion, vbom);
        if (Session.getActiveSession() == null) {
            quit.setPosition(((endScreen.getWidth() / 2) - 25), (endScreen.getHeight() / 2 + 30));
        }

        quit.setOnClickListener(new ButtonSprite.OnClickListener() {
            @Override
            public void onClick(ButtonSprite pButtonSprite, float pTouchAreaLocalX, float pTouchAreaLocalY) {
                Intent intent = new Intent(activity, HomeActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                intent.putExtra("EXIT", true);
                activity.startActivity(intent);
                activity.finish();
            }
        });

        Sprite monkey = createSprite(((endScreen.getWidth() - resourcesManager.monkeyRegion.getWidth())/2),  endScreen.getHeight() / 2 - 60, resourcesManager.monkeyRegion, vbom);
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
        endScreen.attachChild(quit);
        this.registerTouchArea(quit);
        this.attachChild(endScreen);
    }

    /**
     * POST resuest with a certain action
     */
    private class ActionTask extends AsyncTask<String, Void, String> {

        private String actionString;
        private Gson gson;
        private Sprite shipSprite;
        private Planet planet;

        public ActionTask (Action action, Sprite shipSprite, Planet planet) {
            super();
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
                if (shipSprite != null && planet !=null) {
                    if (result.equals("406")) {
                        Log.i("GameActivity", "Move is not valid");
                        shipSprite.setPosition((planet.x * GameActivity.SCALE_X) - 10, (planet.y * GameActivity.SCALE_Y) - 10);
                    } else {
                        Log.i("GameActivity", "Move valid");
                    }
                } else {
                    Log.i("Result", result);
                }

            } else {
                Toast.makeText(activity, activity.getString(R.string.something_went_wrong), Toast.LENGTH_SHORT).show();
            }
        }
    }


    /**
     * Register touch events on the Scene
     * @param pScene The {@link Scene} that the {@link TouchEvent} has been dispatched to.
     * @param pSceneTouchEvent The {@link TouchEvent} object containing full information about the event.
     *
     * @return
     */
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

    /**
     * While the user is pinching, zoom in/out on the Scene
     * @param pPinchZoomDetector
     * @param pTouchEvent
     * @param pZoomFactor
     */
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

    /**
     * If the user drags the screen, move the camera towards the desired position
     * @param pScollDetector
     * @param pPointerID
     * @param pDistanceX
     * @param pDistanceY
     */
    @Override
    public void onScroll(ScrollDetector pScollDetector, int pPointerID, float pDistanceX, float pDistanceY) {
        final float zoomFactor = camera.getZoomFactor();
        camera.offsetCenter(-pDistanceX/zoomFactor, -pDistanceY/zoomFactor);
    }

    @Override
    public void onScrollFinished(ScrollDetector pScollDetector, int pPointerID, float pDistanceX, float pDistanceY) {

    }
}
