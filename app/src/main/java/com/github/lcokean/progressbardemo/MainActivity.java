package com.github.lcokean.progressbardemo;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import com.github.library.progressbarview.HorizontalProgressBarWithAnim;

public class MainActivity extends AppCompatActivity {

    HorizontalProgressBarWithAnim mProgressBar;
    Button button;

    private static final int MSG_PROGRESS_UPDATE = 0x110;

    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            int progress = mProgressBar.getProgress();
            mProgressBar.setProgress(++progress);
            if (progress >= 100) {
                mHandler.removeMessages(MSG_PROGRESS_UPDATE);
            }
            mHandler.sendEmptyMessageDelayed(MSG_PROGRESS_UPDATE, 100);
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mProgressBar = (HorizontalProgressBarWithAnim) findViewById(R.id.progressbar);
        button = (Button) findViewById(R.id.button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mProgressBar.setProgress(0);
                mHandler.sendEmptyMessage(MSG_PROGRESS_UPDATE);
            }
        });
        Button button1 = (Button) findViewById(R.id.button1);
        button1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!mProgressBar.isRunning()) {
                    mProgressBar.startAnim(200);
                } else {
                    mProgressBar.stopAnim();
                }
            }
        });
    }
}
