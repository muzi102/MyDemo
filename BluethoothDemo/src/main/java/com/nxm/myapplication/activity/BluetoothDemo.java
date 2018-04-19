package com.nxm.myapplication.activity;


import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.RequiresApi;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;

import com.nxm.myapplication.R;
import com.nxm.myapplication.adapter.TabFragmentPagerAdapter;
import com.nxm.myapplication.configData.ConfigParameter;
import com.nxm.myapplication.fragments.BluetoothListFragment;
import com.nxm.myapplication.fragments.FunctionsFragment;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

public class BluetoothDemo extends AppCompatActivity {
    private static final String TAG = "BluetoothDemo";
    private ViewPager view_pager;
    private TabFragmentPagerAdapter mTabFragmentPagerAdapter;
    private BluetoothListFragment mBluetoothListFragment;
    private FunctionsFragment mFunctionsFragment;
    private List<Fragment> fragmentList;
    private BluetoothAdapter mBluetoothAdapter;
    private MainHandler mMainHandler;

    @RequiresApi(api = Build.VERSION_CODES.ECLAIR)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bluetooth_demo);
        initView();
        initData();
    }

    @RequiresApi(api = Build.VERSION_CODES.ECLAIR)
    private void initData() {
        mBluetoothListFragment = new BluetoothListFragment();
        mFunctionsFragment = new FunctionsFragment();
        fragmentList = new ArrayList<>();
        fragmentList.add(mBluetoothListFragment);
        fragmentList.add(mFunctionsFragment);
        mTabFragmentPagerAdapter = new TabFragmentPagerAdapter(getSupportFragmentManager(), fragmentList);
        view_pager.setAdapter(mTabFragmentPagerAdapter);
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        //fragment绑定
        mMainHandler = new MainHandler(this);
        mBluetoothListFragment.setHandler(mMainHandler);
        mFunctionsFragment.setHandler(mMainHandler);
    }

    /**
     * activity Handler处理类
     */
    public static class MainHandler extends Handler {
        private WeakReference<BluetoothDemo> reference;

        public MainHandler(BluetoothDemo activity) {
            reference = new WeakReference<BluetoothDemo>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            // TODO Auto-generated method stub
            BluetoothDemo activity = reference.get();
            if (activity != null) {
                activity.task(msg);
            }
            super.handleMessage(msg);
        }

    }

    /**
     * Handler 消息处理
     *
     * @param msg
     */
    private void task(Message msg) {
        Log.e(TAG, "onClick:+Message");
        switch (msg.what) {
            case ConfigParameter.MSG_CONNECT_SUCCESS:
                //BluetoothSocket 连接成功
                view_pager.setCurrentItem(1);
                break;
            case ConfigParameter.MSG_CONNECT_NO_SUCCESS:
                //BluetoothSocket断开连接的情况
                view_pager.setCurrentItem(0);
                break;
            case ConfigParameter.MSG_MESSAGE:
                //BluetoothSocket发送消息
                if (null != mBluetoothListFragment)
                    Log.e(TAG, "onClick:");
                mBluetoothListFragment.writeData((String) msg.obj);

                break;
        }
    }

    private void initView() {
        view_pager = f(R.id.view_pager);
    }


    private <T extends View> T f(int id) {
        return findViewById(id);
    }
}
