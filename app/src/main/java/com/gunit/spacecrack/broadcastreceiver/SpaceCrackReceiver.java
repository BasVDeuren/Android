package com.gunit.spacecrack.broadcastreceiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.gunit.spacecrack.service.SpaceCrackService;

/**
 * Created by Dimitri on 11/03/14.
 */
public class SpaceCrackReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        context.startService(new Intent(SpaceCrackService.class.getName()));
    }
}
