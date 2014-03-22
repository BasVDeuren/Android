package com.gunit.spacecrack.model;

import java.util.List;

/**
 * Created by Dimitri on 26/02/14.
 */

/**
 * All models use public fields because this increases performance.
 */
public class Planet {

    public int planetId;
    public String name;
    public int x;
    public int y;
    public List<Planet> connectedPlanets;
}
