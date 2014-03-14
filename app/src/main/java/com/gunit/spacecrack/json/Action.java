package com.gunit.spacecrack.json;

/**
 * Created by Dimitri on 28/02/14.
 */
public class Action {
    public String actionType;
    public String destinationPlanetName;
    public int gameId;
    public int playerId;
    public ShipViewModel ship;
    public ColonyViewModel colony;

    public Action (String actionType, int gameId, int playerId) {
        this.actionType = actionType;
        this.gameId = gameId;
        this.playerId = playerId;
    }

    public Action(String actionType, String destinationPlanetName, int gameId, int playerId, ShipViewModel ship) {
        this.actionType = actionType;
        this.destinationPlanetName = destinationPlanetName;
        this.gameId = gameId;
        this.playerId = playerId;
        this.ship = ship;
    }

    public Action(String actionType, ColonyViewModel colony, int gameId, int playerId) {
        this.actionType = actionType;
        this.colony = colony;
        this.gameId = gameId;
        this.playerId = playerId;
    }
}

