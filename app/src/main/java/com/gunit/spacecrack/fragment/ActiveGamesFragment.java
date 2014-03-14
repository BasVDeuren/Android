package com.gunit.spacecrack.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.gunit.spacecrack.R;
import com.gunit.spacecrack.adapter.GameAdapter;
import com.gunit.spacecrack.game.GameActivity;
import com.gunit.spacecrack.json.GameViewModel;

import java.util.List;

/**
 * Created by Dimitri on 28/02/14.
 */

/**
 * Fragment to show all the active games of the user
 */
public class ActiveGamesFragment extends Fragment implements AdapterView.OnItemClickListener {

    private ListView lstGames;
    private List games;
    private TextView leftArrow;

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_games, container, false);

        TextView txtTitle = (TextView) view.findViewById(R.id.txt_lobby_title);
        txtTitle.setText(R.string.active_games);

        games = LobbyFragment.activeGames;
        leftArrow = (TextView) view.findViewById(R.id.img_lobby_leftarrow);
        leftArrow.setVisibility(View.INVISIBLE);
        lstGames = (ListView) view.findViewById(R.id.lst_games_games);
        GameAdapter gameAdapter = new GameAdapter(getActivity(), games);
        lstGames.setAdapter(gameAdapter);
        lstGames.setOnItemClickListener(this);
        TextView txtNoGames = (TextView) view.findViewById(R.id.txt_lobby_no_games);
        lstGames.setEmptyView(txtNoGames);

        return view;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        GameViewModel game = (GameViewModel) games.get(position);
        Intent intent = new Intent(getActivity(), GameActivity.class);
        intent.putExtra("gameId", game.gameId);
        intent.putExtra("replay", false);
        startActivity(intent);
    }

}
