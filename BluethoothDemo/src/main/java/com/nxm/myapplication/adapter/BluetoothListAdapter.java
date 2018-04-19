package com.nxm.myapplication.adapter;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.nxm.myapplication.R;

import java.util.List;

/**
 * Created by yunyan.lin on 2018/4/9.
 */

public class BluetoothListAdapter extends BaseAdapter {

    private List<BluetoothDevice> devicesList;
    private Context context;
    private LayoutInflater layoutInflater;

    public BluetoothListAdapter(Context context, List<BluetoothDevice> devicesList) {
        this.devicesList = devicesList;
        this.context = context;
        this.devicesList = devicesList;
    }

    public void setListData(List<BluetoothDevice> devicesList) {
        this.devicesList = devicesList;
        notifyDataSetChanged();
    }

    public void setItem(BluetoothDevice device) {
        if (device != null) {
            devicesList.add(device);
            notifyDataSetChanged();
        }
    }

    @Override
    public int getCount() {
        return devicesList.size();
    }

    @Override
    public Object getItem(int position) {
        return devicesList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;

        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.layout_item_bt_device, parent, false);
            holder = new ViewHolder();
            holder.device_name = convertView.findViewById(R.id.device_name);
            holder.device_mac = convertView.findViewById(R.id.device_mac);
            holder.device_state = convertView.findViewById(R.id.device_state);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        BluetoothDevice bluetoothDevice = devicesList.get(position);
        int state = bluetoothDevice.getBondState();
        String name = bluetoothDevice.getName();
        String mac = bluetoothDevice.getAddress();
        if (name == null || name.isEmpty()) {
            name = context.getString(R.string.no_name_devices);
        }
        if (mac == null || mac.isEmpty()) {
            mac = context.getString(R.string.no_mac_devices);
        }
        if (state == BluetoothDevice.BOND_BONDED) {

            holder.device_state.setTextColor(context.getResources().getColor(R.color.green));
            holder.device_state.setText(R.string.match);
        } else {
            holder.device_state.setTextColor(context.getResources().getColor(R.color.red));
            holder.device_state.setText(R.string.no_match);
        }
        holder.device_name.setText(name);
        holder.device_mac.setText(mac);
        return convertView;
    }

    private class ViewHolder {
        private TextView device_name, device_mac, device_state;
    }
}
