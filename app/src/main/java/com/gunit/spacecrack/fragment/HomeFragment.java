package com.gunit.spacecrack.fragment;

import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.Request;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.model.GraphUser;
import com.facebook.widget.ProfilePictureView;
import com.gunit.spacecrack.R;
import com.gunit.spacecrack.activity.ProfileActivity;
import com.gunit.spacecrack.application.SpaceCrackApplication;
import com.gunit.spacecrack.game.GameActivity;

import java.util.List;

/**
 * Created by Dimitri on 28/02/14.
 */
public class HomeFragment extends Fragment {
    private ProfilePictureView fbPictureView;
    private ImageView profilePicture;
    private TextView name;
    private LinearLayout profile;
    private Button newGame;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        fbPictureView = (ProfilePictureView) view.findViewById(R.id.ppv_home_fbpicture);
        profilePicture = (ImageView) view.findViewById(R.id.img_home_profilepicture);
        name = (TextView) view.findViewById(R.id.txt_home_welcome);
        profile = (LinearLayout) view.findViewById(R.id.llt_home_profile);
        profile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (SpaceCrackApplication.profile != null) {
                    Intent intent = new Intent(getActivity(), ProfileActivity.class);
                    startActivity(intent);
                } else {
                    Toast.makeText(getActivity(), getResources().getText(R.string.profile_not_available), Toast.LENGTH_SHORT).show();
                }
            }
        });
        newGame = (Button) view.findViewById(R.id.btn_home_newgame);
        newGame.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().getFragmentManager().beginTransaction()
                        .replace(R.id.container, new NewGameFragment())
                        .addToBackStack("HomeFragment")
                        .commit();
            }
        });

        return view;
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
