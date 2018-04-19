package com.nxm.amaputildemo;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import com.amap.api.maps.AMap;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.MapView;
import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.amap.api.maps.model.CameraPosition;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.MarkerOptions;

public class MainActivity extends AppCompatActivity {
    private EditText edt_chengshi, edt_address;
    //地图相关
    private MapView mapView;
    private AMap aMap;
    //地图相关工具
    private AMapUtil locationUtil;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
        if (mapView != null)
            mapView.onCreate(savedInstanceState);// 此方法必须重写
        initData();
        aMap = mapView.getMap();
    }

    private void initData() {
        locationUtil = new AMapUtil(this);
        locationUtil.setLocationLatlng(new AMapUtil.LocationLatlng() {
            @Override
            public void locatinmLatlng(LatLng latLng, String address) {
                //定位结果的回调
                moveToPosition(latLng);
                Log.d("nxm", "locatinmLatlng: " + latLng.latitude + ":" + latLng.longitude);

            }

            @Override
            public void searchResult(LatLng latLng) {
                //地理编码的回调结果
                moveToPosition(latLng);
                Log.d("nxm", "locatinmLatlng: " + latLng.latitude + ":" + latLng.longitude);
            }
        });
    }


    private void initView() {
        edt_chengshi = f(R.id.edt_chengshi);
        edt_address = f(R.id.edt_address);
        f(R.id.start_s).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //获取要定位的城市和地址 地理编码
                String shengfen = edt_chengshi.getText().toString();
                String address = edt_address.getText().toString();
                if (shengfen != null && address != null) {
                    locationUtil.getAddressLatlon(shengfen, address);
                }
            }
        });
        f(R.id.start_l).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                locationUtil.getLocationLatlng();
            }
        });
        mapView = f(R.id.mapView);
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
        //定位
        locationUtil.getLocationLatlng();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }

    /**
     * 移动到点
     *
     * @param latLng
     */
    public void moveToPosition(LatLng latLng) {
        aMap.clear();
        changeCamera(latLng);
        aMap.addMarker(new MarkerOptions().position(latLng).icon(BitmapDescriptorFactory
                .defaultMarker(BitmapDescriptorFactory.HUE_RED)));
    }

    /**
     * 根据动画按钮状态，调用函数animateCamera或moveCamera来改变可视区域
     */
    private void changeCamera(LatLng latLng) {
        aMap.moveCamera(CameraUpdateFactory.newCameraPosition(new CameraPosition(latLng, 15, 0, 0)));
    }

    private <T extends View> T f(int id) {
        return findViewById(id);
    }
}
