package com.gunit.spacecrack.chat;

import android.app.ListActivity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.DataSetObserver;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.ValueEventListener;
import com.gunit.spacecrack.R;
import com.gunit.spacecrack.application.SpaceCrackApplication;

public class ChatActivity extends ListActivity {

    private String username;
    private Firebase ref;
    private ValueEventListener connectedListener;
    private ChatListAdapter chatListAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        SharedPreferences prefs = getApplication().getSharedPreferences("ChatPrefs", 0);

        Intent intent = getIntent();

        // Setup our Firebase ref
        if (intent.getStringExtra("gameId") != null) {
            ref = new Firebase(SpaceCrackApplication.URL_FIREBASE_CHAT).child(intent.getStringExtra("gameId"));
        }

        // Make sure we have a username
        setupUsername();

        setTitle("Chatting as " + username);


        // Setup our input methods. Enter key on the keyboard or pushing the send button
        EditText inputText = (EditText)findViewById(R.id.messageInput);
        inputText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent) {
                if (actionId == EditorInfo.IME_NULL && keyEvent.getAction() == KeyEvent.ACTION_DOWN) {
                    sendMessage();
                }
                return true;
            }
        });

        findViewById(R.id.sendButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendMessage();
            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        if (username == null) {
            setupUsername();
        }

        // Setup our view and list adapter. Ensure it scrolls to the bottom as data changes
        final ListView listView = getListView();
        // Tell our list adapter that we only want 50 messages at a time
        chatListAdapter = new ChatListAdapter(ref.limit(50), this, R.layout.list_chat_item, username);
        listView.setAdapter(chatListAdapter);
        chatListAdapter.registerDataSetObserver(new DataSetObserver() {
            @Override
            public void onChanged() {
                super.onChanged();
                listView.setSelection(chatListAdapter.getCount() - 1);
            }
        });

        //Check the connection
        connectedListener = ref.getRoot().child(".info/connected").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                boolean connected = (Boolean)dataSnapshot.getValue();
                if (connected) {
                    Log.i("Firebase", "Connected to Firebase");
                } else {
                    Log.i("Firebase", "Disconnected from Firebase");
                }
            }

            @Override
            public void onCancelled() {
                // No-op
            }
        });
    }

    @Override
    public void onStop() {
        super.onStop();
        ref.getRoot().child(".info/connected").removeEventListener(connectedListener);
        chatListAdapter.cleanup();
    }

    private void setupUsername() {
        SharedPreferences prefs = getApplication().getSharedPreferences("ChatPrefs", 0);
        username = prefs.getString("username", null);
        if (username == null) {
            Intent intent = getIntent();
            username = intent.getStringExtra("username");
            prefs.edit().putString("username", username).commit();
        }
    }

    private void sendMessage() {
        EditText inputText = (EditText)findViewById(R.id.messageInput);
        String input = inputText.getText().toString();
        if (!input.equals("")) {
            // Create our 'model', a Chat object
            Chat chat = new Chat(input, username);
            // Create a new, auto-generated child of that chatRegion location, and save our chatRegion data there
            ref.push().setValue(chat);
            inputText.setText("");
        }
    }
}
