package ru.tibbo.TibboPanel;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;


public class MyActivity extends Activity {

    public final static String BROADCAST_ACTION = "ru.tibbo.TibboPanel";
    public final static String RMESSAGE = "RECEIVED";
    TextView tvRT, tvST;
    Button btnSend;
    BroadcastReceiver br;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        tvRT = (TextView) findViewById(R.id.tvRoomTemp);
        tvST = (TextView) findViewById(R.id.tvStreetTemp);
        tvRT.setText("--");
        tvST.setText("--");
        // создаем BroadcastReceiver
        br = new BroadcastReceiver() {
            // действия при получении сообщений
            public void onReceive(Context context, Intent intent) {
                String[] result = Parse(intent.getStringExtra(RMESSAGE));
                tvST.setText(result[0]);
                tvRT.setText(result[1]);
            }
        };

        // создаем фильтр для BroadcastReceiver
        IntentFilter intFilt = new IntentFilter(BROADCAST_ACTION);
        // регистрируем (включаем) BroadcastReceiver
        registerReceiver(br, intFilt);
        Intent intent = new Intent(this,TibboService.class);
        startService(intent);
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

    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.mymenu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId())
        {
            case R.id.menu_settings:
                Intent intentset = new Intent(this, SetActivity.class);
                startActivity(intentset);
                return true;
            case R.id.menu_notifications:
                Intent intentnot = new Intent(this,NotActivity.class);
                startActivity(intentnot);
                return true;
            case R.id.menu_about:
                Intent intentabout = new Intent(this, aboutActivity.class);
                startActivity(intentabout);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public String[] Parse(String message) {
        String[] temp;
        String[] parseresult = new String[2];
        temp = message.split(";");
        if (temp.length == 1) {
            parseresult[0] = "error";
            parseresult[1] = "error";
        }
        else {
            parseresult[0] = temp[0];
            parseresult[1] = temp[1];
        }
        return parseresult;
    }
    public void sendmess(View v) {
        tvRT.setText("Send");
        startService(new Intent(this,TibboService.class).putExtra("Command","We are click the activity!"));
    }
}
