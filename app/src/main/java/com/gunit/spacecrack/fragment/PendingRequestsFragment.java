package com.gunit.spacecrack.fragment;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import com.gunit.spacecrack.R;
import com.gunit.spacecrack.adapter.GameAdapter;

import java.util.List;

/**
 * Created by Dimitri on 12/03/14.
 */

/**
 * Fragment to show all the Pending requests
 */
public class PendingRequestsFragment extends Fragment {

    private ListView lstGames;
    private List games;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_games, container, false);

        TextView txtTitle = (TextView) view.findViewById(R.id.txt_lobby_title);
        txtTitle.setText(R.string.pending_requests);

        games = LobbyFragment.pendingGames;
        lstGames = (ListView) view.findViewById(R.id.lst_games_games);
        GameAdapter gameAdapter = new GameAdapter(getActivity(), games);
        lstGames.setAdapter(gameAdapter);
        TextView txtNoGames = (TextView) view.findViewById(R.id.txt_lobby_no_games);
        lstGames.setEmptyView(txtNoGames);

        return view;
    }

}
