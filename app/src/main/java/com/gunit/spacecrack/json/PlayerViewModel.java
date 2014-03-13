package com.gunit.spacecrack.json;

import java.util.List;

/**
 * Created by Dimitri on 4/03/14.
 */
public class PlayerViewModel {
    public int playerId;
    public List<ColonyViewModel> colonies;
    public List<ShipViewModel> ships;
    public int commandPoints;
    public boolean requestAccepted;
    public boolean turnEnded;
    public int profileId;
    public String playerName;
}
