package com.gunit.spacecrack.fragment;


import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
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
import java.util.List;

/**
 * Created by Dimitri on 12/03/14.
 */
public class InvitedGamesFragment extends Fragment implements AdapterView.OnItemClickListener {

    private ListView lstGames;
    private List games;
    private ProgressDialog progressDialog;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_games, container, false);

        TextView txtTitle = (TextView) view.findViewById(R.id.txt_lobby_title);
        txtTitle.setText(R.string.invited_games);

        games = LobbyFragment.pendingGames;
        lstGames = (ListView) view.findViewById(R.id.lst_games_games);
        GameAdapter gameAdapter = new GameAdapter(getActivity(), games);
        lstGames.setAdapter(gameAdapter);
        lstGames.setOnItemClickListener(this);

        return view;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
        final GameViewModel selectedGame = (GameViewModel) games.get(position);
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.invitation)
                .setMessage(R.string.join_game)
                .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        new InviteTask(true, selectedGame.gameId).execute(SpaceCrackApplication.URL_GAMEINVITE + "/" + selectedGame.gameId);
                    }
                })
                .setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        new InviteTask(false, selectedGame.gameId).execute(SpaceCrackApplication.URL_GAMEINVITE + "/" + selectedGame.gameId);
                    }
                });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    //GET request to get the games
    private class InviteTask extends AsyncTask<String, Void, Integer> {

        private boolean accepted;
        private int gameId;

        public InviteTask (boolean accepted, int gameId) {
            this.accepted = accepted;
            this.gameId = gameId;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = ProgressDialog.show(getActivity(), getString(R.string.please_wait), getString(R.string.processing_data));
        }

        @Override
        protected Integer doInBackground (String...url)
        {
            if (accepted) {
                return RestService.acceptInvite(url[0]);
            } else {
                return RestService.declineInvite(url[0]);
            }
        }

        @Override
        protected void onPostExecute (Integer result)
        {
            progressDialog.dismiss();
            if (result == 200) {
                Intent intent = new Intent(getActivity(), GameActivity.class);
                intent.putExtra("gameId", gameId);
                intent.putExtra("replay", false);
                startActivity(intent);
            } else {
                Toast.makeText(getActivity(), getResources().getText(R.string.game_not_found), Toast.LENGTH_SHORT).show();
            }
        }
    }
}
