package com.nxm.test;

import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Rect;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    ClipboardManager cm;
    TextView text;

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findViewById(R.id.button1).setOnClickListener(this);
        findViewById(R.id.button2).setOnClickListener(this);
        findViewById(R.id.button3).setOnClickListener(this);
        text = findViewById(R.id.text);
        cm = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.button1:
                // 将文本内容放到系统剪贴板里。
                cm.setText("gzapp4");
                text.setText("gzapp4");
                break;
            case R.id.button2:
                // 将文本内容放到系统剪贴板里。
                cm.setText("1234");
                text.setText("1234");
                break;
            case R.id.button3:
                cm.setText("59.111.88.32:8181");
                text.setText("59.111.88.32:8181");
                Intent intent=new Intent();

                break;
        }
    }
}
