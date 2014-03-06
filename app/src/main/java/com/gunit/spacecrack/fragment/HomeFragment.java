package com.gunit.spacecrack.fragment;

import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.IconButton;
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
import com.gunit.spacecrack.activity.LoginActivity;
import com.gunit.spacecrack.activity.ProfileActivity;
import com.gunit.spacecrack.application.SpaceCrackApplication;
import com.gunit.spacecrack.service.SpaceCrackService;

import java.util.List;

/**
 * Created by Dimitri on 28/02/14.
 */
public class HomeFragment extends Fragment {
    private ProfilePictureView fbPictureView;
    private ImageView imgProfilePicture;
    private TextView txtName;
    private LinearLayout lltProfile;
    private Button btnNewGame;
    private Button btnActiveGames;
    private IconButton btnLogout;
    private IconButton btnSettings;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        fbPictureView = (ProfilePictureView) view.findViewById(R.id.ppv_home_fbpicture);
        imgProfilePicture = (ImageView) view.findViewById(R.id.img_home_profilepicture);
        txtName = (TextView) view.findViewById(R.id.txt_home_welcome);
        lltProfile = (LinearLayout) view.findViewById(R.id.llt_home_profile);

        btnSettings = (IconButton) view.findViewById(R.id.btn_home_settings);
        btnNewGame = (Button) view.findViewById(R.id.btn_home_newgame);
        btnActiveGames = (Button) view.findViewById(R.id.btn_home_activegames);
        btnLogout = (IconButton) view.findViewById(R.id.btn_home_logout);

        addListeners();

        updateAccount();

        Intent intent = new Intent(getActivity(), SpaceCrackService.class);
        intent.putExtra("username", SpaceCrackApplication.user.username);
        getActivity().startService(intent);

        return view;
    }

    private void addListeners() {
        lltProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (SpaceCrackApplication.user.profile != null) {
                    Intent intent = new Intent(getActivity(), ProfileActivity.class);
                    startActivity(intent);
                } else {
                    Toast.makeText(getActivity(), getResources().getText(R.string.profile_not_available), Toast.LENGTH_SHORT).show();
                }
            }
        });
        btnSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().getFragmentManager().beginTransaction()
                        .replace(R.id.container, new SettingsFragment(), "Settings")
                        .addToBackStack("HomeFragment")
                        .commit();
            }
        });
        btnNewGame.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().getFragmentManager().beginTransaction()
                        .replace(R.id.container, new NewGameFragment(), "New Game")
                        .addToBackStack("HomeFragment")
                        .commit();
            }
        });
        btnActiveGames.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().getFragmentManager().beginTransaction()
                        .replace(R.id.container, new ActiveGamesFragment(), "Active Games")
                        .addToBackStack("HomeFragment")
                        .commit();
            }
        });
        btnLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Session.getActiveSession() != null) {
                    Session.getActiveSession().closeAndClearTokenInformation();
                }
                SpaceCrackApplication.logout();
                getActivity().getSharedPreferences("Login", 0).edit().clear().commit();
                Intent intent = new Intent(getActivity(), LoginActivity.class);
                startActivity(intent);
                getActivity().finish();
            }
        });
    }

    //Update account with information retrieved from Facebook
    private void updateAccount() {
        final Session session = Session.getActiveSession();
        if (session != null && session.isOpened()) {
            fbPictureView.setVisibility(View.VISIBLE);
            imgProfilePicture.setVisibility(View.GONE);
            if (SpaceCrackApplication.graphUser != null) {
                fbPictureView.setProfileId(SpaceCrackApplication.graphUser.getId());
                txtName.setText(SpaceCrackApplication.graphUser.getFirstName() + " " + SpaceCrackApplication.graphUser.getLastName());
            } else {
                Request request = Request.newMeRequest(session, new Request.GraphUserCallback() {
                    @Override
                    public void onCompleted(GraphUser user, Response response) {
                        if (user != null && session == Session.getActiveSession()) {
                            fbPictureView.setProfileId(user.getId());
                            txtName.setText(user.getFirstName() + " " + user.getLastName());
                        }
                    }
                });
                request.executeAsync();
                //getFriends();
            }
        } else {
            if (SpaceCrackApplication.user.profile != null) {
                if (SpaceCrackApplication.user.profile.firstname != null && SpaceCrackApplication.user.profile.lastname != null) {
                    txtName.setText(SpaceCrackApplication.user.profile.firstname + " " + SpaceCrackApplication.user.profile.lastname);
                }
                if (SpaceCrackApplication.profilePicture != null) {
                    imgProfilePicture.setImageBitmap(SpaceCrackApplication.profilePicture);
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
