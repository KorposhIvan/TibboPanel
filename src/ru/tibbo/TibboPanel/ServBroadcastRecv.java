package ru.tibbo.TibboPanel;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * Created by Ivan on 04.02.2016.
 */
public class ServBroadcastRecv extends BroadcastReceiver {

    public void onReceive(Context context, Intent intent) {
        context.startService(new Intent(context, ru.tibbo.TibboPanel.TibboService.class));
    }
}