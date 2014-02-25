package com.gunit.spacecrack.game;

import android.graphics.Color;

import org.andengine.engine.Engine;
import org.andengine.engine.camera.Camera;
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
import org.andengine.opengl.vbo.VertexBufferObjectManager;
import org.andengine.util.debug.Debug;

/**
 * Created by Dimitri on 24/02/14.
 */
public class ResourcesManager {

    //Singleton
    private static final ResourcesManager INSTANCE = new ResourcesManager();

    public GameActivity gameActivity;
    public Engine engine;
    public Camera camera;
    public VertexBufferObjectManager vertexBufferObjectManager;

    //Font
    public Font font;

    //Splash screen
    public ITextureRegion splashRegion;
    private BitmapTextureAtlas splashTextureAtlas;

    //Menu screen
    public ITextureRegion menuBackgroundRegion;
    public ITextureRegion playRegion;
    public ITextureRegion optionsRegion;
    private BuildableBitmapTextureAtlas menuTextureAtlas;

    //Game screen
    public ITextureRegion gameBackgroundRegion;
    public ITextureRegion planetRegion;
    private BuildableBitmapTextureAtlas gameTextureAtlas;

    private ResourcesManager() {

    }

    public static ResourcesManager getInstance() {
        return INSTANCE;
    }

    public void init (GameActivity gameActivity) {
        this.gameActivity = gameActivity;
        this.engine = gameActivity.getEngine();
        this.camera = engine.getCamera();
        this.vertexBufferObjectManager = engine.getVertexBufferObjectManager();
    }

    public void loadMenuResources() {
        loadMenuGraphics();
        loadMenuFonts();
    }

    public void loadMenuGraphics() {
        BitmapTextureAtlasTextureRegionFactory.setAssetBasePath("gfx/");
        menuTextureAtlas = new BuildableBitmapTextureAtlas(gameActivity.getTextureManager(), 1024, 1024, TextureOptions.BILINEAR);
        menuBackgroundRegion = BitmapTextureAtlasTextureRegionFactory.createFromAsset(menuTextureAtlas, gameActivity, "spacebackground.jpg");
        BitmapTextureAtlasTextureRegionFactory.setAssetBasePath("gfx/menu/");
        playRegion = BitmapTextureAtlasTextureRegionFactory.createFromAsset(menuTextureAtlas, gameActivity, "play.png");
        optionsRegion = BitmapTextureAtlasTextureRegionFactory.createFromAsset(menuTextureAtlas, gameActivity, "options.png");

        try
        {
            this.menuTextureAtlas.build(new BlackPawnTextureAtlasBuilder<IBitmapTextureAtlasSource, BitmapTextureAtlas>(0, 1, 0));
            this.menuTextureAtlas.load();
        }
        catch (final ITextureAtlasBuilder.TextureAtlasBuilderException e)
        {
            Debug.e(e);
        }
    }

    public void loadGameResources() {
        loadGameGraphics();
        loadMenuFonts();
        loadGameAudio();
    }

    private void loadGameGraphics() {
        BitmapTextureAtlasTextureRegionFactory.setAssetBasePath("gfx/game/");
        gameTextureAtlas = new BuildableBitmapTextureAtlas(gameActivity.getTextureManager(), 1024, 1024, TextureOptions.BILINEAR);
//        gameBackgroundRegion = BitmapTextureAtlasTextureRegionFactory.createFromAsset(gameTextureAtlas, gameActivity, "galaxy.jpg");
        planetRegion = BitmapTextureAtlasTextureRegionFactory.createFromAsset(gameTextureAtlas, gameActivity, "planet1.png");

        try {
            this.gameTextureAtlas.build(new BlackPawnTextureAtlasBuilder<IBitmapTextureAtlasSource, BitmapTextureAtlas>(0, 1, 0));
            this.gameTextureAtlas.load();
        } catch (final ITextureAtlasBuilder.TextureAtlasBuilderException e) {
            Debug.e(e);
        }
    }

    private void loadMenuFonts() {
        FontFactory.setAssetBasePath("font/");
        final ITexture mainFontTexture = new BitmapTextureAtlas(gameActivity.getTextureManager(), 256, 256, TextureOptions.BILINEAR_PREMULTIPLYALPHA);

        font = FontFactory.createStrokeFromAsset(gameActivity.getFontManager(), mainFontTexture, gameActivity.getAssets(), "font.ttf", 50, true, Color.WHITE, 2, Color.BLACK);
        font.load();
    }

    private void loadGameAudio() {

    }

    public void loadSplashScreen()
    {
        BitmapTextureAtlasTextureRegionFactory.setAssetBasePath("gfx/game/");
        splashTextureAtlas = new BitmapTextureAtlas(gameActivity.getTextureManager(), 256, 256, TextureOptions.BILINEAR);
        splashRegion = BitmapTextureAtlasTextureRegionFactory.createFromAsset(splashTextureAtlas, gameActivity, "planet1.png", 0, 0);
        splashTextureAtlas.load();
    }

    public void unloadSplashScreen()
    {
        splashTextureAtlas.unload();
        splashRegion = null;
    }

    public void unloadMenuTextures()
    {
        menuTextureAtlas.unload();
    }

    public void loadMenuTextures()
    {
        menuTextureAtlas.load();
    }

    public void unloadGameTextures() {

    }

}
