package com.gunit.spacecrack.fragment;

import android.app.Fragment;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.JsonParseException;
import com.google.gson.reflect.TypeToken;
import com.gunit.spacecrack.R;
import com.gunit.spacecrack.adapter.GameAdapter;
import com.gunit.spacecrack.application.SpaceCrackApplication;
import com.gunit.spacecrack.game.GameActivity;
import com.gunit.spacecrack.json.GameViewModel;
import com.gunit.spacecrack.restservice.RestService;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Dimitri on 28/02/14.
 */
public class ActiveGamesFragment extends Fragment implements AdapterView.OnItemClickListener {

    private ListView lstGames;
    private List games;

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_active_games, container, false);

        lstGames = (ListView) view.findViewById(R.id.lst_activegames_games);
        lstGames.setOnItemClickListener(this);
        new GetGamesTask().execute(SpaceCrackApplication.URL_GAME);

        return view;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        GameViewModel game = (GameViewModel) games.get(position);
        Intent intent = new Intent(getActivity(), GameActivity.class);
        intent.putExtra("gameId", game.gameId);
        startActivity(intent);
    }

    //POST request to edit the profile
    private class GetGamesTask extends AsyncTask<String, Void, String> {

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
                    Type listType = new TypeToken<List<GameViewModel>>(){}.getType();
                    games = gson.fromJson(result, listType);
                    GameAdapter gameAdapter = new GameAdapter(getActivity(), games);
                    lstGames.setAdapter(gameAdapter);

                } catch (JsonParseException e) {
                    e.printStackTrace();
                }
            } else {
                Toast.makeText(getActivity(), getResources().getText(R.string.game_not_found), Toast.LENGTH_SHORT).show();
            }
        }
    }

}
