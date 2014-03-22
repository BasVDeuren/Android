package com.gunit.spacecrack.model;

/**
 * Created by Dimitri on 3/03/14.
 */
/**
 * All models use public fields because this increases performance.
 */
public class User {
    public int userId;
    public String username;
    public String password;
    public String email;
    public Token token;
    public Profile profile;
}
