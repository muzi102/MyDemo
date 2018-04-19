package com.nxm.myapplication.fragments;


import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.nxm.myapplication.R;
import com.nxm.myapplication.activity.BluetoothDemo;
import com.nxm.myapplication.adapter.BluetoothListAdapter;
import com.nxm.myapplication.configData.ConfigParameter;
import com.nxm.myapplication.runnable.ClientThread;
import com.nxm.myapplication.utils.ClsUtils;
import com.nxm.myapplication.utils.ToastUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static android.app.Activity.RESULT_OK;


/**
 * 2018年4月9日 10:13:06 xmz
 * A simple {@link Fragment} subclass.
 */
public class BluetoothListFragment extends Fragment implements View.OnClickListener {
    private static final String TAG = "BluetoothListFragment";
    private Button setting, search;
    private ListView bluetooth_list;
    private BluetoothAdapter mBluetoothAdapter;
    private List<BluetoothDevice> devicesList = new ArrayList<>();
    private BluetoothListAdapter mBluetoothListAdapter;
    private BroadcastReceiver mBroadcastReceiver;
    private ClientThread mClientThread;

    private BluetoothDemo.MainHandler mainHandler;

    public BluetoothListFragment() {
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case ConfigParameter.MY_PERMISSION_REQUEST_CONSTANT: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // 运行时权限已授权
                }
                return;
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_bluetooth_list, container, false);
        setting = f(view, R.id.setting);
        search = f(view, R.id.search);
        bluetooth_list = f(view, R.id.bluetooth_list);
        search.setOnClickListener(this);
        setting.setOnClickListener(this);
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initData();
        //判断是否可用
        isEnable();
    }

    /**
     * 初始化参数
     */
    private void initData() {
        mBluetoothListAdapter = new BluetoothListAdapter(getActivity(), devicesList);
        bluetooth_list.setAdapter(mBluetoothListAdapter);
        bluetooth_list.setOnItemClickListener(mOnItemClickListener);
        //注册广播监听
        mBroadcastReceiver = new BluetoothReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothDevice.ACTION_FOUND);
        intentFilter.addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
        intentFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
        intentFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        intentFilter.addAction(BluetoothAdapter.ACTION_SCAN_MODE_CHANGED);
        intentFilter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        getActivity().registerReceiver(mBroadcastReceiver, intentFilter);
    }

    /**
     * 显示绑定的蓝牙
     */
    private void isEnable() {
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (!mBluetoothAdapter.isEnabled()) {
            Intent turnOnBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(turnOnBtIntent, ConfigParameter.REQUEST_ENABLE_BT);
        } else {
            showBondDevices();
        }
    }

    /**
     * 显示已绑定过的蓝牙
     */
    private void showBondDevices() {
        devicesList.clear();
        Set<BluetoothDevice> tmp = mBluetoothAdapter.getBondedDevices();
        if (!tmp.isEmpty()) {
            for (BluetoothDevice device : tmp) {
                devicesList.add(device);
                Log.e("TAG", device.getName());
            }
        }
        //刷新适配器
        mBluetoothListAdapter.setListData(devicesList);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.search:
                //搜索设备
                if (mBluetoothAdapter.isDiscovering()) {
                    mBluetoothAdapter.cancelDiscovery();
                }
                if (Build.VERSION.SDK_INT >= 6.0) {
                    ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                            ConfigParameter.MY_PERMISSION_REQUEST_CONSTANT);
                }
                mBluetoothAdapter.startDiscovery();
                break;
            case R.id.setting:
                writeData("身份证识别");
                break;
        }
    }

    /**
     * listView item监听器
     */
    AdapterView.OnItemClickListener mOnItemClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            //先暂停搜索
            if (mBluetoothAdapter.isDiscovering())
                mBluetoothAdapter.cancelDiscovery();
            TextView mState = view.findViewById(R.id.device_state);
            String state = mState.getText().toString();
            BluetoothDevice btDevice = (BluetoothDevice) mBluetoothListAdapter.getItem(position);
            if (null != btDevice) {
                switch (state) {
                    case "未配对":
                        if (btDevice.getBondState() == BluetoothDevice.BOND_NONE) {
                            try {
                                //通过工具类ClsUtils,调用createBond方法
                                ClsUtils.createBond(btDevice.getClass(), btDevice);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                        break;
                    case "已配对":
                        // 开启客户端线程，连接点击的远程设备
                        mClientThread = new ClientThread(mBluetoothAdapter, btDevice, mainHandler);
                        new Thread(mClientThread).start();
                        break;
                }
            }
        }
    };

    /**
     * 蓝牙广播接受
     */
    private class BluetoothReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
            Log.e(TAG, action);
            if (action.equals(BluetoothAdapter.ACTION_STATE_CHANGED)) {
                //蓝牙状态值的改变
            } else if (action.equals(BluetoothDevice.ACTION_FOUND)) {
                Log.e(TAG, device.getAddress());
                //搜索设备
                if (isNewDevice(device)) {
                    mBluetoothListAdapter.setItem(device);
                    Log.e(TAG, "add" + device.getAddress());
                }
            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                ToastUtils.showToast(context, "开始搜索 ...");
            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                ToastUtils.showToast(context, "搜索结束 ...");
            } else if (BluetoothDevice.ACTION_BOND_STATE_CHANGED.equals(action)) {
                switch (device.getBondState()) {
                    case BluetoothDevice.BOND_BONDING:
                        Log.e(TAG, "正在配对");
                        break;
                    case BluetoothDevice.BOND_BONDED:
                        Log.e(TAG, "完成配对");
                        if (isNewDevice(device)) {
                            /**
                             * ？？？
                             */
                            mBluetoothListAdapter.setItem(device);
                        } else {
                            mBluetoothListAdapter.setItem(device);
                        }
                        break;
                    case BluetoothDevice.BOND_NONE:
                        Log.e(TAG, "取消配对");
                        break;

                }
            }
        }
    }

    /**
     * 向 socket 写入发送的数据
     *
     * @param dataSend
     */
    public void writeData(String dataSend) {
        if (mClientThread != null) {
            mClientThread.write(dataSend);
            Log.e(TAG, "writeData:"+dataSend);
        }
    }

    /**
     * 判断搜索的设备是新蓝牙设备，且不重复
     *
     * @param device
     * @return
     */
    private boolean isNewDevice(BluetoothDevice device) {
        boolean repeatFlag = false;
        for (BluetoothDevice d :
                devicesList) {
            if (d.getAddress().equals(device.getAddress())) {
                repeatFlag = true;
            }
        }
        //不是已绑定状态，且列表中不重复
        return device.getBondState() != BluetoothDevice.BOND_BONDED && !repeatFlag;
    }

    public void setHandler(BluetoothDemo.MainHandler handler) {
        mainHandler = handler;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.e("TAG", "onActivityResult");
        switch (requestCode) {
            case ConfigParameter.REQUEST_ENABLE_BT:
                //蓝牙已设置可见
                if (resultCode == RESULT_OK) {
                    showBondDevices();
                    Log.e("TAG", "onActivityResult");
                }
                break;
        }
    }


    @Override
    public void onDestroy() {
        //注销广播
        getActivity().unregisterReceiver(mBroadcastReceiver);
        if (mBluetoothAdapter.isDiscovering())
            mBluetoothAdapter.cancelDiscovery();
        super.onDestroy();
    }

    private <T extends View> T f(View view, int id) {
        return view.findViewById(id);
    }
}
