package com.gunit.spacecrack.game.manager;

import android.graphics.Bitmap;
import android.graphics.Color;

import com.gunit.spacecrack.game.GameActivity;
import com.gunit.spacecrack.game.pool.PlanetPool;
import com.gunit.spacecrack.game.pool.ShipPool;
import com.gunit.spacecrack.game.sprite.PlanetSprite;
import com.gunit.spacecrack.game.sprite.ShipSprite;

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
public class ResourcesManager {

    //Singleton
    private static final ResourcesManager INSTANCE = new ResourcesManager();

    public GameActivity gameActivity;
    public Engine engine;
    public SmoothCamera camera;
    public VertexBufferObjectManager vertexBufferObjectManager;

    //Font
    public Font font;

    //Splash screen

    //Menu screen
    public ITextureRegion menuBackgroundRegion;
    public ITextureRegion playRegion;
    public ITextureRegion optionsRegion;
    private BuildableBitmapTextureAtlas menuTextureAtlas;

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

            font = FontFactory.createStrokeFromAsset(gameActivity.getFontManager(), mainFontTexture, gameActivity.getAssets(), "font.ttf", 30, true, Color.RED, 2, Color.BLACK);
            font.load();
        }
    }

    public void unloadFonts() {
        if (font != null) {
            font.unload();
            font = null;
        }
    }

    public void loadSplashScreenResources()
    {

    }

    public void loadMenuResources() {
        getInstance().loadMenuGraphics();
    }

    public void loadGameResources() {
        loadGameGraphics();
        loadGameAudio();
    }

    private void loadMenuGraphics() {
        //Set the path to assets/gfx/
        BitmapTextureAtlasTextureRegionFactory.setAssetBasePath("gfx/");
        menuTextureAtlas = new BuildableBitmapTextureAtlas(gameActivity.getTextureManager(), 1024, 1024, TextureOptions.BILINEAR);
        menuBackgroundRegion = BitmapTextureAtlasTextureRegionFactory.createFromAsset(menuTextureAtlas, gameActivity, "spacebackground.jpg");
        BitmapTextureAtlasTextureRegionFactory.setAssetBasePath("gfx/menu/");
        playRegion = BitmapTextureAtlasTextureRegionFactory.createFromAsset(menuTextureAtlas, gameActivity, "play.png");
        optionsRegion = BitmapTextureAtlasTextureRegionFactory.createFromAsset(menuTextureAtlas, gameActivity, "options.png");

        try
        {
            menuTextureAtlas.build(new BlackPawnTextureAtlasBuilder<IBitmapTextureAtlasSource, BitmapTextureAtlas>(0, 1, 0));
            menuTextureAtlas.load();
        }
        catch (final ITextureAtlasBuilder.TextureAtlasBuilderException e)
        {
            Debug.e(e);
        }
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
        cancelRegion = BitmapTextureAtlasTextureRegionFactory.createFromAsset(gameTextureAtlas, gameActivity, "cancel.png");

        try {
            gameTextureAtlas.build(new BlackPawnTextureAtlasBuilder<IBitmapTextureAtlasSource, BitmapTextureAtlas>(0, 1, 0));
            gameTextureAtlas.load();
        } catch (final ITextureAtlasBuilder.TextureAtlasBuilderException e) {
            Debug.e(e);
        }

    }



    private void loadGameAudio() {

    }



    public void unloadSplashScreenResources()
    {

    }

//    public void loadMenuTextures() {
//        menuTextureAtlas.load();
//    }

    public void unloadMenuResources() {
        menuTextureAtlas.unload();
        menuTextureAtlas = null;
    }


    public void unloadGameResources() {
        gameTextureAtlas.unload();
        gameTextureAtlas = null;
    }

}
