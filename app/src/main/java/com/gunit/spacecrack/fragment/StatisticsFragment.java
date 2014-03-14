package com.gunit.spacecrack.fragment;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.JsonParseException;
import com.gunit.spacecrack.R;
import com.gunit.spacecrack.application.SpaceCrackApplication;
import com.gunit.spacecrack.json.StatisticsViewModel;
import com.gunit.spacecrack.restservice.RestService;

/**
 * Created by Dimitri on 7/03/14.
 */

/**
 * Fragment to show the Statistics
 */
public class StatisticsFragment extends Fragment {

    private TextView txtWinRatio;
    private TextView txtAmountGames;
    private TextView txtAmountColonies;
    private TextView txtAmountShips;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_statistics, container, false);

        txtWinRatio = (TextView) view.findViewById(R.id.txt_statistics_winratio);
        txtAmountGames = (TextView) view.findViewById(R.id.txt_statistics_amountgames);
        txtAmountColonies = (TextView) view.findViewById(R.id.txt_statistics_averagecolonies);
        txtAmountShips = (TextView) view.findViewById(R.id.txt_statistics_averageships);

        new GetStatistics().execute(SpaceCrackApplication.URL_STATISTICS);

        return view;
    }

    private class GetStatistics extends AsyncTask<String, Void, String> {
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
                    StatisticsViewModel statisticsViewModel = gson.fromJson(result, StatisticsViewModel.class);
                    if (statisticsViewModel != null) {
                        txtWinRatio.setText(String.valueOf(statisticsViewModel.winRatio));
                        txtAmountGames.setText(String.valueOf(statisticsViewModel.amountOfGames));
                        txtAmountColonies.setText(String.valueOf(statisticsViewModel.averageAmountOfColoniesPerWin));
                        txtAmountShips.setText(String.valueOf(statisticsViewModel.averageAmountOfShipsPerWin));
                    }
                } catch (JsonParseException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
