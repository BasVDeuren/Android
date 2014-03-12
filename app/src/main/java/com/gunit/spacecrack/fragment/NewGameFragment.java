package com.gunit.spacecrack.fragment;

import android.app.Fragment;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.ContactsContract.CommonDataKinds;
import android.provider.ContactsContract.Contacts;
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
import android.widget.Toast;

import com.facebook.FacebookException;
import com.facebook.FacebookOperationCanceledException;
import com.facebook.Request;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.model.GraphUser;
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

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Dimitri on 28/02/14.
 */
public class NewGameFragment extends Fragment implements AdapterView.OnItemClickListener {

    private EditText edtGameName;
    private EditText edtOpponent;
    private IconButton btnSearch;
    private IconButton btnContact;
    private IconButton btnFacebook;
    private IconButton btnRandom;
    private Button btnCreateGame;
    private ListView lstUsers;
    private List users;
    private User selectedUser;
    private RadioGroup rdgUserType;
    private RadioButton rdbUsername;
    private RadioButton rdbEemail;

    private final int CONTACT_PICKER_RESULT = 1;
    private final int RESULT_OK = -1;

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_new_game, container, false);
        edtGameName = (EditText) view.findViewById(R.id.edt_newgame_gamename);
        edtOpponent = (EditText) view.findViewById(R.id.edt_newgame_opponent);

        btnSearch = (IconButton) view.findViewById(R.id.btn_newgame_search);

        btnContact = (IconButton) view.findViewById(R.id.btn_newgame_contact);
        btnFacebook = (IconButton) view.findViewById(R.id.btn_newgame_facebook);
        btnRandom = (IconButton) view.findViewById(R.id.btn_newgame_random);

        rdgUserType = (RadioGroup) view.findViewById(R.id.rdg_newgame_usertype);
        rdbUsername = (RadioButton) view.findViewById(R.id.rdb_newgame_username);
        rdbEemail = (RadioButton) view.findViewById(R.id.rdb_newgame_email);
        rdgUserType.check(rdbUsername.getId());

        btnCreateGame = (Button) view.findViewById(R.id.btn_newgame_create);

        addListeners();

        edtGameName.setText("Android");
        edtOpponent.setText("test");
        btnSearch.callOnClick();

        lstUsers = (ListView) view.findViewById(R.id.lst_newgame_users);
        lstUsers.setOnItemClickListener(this);

//        List<String> users = getNameEmailDetails();
        return view;
    }

    private void addListeners() {
        btnSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (edtOpponent.getText() != null) {
                    if (rdbUsername.isChecked()) {
                        new FindUserTask(true).execute(SpaceCrackApplication.URL_FIND_USERNAME + "/" + edtOpponent.getText().toString());
                    } else if (rdbEemail.isChecked()) {
                        new FindUserTask(true).execute(SpaceCrackApplication.URL_FIND_EMAIL + "/" + edtOpponent.getText().toString());
                    }
                }
            }
        });
        btnContact.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent contactPickerIntent = new Intent(Intent.ACTION_PICK, Contacts.CONTENT_URI);
                contactPickerIntent.setType(CommonDataKinds.Email.CONTENT_TYPE);
                startActivityForResult(contactPickerIntent, CONTACT_PICKER_RESULT);
            }
        });
        btnFacebook.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendRequestDialog();
            }
        });
        btnRandom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new FindUserTask(false).execute(SpaceCrackApplication.URL_FIND_USERID + "/" + SpaceCrackApplication.user.userId);
            }
        });
        btnCreateGame.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!edtGameName.getText().toString().equals("") && selectedUser != null) {
                    Intent intent = new Intent(getActivity(), GameActivity.class);
                    intent.putExtra("gameName", edtGameName.getText().toString());
                    intent.putExtra("opponent", selectedUser.userId);
                    startActivity(intent);
                } else {
                    Toast.makeText(getActivity(), getResources().getText(R.string.fill_in_fields), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

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
                            SpaceCrackApplication.friends = users;

                        }
                    });
            Bundle params = new Bundle();
            params.putString("fields", "id,name,picture");
            friendRequest.setParameters(params);
            friendRequest.executeAsync();
        }
    }

    private void sendRequestDialog() {
        Bundle params = new Bundle();
        params.putString("message", getString(R.string.play_a_game));

        WebDialog requestsDialog = (
                new WebDialog.RequestsDialogBuilder(getActivity(),
                        Session.getActiveSession(),
                        params))
                .setOnCompleteListener(new WebDialog.OnCompleteListener() {

                    @Override
                    public void onComplete(Bundle values,
                                           FacebookException error) {
                        if (error != null) {
                            if (error instanceof FacebookOperationCanceledException) {
                                Toast.makeText(getActivity().getApplicationContext(),
                                        "Request cancelled",
                                        Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(getActivity().getApplicationContext(),
                                        "Network Error",
                                        Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            final String requestId = values.getString("request");
                            if (requestId != null) {
                                Toast.makeText(getActivity().getApplicationContext(),
                                        "Request sent",
                                        Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(getActivity().getApplicationContext(),
                                        "Request cancelled",
                                        Toast.LENGTH_SHORT).show();
                            }
                        }
                    }

                })
                .build();
        requestsDialog.show();
    }

    //GET request to find the users
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
                    } else {
                        selectedUser = gson.fromJson(result, User.class);
                        edtOpponent.setText(selectedUser.username);
                    }
                } catch (JsonParseException e) {
                    e.printStackTrace();
                }
            } else {
                Toast.makeText(getActivity(), getResources().getText(R.string.user_not_found), Toast.LENGTH_SHORT).show();
            }
        }
    }

}
