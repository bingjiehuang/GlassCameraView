package cn.edu.fudan.ee.cameraview;

import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import cn.edu.fudan.ee.glasscamera.CameraParams;

/**
 * Created by hbj on 2014/11/4.
 */
public class FileOperation {
    private static FileOperation fileOperation = null;

    private FileOperation()
    {

    }

    public static synchronized FileOperation getInstance()
    {
        if(fileOperation == null)
        {
            fileOperation = new FileOperation();
        }
        return fileOperation;
    }

    FileInputStream fi;
    ObjectInputStream oi;
    FileOutputStream fo;
    ObjectOutputStream os;
    String filePath = Environment.getExternalStorageDirectory().getPath()+"/savedInitialParams.ser";// 保存相机参数的文件
    CameraParams myParams = getInitialParams();

    public CameraParams getInitialParams()
    {
        if(myParams == null)
        {
            myParams = createOrLoadParamsFromFile();
        }
        return myParams;
    }

    public void saveMyParams(CameraParams cameraParams)
    {
        saveParamsToFile(cameraParams);
    }

    // 启动相机时，若存在保存相机参数的文件，则加载；若不存在，则创建
    public CameraParams createOrLoadParamsFromFile()
    {
        CameraParams cameraParams = null;
        File file = new File(filePath);
        if(!file.exists())
        {
            cameraParams = new CameraParams();// 第一次使用此Glass应用，相机要初次实例化
            try {
                file.createNewFile();// 创建保存相机参数的文件
                Log.i("loadParamsFromFile", "第一次运行此Glass应用，创建保存相机参数的.ser文件");
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
            saveParamsToFile(cameraParams);// 保存相机参数到文件
        }
        else// 打开Glass应用时自动加载参数，设置照相机的参数值
        {
            Log.i("loadParamsFromFile", "已存在保存相机参数的.ser文件");
            cameraParams = loadParamsFromFile();
        }
        return cameraParams;
    }

    // 从文件加载参数
    public CameraParams loadParamsFromFile()
    {
        CameraParams cameraParams = null;
        try {
            fi = new FileInputStream(filePath);
            oi = new ObjectInputStream(fi);
            cameraParams = (CameraParams)oi.readObject();
            oi.close();
            fi.close();
        }
        catch(ClassNotFoundException e)
        {
            e.printStackTrace();
        }
        catch(IOException e)
        {
            e.printStackTrace();
        }
        Log.i("Already finished loading Params?", "Finished");
        Log.i("Initial params1", ""+cameraParams.params1);
        Log.i("Initial params2", ""+cameraParams.params2);
        Log.i("Initial params3", ""+cameraParams.params3);
        return cameraParams;
    }

    // 保存参数到文件
    public void saveParamsToFile(CameraParams cameraParams)
    {
        try {
            fo = new FileOutputStream(filePath);
            os = new ObjectOutputStream(fo);
            os.writeObject(cameraParams);
            os.close();
            fo.close();
        }
        catch(IOException e1)
        {
            e1.printStackTrace();
        }
        Log.i("Saved?", "success");
        Log.i("Saved params1", ""+cameraParams.params1);
        Log.i("Saved params2", ""+cameraParams.params2);
        Log.i("Saved params3", ""+cameraParams.params3);
    }
}