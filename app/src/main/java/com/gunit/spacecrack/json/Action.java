package com.gunit.spacecrack.json;

import com.gunit.spacecrack.model.Colony;
import com.gunit.spacecrack.model.Ship;

/**
 * Created by Dimitri on 28/02/14.
 */
public class Action {
    public String actionType;
    public String destinationPlanetName;
    public int gameId;
    public int playerId;
    public Ship ship;
    public Colony colony;

    public Action (String actionType, int gameId, int playerId) {
        this.actionType = actionType;
        this.gameId = gameId;
        this.playerId = playerId;
    }

    public Action(String actionType, String destinationPlanetName, int gameId, int playerId, Ship ship) {
        this.actionType = actionType;
        this.destinationPlanetName = destinationPlanetName;
        this.gameId = gameId;
        this.playerId = playerId;
        this.ship = ship;
    }

    public Action(String actionType, Colony colony, int gameId, int playerId) {
        this.actionType = actionType;
        this.colony = colony;
        this.gameId = gameId;
        this.playerId = playerId;
    }
}

