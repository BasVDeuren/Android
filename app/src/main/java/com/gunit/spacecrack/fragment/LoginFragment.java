package com.gunit.spacecrack.fragment;

import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.facebook.Session;
import com.facebook.model.GraphUser;
import com.facebook.widget.LoginButton;
import com.google.gson.Gson;
import com.google.gson.JsonParseException;
import com.gunit.spacecrack.R;
import com.gunit.spacecrack.activity.HomeActivity;
import com.gunit.spacecrack.application.SpaceCrackApplication;
import com.gunit.spacecrack.interfacerequest.ILoginRequest;
import com.gunit.spacecrack.interfacerequest.IUserRequest;
import com.gunit.spacecrack.model.User;
import com.gunit.spacecrack.restservice.RestService;
import com.gunit.spacecrack.task.LoginTask;
import com.gunit.spacecrack.task.UserTask;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;

/**
 * Created by Dimitri on 20/02/14.
 */

/**
 * Fragment to handle the login
 */
public class LoginFragment extends Fragment implements ILoginRequest, IUserRequest {

    private EditText edtEmail;
    private EditText edtPassword;
    private Button btnLogin;
    private Button btnRegister;
    private LoginButton btnFacebook;
    private SharedPreferences sharedPreferences;
    private boolean facebookLogin;
    private String email;
    private String password;

    private static final String TAG = "LoginFragment";

