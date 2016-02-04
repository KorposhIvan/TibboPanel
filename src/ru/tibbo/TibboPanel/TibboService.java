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
import android.os.IBinder;

import android.app.PendingIntent;


public class TibboService extends Service {
    // Идентификатор уведомления
    private static final int NOTIFY_ID = 101;
    ExecutorService es;
    private TCPClient mTcpClient;

    public void onCreate() {
        super.onCreate();
    }

    public void onDestroy() {
        super.onDestroy();
        mTcpClient.stopClient();
    }

    public int onStartCommand(Intent intent, int flags, int startId) {
        es = Executors.newFixedThreadPool(3);
        ListenRun lr = new ListenRun();
        SendRun sr = new SendRun();
        es.execute(lr);
        es.execute(sr);
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
            mTcpClient = new TCPClient(new TCPClient.OnMessageReceived() {
                @Override
                //here the messageReceived method is implemented
                public void messageReceived(String message) {
                    //here we have received message we should do smt
                    final Intent intent = new Intent(MyActivity.BROADCAST_ACTION);
                    intent.putExtra(MyActivity.RMESSAGE,message);
                    sendBroadcast(intent);
                    if (message.equals("Dry in podval")) {sendNotif();}
                    if (message.equals("Socket is closed")) {stopSelf();}
                }
            });
            mTcpClient.run();
        }
    }
    public class SendRun implements Runnable {
        public SendRun() {

        }

        public void run() {
            while (true) {
                try {
                    TimeUnit.SECONDS.sleep(15);
                    if (mTcpClient!=null) {
                        mTcpClient.sendMessage("getAll");
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}