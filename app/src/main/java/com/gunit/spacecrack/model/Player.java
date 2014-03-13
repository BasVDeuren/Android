package com.gunit.spacecrack.model;


import java.util.List;

/**
 * Created by Dimitri on 26/02/14.
 */
public class Player {
    public int playerId;
    public String playerName;
    public Profile profile;
    public List<Colony> colonies;
    public List<Ship> ships;
    public int commandPoints;
    public boolean turnEnded;
}
