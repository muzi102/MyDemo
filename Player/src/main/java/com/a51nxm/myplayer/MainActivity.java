package com.a51nxm.myplayer;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Toast;

import com.aliyun.vodplayer.media.AliyunLocalSource;
import com.aliyun.vodplayer.media.AliyunVodPlayer;
import com.aliyun.vodplayer.media.IAliyunVodPlayer;
import com.aliyun.vodplayerview.utils.NetWatchdog;

import java.lang.ref.WeakReference;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class MainActivity extends FragmentActivity implements View.OnClickListener {
    private static final String TAG = MainActivity.class.getSimpleName();
    private SimpleDateFormat format = new SimpleDateFormat("HH:mm:ss.SSS");
    private SurfaceView mSurfaceView;
    private static String url = "rtmp://xiaomuzi.51nxm.com/nxm/test";
    //是否是静音模式
    private boolean mMute = false;
    private List<String> logStrs = new ArrayList<>();
    private AliyunVodPlayer mPlayer;
    //private String mUrl;
    private NetWatchdog netWatchdog;
    private Button pauseBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_main);

        mSurfaceView = findViewById(R.id.surfaceView);
        findViewById(R.id.play).setOnClickListener(this);
        findViewById(R.id.stop).setOnClickListener(this);
        pauseBtn = findViewById(R.id.pause);
        findViewById(R.id.pause).setOnClickListener(this);
        mSurfaceView.getHolder().addCallback(callback);
        //初始化播放器
        initVodPlayer();
        netWatchdog = new NetWatchdog(this);
        //网络监听
        netWatchdog.setNetChangeListener(new NetWatchdog.NetChangeListener() {
            @Override
            public void onWifiTo4G() {
                if (mPlayer.isPlaying()) {
                    pause();
                }
                AlertDialog.Builder alertDialog = new AlertDialog.Builder(MainActivity.this);
                alertDialog.setTitle(getString(R.string.net_change_to_4g));
                alertDialog.setMessage(getString(R.string.net_change_to_continue));
                alertDialog.setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        replay();
                    }
                });
                alertDialog.setNegativeButton(getString(R.string.no), null);
                AlertDialog alert = alertDialog.create();
                alert.show();
                Toast.makeText(MainActivity.this.getApplicationContext(), R.string.net_change_to_4g, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void on4GToWifi() {
                Toast.makeText(MainActivity.this.getApplicationContext(), R.string.net_change_to_wifi, Toast.LENGTH_SHORT).show();

            }

            @Override
            public void onNetDisconnected() {
                Toast.makeText(MainActivity.this.getApplicationContext(), R.string.net_disconnect, Toast.LENGTH_SHORT).show();
            }
        });
        netWatchdog.startWatch();
    }

    private void shwoToast(String content) {
        Toast.makeText(this, content, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {

            case R.id.play:
                //开始
                logStrs.add(format.format(new Date()) + getString(R.string.log_start_get_data));
                shwoToast(getString(R.string.log_start_get_data));
                replay();
                if (mMute) {
                    mPlayer.setMuteMode(mMute);
                }
                logStrs.add(format.format(new Date()) + getString(R.string.log_strart_play));
                shwoToast(getString(R.string.log_strart_play));
                break;
            case R.id.stop:
                //停止
                stop();
                break;
            case R.id.pause:
                //暂停
                IAliyunVodPlayer.PlayerState playerState = mPlayer.getPlayerState();
                if (playerState == IAliyunVodPlayer.PlayerState.Started) {
                    pause();
                    pauseBtn.setText(R.string.resume_button);
                } else if (playerState == IAliyunVodPlayer.PlayerState.Paused) {
                    resume();
                    pauseBtn.setText(R.string.pause_button);
                } else {

                }
                break;
            default:
                break;
        }
    }

    private void initVodPlayer() {
        mPlayer = new AliyunVodPlayer(this);
        mPlayer.setOnPreparedListener(new MyPreparedListener(this));
        mPlayer.setOnFirstFrameStartListener(new MyFrameInfoListener(this));
        mPlayer.setOnErrorListener(new MyErrorListener(this));
        mPlayer.setOnCompletionListener(new MyPlayerCompletedListener(this));
        mPlayer.setOnSeekCompleteListener(new MySeekCompleteListener(this));
        mPlayer.setOnStoppedListner(new MyStoppedListener(this));
        mPlayer.setOnTimeShiftUpdaterListener(new MyTimeShiftUpdaterListener(this));
        mPlayer.setOnSeekLiveCompletionListener(new MySeekLiveCompletionListener(this));
        mPlayer.enableNativeLog();
//        //原比例填充fit
//        if (mPlayer != null) {
//            mPlayer.setVideoScalingMode(IAliyunVodPlayer.VideoScalingMode.VIDEO_SCALING_MODE_SCALE_TO_FIT);
//        }
        //剪切占满全屏fill
        if (mPlayer != null) {
            mPlayer.setVideoScalingMode(IAliyunVodPlayer.VideoScalingMode.VIDEO_SCALING_MODE_SCALE_TO_FIT_WITH_CROPPING);

        }
        //屏幕的亮度、音量值
        mPlayer.setScreenBrightness(100);
        mPlayer.setVolume(100);
    }


    private static class MySeekLiveCompletionListener implements IAliyunVodPlayer.OnSeekLiveCompletionListener {
        private WeakReference<MainActivity> activityWeakReference;

        public MySeekLiveCompletionListener(MainActivity activity) {
            activityWeakReference = new WeakReference<MainActivity>(activity);
        }

        @Override
        public void onSeekLiveCompletion(long playTime) {
            MainActivity activity = activityWeakReference.get();
            if (activity != null) {
                activity.onSeekLiveCompletion(playTime);
            }
        }
    }

    private void onSeekLiveCompletion(long playTime) {
        mPlayTime = playTime;
    }

    private static class MyTimeShiftUpdaterListener implements IAliyunVodPlayer.OnTimeShiftUpdaterListener {
        private WeakReference<MainActivity> activityWeakReference;

        public MyTimeShiftUpdaterListener(MainActivity activity) {
            activityWeakReference = new WeakReference<MainActivity>(activity);
        }

        @Override
        public void onUpdater(long currentTime, long shiftStartTime, long shiftEndTime) {
            MainActivity activity = activityWeakReference.get();
            if (activity != null) {
                activity.onUpdater(currentTime, shiftStartTime, shiftEndTime);
            }
        }
    }

    long mLiveTime;
    long mShiftStartTime;
    long mShiftEndTime;
    long mPlayTime;
    long mEndTime = -1;

    private void onUpdater(long currentLiveTime, long shiftStartTime, long shiftEndTime) {
        mLiveTime = currentLiveTime;
        mShiftStartTime = shiftStartTime;
        mShiftEndTime = shiftEndTime;
    }

    private static class MyErrorListener implements IAliyunVodPlayer.OnErrorListener {

        private WeakReference<MainActivity> activityWeakReference;

        public MyErrorListener(MainActivity activity) {
            activityWeakReference = new WeakReference<MainActivity>(activity);
        }

        @Override
        public void onError(int arg0, int arg1, String msg) {
            MainActivity activity = activityWeakReference.get();
            if (activity != null) {
                activity.onError(arg0, msg);
            }
        }
    }

    private void onError(int i, String msg) {
        stop();
        Toast.makeText(MainActivity.this.getApplicationContext(), getString(R.string.toast_fail_msg) + msg, Toast.LENGTH_SHORT).show();
    }

    private static class MyPreparedListener implements IAliyunVodPlayer.OnPreparedListener {

        private WeakReference<MainActivity> activityWeakReference;

        public MyPreparedListener(MainActivity activity) {
            activityWeakReference = new WeakReference<MainActivity>(activity);
        }

        @Override
        public void onPrepared() {
            MainActivity activity = activityWeakReference.get();
            if (activity != null) {
                activity.onPrepared();
            }
        }

    }

    void onPrepared() {
        Toast.makeText(MainActivity.this.getApplicationContext(), R.string.toast_prepare_success, Toast.LENGTH_SHORT).show();
        logStrs.add(format.format(new Date()) + getString(R.string.log_prepare_success));
    }

    private static class MyStoppedListener implements IAliyunVodPlayer.OnStoppedListener {

        private WeakReference<MainActivity> activityWeakReference;

        public MyStoppedListener(MainActivity activity) {
            activityWeakReference = new WeakReference<MainActivity>(activity);
        }


        @Override
        public void onStopped() {
            MainActivity activity = activityWeakReference.get();
            if (activity != null) {
                activity.onStopped();
            }
        }
    }

    void onStopped() {
        logStrs.add(format.format(new Date()) + getString(R.string.log_play_stopped));
    }

    private static class MySeekCompleteListener implements IAliyunVodPlayer.OnSeekCompleteListener {


        private WeakReference<MainActivity> activityWeakReference;

        public MySeekCompleteListener(MainActivity activity) {
            activityWeakReference = new WeakReference<MainActivity>(activity);
        }

        @Override
        public void onSeekComplete() {
            MainActivity activity = activityWeakReference.get();
            if (activity != null) {
                activity.onSeekCompleted();
            }
        }
    }

    void onSeekCompleted() {
        logStrs.add(format.format(new Date()) + getString(R.string.log_seek_completed));
    }

    private static class MyPlayerCompletedListener implements IAliyunVodPlayer.OnCompletionListener {

        private WeakReference<MainActivity> activityWeakReference;

        public MyPlayerCompletedListener(MainActivity activity) {
            activityWeakReference = new WeakReference<MainActivity>(activity);
        }

        @Override
        public void onCompletion() {
            MainActivity activity = activityWeakReference.get();
            if (activity != null) {
                activity.onCompleted();
            }
        }
    }

    private void onCompleted() {
        Log.d(TAG, "onCompleted--- ");
    }

    private static class MyFrameInfoListener implements IAliyunVodPlayer.OnFirstFrameStartListener {

        private WeakReference<MainActivity> activityWeakReference;

        public MyFrameInfoListener(MainActivity activity) {
            activityWeakReference = new WeakReference<MainActivity>(activity);
        }

        @Override
        public void onFirstFrameStart() {
            MainActivity activity = activityWeakReference.get();
            if (activity != null) {
                activity.onFrameInfoListener();
            }
        }
    }

    private SurfaceHolder.Callback callback = new SurfaceHolder.Callback() {
        @Override
        public void surfaceCreated(SurfaceHolder holder) {
            holder.setType(SurfaceHolder.SURFACE_TYPE_GPU);
            holder.setKeepScreenOn(true);
            Log.d(TAG, "AlivcPlayer onSurfaceCreated." + mPlayer);

            // Important: surfaceView changed from background to front, we need reset surface to mediaplayer.
            // 对于从后台切换到前台,需要重设surface;部分手机锁屏也会做前后台切换的处理
            if (mPlayer != null) {
                mPlayer.setSurface(mSurfaceView.getHolder().getSurface());
            }

            Log.d(TAG, "AlivcPlayeron SurfaceCreated over.");
        }

        @Override
        public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            Log.d(TAG, "onSurfaceChanged is valid ? " + holder.getSurface().isValid());
            if (mPlayer != null)
                mPlayer.surfaceChanged();
        }

        @Override
        public void surfaceDestroyed(SurfaceHolder holder) {

        }
    };

    private void onFrameInfoListener() {

        Map<String, String> debugInfo = mPlayer.getAllDebugInfo();
        long createPts = 0;
        if (debugInfo.get("create_player") != null) {
            String time = debugInfo.get("create_player");
            createPts = (long) Double.parseDouble(time);
            logStrs.add(format.format(new Date(createPts)) + getString(R.string.log_player_create_success));
        }
        if (debugInfo.get("open-url") != null) {
            String time = debugInfo.get("open-url");
            long openPts = (long) Double.parseDouble(time) + createPts;
            logStrs.add(format.format(new Date(openPts)) + getString(R.string.log_open_url_success));
        }
        if (debugInfo.get("find-stream") != null) {
            String time = debugInfo.get("find-stream");
            Log.d(TAG + "lfj0914", "find-Stream time =" + time + " , createpts = " + createPts);
            long findPts = (long) Double.parseDouble(time) + createPts;
            logStrs.add(format.format(new Date(findPts)) + getString(R.string.log_request_stream_success));
        }
        if (debugInfo.get("open-stream") != null) {
            String time = debugInfo.get("open-stream");
            Log.d(TAG + "lfj0914", "open-Stream time =" + time + " , createpts = " + createPts);
            long openPts = (long) Double.parseDouble(time) + createPts;
            logStrs.add(format.format(new Date(openPts)) + getString(R.string.log_start_open_stream));
        }
        logStrs.add(format.format(new Date()) + getString(R.string.log_first_frame_played));
    }

    private void start() {
        if (mPlayer != null) {
            mPlayer.setAutoPlay(true);
            AliyunLocalSource.AliyunLocalSourceBuilder asb = new AliyunLocalSource.AliyunLocalSourceBuilder();
            asb.setSource(url);
            AliyunLocalSource mLocalSource = asb.build();

            mPlayer.prepareAsync(mLocalSource);
        }

    }

    private void pause() {
        if (mPlayer != null) {
            mPlayer.pause();
            pauseBtn.setText(R.string.resume_button);
        }
    }

    private void stop() {
        if (mPlayer != null) {
            mPlayer.stop();
        }
    }

    private void resume() {
        if (mPlayer != null) {
            mPlayer.start();
            pauseBtn.setText(R.string.pause_button);
        }
    }

    private void destroy() {
        if (mPlayer != null) {
            mPlayer.stop();
            mPlayer.release();
        }
    }

    private void replay() {
        stop();
        start();
    }

    @Override
    protected void onResume() {
        super.onResume();

    }

    @Override
    protected void onStop() {
        super.onStop();
        savePlayerState();
    }

    private void savePlayerState() {
        if (mPlayer.isPlaying()) {
            pause();
        }
    }

    @Override
    protected void onDestroy() {
        stop();
        destroy();
        netWatchdog.stopWatch();
        super.onDestroy();
    }

}
