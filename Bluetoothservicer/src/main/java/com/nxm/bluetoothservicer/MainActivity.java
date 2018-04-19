package com.nxm.bluetoothservicer;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {
    private BluetoothAdapter bTAdatper;
    private static final String NAME = "BT_DEMO";
    private static final UUID BT_UUID = UUID.fromString("02001101-0001-1000-8080-00805F9BA9BA");
    private TextView text_msg;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
        initData();
        initReceiver();
    }

    private void initData() {
        bTAdatper = BluetoothAdapter.getDefaultAdapter();
        //检测蓝牙开启状态
        openBlueTooth();
        //开启socket
        startBluetToothSocket();
    }

    private void initView() {
        text_msg = findViewById(R.id.text_msg);

    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    /**
     * 开启蓝牙
     */
    private void openBlueTooth() {
        if (bTAdatper == null) {
            Toast.makeText(this, "当前设备不支持蓝牙功能", Toast.LENGTH_SHORT).show();
        }
        if (!bTAdatper.isEnabled()) {
           /* Intent i = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivity(i);*/
            bTAdatper.enable();
        }
        //开启被其它蓝牙设备发现的功能
        if (bTAdatper.getScanMode() != BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) {
            Intent i = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
            //设置为一直开启
            i.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 0);
            startActivity(i);
        }
    }

    private InputStream mmInStream;

    /**
     * 开启蓝牙socket 监听蓝牙信息
     */
    private void startBluetToothSocket() {
        if (bTAdatper.isEnabled()) {
            //服务端开启连接
            try {
                final BluetoothServerSocket mServerSocket = bTAdatper.listenUsingRfcommWithServiceRecord(NAME, BT_UUID);
                Thread mThreda = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        boolean isconnent = true;
                        while (isconnent) {
                            try {
                                BluetoothSocket socket = mServerSocket.accept();
                                if (socket != null) {
                                    isconnent = false;
                                    Log.e("2018", "服务端开启连接");
                                    mmInStream = socket.getInputStream();
                                }
                            } catch (IOException e) {
                                e.printStackTrace();
                                Log.e("2018", "服务端开启连接" + e.toString());
                            }
                        }
                    }
                });
                mThreda.start();
                //线程接受发送过来的消息
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        // 监听输入流
                        while (true) {
                            try {
                                if (null != mmInStream) {
                                    ByteArrayOutputStream outStream = new ByteArrayOutputStream();
                                    byte[] buffer = new byte[1024];
                                    // 读取输入流
                                    int length = mmInStream.read(buffer);
                                    outStream.write(buffer, 0, length);
                                    final String source = new String(outStream.toByteArray(), "UTF-8");
                                    Log.e("2018", source);
                                    if (source != null)
                                        text_msg.post(new Runnable() {
                                            @Override
                                            public void run() {
                                                text_msg.setText(source);
                                            }
                                        });
                                }
                            } catch (IOException e) {
                                Log.e("2018", "disconnected错误" + e.toString());
                                try {
                                    mmInStream.close();
                                } catch (IOException e1) {
                                    e1.printStackTrace();
                                }
                                break;
                            }
                        }
                    }
                }).start();

            } catch (IOException e) {
                e.printStackTrace();
                Log.e("2018", "服务端开启连接" + e.toString());
            }
        }

    }

    private void initReceiver() {
        //注册广播
        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothDevice.ACTION_FOUND);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        registerReceiver(mReceiver, filter);
    }

    /**
     * 广播 接受蓝牙返回信息
     */
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {

            }
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //注销BroadcastReceiver，防止资源泄露
        unregisterReceiver(mReceiver);
    }

}
