package com.gunit.spacecrack.game.scene;

import com.gunit.spacecrack.game.manager.SceneManager;

import org.andengine.engine.camera.Camera;
import org.andengine.entity.sprite.Sprite;
import org.andengine.opengl.util.GLState;

/**
 * Created by Dimitri on 24/02/14.
 */
public class SplashScene extends BaseScene {

    private Sprite splash;

    @Override
    public void createScene() {
        splash = new Sprite(0, 0, resourcesManager.splashRegion, vbom)
        {
            @Override
            protected void preDraw(GLState pGLState, Camera pCamera)
            {
                super.preDraw(pGLState, pCamera);
                pGLState.enableDither();
            }
        };

        splash.setScale(1.5f);
        splash.setPosition(400, 240);
        attachChild(splash);
    }

    @Override
    public void onBackKeyPressed() {

    }

    @Override
    public SceneManager.SceneType getSceneType() {
        return SceneManager.SceneType.SCENE_SPLASH;
    }

    @Override
    public void disposeScene() {
        splash.detachSelf();
        splash.dispose();
        this.detachSelf();
        this.dispose();
    }
}
