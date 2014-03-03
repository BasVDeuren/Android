package com.gunit.spacecrack.fragment;

import android.app.Fragment;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
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
import java.util.List;

/**
 * Created by Dimitri on 28/02/14.
 */
public class NewGameFragment extends Fragment implements AdapterView.OnItemClickListener {

    private EditText gameName;
    private EditText opponent;
    private Button search;
    private Button createGame;
    private ListView usersList;
    private List users;
    private User selectedUser;

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_new_game, container, false);
        gameName = (EditText) view.findViewById(R.id.edt_newgame_gamename);
        opponent = (EditText) view.findViewById(R.id.edt_newgame_opponent);
        search = (Button) view.findViewById(R.id.btn_newgame_search);
        search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (opponent.getText() != null) {
                    new FindUsernameTask().execute(SpaceCrackApplication.URL_FIND_USERNAME + "/" + opponent.getText().toString());
                }
            }
        });
        createGame = (Button) view.findViewById(R.id.btn_newgame_create);
        createGame.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!gameName.getText().toString().equals("") && selectedUser != null) {
                    Intent intent = new Intent(getActivity(), GameActivity.class);
                    intent.putExtra("gameName", gameName.getText().toString());
                    intent.putExtra("opponent", selectedUser.userId);
                    startActivity(intent);
                } else {
                    Toast.makeText(getActivity(), getResources().getText(R.string.fill_in_fields), Toast.LENGTH_SHORT).show();
                }
            }
        });
        usersList = (ListView) view.findViewById(R.id.lst_newgame_users);
        usersList.setOnItemClickListener(this);
        return view;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        selectedUser = (User) users.get(position);
        opponent.setText(selectedUser.username);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

    }

    //POST request to edit the profile
    private class FindUsernameTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground (String...url)
        {
            return RestService.getRequest(url[0]);
        }

        @Override
        protected void onPostExecute (String result)
        {
            if (result != null) {
                try {
                    Gson gson = new Gson();
                    Type listType = new TypeToken<List<User>>(){}.getType();
                    users = gson.fromJson(result, listType);
                    UserAdapter userAdapter = new UserAdapter(getActivity(), users);
                    usersList.setAdapter(userAdapter);

                } catch (JsonParseException e) {
                    e.printStackTrace();
                }
            } else {
                Toast.makeText(getActivity(), getResources().getText(R.string.user_not_found), Toast.LENGTH_SHORT).show();
            }
        }
    }

}
