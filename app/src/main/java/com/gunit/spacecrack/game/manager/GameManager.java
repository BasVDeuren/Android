package com.gunit.spacecrack.game.manager;

/**
 * Created by Dimitri on 25/02/14.
 */
public class GameManager {

    private static GameManager INSTANCE;

    private GameManager () {

    }

    public static GameManager getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new GameManager();
        }
        return INSTANCE;
    }
}
