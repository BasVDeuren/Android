package com.gunit.spacecrack.game.scene;

import com.gunit.spacecrack.game.manager.ResourcesManager;
import com.gunit.spacecrack.game.manager.SceneManager;

import org.andengine.entity.text.Text;
import org.andengine.opengl.font.Font;

/**
 * Created by Dimitri on 24/02/14.
 */
public class SplashScene extends BaseScene {

    private Text spaceCrackText;
    private String spaceCrack;

    @Override
    public void createScene() {
//        splash = new Sprite(0, 0, resourcesManager.splashRegion, vbom)
//        {
//            @Override
//            protected void preDraw(GLState pGLState, Camera pCamera)
//            {
//                super.preDraw(pGLState, pCamera);
//                pGLState.enableDither();
//            }
//        };
//
//        splash.setScale(1.5f);
//        splash.setPosition(400, 240);
//        attachChild(splash);
//        Font font = ResourcesManager.getInstance().font;
//        float x = GameActivity.CAMERA_WIDTH / 2 - FontUtils.measureText(font, spaceCrack) / 2;
//        float y = GameActivity.CAMERA_HEIGHT / 2 - font.getLineHeight() / 2;

//        spaceCrackText = new Text(400, 200, font, "Space Crack", vbom);
//        attachChild(spaceCrackText);
    }

    @Override
    public void onBackKeyPressed() {
        return;
    }

    @Override
    public SceneManager.SceneType getSceneType() {
        return SceneManager.SceneType.SCENE_SPLASH;
    }

    @Override
    public void disposeScene() {
        ResourcesManager.getInstance().unloadSplashScreenResources();
        this.detachSelf();
        this.dispose();
    }
}
