package com.nxm.myapplication.fragments;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.nxm.myapplication.R;
import com.nxm.myapplication.activity.BluetoothDemo;
import com.nxm.myapplication.configData.ConfigParameter;


/**
 * 2018年4月10日 21:53:27 lzx
 * A simple {@link Fragment} subclass.
 */
public class FunctionsFragment extends Fragment implements View.OnClickListener {
    private static final String TAG = "FunctionsFragment";
    private Button recogFace, recogID, recogCar, stopNavigate;

    private BluetoothDemo.MainHandler mAinHandler;

    public FunctionsFragment() {

    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_functions, container, false);
        view.findViewById(R.id.recogFace).setOnClickListener(this);
        view.findViewById(R.id.recogID).setOnClickListener(this);
        view.findViewById(R.id.recogCar).setOnClickListener(this);
        view.findViewById(R.id.stopNavigate).setOnClickListener(this);
        return view;
    }


    public void setHandler(BluetoothDemo.MainHandler handler) {
        mAinHandler = handler;
    }

    @Override
    public void onClick(View v) {
        Log.e(TAG, "onClick:");
        switch (v.getId()) {
            case R.id.recogFace:
                //人脸识别
                mAinHandler.obtainMessage(ConfigParameter.MSG_MESSAGE, getActivity().getString(R.string.btn_recog_face)).sendToTarget();
                break;
            case R.id.recogID:
                //身份证识别
                mAinHandler.obtainMessage(ConfigParameter.MSG_MESSAGE, getActivity().getString(R.string.btn_recog_id)).sendToTarget();
                break;
            case R.id.recogCar:
                //车牌识别
                mAinHandler.obtainMessage(ConfigParameter.MSG_MESSAGE, getActivity().getString(R.string.btn_recog_car)).sendToTarget();
                break;
            case R.id.stopNavigate:
                //停止定位
                mAinHandler.obtainMessage(ConfigParameter.MSG_MESSAGE, getActivity().getString(R.string.btn_stop_navigate)).sendToTarget();
                break;
        }
    }
}
