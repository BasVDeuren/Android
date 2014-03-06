package com.gunit.spacecrack.game.manager;

import com.gunit.spacecrack.game.scene.BaseScene;
import com.gunit.spacecrack.game.scene.GameScene;
import com.gunit.spacecrack.game.scene.LoadingScene;
import com.gunit.spacecrack.game.scene.MainMenuScene;
import com.gunit.spacecrack.game.scene.SplashScene;

import org.andengine.engine.Engine;
import org.andengine.engine.handler.timer.ITimerCallback;
import org.andengine.engine.handler.timer.TimerHandler;
import org.andengine.ui.IGameInterface;

/**
 * Created by Dimitri on 24/02/14.
 */
public class SceneManager {

    private BaseScene splashScene;
    private BaseScene menuScene;
    private BaseScene gameScene;
    private BaseScene loadingScene;

    private static final SceneManager INSTANCE = new SceneManager();

    private SceneType currentSceneType = SceneType.SCENE_SPLASH;

    private BaseScene currentScene;

    private Engine engine = ResourcesManager.getInstance().engine;

    public enum SceneType
    {
        SCENE_SPLASH,
        SCENE_MENU,
        SCENE_GAME,
        SCENE_LOADING,
    }

    public static SceneManager getInstance()
    {
        return INSTANCE;
    }

    public SceneType getCurrentSceneType()
    {
        return currentSceneType;
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
//        currentSceneType = scene.getSceneType();
    }

    public void setScene(SceneType sceneType)
    {
        switch (sceneType)
        {
            case SCENE_MENU:
                setScene(menuScene);
                break;
            case SCENE_GAME:
                setScene(gameScene);
                break;
            case SCENE_SPLASH:
                setScene(splashScene);
                break;
            case SCENE_LOADING:
                setScene(loadingScene);
                break;
            default:
                break;
        }
    }

    public void createSplashScene(IGameInterface.OnCreateSceneCallback pOnCreateSceneCallback) {
        ResourcesManager.getInstance().loadSplashScreenResources();
        splashScene = new SplashScene();
        SceneManager.getInstance().setScene(splashScene);
        pOnCreateSceneCallback.onCreateSceneFinished(splashScene);
    }

    public void createLoadingScene(IGameInterface.OnCreateSceneCallback pOnCreateSceneCallback) {
        ResourcesManager.getInstance().loadSplashScreenResources();
        splashScene = new SplashScene();
        SceneManager.getInstance().setScene(splashScene);
        pOnCreateSceneCallback.onCreateSceneFinished(splashScene);
    }

//    public void createMenuScene() {
//        ResourcesManager.getInstance().loadMenuResources();
//        menuScene = new MainMenuScene();
//        SceneManager.getInstance().setScene(menuScene);
//    }

    public void loadMenuScene(final Engine engine) {
        loadingScene = new LoadingScene();
        setScene(loadingScene);
        engine.registerUpdateHandler(new TimerHandler(0.1f, new ITimerCallback() {
            @Override
            public void onTimePassed(TimerHandler pTimerHandler) {
                engine.unregisterUpdateHandler(pTimerHandler);
                ResourcesManager.getInstance().loadMenuResources();
                menuScene = new MainMenuScene();
                setScene(menuScene);
            }
        }));
    }

    public void createGameScene() {
        ResourcesManager.getInstance().loadGameResources();
        gameScene = new GameScene();
        loadingScene = new LoadingScene();
        SceneManager.getInstance().setScene(gameScene);
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

    public void loadLoadingScene() {
        setScene(loadingScene);
    }
}
