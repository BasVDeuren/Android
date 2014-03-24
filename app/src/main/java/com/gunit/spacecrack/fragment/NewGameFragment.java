package com.gunit.spacecrack.fragment;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.ContactsContract.CommonDataKinds;
import android.provider.ContactsContract.Contacts;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.IconButton;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.FacebookException;
import com.facebook.FacebookOperationCanceledException;
import com.facebook.Session;
import com.facebook.widget.WebDialog;
import com.google.gson.Gson;
import com.google.gson.JsonParseException;
import com.google.gson.reflect.TypeToken;
import com.gunit.spacecrack.R;
import com.gunit.spacecrack.adapter.UserAdapter;
import com.gunit.spacecrack.application.SpaceCrackApplication;
import com.gunit.spacecrack.game.GameActivity;
import com.gunit.spacecrack.model.User;
import com.gunit.spacecrack.restservice.RestService;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Dimitri on 28/02/14.
 */

/**
 * Displays the select screen for a new Game
 */
public class NewGameFragment extends Fragment implements AdapterView.OnItemClickListener {

    private EditText edtGameName;
    private EditText edtOpponent;
    private IconButton btnSearch;
    private IconButton btnMail;
    private IconButton btnContact;
    private IconButton btnFacebook;
    private IconButton btnRandom;
    private Button btnCreateGame;
    private ListView lstUsers;
    private List users;
    private User selectedUser;
    private RadioGroup rdgUserType;
    private RadioButton rdbUsername;
    private RadioButton rdbEmail;
    private TextView txtNoUsers;

