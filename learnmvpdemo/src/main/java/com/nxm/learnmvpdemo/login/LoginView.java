package com.nxm.learnmvpdemo.login;

/**
 * @Auther: muzi102
 * @Date: 2018/4/25 11:38 36
 * @Describe: the infor of the class
 */
public interface LoginView {
    /**
     * 显示进度条
     */
    void showProgress();

    /**
     * 隐藏进度条
     */
    void hideProgress();

    /**
     * 密码错误
     */
    void passwordError();

    /**
     * 用户名错误
     */
    void usernameError();

    /**
     * 登录主界面
     */
    void goToMainActivity();
}

