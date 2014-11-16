package cn.edu.fudan.ee.cameraview;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import cn.edu.fudan.ee.glasscamera.CameraParams;

/**
 * Created by hbj on 2014/11/12.
 */
public class mySharedPreferences extends Activity {
    CameraParams myParams;
    private SharedPreferences sp1;
    private SharedPreferences sp2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sp1 = getSharedPreferences("initial", MODE_PRIVATE);
        sp2 = getSharedPreferences("CameraParams", MODE_PRIVATE);
        try{
            myParams = createOrLoadInitialParams();
        }
        catch (Throwable e)
        {
            e.printStackTrace();
        }
    }

    public void saveMyParams(CameraParams cameraParams) throws Throwable
    {
        saveParamsToSP(cameraParams);
    }

    public CameraParams createOrLoadInitialParams() throws Throwable
    {
        CameraParams cameraParams = null;
        Log.i("------------------------","------------------------");
        Log.i("++++++++++++++++++++++++","++++++++++++++++++++++++");
        Boolean runFirstTime = sp2.getBoolean("runFirstTime", true);
        if(runFirstTime)
        {
            sp2.edit().putBoolean("runFirstTime", false).commit();
            cameraParams = new CameraParams();
            saveParamsToSP(cameraParams);
            Log.i("load params from SharedPreferences", "第一次运行此Glass应用，创建保存相机参数的SharedPreferences");
        }
        else
        {
            Log.i("load params from SharedPreferences", "已存在保存相机参数的SharedPreferences");
            cameraParams = getMyParamsFromSP();
        }
        return cameraParams;
    }

    public void saveParamsToSP(CameraParams cameraParams) throws Throwable
    {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteArrayOutputStream);
        objectOutputStream.writeObject(cameraParams);
        Editor editor = sp2.edit();
        String data = new String(Base64.encode(byteArrayOutputStream.toByteArray(), Base64.DEFAULT));
        editor.putString("data", data);
        editor.commit();
        objectOutputStream.close();
    }

    public CameraParams getMyParamsFromSP() throws Throwable
    {
        String data = sp2.getString("data", "");
        byte[] dataBytes = Base64.decode(data.getBytes(), Base64.DEFAULT);
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(dataBytes);
        ObjectInputStream objectInputStream = new ObjectInputStream(byteArrayInputStream);
        CameraParams cameraParams = (CameraParams) objectInputStream.readObject();
        objectInputStream.close();
        return cameraParams;
    }
}