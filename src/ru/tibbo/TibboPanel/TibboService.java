package ru.tibbo.TibboPanel;

/**
 * Created by Ivan on 02.02.2016.
 */
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;

public class TibboService extends Service {
    //Служба будет в фоне опрашивать контроллер и будет всегда перезапускаться
    ExecutorService es; //Выполнитель службы (хэндлер)
    private TCPClient mTcpClient; //Экземпляр класса TCP клиента
    ListenRun lr = new ListenRun(); //Объявляем Runnable поток для слушателя TCP
    SendRun sr = new SendRun(); //Обявляем Runnable поток для отсылки сообщений
    //boolean RESTARTSERVICE=false; //Флаг рестарта службы

    public void onCreate() {
        super.onCreate();
        es = Executors.newFixedThreadPool(4); //Определяем 4 потока для выполнения комманд
        es.execute(lr); //Тут и ниже запускаем потоки
        es.execute(sr);
    }

    public void onDestroy() {
        super.onDestroy();
        mTcpClient.stopClient();
        //if (RESTARTSERVICE) {
        //    mTcpClient.stopClient();
        //   startService(new Intent(this,TibboService.class));
        //}
    }

    public int onStartCommand(Intent intent, int flags, int startId) {
        if (mTcpClient != null) {
            mTcpClient.sendMessage(intent.getStringExtra("Command")); //Отсылаем команду (пришла из MyActivity)
        }
        return super.onStartCommand(intent, flags, startId);
    }

    public IBinder onBind(Intent arg0) {
        return null;
    }

    public class ListenRun implements Runnable {

        public ListenRun() {

        }

        public void run() {
            //------------------Get settings
            SharedPreferences tibboSettings;
            final String APP_PREFERENCES = "tibbosettings";
            final String APP_PREFERENCES_IP = "IP";
            final String APP_PREFERENCES_PORT = "PORT";
            String strIP = "192.168.1.100";
            String strPort = "1001";
            tibboSettings = getSharedPreferences(APP_PREFERENCES, Context.MODE_PRIVATE);

            if (tibboSettings.contains(APP_PREFERENCES_IP)) {
                strIP = tibboSettings.getString(APP_PREFERENCES_IP,"");
            }
            if (tibboSettings.contains(APP_PREFERENCES_PORT)) {
                strPort = tibboSettings.getString(APP_PREFERENCES_PORT,"");
            }

            mTcpClient = new TCPClient(new TCPClient.OnMessageReceived() {
                @Override
                //Здесь включаем интерфейс класса TCP
                public void messageReceived(String message) {
                    //Получаем сообщение от TCP, чтобы его обработать
                    final Intent intent = new Intent(MyActivity.BROADCAST_ACTION);
                    intent.putExtra(MyActivity.RMESSAGE,message); //Передаем сообщение в активити
                    sendBroadcast(intent);
                    if (message.equals("Socket is closed")) {stopSelf();}
                    //Вот здесь думаем. Как перезапустить иначе?
                }
            },strIP,Integer.parseInt(strPort));
            mTcpClient.run();
        }
    }

    public class SendRun implements Runnable {
        public SendRun() {

        }

        public void run() {
            SharedPreferences tbset;
            String roomid="001"; //Нужно взять из настроек номер команты и посылать его.
            final String APP_PREFERENCES = "tibbosettings";
            final String APP_PREFERENCES_IDROOM = "IDROOM";
            tbset = getSharedPreferences(APP_PREFERENCES, Context.MODE_PRIVATE);
            if (tbset.contains(APP_PREFERENCES_IDROOM)) {
                roomid = tbset.getString(APP_PREFERENCES_IDROOM,"");
            }

            while (true) {
                try {
                    if (mTcpClient!=null) {
                        mTcpClient.sendMessage("FF;"+roomid+";00; ; ; ; "); //Посылка команды получить все. На практике нужно изменить
                        TimeUnit.SECONDS.sleep(15);
                    }
                    else {
                        final Intent intent = new Intent(MyActivity.BROADCAST_ACTION);
                        intent.putExtra(MyActivity.RMESSAGE,"Socket is closed"); //Передаем сообщение в активити
                        sendBroadcast(intent);
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}