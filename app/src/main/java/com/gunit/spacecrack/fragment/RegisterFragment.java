package com.gunit.spacecrack.fragment;

import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.JsonParseException;
import com.gunit.spacecrack.R;
import com.gunit.spacecrack.application.SpaceCrackApplication;
import com.gunit.spacecrack.activity.HomeActivity;
import com.gunit.spacecrack.interfacerequest.IUserRequest;
import com.gunit.spacecrack.model.User;
import com.gunit.spacecrack.restservice.RestService;
import com.gunit.spacecrack.task.UserTask;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Dimitri on 20/02/14.
 */

/**
 * RegisterFragment to handle the registration of an new user
 */
public class RegisterFragment extends Fragment implements IUserRequest {

    private Button btnRegister;
    private EditText edtUsername;
    private EditText edtPassword;
    private EditText edtConfirmPassword;
    private EditText edtEmail;

    private Context context;

    private static final String TAG = "RegisterFragment";
    private ProgressDialog progressDialog;
    private IUserRequest userRequest;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_register, container, false);

        context = getActivity();

        edtUsername = (EditText) view.findViewById(R.id.edt_register_username);
        edtPassword = (EditText) view.findViewById(R.id.edt_register_password);
        edtConfirmPassword = (EditText) view.findViewById(R.id.edt_register_password_confirm);
        edtEmail = (EditText) view.findViewById(R.id.edt_register_email);
        btnRegister = (Button) view.findViewById(R.id.btn_register_register);
        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!edtUsername.getText().toString().equals("") && !edtPassword.getText().toString().equals("") && !edtConfirmPassword.getText().toString().equals("") && !edtEmail.getText().toString().equals("")) {
                    if (checkPasswords(edtPassword.getText().toString(), edtConfirmPassword.getText().toString())) {
                        if (SpaceCrackApplication.isValidEmail(edtEmail.getText().toString())) {
                            new RegisterTask(edtUsername.getText().toString(), SpaceCrackApplication.hashPassword(edtPassword.getText().toString()), SpaceCrackApplication.hashPassword(edtConfirmPassword.getText().toString()), edtEmail.getText().toString()).execute(SpaceCrackApplication.URL_REGISTER);
                        } else {
                            Toast.makeText(getActivity(), getString(R.string.valid_email_error), Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(getActivity(), getString(R.string.password_match), Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(getActivity(), getString(R.string.fill_in_fields), Toast.LENGTH_SHORT).show();
                }
            }
        });
        userRequest = this;

        return view;
    }

    public boolean checkPasswords(String password, String confirmPassword) {
        return password.equals(confirmPassword);
    }

    /**
     * Start of the UserTask
     */
    @Override
    public void startTask() {
        btnRegister.setEnabled(false);
        progressDialog = ProgressDialog.show(getActivity(), getString(R.string.please_wait), getString(R.string.registrating));
    }

    /**
     * Callback method from the UserTask
     * @param result
     */
    @Override
    public void userCallback(String result) {
        if (result != null) {
            try {
                Gson gson = new Gson();
                SpaceCrackApplication.user = gson.fromJson(result, User.class);

                if (SpaceCrackApplication.user.profile.image != null) {
                    //Get the image from the Data URI
                    String image = SpaceCrackApplication.user.profile.image.substring(SpaceCrackApplication.user.profile.image.indexOf(",") + 1);
                    byte[] decodedString = Base64.decode(image, 0);
                    SpaceCrackApplication.profilePicture = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                }

                Intent intent = new Intent(getActivity(), HomeActivity.class);
                startActivity(intent);
                getActivity().finish();
            } catch (JsonParseException e) {
                e.printStackTrace();
            }
        } else {
            Toast.makeText(context, getResources().getText(R.string.profile_fail), Toast.LENGTH_SHORT).show();
        }
        btnRegister.setEnabled(true);
        progressDialog.dismiss();
    }

    /**
     * POST request to register the user
     */
    private class RegisterTask extends AsyncTask<String, Void, String> {

        private JSONObject newUser;
        private String email;
        private String password;

        public RegisterTask (String username, String password, String passwordRepeated, String email)
        {
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
            btnRegister.setEnabled(false);
            progressDialog = ProgressDialog.show(getActivity(), getString(R.string.please_wait), getString(R.string.registrating));
        }

        @Override
        protected String doInBackground (String...url)
        {
            return RestService.postRequestWithoutAccessToken(url[0], newUser);
        }

        @Override
        protected void onPostExecute (String result)
        {
            Toast.makeText(getActivity(), result != null ? getResources().getString(R.string.register_succes) : getResources().getString(R.string.register_fail), Toast.LENGTH_SHORT).show();
            SpaceCrackApplication.accessToken = result;
            SharedPreferences sharedPreferences = getActivity().getSharedPreferences("Login", 0);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString("accessToken", result);
            editor.putString("email", email);
            editor.putString("password", password);
            editor.commit();
            if (result != null) {
                new UserTask(userRequest).execute(SpaceCrackApplication.URL_USER);
            }
            btnRegister.setEnabled(true);
            progressDialog.dismiss();
        }
    }
}
