package ru.tibbo.TibboPanel;

import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import java.util.List;


/**
 * Created by Ivan on 05.02.2016.
 */
public class TimeBroadcastReceiver extends BroadcastReceiver {
    public TimeBroadcastReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        context.startService(new Intent(context, ru.tibbo.TibboPanel.TibboService.class));
        //if (!isServiceRunning(context,"TibboPanel")) {context.startService(new Intent(context, ru.tibbo.TibboPanel.TibboService.class));}
    }

    public boolean isServiceRunning(Context context, String ServiceClassName){
        ActivityManager am = (ActivityManager) context.getSystemService(context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningServiceInfo> rs = am.getRunningServices(50);

        for (int i = 0; i < rs.size(); i++) {
            ActivityManager.RunningServiceInfo rsi = rs.get(i);
            if (rsi.service.getClassName().equals(ServiceClassName)){
                return true;
            }
        }
        return false;
    }
}