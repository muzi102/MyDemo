package com.nxm.learnmvpdemo.login;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;

import com.nxm.learnmvpdemo.MainActivity;
import com.nxm.learnmvpdemo.R;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener, LoginView {
    private EditText username, password;
    private ProgressBar progressBar;
    private LoginPresenter loginPresenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        ininView();
        initData();
    }

    /**
     * 初始化参数
     */
    private void initData() {
        loginPresenter = new LoginPresenterImpl(this, new LoginInteractorImpl());

    }

    /**
     * 初始化控件
     */
    private void ininView() {
        username = f(R.id.username);
        password = f(R.id.password);
        f(R.id.button).setOnClickListener(this);
        progressBar = f(R.id.progressBar);

    }

    /**
     * 绑定控件
     *
     * @param id
     * @param <T>
     * @return
     */
    private <T extends View> T f(int id) {
        return findViewById(id);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.button:
                loginPresenter.validateCredentials(username.getText().toString(), password.getText().toString());
                break;
        }
    }

    @Override
    public void showProgress() {
        progressBar.setVisibility(View.VISIBLE);
    }

    @Override
    public void hideProgress() {
        progressBar.setVisibility(View.GONE);
    }

    @Override
    public void passwordError() {
        password.setText("");
        password.setText("");
    }

    @Override
    public void usernameError() {
        password.setText("");
        password.setText("");
    }

    @Override
    public void goToMainActivity() {
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }

    @Override
    protected void onDestroy() {
        loginPresenter.onDestroy();
        super.onDestroy();
    }
}
