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
import android.widget.GridLayout;
import android.widget.ImageButton;
import android.widget.TextView;


public class MyActivity extends Activity {

    public final static String BROADCAST_ACTION = "ru.tibbo.TibboPanel";
    public final static String RMESSAGE = "RECEIVED";
    TextView tvRT, tvST, tvSetTemp, tvStatus;
    ImageButton btnHFon,btnHFoff,btnModeMan,btnModeAuto;
    GridLayout modegridm, modegrida;
    BroadcastReceiver br;
    String goalTemp = "+%1$d C";
    String strTemp = "%1$s C";
    int CurrSTemp = 22;
    int MaxSTemp = 27;
    int MinSTemp = 17;
    String[] answer_controller = new String[16];
    String[] command_controller = new String[23];

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        modegridm = (GridLayout) findViewById(R.id.modegridm);
        modegrida = (GridLayout) findViewById(R.id.modegrida);
        tvStatus = (TextView) findViewById(R.id.tvStatus);
        tvRT = (TextView) findViewById(R.id.tvRoomTemp);
        tvST = (TextView) findViewById(R.id.tvStreetTemp);
        tvSetTemp = (TextView) findViewById(R.id.tvSetTemp);
        btnHFon = (ImageButton) findViewById(R.id.imbtnHFon);
        btnHFoff = (ImageButton) findViewById(R.id.imbtnHFoff);
        btnModeMan = (ImageButton) findViewById(R.id.imbtnMdman);
        btnModeAuto = (ImageButton) findViewById(R.id.imbtnMdauto);

        //Команды для контроллера
        command_controller[0] = "00"; //Получить все данные
        command_controller[1] = "01"; //Получить температуру на улице
        command_controller[2] = "02"; //Получить температуру в комнате
        command_controller[3] = "03"; //Получить влажность
        command_controller[4] = "04"; //Получить состояние теплого пола (вкл.выкл)
        command_controller[5] = "05"; //Получить состояние фанкойла (выкл.1.2.3.)
        command_controller[6] = "06"; //Получить состояние протечки
        command_controller[7] = "07"; //Включить теплый пол
        command_controller[8] = "08"; //Выключить теплый пол
        command_controller[9] = "09"; //Включить фанкойл 1
        command_controller[10] = "10"; //Включить фанкойл 2
        command_controller[11] = "11"; //Включить фанкойл 3
        command_controller[12] = "12"; //Выключить фанкойл
        command_controller[13] = "13"; //Затенение вверх (в данных номер окна)
        command_controller[14] = "14"; //Затенение вниз (в данных номер окна)
        command_controller[15] = "15"; //Приточка включить
        command_controller[16] = "16"; //Приточка выключить
        command_controller[17] = "17"; //Получить состояние заслонки
        command_controller[18] = "18"; //Получить режим (зима/лето)
        command_controller[19] = "19"; //Получить режим (авто/ручной)
        command_controller[20] = "20"; //Задать режим (авто/ручной)
        command_controller[21] = "21"; //Задать температуру
        command_controller[22] = "22"; //Получить заданную температуру

        //Инициализация массива ответа. Потом заменить на значения из preferences
        answer_controller[0] = "FF"; //Символ начала пакета
        answer_controller[1] = "001"; //ID комнаты
        answer_controller[2] = "00"; //Команда. 00 - получить все
        answer_controller[3] = "--"; //Температура улицы
        answer_controller[4] = "--"; //Температура комнаты
        answer_controller[5] = "22"; //Заданная температура
        answer_controller[6] = "0.5"; //Дельта температур
        answer_controller[7] = "0"; //Режим автомата
        answer_controller[8] = "1"; //Режим зима лето
        answer_controller[9] = "--"; //Влажность
        answer_controller[10] = "0"; //Состояние теплого пола
        answer_controller[11] = "0"; //Состояние фанкойла
        answer_controller[12] = "0"; //Состояние приточки
        answer_controller[13] = " "; //Сообщение информации
        answer_controller[14] = " "; //Сообщение системное
        answer_controller[15] = " "; //Сообщение фатальной ошибки

        //Первичная инициализация экрана
        GuiUpdate(answer_controller);

