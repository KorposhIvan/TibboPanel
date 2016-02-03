package ru.tibbo.TibboPanel;

/**
 * Created by Ivan on 02.02.2016.
 */
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.text.format.Time;


public class TibboService extends Service {

    ExecutorService es;
    private TCPClient mTcpClient;

    public void onCreate() {
        super.onCreate();
        es = Executors.newFixedThreadPool(1);
        ListenRun lr = new ListenRun();
        es.execute(lr);
    }

    public void onDestroy() {
        super.onDestroy();
        mTcpClient.stopClient();
    }

    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }

    public IBinder onBind(Intent arg0) {
        return null;
    }

    public class ListenRun implements Runnable {

        public ListenRun() {

        }

        public void run() {
            final Intent intent = new Intent(MyActivity.BROADCAST_ACTION);
            mTcpClient = new TCPClient(new TCPClient.OnMessageReceived() {
                @Override
                //here the messageReceived method is implemented
                public void messageReceived(String message) {
                    //here we have received message we should do smt
                    intent.putExtra(MyActivity.RMESSAGE,message);
                    sendBroadcast(intent);
                    mTcpClient.sendMessage("ok!");
                    if (message=="Socket is closed") { stopSelf(); }
                }
            });
            mTcpClient.run();
            while (true) {
                try {
                    //Не работает!!!
                    TimeUnit.SECONDS.sleep(10);
                    mTcpClient.sendMessage("getAll");
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}