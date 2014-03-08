package com.gunit.spacecrack.adapter;

import android.app.Activity;
import android.content.Context;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.gunit.spacecrack.R;
import com.gunit.spacecrack.json.GameViewModel;
import com.gunit.spacecrack.model.User;

import java.util.List;

/**
 * Created by Dimitri on 4/03/14.
 */
public class GameAdapter extends BaseAdapter {

    private Context context;
    private List<GameViewModel> gameList;

    public GameAdapter (Context context, List<GameViewModel> gameList) {
        this.context = context;
        this.gameList = gameList;
    }

    @Override
    public int getCount() {
        return gameList.size();
    }

    @Override
    public Object getItem(int position) {
        return gameList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return gameList.indexOf(getItem(position));
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder = null;
        GameViewModel game = (GameViewModel) getItem(position);

        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.list_game_item, null);
            viewHolder = new ViewHolder();
            viewHolder.txtGameName = (TextView) convertView.findViewById(R.id.txt_listgames_name);
            viewHolder.txtTurn = (TextView) convertView.findViewById(R.id.txt_listgames_turn);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        viewHolder.txtGameName.setText(game.name);
//        if (game.player1.turnEnded) {
//            viewHolder.txtTurn.setText(R.string.player2_turn);
//        } else {
//            viewHolder.txtTurn.setText(R.string.player1_turn);
//        }

        return convertView;
    }

    private class ViewHolder {
        TextView txtGameName;
        TextView txtTurn;
    }
}
