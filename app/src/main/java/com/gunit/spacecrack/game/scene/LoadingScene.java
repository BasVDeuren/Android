package com.gunit.spacecrack.game.scene;

import org.andengine.entity.scene.background.Background;
import org.andengine.entity.text.Text;
import org.andengine.util.color.Color;

/**
 * Created by Dimitri on 24/02/14.
 */
public class LoadingScene extends BaseScene {

    @Override
    public void createScene() {
        setBackground(new Background(Color.BLACK));
        attachChild(new Text(400, 240, resourcesManager.font, "Loading...", vbom));
    }

    @Override
    public void onBackKeyPressed() {
        return;
    }

    @Override
    public SceneManager.SceneType getSceneType() {
        return SceneManager.SceneType.SCENE_LOADING;
    }

    @Override
    public void disposeScene() {

    }
}