    private final int CONTACT_PICKER_RESULT = 1;
    private final int RESULT_OK = -1;
    private boolean contactPicked = false;
    private final String TAG = "NewGameFragment";

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_new_game, container, false);
        edtGameName = (EditText) view.findViewById(R.id.edt_newgame_gamename);
        edtOpponent = (EditText) view.findViewById(R.id.edt_newgame_opponent);

        btnSearch = (IconButton) view.findViewById(R.id.btn_newgame_search);
        btnMail = (IconButton) view.findViewById(R.id.btn_newgame_mail);
        btnContact = (IconButton) view.findViewById(R.id.btn_newgame_contact);
        btnFacebook = (IconButton) view.findViewById(R.id.btn_newgame_facebook);
        btnRandom = (IconButton) view.findViewById(R.id.btn_newgame_random);

        rdgUserType = (RadioGroup) view.findViewById(R.id.rdg_newgame_usertype);
        rdbUsername = (RadioButton) view.findViewById(R.id.rdb_newgame_username);
        rdbEmail = (RadioButton) view.findViewById(R.id.rdb_newgame_email);
        rdgUserType.check(rdbUsername.getId());

        txtNoUsers = (TextView) view.findViewById(R.id.txt_newgame_no_users);

        btnCreateGame = (Button) view.findViewById(R.id.btn_newgame_create);

        addListeners();

        lstUsers = (ListView) view.findViewById(R.id.lst_newgame_users);
        lstUsers.setOnItemClickListener(this);
        lstUsers.setEmptyView(txtNoUsers);

        return view;
    }

    private void addListeners() {
        btnSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (edtOpponent.getText() != null) {
                    if (rdbUsername.isChecked()) {
                        new FindUserTask(true).execute(SpaceCrackApplication.URL_FIND_USERNAME + "/" + edtOpponent.getText().toString());
                    } else if (rdbEmail.isChecked() || contactPicked) {
                        if (SpaceCrackApplication.isValidEmail(edtOpponent.getText().toString())) {
                            new FindUserTask(true).execute(SpaceCrackApplication.URL_FIND_EMAIL + "/" + edtOpponent.getText().toString());
                        } else {
                            Toast.makeText(getActivity(), R.string.valid_email_error, Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            }
        });
        btnMail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setTitle(R.string.email)
                        .setMessage(R.string.send_email)
                        .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if (SpaceCrackApplication.isValidEmail(edtOpponent.getText().toString())) {
                                    new InvitationTask(edtOpponent.getText().toString()).execute(SpaceCrackApplication.URL_INVITATION);
                                } else {
                                    Toast.makeText(getActivity(), R.string.valid_email_error, Toast.LENGTH_SHORT).show();
                                }
                            }
                        })
                        .setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                btnSearch.setVisibility(View.VISIBLE);
                                btnMail.setVisibility(View.GONE);
                            }
                        });
                AlertDialog dialog = builder.create();
                dialog.show();
            }
        });
        btnContact.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                btnSearch.setVisibility(View.VISIBLE);
                btnMail.setVisibility(View.GONE);
                Intent contactPickerIntent = new Intent(Intent.ACTION_PICK, Contacts.CONTENT_URI);
                contactPickerIntent.setType(CommonDataKinds.Email.CONTENT_TYPE);
                startActivityForResult(contactPickerIntent, CONTACT_PICKER_RESULT);
            }
        });
        btnFacebook.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                btnSearch.setVisibility(View.VISIBLE);
                btnMail.setVisibility(View.GONE);
                sendRequestDialog();
            }
        });
        btnRandom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                btnSearch.setVisibility(View.VISIBLE);
                btnMail.setVisibility(View.GONE);
                new FindUserTask(false).execute(SpaceCrackApplication.URL_FIND_USERID + "/" + SpaceCrackApplication.user.userId);
            }
        });
        btnCreateGame.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                btnSearch.setVisibility(View.VISIBLE);
                btnMail.setVisibility(View.GONE);
                if (!edtGameName.getText().toString().equals("") && selectedUser != null) {
                    if (SpaceCrackApplication.isPlainText(edtGameName.getText().toString())) {
                        Intent intent = new Intent(getActivity(), GameActivity.class);
                        intent.putExtra("gameName", edtGameName.getText().toString());
                        intent.putExtra("opponentId", selectedUser.userId);
                        intent.putExtra("opponentName", selectedUser.username);
                        startActivity(intent);
                    } else {
                        Toast.makeText(getActivity(), getString(R.string.symbols_not_allowed), Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(getActivity(), getResources().getText(R.string.fill_in_fields), Toast.LENGTH_SHORT).show();
                }
            }
        });
        rdgUserType.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (rdbUsername.getId() == checkedId) {
                    btnSearch.setVisibility(View.VISIBLE);
                    btnMail.setVisibility(View.GONE);
                }
            }
        });
    }

    /**
     * Callback method of the Contact Picker
     * @param requestCode
     * @param resultCode
     * @param data
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            //Get the contact information
            if (requestCode == CONTACT_PICKER_RESULT) {
                Cursor cursor = null;
                String email;
                String name;
                try {
                    Uri result = data.getData();
                    Log.i("Contact result", result.toString());
                    cursor = getActivity().getContentResolver().query(result, null, null, null, null);
                    int nameId = cursor.getColumnIndex(Contacts.DISPLAY_NAME);
                    int emailId = cursor.getColumnIndex(CommonDataKinds.Email.DATA);
                    if (cursor.moveToFirst()) {
                        email = cursor.getString(emailId);
                        name = cursor.getString(nameId);
                        Log.i("Name", name);
                        Log.i("Email", email);
                        edtOpponent.setText(email);
                        contactPicked = true;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    if (cursor != null) {
                        cursor.close();
                    }
                }
            }
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        selectedUser = (User) users.get(position);
        edtOpponent.setText(selectedUser.username);
    }

    /**
     * Shows a request dialog from Facebook
     */
    private void sendRequestDialog() {
        if (Session.getActiveSession() != null) {
            Bundle params = new Bundle();
            params.putString("message", getString(R.string.play_a_game));

            WebDialog requestsDialog = (
                    new WebDialog.RequestsDialogBuilder(getActivity(),
                            Session.getActiveSession(),
                            params))
                    .build();
            requestsDialog.show();
        } else {
            Toast.makeText(getActivity(), getString(R.string.login_facebook), Toast.LENGTH_SHORT).show();
        }
    }

    public User getSelectedUser() {
        return selectedUser;
    }

    public List getUsers() {
        return users;
    }

    /**
     * GET request to find the users
     */
    private class FindUserTask extends AsyncTask<String, Void, String> {

        private boolean multiple;

        public FindUserTask (boolean multiple) {
            this.multiple = multiple;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            btnSearch.setEnabled(false);
            btnContact.setEnabled(false);
            btnFacebook.setEnabled(false);
            btnRandom.setEnabled(false);
        }

        @Override
        protected String doInBackground (String...url)
        {
            return RestService.getRequest(url[0]);
        }

        @Override
        protected void onPostExecute (String result)
        {
            btnSearch.setEnabled(true);
            btnContact.setEnabled(true);
            btnFacebook.setEnabled(true);
            btnRandom.setEnabled(true);

            if (result != null) {
                try {
                    Gson gson = new Gson();
                    if (multiple) {
                        users = new ArrayList();
                        Type listType = new TypeToken<List<User>>(){}.getType();
                        users = gson.fromJson(result, listType);
                        for (int i  = 0; i < users.size(); i++) {
                            User user = (User) users.get(i);
                            if (user.userId == SpaceCrackApplication.user.userId) {
                                users.remove(users.get(i));
                            }
                        }
                        users.remove(SpaceCrackApplication.user);
                        UserAdapter userAdapter = new UserAdapter(getActivity(), users);
                        lstUsers.setAdapter(userAdapter);
                        Log.i(TAG, "Users retrieved");
                    } else {
                        selectedUser = gson.fromJson(result, User.class);
                        edtOpponent.setText(selectedUser.username);
                        Log.i(TAG, "Random user selected");
                    }
                } catch (JsonParseException e) {
                    e.printStackTrace();
                }
            } else {
                Toast.makeText(getActivity(), getResources().getText(R.string.user_not_found), Toast.LENGTH_SHORT).show();
                if (rdbEmail.isChecked() || contactPicked) {
                    btnSearch.setVisibility(View.GONE);
                    btnMail.setVisibility(View.VISIBLE);
                }
                contactPicked = false;
            }
        }
    }

    /**
     * POST request to send an email
     */
    private class InvitationTask extends AsyncTask<String, Void, Boolean> {

        private JSONObject user;

        public InvitationTask (String email) {
            try {
                user = new JSONObject();
                user.put("emailAddress", email);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            btnSearch.setEnabled(false);
            btnMail.setEnabled(false);
            btnContact.setEnabled(false);
            btnFacebook.setEnabled(false);
            btnRandom.setEnabled(false);
        }

        @Override
        protected Boolean doInBackground (String...url)
        {
            return RestService.postRequest(url[0], user);
        }

        @Override
        protected void onPostExecute (Boolean result)
        {
            btnSearch.setEnabled(true);
            btnMail.setEnabled(true);
            btnContact.setEnabled(true);
            btnFacebook.setEnabled(true);
            btnRandom.setEnabled(true);

            if (result) {
                Toast.makeText(getActivity(), getString(R.string.email_sent), Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getActivity(), getText(R.string.email_sent_error), Toast.LENGTH_SHORT).show();
            }
            btnSearch.setVisibility(View.VISIBLE);
            btnMail.setVisibility(View.GONE);
        }
    }

}
