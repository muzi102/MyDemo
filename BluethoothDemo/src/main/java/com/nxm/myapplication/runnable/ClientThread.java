package com.nxm.myapplication.runnable;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.Handler;
import android.util.Log;

import com.nxm.myapplication.configData.ConfigParameter;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

/**
 * Runnable 蓝牙通讯监听线程
 */

public class ClientThread implements Runnable {
    private final String TAG = "ClientThread";

    private BluetoothAdapter bluetoothAdapter;
    private BluetoothDevice device;

    private Handler uiHandler;

    private BluetoothSocket socket;
    private OutputStream out;
    private InputStream in;

    public ClientThread(BluetoothAdapter bluetoothAdapter, BluetoothDevice device,
                        Handler handler) {
        this.bluetoothAdapter = bluetoothAdapter;
        this.device = device;
        this.uiHandler = handler;
        BluetoothSocket tmp = null;
        //得到一个BluetoothSocket
        try {
            socket = device.createRfcommSocketToServiceRecord(UUID.fromString(ConfigParameter.UUID));
        } catch (IOException e) {
            e.printStackTrace();
            socket = tmp;
        }
    }

    @Override
    public void run() {

        Log.e(TAG, "----------------- 客户端线程运行1");
        if (bluetoothAdapter.isDiscovering()) {
            bluetoothAdapter.cancelDiscovery();
        }

        try {
            //socket连接，该调用会阻塞，直到连接成功或失败
            socket.connect();
            out = socket.getOutputStream();
            in = socket.getInputStream();
            uiHandler.obtainMessage(ConfigParameter.MSG_CONNECT_SUCCESS).sendToTarget();
        } catch (IOException e) {
            e.printStackTrace();
            Log.e(TAG, "-------------- 客户端未连接");
            //从新开启连接
        }
    }


    public void write(String data) {
        try {
            if (out == null) {
                return;
            }
            out.write(data.getBytes("utf-8"));
            Log.e(TAG, "---------- 客户端写入数据 " + data);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void cancel() {
        try {
            socket.close();
        } catch (IOException e) {
            Log.e(TAG, "关闭socket连接失败", e);
        }
    }
}
