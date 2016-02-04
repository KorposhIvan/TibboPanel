package ru.tibbo.TibboPanel;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.widget.TextView;


public class MyActivity extends Activity {

    public final static String BROADCAST_ACTION = "ru.tibbo.TibboPanel";
    public final static String RMESSAGE = "RECEIVED";
    private TimeBroadcastReceiver mTimeBroadCastReceiver = new TimeBroadcastReceiver();
    TextView tv;
    BroadcastReceiver br;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        tv = (TextView) findViewById(R.id.rmes);
        tv.setText("Waiting...");
        // создаем BroadcastReceiver
        br = new BroadcastReceiver() {
            // действия при получении сообщений
            public void onReceive(Context context, Intent intent) {
                String result = intent.getStringExtra(RMESSAGE);
                tv.setText(result);
            }
        };
        // создаем фильтр для BroadcastReceiver
        IntentFilter intFilt = new IntentFilter(BROADCAST_ACTION);
        // регистрируем (включаем) BroadcastReceiver
        registerReceiver(br, intFilt);
        registerBroadcastReceiver();
        Intent intent = new Intent(this,TibboService.class);
        startService(intent);
    }

    public void registerBroadcastReceiver() {
        this.registerReceiver(mTimeBroadCastReceiver, new IntentFilter(
                "android.intent.action.TIME_TICK"));
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // дерегистрируем (выключаем) BroadcastReceiver
        unregisterReceiver(br);
    }

}
