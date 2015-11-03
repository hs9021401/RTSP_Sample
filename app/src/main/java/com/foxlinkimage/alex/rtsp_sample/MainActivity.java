package com.foxlinkimage.alex.rtsp_sample;

import android.content.Intent;
import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.VideoView;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    EditText rtspUrl;
    Button playButton, btnJump, btnCap;
    VideoView mVideoView;
    MediaMetadataRetriever mMediaDataRetriever;
    private FileOutputStream bos;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        rtspUrl = (EditText) this.findViewById(R.id.editText);
        mVideoView = (VideoView) this.findViewById(R.id.rtspVideo);
        playButton = (Button) this.findViewById(R.id.playButton);
        btnJump = (Button) findViewById(R.id.jump);
        btnCap = (Button) findViewById(R.id.capButton);

        btnJump.setOnClickListener(this);
        playButton.setOnClickListener(this);
        btnCap.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.playButton:
                RtspStream(rtspUrl.getEditableText().toString());
                break;
            case R.id.jump:
                Intent it = new Intent(this, RTSP_TextureView.class);
                startActivity(it);
                break;

            case R.id.capButton:
                //貌似無法從videoview擷取影像, 使用mediametadataretriver需要有確切的影片位置
                // MediaMetadataRetriever doesn't work with URL's on certain versions of Android
                //https://code.google.com/p/android/issues/detail?id=35794
                int currentPosition = mVideoView.getCurrentPosition();  //in millisecond
                Log.i(RTSP_TextureView.LOG_TAG, "video current position is: " + String.valueOf(currentPosition));

                Bitmap bmp = mMediaDataRetriever.getFrameAtTime(currentPosition * 1000);    //in microsecond
                if (bmp == null)
                    Log.i(RTSP_TextureView.LOG_TAG, "Bitmap is null");
                else {
                    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss", Locale.TAIWAN);
                    Date date = new Date(System.currentTimeMillis());
                    String str = simpleDateFormat.format(date);
                    final String file_path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).getAbsolutePath() + "/CapRTSP/Capture-" + str + ".jpg";
                    File file = new File(file_path);

                    try {
                        bos = new FileOutputStream(file);
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                    bmp.compress(Bitmap.CompressFormat.JPEG, 100, bos);
                    try {
                        bos.flush();
                        bos.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                break;
        }
    }

    private void RtspStream(String rtspUrl) {

//        mMediaDataRetriever = new MediaMetadataRetriever();
//        if (Build.VERSION.SDK_INT >= 14)
//            mMediaDataRetriever.setDataSource(rtspUrl, new HashMap<String, String>());
//        else
//            mMediaDataRetriever.setDataSource(rtspUrl);

        mVideoView.setVideoURI(Uri.parse(rtspUrl));
        mVideoView.requestFocus();
        mVideoView.start();
    }

}
