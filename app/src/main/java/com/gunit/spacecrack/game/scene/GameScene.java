package com.gunit.spacecrack.game.scene;

import com.gunit.spacecrack.game.GameActivity;

import org.andengine.engine.camera.Camera;
import org.andengine.engine.camera.hud.HUD;
import org.andengine.entity.scene.background.Background;
import org.andengine.entity.scene.background.SpriteBackground;
import org.andengine.entity.sprite.Sprite;
import org.andengine.entity.text.Text;
import org.andengine.entity.text.TextOptions;
import org.andengine.opengl.util.GLState;
import org.andengine.util.HorizontalAlign;
import org.andengine.util.color.Color;

/**
 * Created by Dimitri on 24/02/14.
 */
public class GameScene extends BaseScene {

    private HUD gameHUD;
    private Text scoreText;
    private int score;

    @Override
    public void createScene() {
        createBackground();
        createHUD();
        createPlanets();
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
        camera.setCenter(GameActivity.CAMERA_WIDTH/2, GameActivity.CAMERA_HEIGHT/2);
        this.detachSelf();
        this.dispose();
    }

    private void createBackground() {
//        attachChild(new Sprite(0, 0, resourcesManager.gameBackgroundRegion, vbom) {
//            @Override
//            protected void preDraw(GLState pGLState, Camera pCamera) {
//                super.preDraw(pGLState, pCamera);
//                pGLState.enableDither();
//            }
//        });
        this.setBackground(new Background(Color.BLACK));
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
}