    private ProgressDialog progressDialog;
    private ILoginRequest loginFragment;
    private IUserRequest userCall;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_login, container, false);

        sharedPreferences = getActivity().getSharedPreferences("Login", 0);

        //Find the views
        edtEmail = (EditText) view.findViewById(R.id.edt_login_email);
        edtPassword = (EditText) view.findViewById(R.id.edt_login_password);
        btnLogin = (Button) view.findViewById(R.id.btn_login_login);
        btnRegister = (Button) view.findViewById(R.id.btn_login_register);
        btnFacebook = (LoginButton) view.findViewById(R.id.btn_login_facebook);
        //Set the permissions
        btnFacebook.setReadPermissions(Arrays.asList("email", "user_birthday", "user_photos"));

        facebookLogin = false;
        addListeners();
        loginFragment = this;
        userCall = this;

        return view;
    }

    private void addListeners() {
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!edtEmail.getText().toString().equals("") && !edtPassword.getText().toString().equals("")) {
                    if (SpaceCrackApplication.isValidEmail(edtEmail.getText().toString())) {
                        email = edtEmail.getText().toString();
                        password = SpaceCrackApplication.hashPassword(edtPassword.getText().toString());
                        new LoginTask(email, password, loginFragment).execute();
                    } else {
                        Toast.makeText(getActivity(), getString(R.string.valid_email_error), Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(getActivity(), getResources().getString(R.string.fill_in_fields), Toast.LENGTH_SHORT).show();
                }
            }
        });
        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().getFragmentManager().beginTransaction()
                        .replace(R.id.container, new RegisterFragment(), "Register")
                        .addToBackStack("LoginFragment")
                        .commit();
            }
        });
        btnFacebook.setUserInfoChangedCallback(new LoginButton.UserInfoChangedCallback() {
            @Override
            public void onUserInfoFetched(GraphUser user) {
                SpaceCrackApplication.graphUser = user;
                if (user != null) {
                    facebookLogin = true;
                    email = (String) SpaceCrackApplication.graphUser.asMap().get("email");
                    password = "facebook" + user.getId();
                    new LoginTask(email, password, loginFragment).execute();
                }
            }
        });
    }

    @Override
    public void startTask() {
        btnLogin.setEnabled(false);
        btnRegister.setEnabled(false);
        btnFacebook.setEnabled(false);
        progressDialog = ProgressDialog.show(getActivity(), getString(R.string.please_wait), getString(R.string.logging_in));
        progressDialog.setCancelable(true);
    }

    @Override
    public void loginCallback(String result) {
        if (!facebookLogin) {
            Toast.makeText(getActivity(), result != null ? getString(R.string.login_succes) : getString(R.string.login_fail), Toast.LENGTH_SHORT).show();
        }

        saveLoginCredentials(result, email, password);

        if (result != null) {
            Log.i(TAG, "Login success");
            new UserTask(userCall).execute(SpaceCrackApplication.URL_USER);
        } else if (facebookLogin) {
            new FacebookRegisterTask(SpaceCrackApplication.graphUser.getName().replaceAll("\\s", "_"), "facebook" + SpaceCrackApplication.graphUser.getId(), "facebook" + SpaceCrackApplication.graphUser.getId(), (String) SpaceCrackApplication.graphUser.asMap().get("email")).execute(SpaceCrackApplication.URL_FBREGISTER);
        }

        Log.i(TAG, "Login failed");
        enableButtons();
    }

    @Override
    public void userCallback(String result) {
        if (result != null) {
            try {
                Gson gson = new Gson();
                SpaceCrackApplication.user = gson.fromJson(result, User.class);

                if (SpaceCrackApplication.user.profile.image != null) {
                    //Get the image from the Data URI
                    String image = SpaceCrackApplication.user.profile.image.substring(SpaceCrackApplication.user.profile.image.indexOf(",") + 1);

                    try {
                        byte[] decodedString = Base64.decode(image, 0);
                        SpaceCrackApplication.profilePicture = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);

                    } catch (IllegalStateException ise) {

                    }

                }


                Intent intent = new Intent(getActivity(), HomeActivity.class);
                startActivity(intent);
                getActivity().finish();
            } catch (JsonParseException e) {
                e.printStackTrace();
            }
        } else {
            Toast.makeText(getActivity(), getString(R.string.profile_fail), Toast.LENGTH_SHORT).show();
        }
        enableButtons();
    }


    private void enableButtons() {
        btnLogin.setEnabled(true);
        btnRegister.setEnabled(true);
        btnFacebook.setEnabled(true);
        progressDialog.dismiss();
    }

    private void saveLoginCredentials(String accessToken, String email, String password) {
        SpaceCrackApplication.accessToken = accessToken;

        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("accessToken", accessToken);
        editor.putString("email", email);
        editor.putString("password", password);
        editor.commit();
    }

    //POST request to register the user
    public class FacebookRegisterTask extends AsyncTask<String, Void, String> {

        private JSONObject newUser;
        private String email;
        private String password;
        private ProgressDialog progressDialog;

        public FacebookRegisterTask(String username, String password, String passwordRepeated, String email) {
            super();
            this.email = email;
            this.password = password;
            newUser = new JSONObject();
            try {
                newUser.put("username", username);
                newUser.put("password", password);
                newUser.put("passwordRepeated", passwordRepeated);
                newUser.put("email", email);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        @Override
        protected void onPreExecute() {
            btnLogin.setEnabled(false);
            btnRegister.setEnabled(false);
            btnFacebook.setEnabled(false);
            progressDialog = ProgressDialog.show(getActivity(), getString(R.string.please_wait), getString(R.string.logging_in));
            progressDialog.setCancelable(true);
        }

        @Override
        protected String doInBackground(String... url) {
            //Register newUser
            Integer statuscode = RestService.postRegisterUser(url[0], newUser);

            String accessToken = "";
            switch (statuscode) {
                case 200:
                    accessToken = facebookLoginAndReadProfileData();
                    break;
                case 409:
                    Toast.makeText(getActivity(), getResources().getString(R.string.username_or_email_conflict), Toast.LENGTH_SHORT).show();
                    break;
                case 406:
                    Toast.makeText(getActivity(), getResources().getString(R.string.invalid_registration_fieldcontents), Toast.LENGTH_SHORT).show();
                    break;
                default:
                    Toast.makeText(getActivity(), getResources().getString(R.string.login_fail), Toast.LENGTH_SHORT).show();
            }
            return accessToken;
        }

        private String facebookLoginAndReadProfileData() {
            JSONObject loginUser = new JSONObject();
            JSONObject profile = new JSONObject();
            String accessToken = null;
            try {
                loginUser.put("email", newUser.get("email"));
                loginUser.put("password", newUser.get("password"));
                accessToken = RestService.postRequestWithoutAccessToken(SpaceCrackApplication.URL_LOGIN, loginUser);
                SpaceCrackApplication.accessToken = accessToken;

                //Get the URL of the Facebook profilepicture
                String profilePictureUrl = SpaceCrackApplication.getFacebookPictureUrl();
                String picture = null;
                if (profilePictureUrl != null) {
                    String jsonString = RestService.getRequest(profilePictureUrl);
                    if (jsonString != null) {
                        jsonString = jsonString.replace("\\/", "/");
                        JSONObject jsonObject = new JSONObject(jsonString);
                        picture = jsonObject.getJSONObject("data").getString("url");
                    }
                }
                profile.put("dayOfBirth", SpaceCrackApplication.getLocalDate(SpaceCrackApplication.graphUser.getBirthday()));
                profile.put("email", SpaceCrackApplication.graphUser.asMap().get("email"));
                profile.put("firstname", SpaceCrackApplication.graphUser.getFirstName());
                profile.put("lastname", SpaceCrackApplication.graphUser.getLastName());
                if (picture != null) {
                    profile.put("image", picture);
                } else {
                    profile.put("image", "");
                }
                Log.i("FacebookProfile", profile.toString());
                RestService.postRequest(SpaceCrackApplication.URL_PROFILE, profile);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            return accessToken;
        }

        @Override
        protected void onPostExecute(String result) {
            saveLoginCredentials(result, email, password);

            if (result != null) {
                new UserTask(userCall).execute(SpaceCrackApplication.URL_USER);
            } else {
                facebookLogin = false;
                Toast.makeText(getActivity(), getString(R.string.email_register), Toast.LENGTH_SHORT).show();
                if (Session.getActiveSession() != null) {
                    Session.getActiveSession().closeAndClearTokenInformation();
                }
                enableButtons();
            }
        }
    }
}
