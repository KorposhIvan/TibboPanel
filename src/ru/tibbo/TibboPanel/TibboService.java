package ru.tibbo.TibboPanel;

/**
 * Created by Ivan on 02.02.2016.
 */
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;

import android.app.PendingIntent;

public class TibboService extends Service {
    // Идентификатор уведомления
    private static final int NOTIFY_ID = 101;
    ExecutorService es;
    private TCPClient mTcpClient;
    boolean RESTARTSERVICE=false;
    public String sendmess;

    public void onCreate() {
        super.onCreate();
        es = Executors.newFixedThreadPool(4);
        ListenRun lr = new ListenRun();
        SendRun sr = new SendRun();
        es.execute(lr);
        es.execute(sr);
    }

    public void onDestroy() {
        super.onDestroy();
        if (RESTARTSERVICE == true) {
            mTcpClient.stopClient();
            startService(new Intent(this,TibboService.class));
        }
    }

    public int onStartCommand(Intent intent, int flags, int startId) {
        if (mTcpClient != null) {
            mTcpClient.sendMessage(intent.getStringExtra("Command"));
        }
        return super.onStartCommand(intent, flags, startId);
    }

    void sendNotif() {
        Context context = getApplicationContext();
        Intent notificationIntent = new Intent(context, MyActivity.class);
        PendingIntent contentIntent = PendingIntent.getActivity(context,
                0, notificationIntent,
                PendingIntent.FLAG_CANCEL_CURRENT);
        NotificationManager nm;
        Notification.Builder nb;
        nm = (NotificationManager) context.getSystemService(NOTIFICATION_SERVICE);
        nb = new Notification.Builder(context);
        nb.setContentIntent(contentIntent)
                .setSmallIcon(R.drawable.ic_launcher)
                .setTicker("Обнаружена протечка!")
                .setWhen(System.currentTimeMillis())
                .setAutoCancel(true)
                .setContentTitle("Тревога")
                .setContentText("Обнаружена протечка в подвале");
        Notification notif = nb.build();
        notif.defaults = notif.DEFAULT_ALL;
        notif.flags = notif.flags | notif.FLAG_ONGOING_EVENT;
        nm.notify(NOTIFY_ID,notif);
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
                //here the messageReceived method is implemented
                public void messageReceived(String message) {
                    //here we have received message we should do smt
                    final Intent intent = new Intent(MyActivity.BROADCAST_ACTION);
                    intent.putExtra(MyActivity.RMESSAGE,message);
                    sendBroadcast(intent);
                    if (message.equals("Dry in podval;d")) {sendNotif();}
                    if (message.equals("Socket is closed")) {RESTARTSERVICE = true; stopSelf();}
                }
            },strIP,Integer.parseInt(strPort));
            mTcpClient.run();
        }
    }
    public class SendRun implements Runnable {
        public SendRun() {

        }

        public void run() {
            while (true) {
                try {
                    if (mTcpClient!=null) {
                        mTcpClient.sendMessage("getAll");
                        TimeUnit.SECONDS.sleep(15);
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}