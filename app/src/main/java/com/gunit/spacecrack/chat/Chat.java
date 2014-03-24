package com.gunit.spacecrack.chat;

/**
 * Created by Dimitri on 25/02/14.
 */
public class Chat {

    private String body;
    private String from;

    /*
     Required default constructor for Firebase object mapping
      */
    @SuppressWarnings("unused")
    private Chat() { }

    public Chat(String body, String from) {
        this.body = body;
        this.from = from;
    }

    public String getBody() {
        return body;
    }

    public String getFrom() {
        return from;
    }
}
