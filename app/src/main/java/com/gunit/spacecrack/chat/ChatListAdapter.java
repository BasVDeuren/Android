package com.gunit.spacecrack.chat;

import android.app.Activity;
import android.graphics.Color;
import android.view.View;
import android.widget.TextView;
import com.firebase.client.Query;
import com.gunit.spacecrack.R;

/**
 * Created by Dimitri on 25/02/14.
 */
public class ChatListAdapter extends FirebaseListAdapter<Chat> {

    // The username for this client. We use this to indicate which messages originated from this user
    private String username;

    public ChatListAdapter(Query ref, Activity activity, int layout, String username) {
        super(ref, Chat.class, layout, activity);
        this.username = username;
    }

    /**
     * Bind an instance of the <code>Chat</code> class to our view. This method is called by <code>FirebaseListAdapter</code>
     * when there is a data change, and we are given an instance of a View that corresponds to the layout that we passed
     * to the constructor, as well as a single <code>Chat</code> instance that represents the current data to bind.
     * @param view A view instance corresponding to the layout we passed to the constructor.
     * @param chat An instance representing the current state of a chatRegion message
     */
    @Override
    protected void populateView(View view, Chat chat) {
        // Map a Chat object to an entry in our listview
        String author = chat.getFrom();
        TextView authorText = (TextView)view.findViewById(R.id.author);
        authorText.setText(author + ": ");
        // If the message was sent by this user, color it differently
        if (author != null) {
            if (author.equals(username)) {
                authorText.setTextColor(Color.RED);
            } else {
                authorText.setTextColor(Color.BLUE);
            }
        }
        ((TextView)view.findViewById(R.id.message)).setText(chat.getBody());
    }
}
