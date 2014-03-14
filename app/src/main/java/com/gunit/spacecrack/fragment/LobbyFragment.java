package com.gunit.spacecrack.fragment;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.IconButton;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.JsonParseException;
import com.google.gson.reflect.TypeToken;
import com.gunit.spacecrack.R;
import com.gunit.spacecrack.adapter.ViewPageAdapter;
import com.gunit.spacecrack.application.SpaceCrackApplication;
import com.gunit.spacecrack.json.GameViewModel;
import com.gunit.spacecrack.json.PlayerViewModel;
import com.gunit.spacecrack.restservice.RestService;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Dimitri on 12/03/14.
 */

/**
 * LobbyFragment loads all the current games of the user, which will be displayed by the matching Fragment
 */
public class LobbyFragment extends Fragment {

    private ProgressDialog progressDialog;
    private ViewPager viewPager;
    private IconButton btnRefresh;
    private static List<GameViewModel> games;
    protected static List<GameViewModel> activeGames;
    protected static List<GameViewModel> pendingGames;
    protected static List<GameViewModel> invitedGames;

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_lobby, container, false);

        viewPager = (ViewPager) view.findViewById(R.id.vpg_lobby_viewpager);
        btnRefresh = (IconButton) view.findViewById(R.id.btn_lobby_refresh);
        btnRefresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new GamesTask().execute(SpaceCrackApplication.URL_GAME);
            }
        });

        games = new ArrayList<GameViewModel>();
        activeGames = new ArrayList<GameViewModel>();
        pendingGames = new ArrayList<GameViewModel>();
        invitedGames = new ArrayList<GameViewModel>();
        new GamesTask().execute(SpaceCrackApplication.URL_GAME);

        return view;
    }

    /**
     * Fragments which will be used by the ViewPager
     * @return
     */
    private List<Fragment> getFragments() {
        List<Fragment> fragments = new ArrayList<Fragment>();
        fragments.add(new ActiveGamesFragment());
        fragments.add(new PendingRequestsFragment());
        fragments.add(new InvitedGamesFragment());
        return fragments;
    }

    private List<GameViewModel> getActiveGames() {
        List<GameViewModel> gameViewModels = new ArrayList<GameViewModel>();
        for (GameViewModel game : games) {
            List<PlayerViewModel> players = getPlayersOrdered(game);
            if (players.get(0).requestAccepted && players.get(1).requestAccepted) {
                gameViewModels.add(game);
            }
        }
        return gameViewModels;
    }

    private List<GameViewModel> getPendingGames() {
        List<GameViewModel> gameViewModels = new ArrayList<GameViewModel>();
        for (GameViewModel game : games) {
            List<PlayerViewModel> players = getPlayersOrdered(game);
            if (players.get(0).requestAccepted && !players.get(1).requestAccepted) {
                gameViewModels.add(game);
            }
        }
        return gameViewModels;
    }

    private List<GameViewModel> getInvitedGames() {
        List<GameViewModel> gameViewModels = new ArrayList<GameViewModel>();
        for (GameViewModel game : games) {
            List<PlayerViewModel> players = getPlayersOrdered(game);
            if (!players.get(0).requestAccepted && players.get(1).requestAccepted) {
                gameViewModels.add(game);
            }
        }
        return gameViewModels;
    }

    /**
     * Determine who is player1 and player2
     * @param game
     * @return
     */
    private List<PlayerViewModel> getPlayersOrdered(GameViewModel game) {
        List<PlayerViewModel> players = new ArrayList<PlayerViewModel>();
        if (game.player1.profileId == SpaceCrackApplication.user.profile.profileId) {
            players.add(game.player1);
            players.add(game.player2);
        } else {
            players.add(game.player2);
            players.add(game.player1);
        }
        return players;
    }

    /**
     *  GET request to get the games
     */
    private class GamesTask extends AsyncTask<String, Void, String> {

        private int currentItem = -1;
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = ProgressDialog.show(getActivity(), getString(R.string.please_wait), getString(R.string.retrieving_games));
            btnRefresh.setEnabled(false);
            currentItem = viewPager.getCurrentItem();
        }

        @Override
        protected String doInBackground (String...url)
        {
            return RestService.getRequest(url[0]);
        }

        @Override
        protected void onPostExecute (String result)
        {
            progressDialog.dismiss();
            btnRefresh.setEnabled(true);
            if (result != null) {
                try {
                    Log.i("Lobby games", result);
                    Gson gson = new Gson();
                    Type listType = new TypeToken<List<GameViewModel>>(){}.getType();
                    games = gson.fromJson(result, listType);
                    activeGames = getActiveGames();
                    pendingGames = getPendingGames();
                    invitedGames = getInvitedGames();
                } catch (JsonParseException e) {
                    e.printStackTrace();
                }
            } else {
                Toast.makeText(getActivity(), getResources().getText(R.string.game_not_found), Toast.LENGTH_SHORT).show();
            }
            List<Fragment> fragments = getFragments();
            ViewPageAdapter viewPageAdapter = new ViewPageAdapter(getFragmentManager(), fragments);
            viewPager.setAdapter(viewPageAdapter);
            if (currentItem >= 0) {
                viewPager.setCurrentItem(currentItem);
            }
        }
    }
}
