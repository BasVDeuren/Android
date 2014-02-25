package com.gunit.spacecrack.game.scene;

import android.app.Activity;

import com.gunit.spacecrack.game.ResourcesManager;

import org.andengine.engine.Engine;
import org.andengine.engine.camera.Camera;
import org.andengine.entity.scene.Scene;
import org.andengine.opengl.vbo.VertexBufferObjectManager;

/**
 * Created by Dimitri on 24/02/14.
 */
public abstract class BaseScene extends Scene {
    protected Engine engine;
    protected Activity activity;
    protected ResourcesManager resourcesManager;
    protected VertexBufferObjectManager vbom;
    protected Camera camera;

    public BaseScene()
    {
        this.resourcesManager = ResourcesManager.getInstance();
        this.engine = resourcesManager.engine;
        this.activity = resourcesManager.gameActivity;
        this.vbom = resourcesManager.vertexBufferObjectManager;
        this.camera = resourcesManager.camera;
        createScene();
    }

    public abstract void createScene();

    public abstract void onBackKeyPressed();

    public abstract SceneManager.SceneType getSceneType();

    public abstract void disposeScene();
}
