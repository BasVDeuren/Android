package com.gunit.spacecrack.fragment;

import android.app.Fragment;
import android.app.ProgressDialog;
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
import com.gunit.spacecrack.restservice.RestService;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Dimitri on 7/03/14.
 */
public class PasswordFragment extends Fragment {

    private EditText edtOldPassword;
    private EditText edtNewPassword;
    private EditText edtRepeatPassword;
    private Button btnSave;
    private ProgressDialog progressDialog;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_password, container, false);

        //Find the views
        edtOldPassword = (EditText) view.findViewById(R.id.edt_password_oldpassword);
        edtNewPassword = (EditText) view.findViewById(R.id.edt_password_newpassword);
        edtRepeatPassword = (EditText) view.findViewById(R.id.edt_password_repeatpassword);
        btnSave = (Button) view.findViewById(R.id.btn_password_save);
        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!edtOldPassword.getText().equals("") && !edtNewPassword.getText().equals("") && !edtRepeatPassword.getText().equals("")) {
                    if (validPasswords()) {
                        new PasswordTask(edtNewPassword.getText().toString(), edtRepeatPassword.getText().toString()).execute(SpaceCrackApplication.URL_USER);
                    } else {
                        Toast.makeText(getActivity(), getString(R.string.password_match), Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(getActivity(), getString(R.string.fill_in_fields), Toast.LENGTH_SHORT).show();
                }
            }
        });
        return view;
    }

    private boolean validPasswords () {
        return edtOldPassword.getText().toString().equals(SpaceCrackApplication.user.password) && edtNewPassword.getText().toString().equals(edtRepeatPassword.getText().toString());
    }

    private class PasswordTask extends AsyncTask<String, Void, Boolean> {

        private JSONObject newUser;
        private String password;

        public PasswordTask (String password, String passwordRepeated) {
            newUser = new JSONObject();
            this.password = password;
            try {
                newUser.put("email", SpaceCrackApplication.user.email);
                newUser.put("username", SpaceCrackApplication.user.username);
                newUser.put("password", password);
                newUser.put("passwordRepeated", passwordRepeated);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            btnSave.setEnabled(false);
            progressDialog = ProgressDialog.show(getActivity(), getString(R.string.please_wait), getString(R.string.processing_data));
        }

        @Override
        protected Boolean doInBackground(String... url) {
            return RestService.editProfile(url[0], newUser);
        }

        @Override
        protected void onPostExecute(Boolean result) {
            progressDialog.dismiss();
            if (result) {
                Toast.makeText(getActivity(), getString(R.string.password_saved), Toast.LENGTH_SHORT).show();
                SpaceCrackApplication.user.password = password;
                SharedPreferences sharedPreferences = getActivity().getSharedPreferences("Login", 0);
                sharedPreferences.edit().putString("password", password).commit();

                //Return to the Profile
                getActivity().getFragmentManager().beginTransaction()
                        .replace(R.id.container, new ProfileFragment(), "Profile")
                        .commit();
            } else {
                Toast.makeText(getActivity(), getString(R.string.password_error), Toast.LENGTH_SHORT).show();
            }
        }
    }
}
