package ru.tibbo.TibboPanel;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.EditText;

/**
 * Created by Ivan on 08.02.2016.
 */
public class SetActivity extends Activity {
    public static final String APP_PREFERENCES = "tibbosettings";
    public static final String APP_PREFERENCES_IP = "IP";
    public static final String APP_PREFERENCES_PORT = "PORT";
    public static final String APP_PREFERENCES_IDROOM = "IDROOM";

    SharedPreferences tibboSettings;
    EditText etIP;
    EditText etPort;
    EditText etIDroom;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings);

        tibboSettings = getSharedPreferences(APP_PREFERENCES, Context.MODE_PRIVATE);
        etIP = (EditText) findViewById(R.id.etIP);
        etPort = (EditText) findViewById(R.id.etPort);
        etIDroom = (EditText) findViewById(R.id.etIDroom);

        if (tibboSettings.contains(APP_PREFERENCES_IDROOM)) {
            etIDroom.setText(tibboSettings.getString(APP_PREFERENCES_IDROOM,""));
        }
        if (tibboSettings.contains(APP_PREFERENCES_IP)) {
            etIP.setText(tibboSettings.getString(APP_PREFERENCES_IP,""));
        }
        if (tibboSettings.contains(APP_PREFERENCES_PORT)) {
            etPort.setText(tibboSettings.getString(APP_PREFERENCES_PORT,""));
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        String strIP = etIP.getText().toString();
        String strPort = etPort.getText().toString();
        String strIDroom = etIDroom.getText().toString();
        SharedPreferences.Editor editor = tibboSettings.edit();
        editor.putString(APP_PREFERENCES_IDROOM,strIDroom);
        editor.putString(APP_PREFERENCES_IP,strIP);
        editor.putString(APP_PREFERENCES_PORT,strPort);
        editor.apply();
    }
}