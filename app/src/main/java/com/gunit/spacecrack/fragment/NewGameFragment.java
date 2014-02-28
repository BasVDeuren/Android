package com.gunit.spacecrack.fragment;

import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.gunit.spacecrack.R;
import com.gunit.spacecrack.game.GameActivity;
import com.gunit.spacecrack.model.Game;

/**
 * Created by Dimitri on 28/02/14.
 */
public class NewGameFragment extends Fragment {

    private EditText gameName;
    private EditText opponent;
    private Button createGame;

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_new_game, container, false);
        gameName = (EditText) view.findViewById(R.id.edt_newgame_gamename);
        opponent = (EditText) view.findViewById(R.id.edt_newgame_opponent);
        createGame = (Button) view.findViewById(R.id.btn_newgame_create);
        createGame.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!gameName.getText().toString().equals("") && !opponent.getText().toString().equals("")) {
                    Intent intent = new Intent(getActivity(), GameActivity.class);
                    intent.putExtra("gameName", gameName.getText().toString());
                    intent.putExtra("opponent", opponent.getText().toString());
                    startActivity(intent);
                } else {
                    Toast.makeText(getActivity(), getResources().getText(R.string.fill_in_fields), Toast.LENGTH_SHORT).show();
                }
            }
        });
        return view;
    }

}
