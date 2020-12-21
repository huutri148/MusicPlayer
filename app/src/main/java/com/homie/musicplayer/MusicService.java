package com.homie.musicplayer;

import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.homie.musicplayer.Model.MusicFiles;

import java.util.ArrayList;

import static com.homie.musicplayer.Activity.PlayerActivity.listSongs;

public class MusicService extends Service implements MediaPlayer.OnCompletionListener {
    IBinder mIBinder = new MyBinder();
    MediaPlayer mMediaPlayer;
    Uri uri;
    int position = -1;
    ArrayList<MusicFiles> mMusicFiles = new ArrayList<>();
    ActionPlaying mActionPlaying;
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        Log.e("Bind", "Method");
        return mIBinder;
    }

    @Override
    public void onCreate() {
        super.onCreate();

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        int myPosition = intent.getIntExtra("servicePosition", -1);
        if (myPosition != -1){
            playMedia(myPosition);
        }
        return START_STICKY;
    }

    private void playMedia(int startPosition) {
        mMusicFiles = listSongs;
        position = startPosition;
        if (mMediaPlayer != null){
            mMediaPlayer.stop();
            mMediaPlayer.release();
            if(mMusicFiles != null){
                createMediaPlayer(position);
                mMediaPlayer.start();
            }
        } else {
            createMediaPlayer(position);
            mMediaPlayer.start();
        }
    }



    public class MyBinder extends Binder{
        public MusicService getService (){
            return MusicService.this;
        }
    }
    public void start(){
        mMediaPlayer.start();
    }
    public boolean isPlaying(){
        return mMediaPlayer.isPlaying();
    }
    public void stop(){
        mMediaPlayer.stop();
    }
    public void release(){
        mMediaPlayer.release();
    }
    public int getDuration(){
        return mMediaPlayer.getDuration();
    }
    public void seekTo(int position){
        mMediaPlayer.seekTo(position);
    }
    public int getCurrentPosition(){
        return mMediaPlayer.getCurrentPosition();
    }
    public void pause(){
        mMediaPlayer.pause();
    }
    public void createMediaPlayer(int postiton){
        uri = Uri.parse(mMusicFiles.get(postiton).getPath());
        mMediaPlayer = MediaPlayer.create(getBaseContext(), uri);
    }
    public void OnCompleted(){
        mMediaPlayer.setOnCompletionListener(this);
    }
    @Override
    public void onCompletion(MediaPlayer mediaPlayer) {
        if (mActionPlaying != null){
            mActionPlaying.nextBtnClicked();
        }

        createMediaPlayer(position);
        mediaPlayer.start();
        OnCompleted();
    }

}
