package com.gunit.spacecrack.model;


import java.util.ArrayList;
import java.util.List;

/**
 * Created by Dimitri on 26/02/14.
 */
public class Player {
    public int playerId;
    public String playerName;
    public Profile profile;
    public List<Colony> colonies = new ArrayList<Colony>();
    public List<Ship> ships = new ArrayList<Ship>();
    public int commandPoints;
    public boolean turnEnded;
}
