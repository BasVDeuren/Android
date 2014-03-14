package com.gunit.spacecrack.model;

/**
 * Created by Dimitri on 14/03/14.
 */
public class Invite {
    public String gameId;
    public String inviter;
    public boolean read;
    public int inviterId;
    public String invited;
    public int invitedId;
    public boolean accepted;

    public Invite(String gameId, String inviter, boolean read) {
        this.gameId = gameId;
        this.inviter = inviter;
        this.read = read;
    }
}
