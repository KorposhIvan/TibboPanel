package ru.tibbo.TibboPanel;

import android.app.Activity;
import android.content.*;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.*;


public class MyActivity extends Activity {

    public final static String BROADCAST_ACTION = "ru.tibbo.TibboPanel";
    public final static String RMESSAGE = "RECEIVED";
    TextView tvRT, tvST, tvSetTemp, tvStatus, tvErrMess;
    ImageButton btnHFon,btnHFoff,btnModeMan,btnModeAuto;
    ImageView imgV;
    Button btnPr, btnFCoff, btnFCon1, btnFCon2, btnFCon3;
    GridLayout modegridm, modegridms, modegrida;
    BroadcastReceiver br;
    String goalTemp = "+%1$d C";
    String strTemp = "%1$s C";
    int CurrSTemp = 22;
    int MaxSTemp = 27;
    int MinSTemp = 17;
    String[] answer_controller = new String[16];
    String[] command_controller = new String[23];
    String strTcmdcontr = "FF;%1$s;%2$s;%3$s; ; ; ";
    Boolean blnSockState = false;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        modegridm = (GridLayout) findViewById(R.id.modegridm);
        modegridms = (GridLayout) findViewById(R.id.modegridms);
        modegrida = (GridLayout) findViewById(R.id.modegrida);
        tvStatus = (TextView) findViewById(R.id.tvStatus);
        tvRT = (TextView) findViewById(R.id.tvRoomTemp);
        tvST = (TextView) findViewById(R.id.tvStreetTemp);
        tvSetTemp = (TextView) findViewById(R.id.tvSetTemp);
        tvErrMess = (TextView) findViewById(R.id.tvErrMess);
        btnHFon = (ImageButton) findViewById(R.id.imbtnHFon);
        btnHFoff = (ImageButton) findViewById(R.id.imbtnHFoff);
        btnModeMan = (ImageButton) findViewById(R.id.imbtnMdman);
        btnModeAuto = (ImageButton) findViewById(R.id.imbtnMdauto);
        btnPr = (Button) findViewById(R.id.btnPr);
        btnFCoff = (Button) findViewById(R.id.btnFCoff);
        btnFCon1 = (Button) findViewById(R.id.btnFCon1);
        btnFCon2 = (Button) findViewById(R.id.btnFCon2);
        btnFCon3 = (Button) findViewById(R.id.btnFCon3);
        imgV = (ImageView) findViewById(R.id.imageView);

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

        //------------------Get settings
        SharedPreferences tibboSettings;
        final String APP_PREFERENCES = "tibbosettings";
        final String APP_PREFERENCES_IDROOM = "IDROOM";
        tibboSettings = getSharedPreferences(APP_PREFERENCES, Context.MODE_PRIVATE);

        if (tibboSettings.contains(APP_PREFERENCES_IDROOM)) {
            answer_controller[1] = tibboSettings.getString(APP_PREFERENCES_IDROOM,"001");
        }

