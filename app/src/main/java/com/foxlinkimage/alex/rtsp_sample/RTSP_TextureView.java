package com.foxlinkimage.alex.rtsp_sample;

import android.graphics.Bitmap;
import android.graphics.SurfaceTexture;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class RTSP_TextureView extends AppCompatActivity implements TextureView.SurfaceTextureListener, View.OnClickListener {

    public static final String LOG_TAG = "LOGG";
    private TextureView mTextureView;
    private MediaPlayer mMediaPlayer;
    private FileOutputStream bos;
    public static int iCapture = 0;
    public static boolean bResolutionChanged = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rtsp__texture_view);
        mTextureView = (TextureView) findViewById(R.id.rtspVideo2);
        Button m_btnCapture = (Button) findViewById(R.id.btnCapture);

        mTextureView.setSurfaceTextureListener(this);
        m_btnCapture.setOnClickListener(this);
    }

    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
        Surface mSurface = new Surface(surface);
        mMediaPlayer = new MediaPlayer();

        try {
            EditText edt = (EditText) findViewById(R.id.editText);
            mMediaPlayer.setDataSource(edt.getEditableText().toString());
            mMediaPlayer.setSurface(mSurface);
            mMediaPlayer.setLooping(true);
            mMediaPlayer.prepareAsync();
            mMediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mp) {
                    mMediaPlayer.start();
                }
            });

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
        Log.i(LOG_TAG, "onSurfaceTextureSizeChanged");
    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
        Log.i(LOG_TAG, "onSurfaceTextureDestroyed");
        return false;
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surface) {

        if (mMediaPlayer != null) {
            //依據影片大小, 重新reset textureview控件的大小
            Log.i(LOG_TAG, "iCapture" + String.valueOf(iCapture) + ", bResolutionChanged: " + bResolutionChanged);
            if (iCapture == 1) {  //抓圖的時候, 將控件大小設為影片大小
                mTextureView.setLayoutParams(new LinearLayout.LayoutParams(mMediaPlayer.getVideoWidth(), mMediaPlayer.getVideoHeight()));
                Log.i(LOG_TAG, "Set size:" + String.valueOf(mMediaPlayer.getVideoWidth()) + "x" + String.valueOf(mMediaPlayer.getVideoHeight()));
                bResolutionChanged = true;
            } else if (iCapture == 0) {   //非抓圖時, 控件設為不超過螢幕
                mTextureView.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
            }
        }
    }

    @Override
    public void onClick(View v) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMdd_HH_mm_ss", Locale.TAIWAN);
        Date date = new Date(System.currentTimeMillis());
        String str = simpleDateFormat.format(date);

        final String file_path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).getAbsolutePath() + "/CapRTSP/" + str + ".jpg";
        final File file = new File(file_path);
        switch (v.getId()) {
            case R.id.btnCapture:
                iCapture = 1;
                ImgSaveTask mTask = new ImgSaveTask();
                mTask.execute(file);
                break;
        }
    }

    public class ImgSaveTask extends AsyncTask<File, Void, Void> {
        @Override
        protected Void doInBackground(File... params) {
            boolean bSaved = false;
            while (!bSaved) {
                Log.i(LOG_TAG, "bResolutionChanged: " + bResolutionChanged);
                if (bResolutionChanged) {
                    Log.i(LOG_TAG, "start to save image");
                    Bitmap bmp = getBitmap();
                    try {
                        bos = new FileOutputStream(params[0]);
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                    bmp.compress(Bitmap.CompressFormat.JPEG, 100, bos);
                    try {
                        bos.flush();
                        bos.close();
                        iCapture = 0;
                        bResolutionChanged = false;
                        bSaved = true;
                        Log.i(LOG_TAG, "****Image saved****");
//                        break;
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
            return null;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mMediaPlayer != null) {
            mMediaPlayer.stop();
            mMediaPlayer.release();
            mMediaPlayer = null;
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mMediaPlayer != null) {
            mMediaPlayer.stop();
            mMediaPlayer.release();
            mMediaPlayer = null;
        }
    }

    public Bitmap getBitmap() {
        return mTextureView.getBitmap();
    }
}
