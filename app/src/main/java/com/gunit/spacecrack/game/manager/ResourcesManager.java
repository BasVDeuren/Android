package com.gunit.spacecrack.game.manager;

import android.graphics.Color;

import com.gunit.spacecrack.game.GameActivity;

import org.andengine.engine.Engine;
import org.andengine.engine.camera.SmoothCamera;
import org.andengine.opengl.font.Font;
import org.andengine.opengl.font.FontFactory;
import org.andengine.opengl.texture.ITexture;
import org.andengine.opengl.texture.TextureOptions;
import org.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlas;
import org.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlasTextureRegionFactory;
import org.andengine.opengl.texture.atlas.bitmap.BuildableBitmapTextureAtlas;
import org.andengine.opengl.texture.atlas.bitmap.source.IBitmapTextureAtlasSource;
import org.andengine.opengl.texture.atlas.buildable.builder.BlackPawnTextureAtlasBuilder;
import org.andengine.opengl.texture.atlas.buildable.builder.ITextureAtlasBuilder;
import org.andengine.opengl.texture.region.ITextureRegion;
import org.andengine.opengl.texture.region.ITiledTextureRegion;
import org.andengine.opengl.vbo.VertexBufferObjectManager;
import org.andengine.util.debug.Debug;

/**
 * Created by Dimitri on 24/02/14.
 */

/**
 * ResourceManager will take care of loading in all the Textures, Fonts, etc.
 */
public class ResourcesManager {

    //Singleton
    private static final ResourcesManager INSTANCE = new ResourcesManager();

    public GameActivity gameActivity;
    public Engine engine;
    public SmoothCamera camera;
    public VertexBufferObjectManager vertexBufferObjectManager;

    //Font
    public Font font;

    //Game screen
    public ITextureRegion gameBackgroundRegion;
    public ITiledTextureRegion planetRegion;
    public ITiledTextureRegion colonyFlagRegion;
    public ITiledTextureRegion castleRegion;
    public ITiledTextureRegion spaceshipRegion;
    public ITextureRegion miniSpaceshipPlayer1Region;
    public ITextureRegion miniSpaceshipPLayer2Region;
    public ITextureRegion chatRegion;
    public ITextureRegion turnRegion;
    public ITextureRegion monkeyRegion;
    public ITextureRegion facebookRegion;
    public ITextureRegion facebookPressedRegion;
    public ITextureRegion quitRegion;
    public ITextureRegion cancelRegion;
    public BuildableBitmapTextureAtlas gameTextureAtlas;

    private ResourcesManager() {

    }

    public static ResourcesManager getInstance() {
        return INSTANCE;
    }

    public void init (GameActivity gameActivity) {
        getInstance().gameActivity = gameActivity;
        getInstance().engine = gameActivity.getEngine();
        getInstance().camera = (SmoothCamera) engine.getCamera();
        getInstance().vertexBufferObjectManager = engine.getVertexBufferObjectManager();
    }

    public void loadFonts() {
        if (font == null) {
            FontFactory.setAssetBasePath("font/");
            final ITexture mainFontTexture = new BitmapTextureAtlas(gameActivity.getTextureManager(), 256, 256, TextureOptions.BILINEAR_PREMULTIPLYALPHA);

            font = FontFactory.createStrokeFromAsset(gameActivity.getFontManager(), mainFontTexture, gameActivity.getAssets(), "roboto.ttf", 30, true, Color.RED, 1, Color.BLACK);
            font.load();
        }
    }

    public void unloadFonts() {
        if (font != null) {
            font.unload();
            font = null;
        }
    }

    public void loadGameResources() {
        loadGameGraphics();
    }

    private void loadGameGraphics() {
        BitmapTextureAtlasTextureRegionFactory.setAssetBasePath("gfx/game/");
        gameTextureAtlas = new BuildableBitmapTextureAtlas(gameActivity.getTextureManager(), 1024, 1024, TextureOptions.BILINEAR);
        gameBackgroundRegion = BitmapTextureAtlasTextureRegionFactory.createFromAsset(gameTextureAtlas, gameActivity, "galaxy.jpg");
        planetRegion = BitmapTextureAtlasTextureRegionFactory.createTiledFromAsset(gameTextureAtlas, gameActivity, "planet.png", 2, 1);
        spaceshipRegion = BitmapTextureAtlasTextureRegionFactory.createTiledFromAsset(gameTextureAtlas, gameActivity, "spaceship.png", 2, 1);
        miniSpaceshipPlayer1Region = BitmapTextureAtlasTextureRegionFactory.createFromAsset(gameTextureAtlas, gameActivity, "mini_player1spaceship.png");
        miniSpaceshipPLayer2Region = BitmapTextureAtlasTextureRegionFactory.createFromAsset(gameTextureAtlas, gameActivity, "mini_player2spaceship.png");
        colonyFlagRegion = BitmapTextureAtlasTextureRegionFactory.createTiledFromAsset(gameTextureAtlas, gameActivity, "playerflag.png", 2, 1);
        castleRegion = BitmapTextureAtlasTextureRegionFactory.createTiledFromAsset(gameTextureAtlas, gameActivity, "castle.png", 2, 1);
        chatRegion = BitmapTextureAtlasTextureRegionFactory.createFromAsset(gameTextureAtlas, gameActivity, "chat.png");
        turnRegion = BitmapTextureAtlasTextureRegionFactory.createFromAsset(gameTextureAtlas, gameActivity, "turn.png");
        monkeyRegion = BitmapTextureAtlasTextureRegionFactory.createFromAsset(gameTextureAtlas, gameActivity, "monkey_winner.png");
        facebookRegion = BitmapTextureAtlasTextureRegionFactory.createFromAsset(gameTextureAtlas, gameActivity, "fb_icon.png");
        facebookPressedRegion = BitmapTextureAtlasTextureRegionFactory.createFromAsset(gameTextureAtlas, gameActivity, "fb_icon_pressed.png");
        quitRegion = BitmapTextureAtlasTextureRegionFactory.createFromAsset(gameTextureAtlas, gameActivity, "quit.png");
        cancelRegion = BitmapTextureAtlasTextureRegionFactory.createFromAsset(gameTextureAtlas, gameActivity, "cancel.png");

        try {
            gameTextureAtlas.build(new BlackPawnTextureAtlasBuilder<IBitmapTextureAtlasSource, BitmapTextureAtlas>(0, 1, 0));
            gameTextureAtlas.load();
        } catch (final ITextureAtlasBuilder.TextureAtlasBuilderException e) {
            Debug.e(e);
        }

    }

    public void unloadGameResources() {
        gameTextureAtlas.unload();
        gameTextureAtlas = null;
        unloadFonts();
    }

}
