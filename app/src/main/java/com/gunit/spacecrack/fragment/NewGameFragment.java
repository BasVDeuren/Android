package com.gunit.spacecrack.fragment;

import android.app.Fragment;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.ContactsContract;
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
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        selectedUser = (User) users.get(position);
        edtOpponent.setText(selectedUser.username);
    }

    private List<String> getNameEmailDetails(){
        List<String> names = new ArrayList<String>();
        ContentResolver cr = getActivity().getContentResolver();
        Cursor cur = cr.query(ContactsContract.Contacts.CONTENT_URI,null, null, null, null);
        if (cur.getCount() > 0) {
            while (cur.moveToNext()) {
                String id = cur.getString(cur.getColumnIndex(ContactsContract.Contacts._ID));
                Cursor cur1 = cr.query(
                        ContactsContract.CommonDataKinds.Email.CONTENT_URI, null,
                        ContactsContract.CommonDataKinds.Email.CONTACT_ID + " = ?",
                        new String[]{id}, null);
                while (cur1.moveToNext()) {
                    //to get the contact names
                    String name=cur1.getString(cur1.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
                    Log.e("Name :", name);
                    String email = cur1.getString(cur1.getColumnIndex(ContactsContract.CommonDataKinds.Email.DATA));
                    Log.e("Email", email);
                    if(email!=null){
                        names.add(name);
                    }
                }
                cur1.close();
            }
        }
        return names;
    }

    //POST request to edit the profile
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
