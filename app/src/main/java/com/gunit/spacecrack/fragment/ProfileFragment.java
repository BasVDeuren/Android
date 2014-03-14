package com.gunit.spacecrack.fragment;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.facebook.widget.ProfilePictureView;
import com.gunit.spacecrack.R;
import com.gunit.spacecrack.application.SpaceCrackApplication;

import java.text.DateFormat;
import java.util.Date;

/**
 * Created by Dimitri on 20/02/14.
 */

/**
 * Fragment to show the Profile of the user
 */
public class ProfileFragment extends Fragment {

    private ProfilePictureView fbPictureView;
    private ImageView imgProfilePicture;
    private TextView txtName;
    private TextView txtAge;
    private TextView txtGender;
    private TextView txtSetup;
    private LinearLayout lltProfileInfo;
    private LinearLayout lltProfileEdit;
    private Button btnEdit;
    private Button btnPassword;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_profile, container, false);

        fbPictureView = (ProfilePictureView) rootView.findViewById(R.id.ppv_profile_fbpicture);
        imgProfilePicture = (ImageView) rootView.findViewById(R.id.img_profile_profilepicture);
        txtName = (TextView) rootView.findViewById(R.id.txt_profile_name);
        txtAge = (TextView) rootView.findViewById(R.id.txt_profile_age);
        txtGender = (TextView) rootView.findViewById(R.id.txt_profile_gender);
        txtSetup = (TextView) rootView.findViewById(R.id.txt_profile_setup);
        lltProfileInfo = (LinearLayout) rootView.findViewById(R.id.llt_profile_info);
        lltProfileEdit = (LinearLayout) rootView.findViewById(R.id.llt_profile_edit);
        if (SpaceCrackApplication.user.profile.firstname == null && SpaceCrackApplication.user.profile.lastname == null && SpaceCrackApplication.graphUser == null) {
            txtSetup.setVisibility(View.VISIBLE);
            lltProfileInfo.setVisibility(View.GONE);
        }
        btnEdit = (Button) rootView.findViewById(R.id.btn_profile_edit);
        btnEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().getFragmentManager().beginTransaction()
                        .replace(R.id.container, new EditProfileFragment(), "Edit Profile")
                        .commit();
            }
        });
        btnPassword = (Button) rootView.findViewById(R.id.btn_profile_password);
        btnPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().getFragmentManager().beginTransaction()
                        .replace(R.id.container, new PasswordFragment(), "Password")
                        .commit();
            }
        });

        if (SpaceCrackApplication.graphUser != null) {
            lltProfileEdit.setVisibility(View.GONE);
            fbPictureView.setVisibility(View.VISIBLE);
            imgProfilePicture.setVisibility(View.GONE);
            fbPictureView.setProfileId(SpaceCrackApplication.graphUser.getId());
            txtName.setText(SpaceCrackApplication.graphUser.getFirstName() + " " + SpaceCrackApplication.graphUser.getLastName());
            txtAge.setText(SpaceCrackApplication.graphUser.getBirthday());
            if (SpaceCrackApplication.graphUser.getProperty("gender").equals("male")) {
                txtGender.setText(getResources().getString(R.string.male));
            } else if (SpaceCrackApplication.graphUser.getProperty("gender").equals("female")) {
                txtGender.setText(getResources().getString(R.string.female));
            }
        } else if (SpaceCrackApplication.user.profile != null) {
            if (SpaceCrackApplication.user.profile.firstname != null && SpaceCrackApplication.user.profile.lastname != null) {
                txtName.setText(SpaceCrackApplication.user.profile.firstname + " " + SpaceCrackApplication.user.profile.lastname);
            }
            DateFormat dateFormat = android.text.format.DateFormat.getDateFormat(getActivity());
            txtAge.setText(dateFormat.format(new Date(SpaceCrackApplication.user.profile.dayOfBirth)));
            if (SpaceCrackApplication.profilePicture != null) {
                imgProfilePicture.setImageBitmap(SpaceCrackApplication.profilePicture);
            }
        }

        return rootView;
    }
}
