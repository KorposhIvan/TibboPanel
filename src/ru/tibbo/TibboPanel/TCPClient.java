package ru.tibbo.TibboPanel;

/**
 * Created by Ivan on 02.02.2016.
 */
import java.io.*;
import java.net.InetAddress;
import java.net.Socket;

public class TCPClient {

    private String serverMessage;
    public static String SERVERIP = "192.168.1.100"; //your computer IP address
    public static int SERVERPORT = 1001;
    private OnMessageReceived mMessageListener = null;
    private boolean mRun = false;
    int state;

    PrintWriter out;
    BufferedReader in;

    public TCPClient(OnMessageReceived listener, String ip, int port) {
        mMessageListener = listener;
        SERVERIP = ip;
        SERVERPORT = port;
    }

    public void sendMessage(String message){
        if (out != null && !out.checkError()) {
            out.println(message);
            out.flush();
        }
    }

    public void stopClient(){
        mRun = false;
    }

    public void run() {

        mRun = true;
        try {
            InetAddress serverAddr = InetAddress.getByName(SERVERIP);
            Socket socket = new Socket(serverAddr, SERVERPORT);
            try {
                out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())), true);
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                while (mRun) {
                    serverMessage = in.readLine();
                    if (serverMessage != null && mMessageListener != null) {
                        mMessageListener.messageReceived(serverMessage);
                    }
                    state = in.read();
                    if (state<=0){ mRun = false; }
                    serverMessage = null;
                }
            } catch (Exception e) {

            } finally {
                socket.close();
                mMessageListener.messageReceived("Socket is closed");
            }
        } catch (Exception e) {

        }

    }

    public interface OnMessageReceived {
        void messageReceived(String message);
    }

}
