package com.gunit.spacecrack.fragment;

import android.app.Fragment;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.facebook.model.GraphUser;
import com.facebook.widget.ProfilePictureView;
import com.gunit.spacecrack.R;
import com.gunit.spacecrack.application.SpaceCrackApplication;
import com.gunit.spacecrack.fragment.EditProfileFragment;
import com.gunit.spacecrack.model.Profile;

import java.text.DateFormat;
import java.util.Date;

/**
 * Created by Dimitri on 20/02/14.
 */
public class ProfileFragment extends Fragment {

    private ProfilePictureView fbPictureView;
    private ImageView profilePicture;
    private TextView name;
    private TextView age;
    private TextView gender;
    private Button edit;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_profile, container, false);

        fbPictureView = (ProfilePictureView) rootView.findViewById(R.id.ppv_profile_fbpicture);
        profilePicture = (ImageView) rootView.findViewById(R.id.img_profile_profilepicture);
        name = (TextView) rootView.findViewById(R.id.txt_profile_name);
        age = (TextView) rootView.findViewById(R.id.txt_profile_age);
        gender = (TextView) rootView.findViewById(R.id.txt_profile_gender);
        edit = (Button) rootView.findViewById(R.id.btn_profile_edit);
        edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().getFragmentManager().beginTransaction()
                        .replace(R.id.container, new EditProfileFragment())
                        .addToBackStack("ProfileFragment")
                        .commit();
            }
        });


        if (SpaceCrackApplication.graphUser != null) {
            edit.setVisibility(View.GONE);
            fbPictureView.setVisibility(View.VISIBLE);
            profilePicture.setVisibility(View.GONE);
            fbPictureView.setProfileId(SpaceCrackApplication.graphUser.getId());
            name.setText(SpaceCrackApplication.graphUser.getFirstName() + " " + SpaceCrackApplication.graphUser.getLastName());
            age.setText(SpaceCrackApplication.graphUser.getBirthday());
            if (SpaceCrackApplication.graphUser.getProperty("gender").equals("male")) {
                gender.setText(getResources().getString(R.string.male));
            } else if (SpaceCrackApplication.graphUser.getProperty("gender").equals("female")) {
                gender.setText(getResources().getString(R.string.female));
            }
        } else if (SpaceCrackApplication.profile != null) {
            if (SpaceCrackApplication.profile.firstname != null && SpaceCrackApplication.profile.lastname != null) {
                name.setText(SpaceCrackApplication.profile.firstname + " " + SpaceCrackApplication.profile.lastname);
            } else {
                name.setText(getResources().getText(R.string.first_name) + " " + getResources().getText(R.string.last_name));
            }
            DateFormat dateFormat = android.text.format.DateFormat.getDateFormat(getActivity());
            age.setText(dateFormat.format(new Date(SpaceCrackApplication.profile.dayOfBirth)));
            if (SpaceCrackApplication.profilePicture != null) {
                profilePicture.setImageBitmap(SpaceCrackApplication.profilePicture);
            }
        }

        return rootView;
    }
}
