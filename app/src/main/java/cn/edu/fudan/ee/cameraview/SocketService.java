package cn.edu.fudan.ee.cameraview;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.ServerSocket;
import java.net.Socket;



/**
 * Created by zxtxin on 2014/9/17.
 */
public class SocketService extends Service {
    private final IBinder mBinder = new LocalBinder();
    private CameraParams rawParams;
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }
    public class LocalBinder extends Binder {
        SocketService getService(){
            return SocketService.this;
        }
    }
    public CameraParams getRawParams(){
        return rawParams;
    }
    private ServerSocket serverSocket = null;
    final int SERVER_PORT = 1111;
    @Override
    public void onCreate() {
        super.onCreate();
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    serverSocket = new ServerSocket(SERVER_PORT);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                    while(true) {
                        try {
                            Socket socket = serverSocket.accept();
                            Log.d("SocketServer", "Accepted");
                            while (true) {
                                try {
                                    ObjectInputStream objIn = new ObjectInputStream(socket.getInputStream());
                                    rawParams = (CameraParams)(objIn.readObject());
                                    Message msg = new Message();
                                    msg.obj = rawParams;
                                    CameraPreview.myHandler.sendMessage(msg);
                                } catch (IOException e) {
                                    e.printStackTrace();
                                    break;
                                } catch (ClassNotFoundException e) {
                                    e.printStackTrace();
                                    break;
                                }
                            }
                        }catch(IOException e) {
                            e.printStackTrace();
                        }
                    }
            }
        }).start();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}
class CameraParams{
    public int params1;
    public int params2;
}