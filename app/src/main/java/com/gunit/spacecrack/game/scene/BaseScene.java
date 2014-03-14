package com.gunit.spacecrack.game.scene;

import android.app.Activity;

import com.gunit.spacecrack.game.GameActivity;
import com.gunit.spacecrack.game.manager.ResourcesManager;
import com.gunit.spacecrack.game.manager.SceneManager;

import org.andengine.engine.Engine;
import org.andengine.engine.camera.Camera;
import org.andengine.engine.camera.SmoothCamera;
import org.andengine.entity.scene.Scene;
import org.andengine.entity.sprite.Sprite;
import org.andengine.entity.sprite.TiledSprite;
import org.andengine.opengl.texture.region.ITextureRegion;
import org.andengine.opengl.texture.region.ITiledTextureRegion;
import org.andengine.opengl.util.GLState;
import org.andengine.opengl.vbo.VertexBufferObjectManager;

/**
 * Created by Dimitri on 24/02/14.
 */

/**
 * BaseScene defines the abstract methods used by the different Scenes
 */
public abstract class BaseScene extends Scene {
    protected Engine engine;
    protected GameActivity activity;
    protected ResourcesManager resourcesManager;
    protected VertexBufferObjectManager vbom;
    protected SmoothCamera camera;

    public BaseScene()
    {
        this.resourcesManager = ResourcesManager.getInstance();
        this.engine = resourcesManager.engine;
        this.activity = resourcesManager.gameActivity;
        this.vbom = resourcesManager.vertexBufferObjectManager;
        this.camera = resourcesManager.camera;
        createScene();
    }

    /**
     * Create a new Sprite, used for display the game elements, and return it
     * @param x
     * @param y
     * @param region
     * @param vbom
     * @return
     */
    protected Sprite createSprite(float x, float y, ITextureRegion region, VertexBufferObjectManager vbom) {
        Sprite sprite = new Sprite(x, y, region, vbom)
        {
            @Override
            protected void preDraw(GLState glState, Camera camera1) {
                super.preDraw(glState, camera1);
                glState.enableDither();
            }
        };
        return sprite;
    }

    /**
     * Create a new TiledSprite, used for display the game elements, and return it
     * The TiledSprite accepts a Sprite sheet and will display the correct image according the index
     * @param x
     * @param y
     * @param region
     * @param vbom
     * @param index
     * @return
     */
    protected TiledSprite createTiledSprite(float x, float y, ITiledTextureRegion region, VertexBufferObjectManager vbom, int index) {
        TiledSprite tiledSprite = new TiledSprite(x, y, region, vbom)
        {
            @Override
            protected void preDraw(GLState glState, Camera camera1) {
                super.preDraw(glState, camera1);
                glState.enableDither();
            }
        };
        tiledSprite.setCurrentTileIndex(index);
        return tiledSprite;
    }

    public abstract void createScene();

    public abstract void onBackKeyPressed();

    public abstract void disposeScene();
}
