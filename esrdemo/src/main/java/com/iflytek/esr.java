/*
 * Copyright (C) 2009 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.iflytek;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.widget.TextView;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;


public class esr extends Activity {
    private static String TAG = "Activity";

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        TextView tv = new TextView(this);

        // copy so from assets to files
        File dir = this.getDir("libs", Activity.MODE_PRIVATE);
        copyFilesFassets(this, "libs", dir.getAbsolutePath());

        // load w_esr.so
        String load_str = load(dir.getAbsolutePath());
        Log.d("w_esr===", load_str);
        tv.setText(load_str);
        setContentView(tv);


        // voice path
        String sdcard_path = Environment.getExternalStorageDirectory().getAbsolutePath();
        String pcm_path = sdcard_path + new String("/w_esr/test.pcm");
        Log.e(TAG, sdcard_path);
        // w_esr Create
        int ret = -1;
        try {
            String cfg_path = sdcard_path + new String("/w_esr/esr.cfg");
            String acmodel_path = sdcard_path + new String("/w_esr/acmode_ubrnn_ctc_chn_fix_20180125_param.bin");
            String lmmodel_path = sdcard_path + new String("/w_esr/wfst_chn_20180125.bin");
            String vadmodel_path = sdcard_path + new String("/w_esr/MLP_RES_DNN_MODEL_VAD.bin");

            System.out.print("hello");
            ret = Create(cfg_path, acmodel_path, lmmodel_path, vadmodel_path);
            System.out.print(ret);
        } catch (Exception e) {
            Log.d("w_esr===", "Create failed!\n");
        }
        if (0 == ret) {
            Log.d("w_esr===", "Create success...\n");
        } else {
            Log.d("w_esr===", "Create failed!\n");
        }

        // start
        Log.d("w_esr===", "Start start...\n");
        String sid = pcm_path;
        try {
            ret = Start(sid);
        } catch (Exception e) {
            Log.d("w_esr===", "Start failed!\n");
        }
        if (0 == ret) {
            Log.d("w_esr===", "Start success...\n");
        } else {
            Log.d("w_esr===", "Start failed!\n");
        }
        // read pcm file and append data
        int nLeftAudioBytes = 0;
        byte[] pOriginalBuf = null;
        try {
            File file = new File(pcm_path);
            FileInputStream fis = new FileInputStream(file);
            pOriginalBuf = new byte[(int) file.length()];
            nLeftAudioBytes = pOriginalBuf.length;
            fis.read(pOriginalBuf);
            fis.close();
        } catch (IOException e) {
            e.printStackTrace();
        }


        int nByteWrites = 320;
        int nWriteLen = 0;
        int nPos = 0;
        byte[] dataArray = null;
        dataArray = new byte[320];

        while (nLeftAudioBytes > 0) {
            if (nLeftAudioBytes > nByteWrites) {
                nWriteLen = nByteWrites;
                System.arraycopy(pOriginalBuf, nPos, dataArray, 0, nWriteLen);
                if (nPos > 0) {
                    AppendData(dataArray, nWriteLen, false);
                } else {
                    AppendData(dataArray, nWriteLen, true);
                }
                nPos += 320;
            } else {
                nWriteLen = nLeftAudioBytes;
                System.arraycopy(pOriginalBuf, nPos, dataArray, 0, nWriteLen);
                AppendData(dataArray, nWriteLen, false);
                EndData();
            }
            nLeftAudioBytes -= nWriteLen;
        }

        GetResultStatus();
        String result_str = GetResult();
        Log.d("w_esr===", "ESR result:" + result_str + new String("\n"));

        Stop();
        Destroy();
    }


    private String load(String w_esr_path) {
        try {
//    		System.load(w_esr_path + File.separator + "libhardinfo.so");
//    		System.load(w_esr_path + File.separator + "liblesl.so");
//    		System.load(w_esr_path + File.separator + "w_esr.so");
            System.loadLibrary("hardinfo");
            System.loadLibrary("lesl");
            System.loadLibrary("w_esr");
            return new String("load successed.\n");
        } catch (Exception e) {
            return new String("load failed.\n");
        }
    }

    public native int Create(String cfg_dir, String acmodel_dir, String lmmodel_dir, String vadmodel_dir);

    public native int Destroy();

    public native int Start(String sid);

    public native int Stop();

    public native int AppendData(byte[] dataArray, int dataSize, boolean start);

    public native int EndData();

    public native int GetResultStatus();

    public native String GetResult();

    public native int GetResCount();

    public native int GetItemId(int resIndex);

    public native String GetItemText(int resIndex);

    public native String GetSlotName(int resIndex);


    /**
     * 从assets目录中复制整个文件夹内容
     *
     * @param context Context 使用CopyFiles类的Activity
     * @param oldPath String  原文件路径  如：/aa
     * @param newPath String  复制后路径  如：xx:/bb/cc
     */
    public void copyFilesFassets(Context context, String oldPath, String newPath) {
        try {
            String fileNames[] = context.getAssets().list(oldPath);//获取assets目录下的所有文件及目录名
            if (fileNames.length > 0) {//如果是目录
                File file = new File(newPath);
                file.mkdirs();//如果文件夹不存在，则递归
                for (String fileName : fileNames) {
                    copyFilesFassets(context, oldPath + "/" + fileName, newPath + "/" + fileName);
                }
            } else {//如果是文件
                InputStream is = context.getAssets().open(oldPath);
                FileOutputStream fos = new FileOutputStream(new File(newPath));
                byte[] buffer = new byte[1024];
                int byteCount = 0;
                while ((byteCount = is.read(buffer)) != -1) {//循环从输入流读取 buffer字节
                    fos.write(buffer, 0, byteCount);//将读取的输入流写入到输出流
                }
                fos.flush();//刷新缓冲区
                is.close();
                fos.close();
            }
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            //如果捕捉到错误则通知UI线程
            Log.e("MainActivity", "[copyFilesFassets] IOException " + e.toString());
        }
    }

}

