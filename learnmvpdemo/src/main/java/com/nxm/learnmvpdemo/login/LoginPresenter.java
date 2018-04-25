package com.nxm.learnmvpdemo.login;

/**
 * @Auther: muzi102
 * @Date: 2018/4/25 11:41 03
 * @Describe: the infor of the class
 * 用于acticity回调
 */
public interface LoginPresenter {
    void validateCredentials(String name, String passward);

    void onDestroy();
}
