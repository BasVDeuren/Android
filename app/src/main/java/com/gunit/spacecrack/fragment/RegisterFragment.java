package com.gunit.spacecrack.fragment;

import android.app.Fragment;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.gunit.spacecrack.R;
import com.gunit.spacecrack.application.SpaceCrackApplication;
import com.gunit.spacecrack.activity.HomeActivity;
import com.gunit.spacecrack.restservice.RestService;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Dimitri on 20/02/14.
 */
public class RegisterFragment extends Fragment {

    private Button btnRegister;
    private EditText txtUsername;
    private EditText txtPassword;
    private EditText txtConfirmPassword;
    private EditText txtEmail;

    private static final String TAG = "RegisterFragment";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_register, container, false);

        txtUsername = (EditText) view.findViewById(R.id.edt_register_username);
        txtPassword = (EditText) view.findViewById(R.id.edt_register_password);
        txtConfirmPassword = (EditText) view.findViewById(R.id.edt_register_password_confirm);
        txtEmail = (EditText) view.findViewById(R.id.edt_register_email);
        btnRegister = (Button) view.findViewById(R.id.btn_register_register);
        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (checkPasswords(txtPassword.getText().toString(), txtConfirmPassword.getText().toString())) {
                    new RegisterTask(txtUsername.getText().toString(), txtPassword.getText().toString(), txtConfirmPassword.getText().toString(), txtEmail.getText().toString()).execute(SpaceCrackApplication.URL_REGISTER);
                } else {
                    Toast.makeText(getActivity(), getActivity().getResources().getString(R.string.password_match), Toast.LENGTH_SHORT).show();
                }
            }
        });

        return view;
    }

    public boolean checkPasswords(String password, String confirmPassword) {
        return password.equals(confirmPassword);
    }

    //POST request to postRequest the user
    public class RegisterTask extends AsyncTask<String, Void, String> {

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
        }

        @Override
        protected String doInBackground (String...url)
        {
            return RestService.postRequest(url[0], newUser);
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
                Intent intent = new Intent(getActivity(), HomeActivity.class);
                startActivity(intent);
                getActivity().finish();
            }
            btnRegister.setEnabled(true);
        }
    }
}
