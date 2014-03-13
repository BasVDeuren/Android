package com.gunit.spacecrack.fragment;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.JsonParseException;
import com.google.gson.reflect.TypeToken;
import com.gunit.spacecrack.R;
import com.gunit.spacecrack.adapter.GameAdapter;
import com.gunit.spacecrack.adapter.ViewPageAdapter;
import com.gunit.spacecrack.application.SpaceCrackApplication;
import com.gunit.spacecrack.json.GameViewModel;
import com.gunit.spacecrack.model.Game;
import com.gunit.spacecrack.restservice.RestService;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Dimitri on 12/03/14.
 */
public class LobbyFragment extends Fragment {

    private ProgressDialog progressDialog;
    private ViewPager viewPager;
    private static List<GameViewModel> games;
    protected static List<GameViewModel> activeGames;
    protected static List<GameViewModel> pendingGames;
    protected static List<GameViewModel> invitedGames;

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_lobby, container, false);

        viewPager = (ViewPager) view.findViewById(R.id.vpg_lobby_viewpager);

        games = new ArrayList<GameViewModel>();
        activeGames = new ArrayList<GameViewModel>();
        pendingGames = new ArrayList<GameViewModel>();
        invitedGames = new ArrayList<GameViewModel>();
        new GetGamesTask().execute(SpaceCrackApplication.URL_GAME);

        return view;
    }

    private List<Fragment> getFragments() {
        List<Fragment> fragments = new ArrayList<Fragment>();
        fragments.add(new ActiveGamesFragment());
        fragments.add(new PendingRequestsFragment());
        fragments.add(new InvitedGamesFragment());
        return fragments;
    }

    private List<GameViewModel> getActiveGames() {
        List<GameViewModel> gameViewModels = new ArrayList<GameViewModel>();
        for (GameViewModel gameViewModel : games) {
            if (gameViewModel.player1.requestAccepted && gameViewModel.player2.requestAccepted) {
                gameViewModels.add(gameViewModel);
            }
        }
        return gameViewModels;
    }

    private List<GameViewModel> getPendingGames() {
        List<GameViewModel> gameViewModels = new ArrayList<GameViewModel>();
        for (GameViewModel gameViewModel : games) {
            if (gameViewModel.player1.requestAccepted && !gameViewModel.player2.requestAccepted) {
                gameViewModels.add(gameViewModel);
            }
        }
        return gameViewModels;
    }

    private List<GameViewModel> getInvitedGames() {
        List<GameViewModel> gameViewModels = new ArrayList<GameViewModel>();
        for (GameViewModel gameViewModel : games) {
            if (!gameViewModel.player1.requestAccepted && gameViewModel.player2.requestAccepted) {
                gameViewModels.add(gameViewModel);
            }
        }
        return gameViewModels;
    }

    //GET request to get the games
    private class GetGamesTask extends AsyncTask<String, Void, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = ProgressDialog.show(getActivity(), getString(R.string.please_wait), getString(R.string.retrieving_games));
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
            if (result != null) {
                try {
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
        }
    }
}
