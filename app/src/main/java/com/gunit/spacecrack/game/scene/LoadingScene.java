package com.gunit.spacecrack.game.scene;

import com.gunit.spacecrack.R;
import com.gunit.spacecrack.game.manager.SceneManager;

import org.andengine.entity.scene.background.Background;
import org.andengine.entity.text.Text;
import org.andengine.util.color.Color;

/**
 * Created by Dimitri on 24/02/14.
 */

/**
 * LoadingScene used while loading the resources for the Game
 */
public class LoadingScene extends BaseScene {

    @Override
    public void createScene() {
        setBackground(new Background(Color.BLACK));
        Text loading = new Text(0, 0, resourcesManager.font, activity.getText(R.string.loading), vbom);
        loading.setPosition(((activity.CAMERA_WIDTH - loading.getWidth()) / 2), (activity.CAMERA_HEIGHT - loading.getHeight()) / 2);
        attachChild(new Text(400, 240, resourcesManager.font, "Loading...", vbom));
    }

    /**
     * Do nothing
     */
    @Override
    public void onBackKeyPressed() {
        return;
    }

    @Override
    public void disposeScene() {
        detachChildren();
    }
}
