package com.gunit.spacecrack.game.manager;

import com.gunit.spacecrack.game.scene.BaseScene;
import com.gunit.spacecrack.game.scene.GameScene;
import com.gunit.spacecrack.game.scene.LoadingScene;
import com.gunit.spacecrack.game.scene.ReplayScene;

import org.andengine.engine.Engine;
import org.andengine.engine.handler.timer.ITimerCallback;
import org.andengine.engine.handler.timer.TimerHandler;
import org.andengine.ui.IGameInterface;

/**
 * Created by Dimitri on 24/02/14.
 */

/**
 * SceneManager will take care to display the correct Scene
 */
public class SceneManager {

    private BaseScene gameScene;
    private BaseScene replayScene;
    private BaseScene loadingScene;

    private static final SceneManager INSTANCE = new SceneManager();

    private BaseScene currentScene;

    private Engine engine = ResourcesManager.getInstance().engine;

    public static SceneManager getInstance()
    {
        return INSTANCE;
    }

    public BaseScene getCurrentScene()
    {
        return currentScene;
    }

    public void setScene(BaseScene scene)
    {
        if (currentScene != null) {
            //Unload the current scene from the memory
            currentScene.disposeScene();
        }
        engine.setScene(scene);
        currentScene = scene;
    }

    public void createLoadingScene(IGameInterface.OnCreateSceneCallback pOnCreateSceneCallback) {
        loadingScene = new LoadingScene();
        SceneManager.getInstance().setScene(loadingScene);
        pOnCreateSceneCallback.onCreateSceneFinished(loadingScene);
    }

    public void loadGameScene(final Engine engine) {
        loadingScene = new LoadingScene();
        setScene(loadingScene);
        engine.registerUpdateHandler(new TimerHandler(0.1f, new ITimerCallback() {
            @Override
            public void onTimePassed(TimerHandler pTimerHandler) {
                engine.unregisterUpdateHandler(pTimerHandler);
                ResourcesManager.getInstance().loadGameResources();
                gameScene = new GameScene();
                setScene(gameScene);
            }
        }));
    }

    public void loadReplayScene(final Engine engine) {
        loadingScene = new LoadingScene();
        setScene(loadingScene);
        engine.registerUpdateHandler(new TimerHandler(0.1f, new ITimerCallback() {
            @Override
            public void onTimePassed(TimerHandler pTimerHandler) {
                engine.unregisterUpdateHandler(pTimerHandler);
                ResourcesManager.getInstance().loadGameResources();
                replayScene = new ReplayScene();
                setScene(replayScene);
            }
        }));
    }
}