        // создаем BroadcastReceiver
        br = new BroadcastReceiver() {
            // действия при получении сообщений
            public void onReceive(Context context, Intent intent) {
                answer_controller = Parse(intent.getStringExtra(RMESSAGE));
                GuiUpdate(answer_controller);
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
        /*String[] temp;
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
        return parseresult;*/
        String[] parseresult = new String[16];
        parseresult = message.split(";");
        if (parseresult.length == 1) {return answer_controller;} else {return parseresult;}
    }

    public void GuiUpdate(String[] param) {
        //Обновление экрана
        String stat;
        tvST.setText(String.format(strTemp,param[3]));
        tvRT.setText(String.format(strTemp,param[4]));
        stat = "Статус: ТП ";
        if (param[10].equals("0")) {stat += "выкл; ";} else {stat += "вкл; ";}
        stat += "Конд ";
        if (param[11].equals("0")) {stat += "выкл; ";} else {stat += "вкл; ";}
        stat += "Пр ";
        if (param[12].equals("0")) {stat += "выкл; " ;} else {stat += "вкл; ";}
        tvStatus.setText(stat);
        CurrSTemp = Integer.parseInt(param[5]);
        //Если режим автомата (0) или режим ручной (1)
        if (param[7].equals("0")) {
            btnModeAuto.setBackgroundResource(R.drawable.mdaten);
            btnModeMan.setBackgroundResource(R.drawable.mdmandis);
            btnModeAuto.setEnabled(false);
            btnModeMan.setEnabled(true);
            modegrida.setVisibility(modegridm.VISIBLE);
            modegridm.setVisibility(modegrida.GONE);
            tvSetTemp.setText(String.format(goalTemp,CurrSTemp));
        }
        else {
            btnModeMan.setBackgroundResource(R.drawable.mdmanen);
            btnModeAuto.setBackgroundResource(R.drawable.mdatdis);
            btnModeMan.setEnabled(false);
            btnModeAuto.setEnabled(true);
            modegridm.setVisibility(modegridm.VISIBLE);
            modegrida.setVisibility(modegridm.GONE);
            if (param[10].equals("0")) {
                btnHFon.setBackgroundResource(R.drawable.tpondis);
                btnHFoff.setBackgroundResource(R.drawable.tpoffen);
                btnHFon.setEnabled(true);
                btnHFoff.setEnabled(false);
            }
            else {
                btnHFon.setBackgroundResource(R.drawable.tponen);
                btnHFoff.setBackgroundResource(R.drawable.tpoffdis);
                btnHFon.setEnabled(false);
                btnHFoff.setEnabled(true);
            }
        }
    }

    public void onBtnHFon (View v) {
        startService(new Intent(this,TibboService.class).putExtra("Command",answer_controller[0]));
        //startService(new Intent(this,TibboService.class).putExtra("Command","TPon"));
        btnHFon.setBackgroundResource(R.drawable.tponen);
        btnHFoff.setBackgroundResource(R.drawable.tpoffdis);
        btnHFon.setEnabled(false);
        btnHFoff.setEnabled(true);
    }

    public void onBtnHFoff (View v) {
        startService(new Intent(this,TibboService.class).putExtra("Command","Привет"));
        btnHFon.setBackgroundResource(R.drawable.tpondis);
        btnHFoff.setBackgroundResource(R.drawable.tpoffen);
        btnHFon.setEnabled(true);
        btnHFoff.setEnabled(false);
    }

    public void onBtnModeauto (View v) {
        startService(new Intent(this,TibboService.class).putExtra("Command","Auto"));
        btnModeAuto.setBackgroundResource(R.drawable.mdaten);
        btnModeMan.setBackgroundResource(R.drawable.mdmandis);
        btnModeAuto.setEnabled(false);
        btnModeMan.setEnabled(true);
        modegrida.setVisibility(modegridm.VISIBLE);
        modegridm.setVisibility(modegrida.GONE);
    }

    public void onBtnModemanual (View v) {
        startService(new Intent(this,TibboService.class).putExtra("Command","Manual"));
        btnModeMan.setBackgroundResource(R.drawable.mdmanen);
        btnModeAuto.setBackgroundResource(R.drawable.mdatdis);
        btnModeMan.setEnabled(false);
        btnModeAuto.setEnabled(true);
        modegridm.setVisibility(modegridm.VISIBLE);
        modegrida.setVisibility(modegridm.GONE);
    }

    public void onBtnSetTempUp (View v) {
        String tempmess;
        if (CurrSTemp>MaxSTemp) {
            tempmess = "max";
        }
        else if (CurrSTemp == MaxSTemp) {
            tempmess = "max";
            CurrSTemp++;
        }
        else {
            CurrSTemp++;
            tempmess = String.format(goalTemp,CurrSTemp);
        }
        tvSetTemp.setText(tempmess);
        startService(new Intent(this,TibboService.class).putExtra("Command",tempmess));
    }

    public void onBtnSetTempDown (View v) {
        String tempmess;
        if (CurrSTemp<MinSTemp) {
            tempmess = "min";
        }
        else if (CurrSTemp==MinSTemp) {
            tempmess = "min";
            CurrSTemp--;
        }
        else {
            CurrSTemp--;
            tempmess = String.format(goalTemp,CurrSTemp);
        }
        tvSetTemp.setText(tempmess);
        startService(new Intent(this,TibboService.class).putExtra("Command",tempmess));
    }
}