        //Инициализация массива ответа. Потом заменить на значения из preferences
        answer_controller[0] = "FF"; //Символ начала пакета
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
        answer_controller[12] = "1"; //Состояние приточки
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
        startService(intent.putExtra("Command",String.format(strTcmdcontr,answer_controller[1],command_controller[0]," ")));
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
        stopService(new Intent(this,TibboService.class));
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
        String[] parseresult = new String[16];
        if (message.equals("Socket is closed")) {
            blnSockState = false;
            return answer_controller;
        }
        blnSockState = true;
        parseresult = message.split(";");
        if (parseresult.length == 1) {
            return answer_controller;
        } else {
            for (int i=0; i<parseresult.length;i++){
                if (parseresult[i].equals(" ")) {parseresult[i]=answer_controller[i];}
            }
            return parseresult;
        }
    }

    public void GuiUpdate(String[] param) {
        //Обновление экрана
        String stat,err;
        tvST.setText(String.format(strTemp,param[3]));
        tvRT.setText(String.format(strTemp,param[4]));
        stat = "Статус: ТП ";
        if (param[10].equals("0")) {stat += "выкл; ";} else {stat += "вкл; ";}
        stat += "Конд ";
        if (param[11].equals("0")) {stat += "выкл; ";} else {stat += "вкл "+param[11]+"; ";}
        stat += "Пр ";
        if (param[12].equals("0")) {
            stat += "min; ";
            btnPr.setBackgroundResource(R.drawable.btn_pr_min_back);
        }
        if (param[12].equals("1")) {
            stat += "norm; ";
            btnPr.setBackgroundResource(R.drawable.btn_pr_norm_back);
        }
        if (param[12].equals("2")) {
            stat += "max; ";
            btnPr.setBackgroundResource(R.drawable.btn_pr_max_back);
        }
        if (param[12].equals("3")) {
            stat += "nd; ";
            btnPr.setBackgroundResource(R.drawable.btn_pr_dis_back);
        }
        if (blnSockState) {stat += "conn";} else {stat += "discon";}
        tvStatus.setText(stat);
        CurrSTemp = Integer.parseInt(param[5]);

        //Если режим автомата (0) или режим ручной (1)
        if (param[7].equals("0")) {
            btnModeAuto.setBackgroundResource(R.drawable.mdaten);
            btnModeMan.setBackgroundResource(R.drawable.mdmandis);
            btnModeAuto.setEnabled(false);
            btnModeMan.setEnabled(true);
            modegrida.setVisibility(modegrida.VISIBLE);
            modegridm.setVisibility(modegridm.GONE);
            tvSetTemp.setText(String.format(goalTemp,CurrSTemp));
            if (param[8].equals("0")) {
                if (param[11].equals("0")) {imgV.setBackgroundResource(R.drawable.jet_cool_off);}
                if (param[11].equals("1")) {imgV.setBackgroundResource(R.drawable.jet_cool_1);}
                if (param[11].equals("2")) {imgV.setBackgroundResource(R.drawable.jet_cool_2);}
                if (param[11].equals("3")) {imgV.setBackgroundResource(R.drawable.jet_cool_3);}
                if (param[11].equals("4")) {imgV.setBackgroundResource(R.drawable.jet_cool_off);}
            } else {
                if (param[10].equals("0")) {
                    imgV.setBackgroundResource(R.drawable.tp_off);
                } else {
                    imgV.setBackgroundResource(R.drawable.tp_on);
                }
            }
        } else {
            btnModeMan.setBackgroundResource(R.drawable.mdmanen);
            btnModeAuto.setBackgroundResource(R.drawable.mdatdis);
            btnModeMan.setEnabled(false);
            btnModeAuto.setEnabled(true);

            //Режим лето (0) или зима (1)
            if (param[8].equals("0")) {
                modegrida.setVisibility(modegrida.GONE);
                modegridm.setVisibility(modegridm.GONE);
                modegridms.setVisibility(modegridms.VISIBLE);
            } else {
                modegridm.setVisibility(modegridm.VISIBLE);
                modegrida.setVisibility(modegrida.GONE);
                modegridms.setVisibility(modegridms.GONE);

                //Теплый пол включен (1) или выключен (0)
                if (param[10].equals("0")) {
                    btnHFon.setBackgroundResource(R.drawable.tpondis);
                    btnHFoff.setBackgroundResource(R.drawable.tpoffen);
                    btnHFon.setEnabled(true);
                    btnHFoff.setEnabled(false);
                } else {
                    btnHFon.setBackgroundResource(R.drawable.tponen);
                    btnHFoff.setBackgroundResource(R.drawable.tpoffdis);
                    btnHFon.setEnabled(false);
                    btnHFoff.setEnabled(true);
                }
            }
        }

        if (param[11].equals("0")) {
            btnFCoff.setBackgroundResource(R.drawable.fancoil_off_active);
            btnFCon1.setBackgroundResource(R.drawable.btn_fc_1_dis_backgr);
            btnFCon2.setBackgroundResource(R.drawable.btn_fc_2_dis_backgr);
            btnFCon3.setBackgroundResource(R.drawable.btn_fc_3_dis_backgr);
        }
        if (param[11].equals("1")) {
            btnFCon1.setBackgroundResource(R.drawable.fancoil_1_active);
            btnFCoff.setBackgroundResource(R.drawable.btn_fc_off_dis_backgr);
            btnFCon2.setBackgroundResource(R.drawable.btn_fc_2_dis_backgr);
            btnFCon3.setBackgroundResource(R.drawable.btn_fc_3_dis_backgr);
        }
        if (param[11].equals("2")) {
            btnFCon2.setBackgroundResource(R.drawable.fancoil_2_active);
            btnFCoff.setBackgroundResource(R.drawable.btn_fc_off_dis_backgr);
            btnFCon1.setBackgroundResource(R.drawable.btn_fc_1_dis_backgr);
            btnFCon3.setBackgroundResource(R.drawable.btn_fc_3_dis_backgr);
        }
        if (param[11].equals("3")) {
            btnFCon3.setBackgroundResource(R.drawable.fancoil_3_active);
            btnFCoff.setBackgroundResource(R.drawable.btn_fc_off_dis_backgr);
            btnFCon1.setBackgroundResource(R.drawable.btn_fc_1_dis_backgr);
            btnFCon2.setBackgroundResource(R.drawable.btn_fc_2_dis_backgr);
        }
        if (param[11].equals("4")) {
            btnFCoff.setBackgroundResource(R.drawable.btn_fc_off_dis_backgr);
            btnFCon1.setBackgroundResource(R.drawable.btn_fc_1_dis_backgr);
            btnFCon2.setBackgroundResource(R.drawable.btn_fc_2_dis_backgr);
            btnFCon3.setBackgroundResource(R.drawable.btn_fc_3_dis_backgr);
        }

        err = param[13]+" " + param[14]+" " + param[15];
        err = "Сообщения: "+err;
        tvErrMess.setText(err);
    }

    public void onBtnHFon (View v) {
        startService(new Intent(this,TibboService.class).putExtra("Command",String.format(strTcmdcontr,answer_controller[1],command_controller[7]," ")));
        //startService(new Intent(this,TibboService.class).putExtra("Command","TPon"));
        btnHFon.setBackgroundResource(R.drawable.tponen);
        btnHFoff.setBackgroundResource(R.drawable.tpoffdis);
        btnHFon.setEnabled(false);
        btnHFoff.setEnabled(true);
    }

    public void onBtnHFoff (View v) {
        startService(new Intent(this,TibboService.class).putExtra("Command",String.format(strTcmdcontr,answer_controller[1],command_controller[8]," ")));
        btnHFon.setBackgroundResource(R.drawable.tpondis);
        btnHFoff.setBackgroundResource(R.drawable.tpoffen);
        btnHFon.setEnabled(true);
        btnHFoff.setEnabled(false);
    }

    public void onBtnModeauto (View v) {
        startService(new Intent(this,TibboService.class).putExtra("Command",String.format(strTcmdcontr,answer_controller[1],command_controller[20],"0")));
        btnModeAuto.setBackgroundResource(R.drawable.mdaten);
        btnModeMan.setBackgroundResource(R.drawable.mdmandis);
        btnModeAuto.setEnabled(false);
        btnModeMan.setEnabled(true);
        modegrida.setVisibility(modegrida.VISIBLE);
        modegridm.setVisibility(modegridm.GONE);
        modegridms.setVisibility(modegridms.GONE);
    }

    public void onBtnModemanual (View v) {
        startService(new Intent(this,TibboService.class).putExtra("Command",String.format(strTcmdcontr,answer_controller[1],command_controller[20],"1")));
        btnModeMan.setBackgroundResource(R.drawable.mdmanen);
        btnModeAuto.setBackgroundResource(R.drawable.mdatdis);
        btnModeMan.setEnabled(false);
        btnModeAuto.setEnabled(true);
        if (answer_controller[8].equals("0")) {
            modegridms.setVisibility(modegridms.VISIBLE);
        } else {
            modegridm.setVisibility(modegridm.VISIBLE);
        }
        modegrida.setVisibility(modegrida.GONE);
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
        startService(new Intent(this,TibboService.class).putExtra("Command",String.format(strTcmdcontr,answer_controller[1],command_controller[21],CurrSTemp)));
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
        startService(new Intent(this,TibboService.class).putExtra("Command",String.format(strTcmdcontr,answer_controller[1],command_controller[21],CurrSTemp)));
    }

    public void onBtnZh (View v) {
        String zhnum, cmd;
        switch (v.getId()) {
            case R.id.ZhUp1:
                zhnum = "1";
                cmd = command_controller[13];
                break;
            case R.id.ZhUp2:
                zhnum = "2";
                cmd = command_controller[13];
                break;
            case R.id.ZhUp3:
                zhnum = "3";
                cmd = command_controller[13];
                break;
            case R.id.ZhUp4:
                zhnum = "4";
                cmd = command_controller[13];
                break;
            case R.id.ZhDw1:
                zhnum = "1";
                cmd = command_controller[14];
                break;
            case R.id.ZhDw2:
                zhnum = "2";
                cmd = command_controller[14];
                break;
            case R.id.ZhDw3:
                zhnum = "3";
                cmd = command_controller[14];
                break;
            case R.id.ZhDw4:
                zhnum = "4";
                cmd = command_controller[14];
                break;
            default:
                zhnum = "0";
                cmd = command_controller[14];
                break;
        }
        startService(new Intent(this,TibboService.class).putExtra("Command",String.format(strTcmdcontr,answer_controller[1],cmd,zhnum)));
    }

    public void onBtnPr (View v) {
        String cmd;
        if (answer_controller[12].equals("3")) {return;}
        if (answer_controller[12].equals("2")) {cmd = command_controller[16];} else {cmd = command_controller[15];}
        startService(new Intent(this,TibboService.class).putExtra("Command",String.format(strTcmdcontr,answer_controller[1],cmd," ")));
    }

    public void onFC(View v) {
        // по id определеяем кнопку, вызвавшую этот обработчик
        String cmd = command_controller[12];
        switch (v.getId()) {
            case R.id.btnFCoff:
                // Выкл фанкойл
                cmd = command_controller[12];
                break;
            case R.id.btnFCon1:
                // Режим 1
                cmd = command_controller[9];
                break;
            case R.id.btnFCon2:
                // Режим 2
                cmd = command_controller[10];
                break;
            case R.id.btnFCon3:
                // Режим 3
                cmd = command_controller[11];
                break;
        }
        startService(new Intent(this,TibboService.class).putExtra("Command",String.format(strTcmdcontr,answer_controller[1],cmd," ")));
    }

}
