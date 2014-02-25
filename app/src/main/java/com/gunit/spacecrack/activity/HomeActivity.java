package com.gunit.spacecrack.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.facebook.Request;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.model.GraphUser;
import com.facebook.widget.ProfilePictureView;
import com.gunit.spacecrack.R;
import com.gunit.spacecrack.application.SpaceCrackApplication;
import com.gunit.spacecrack.chat.ChatActivity;

import java.util.List;

public class HomeActivity extends Activity {

    private ProfilePictureView fbPictureView;
    private ImageView profilePicture;
    private TextView name;
    private LinearLayout profile;
    private Button newGame;
    private Button chat;
    private SpaceCrackApplication application;

    private final String TAG = HomeActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        fbPictureView = (ProfilePictureView) findViewById(R.id.ppv_home_fbpicture);
        profilePicture = (ImageView) findViewById(R.id.img_home_profilepicture);
        name = (TextView) findViewById(R.id.txt_home_welcome);
        profile = (LinearLayout) findViewById(R.id.llt_home_profile);
        profile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (SpaceCrackApplication.profile != null) {
                    Intent intent = new Intent(HomeActivity.this, ProfileActivity.class);
                    startActivity(intent);
                } else {

                }
            }
        });
        newGame = (Button) findViewById(R.id.btn_home_newgame);
        chat = (Button) findViewById(R.id.btn_home_chat);
        chat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(HomeActivity.this, ChatActivity.class);
                startActivity(intent);
            }
        });

        updateAccount();

    }

    //Update account with information retrieved from Facebook
    private void updateAccount() {
        final Session session = Session.getActiveSession();
        if (session != null && session.isOpened()) {
            fbPictureView.setVisibility(View.VISIBLE);
            profilePicture.setVisibility(View.GONE);
            if (SpaceCrackApplication.graphUser != null) {
                fbPictureView.setProfileId(SpaceCrackApplication.graphUser.getId());
                name.setText(SpaceCrackApplication.graphUser.getFirstName() + " " + SpaceCrackApplication.graphUser.getLastName());
            } else {
                Request request = Request.newMeRequest(session, new Request.GraphUserCallback() {
                    @Override
                    public void onCompleted(GraphUser user, Response response) {
                        if (user != null && session == Session.getActiveSession()) {
                            fbPictureView.setProfileId(user.getId());
                            name.setText(user.getFirstName() + " " + user.getLastName());
                        }
                    }
                });
                request.executeAsync();
                //getFriends();
            }
        } else {
            if (SpaceCrackApplication.profile != null) {
                if (SpaceCrackApplication.profile.firstname != null && SpaceCrackApplication.profile.lastname != null) {
                    name.setText(SpaceCrackApplication.profile.firstname + " " + SpaceCrackApplication.profile.lastname);
                }
                if (SpaceCrackApplication.profilePicture != null) {
                    profilePicture.setImageBitmap(SpaceCrackApplication.profilePicture);
                }
            }
        }
    }

    //Get the friendslist
    private void getFriends(){
        Session activeSession = Session.getActiveSession();
        if(activeSession.getState().isOpened()){
            Request friendRequest = Request.newMyFriendsRequest(activeSession,
                    new Request.GraphUserListCallback(){
                        @Override
                        public void onCompleted(List<GraphUser> users,
                                                Response response) {
                            Log.i("INFO", response.toString());

                        }
                    });
            Bundle params = new Bundle();
            params.putString("fields", "id,name,picture");
            friendRequest.setParameters(params);
            friendRequest.executeAsync();
        }
    }

}
