package com.xiaoniu.mybluetoothdemo;

import android.annotation.TargetApi;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import com.xiaoniu.mybluetoothdemo.adapter.BlueToothDeviceAdapter;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.Set;
import java.util.UUID;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private BluetoothAdapter bTAdatper;
    private ListView listView;
    private BlueToothDeviceAdapter adapter;
    private static final String NAME = "BT_DEMO";
    private static final UUID BT_UUID = UUID.fromString("02001101-0001-1000-8080-00805F9BA9BA");
    private BluetoothDevice currBluetoothDevice;
    private TextView text_msg;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
            bTAdatper = BluetoothAdapter.getDefaultAdapter();
        initReceiver();
    }

    private void initView() {
        findViewById(R.id.btn_openBT).setOnClickListener(this);
        findViewById(R.id.btn_search).setOnClickListener(this);
        findViewById(R.id.btn_send).setOnClickListener(this);
        findViewById(R.id.btn_send1).setOnClickListener(this);
        findViewById(R.id.btn_openB).setOnClickListener(this);
        findViewById(R.id.btn_searc).setOnClickListener(this);
        text_msg = (TextView) findViewById(R.id.text_msg);
        listView = (ListView) findViewById(R.id.listView);
        adapter = new BlueToothDeviceAdapter(getApplicationContext(), R.layout.bluetooth_device_list_item);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @TargetApi(Build.VERSION_CODES.KITKAT)
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (bTAdatper.isDiscovering()) {
                    bTAdatper.cancelDiscovery();
                }
                BluetoothDevice device = (BluetoothDevice) adapter.getItem(position);
                //配对
                device.createBond();
                currBluetoothDevice = device;
            }
        });
    }

    private void initReceiver() {
        //注册广播
        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothDevice.ACTION_FOUND);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        registerReceiver(mReceiver, filter);
    }

    public void write(byte[] buffer) {
        try {
            if (null == mmOutStream) {
                return;
            }
            mmOutStream.write(buffer);
            // 分享发送的信息到Activity
            Log.e("2018", "发送消息");
        } catch (IOException e) {
            Log.e("2018", "发送消息出错" + e.toString());
        }
    }

    private OutputStream mmOutStream;

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_openBT:
                openBlueTooth();
                break;
            case R.id.btn_search:
                searchDevices();
                break;
            case R.id.btn_send:
                String mgs = getString(R.string.msg);
                try {
                    write(mgs.getBytes("UTF-8"));
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
                break;
            case R.id.btn_send1:
                String mgs1 = getString(R.string.msg1);
                try {
                    write(mgs1.getBytes("UTF-8"));
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
                break;
            case R.id.btn_searc:
                //客户端连接
                try {
                    BluetoothSocket mSocket = currBluetoothDevice.createRfcommSocketToServiceRecord(BT_UUID);
                    mSocket.connect();
                    Log.e("2018", "客户端连接");
                    mmOutStream = mSocket.getOutputStream();
                } catch (IOException e) {
                    e.printStackTrace();
                    Log.e("2018", "客户端连接" + e.toString());
                }
                break;
            case R.id.btn_openB:
                //服务端开启连接
                try {
                    final BluetoothServerSocket mServerSocket = bTAdatper.listenUsingRfcommWithServiceRecord(NAME, BT_UUID);
                    Log.e("2018", "点击了");
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            Log.e("2018", "开始跑");
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
                    }).start();
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
                                    break;
                                }
                            }
                        }
                    }).start();

                } catch (IOException e) {
                    e.printStackTrace();
                    Log.e("2018", "服务端开启连接" + e.toString());
                }
                break;
        }
    }

    private InputStream mmInStream;

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

    /**
     * 搜索蓝牙设备
     */
    private void searchDevices() {
        if (bTAdatper.isDiscovering()) {
            bTAdatper.cancelDiscovery();
        }
        getBoundedDevices();
        bTAdatper.startDiscovery();
    }

    /**
     * 获取已经配对过的设备
     */
    private void getBoundedDevices() {
        //获取已经配对过的设备
        Set<BluetoothDevice> pairedDevices = bTAdatper.getBondedDevices();
        //将其添加到设备列表中
        if (pairedDevices.size() > 0) {
            if (adapter != null && adapter.isEmpty())
                adapter.clear();
            for (BluetoothDevice device : pairedDevices) {
                adapter.add(device);
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //取消搜索
        if (bTAdatper != null && bTAdatper.isDiscovering()) {
            bTAdatper.cancelDiscovery();
        }
        //注销BroadcastReceiver，防止资源泄露
        unregisterReceiver(mReceiver);
    }

    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                //避免重复添加已经绑定过的设备
                if (device.getBondState() != BluetoothDevice.BOND_BONDED) {
                    adapter.add(device);
                    adapter.notifyDataSetChanged();
                }
            } else if (BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action)) {
                Toast.makeText(MainActivity.this, "开始搜索", Toast.LENGTH_SHORT).show();
            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                Toast.makeText(MainActivity.this, "搜索完毕", Toast.LENGTH_SHORT).show();
            }
        }
    };


    /**
     * 发送数据
     *
     * @param msg
     */
    public void sendMsg(final String msg) {
    }
}
