package ru.tibbo.TibboPanel;

/**
 * Created by Ivan on 02.02.2016.
 */
import java.io.*;
import java.net.InetAddress;
import java.net.Socket;

public class TCPClient {

    public static String SERVERIP = "192.168.1.100"; //Ip адрес по умолчанию. На самом деле берем в другом месте, тут осталось со старой версии
    public static int SERVERPORT = 1001; //То же самое, осталось со старой версии, берем в другом месте
    private OnMessageReceived mMessageListener = null; //Слушатель входящих соединений
    private boolean mRun = false; //Флаг запуска. Пока true работаем, хотим остановить слушатель, ставим false
    int state;

    PrintWriter out; //Буфер передачи и ниже буффер приема
    BufferedReader in;

    public TCPClient(OnMessageReceived listener, String ip, int port) {
        //Объявляем конструктор, на вход которого подаем объект слушателя, адрес и порт
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
        //Создаем Runnable поток. Здесь пытаемся подключиться и далее в бесконечном цикле слушаем данные.
        mRun = true;
        try {
            InetAddress serverAddr = InetAddress.getByName(SERVERIP); //Берем ip из переменной конструктора
            Socket socket = new Socket(serverAddr, SERVERPORT); //То же для порта
            try {
                //Инициализируем буферы отправки и приема
                out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())), true);
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                while (mRun) {
                    String serverMessage = in.readLine();
                    if (serverMessage != null && mMessageListener != null) {
                        mMessageListener.messageReceived(serverMessage);
                    }
                    state = in.read();
                    if (state<0){ mRun = false; socket.close();} //Смотрим на разрыв соединения
                    if (mRun == false) {mMessageListener.messageReceived("Socket is closed");}//serverMessage = null;
                }
            } catch (Exception e) {
                socket.close();
                mMessageListener.messageReceived("Socket is closed"); //Пока при закрытии посылаем сообщение
            } finally {
                socket.close();
                mMessageListener.messageReceived("Socket is closed"); //Пока при закрытии посылаем сообщение
            }
        } catch (Exception e) {
            if (mMessageListener != null) {
                mMessageListener.messageReceived("Socket is closed"); //Пока при закрытии посылаем сообщение
            }
        }

    }

    public interface OnMessageReceived {
        //Интерфейс, который будем использовать в другой библиотеке для обработки сообщений
        void messageReceived(String message);
    }

}
