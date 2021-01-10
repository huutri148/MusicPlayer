package com.homie.musicplayer;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Binder;
import android.os.IBinder;
import android.support.v4.media.session.MediaSessionCompat;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.homie.musicplayer.Activity.PlayerActivity;
import com.homie.musicplayer.Model.MusicFiles;

import java.util.ArrayList;

import static android.app.PendingIntent.FLAG_UPDATE_CURRENT;
import static com.homie.musicplayer.Activity.PlayerActivity.listSongs;
import static com.homie.musicplayer.ApplicationClass.ACTION_NEXT;
import static com.homie.musicplayer.ApplicationClass.ACTION_PLAY;
import static com.homie.musicplayer.ApplicationClass.ACTION_PREVIOUS;
import static com.homie.musicplayer.ApplicationClass.CHANNEL_ID_2;
import static com.homie.musicplayer.MainActivity.LAST_SONG;

public class MusicService extends Service implements MediaPlayer.OnCompletionListener {
    IBinder mIBinder = new MyBinder();
    MediaPlayer mMediaPlayer;
    Uri uri;
    public static int position = -1;
    ArrayList<MusicFiles> mMusicFiles = new ArrayList<>();
    ActionPlaying mActionPlaying;
    MediaSessionCompat mMediaSessionCompat;
    public static final String MUSIC_FILE_LAST_PLAYED = "LAST_PLAYED";
    public static final String MUSIC_FILE = "STORED_MUSIC";
    public static final String ARTIST_NAME = "ARTIST NAME";
    public static final String SONG_NAME = "SONG NAME";
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        Log.e("Bind", "Method");
        return mIBinder;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mMediaSessionCompat = new MediaSessionCompat(getBaseContext(), "My Audio");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        int myPosition = intent.getIntExtra("servicePosition", -1);
        String actionName = intent.getStringExtra("ActionName");
        if (myPosition != -1){
            playMedia(myPosition);
        }
        if (actionName != null){
            switch (actionName){
                case "playPause":
                    Toast.makeText(this,"PlayPause"
                            , Toast.LENGTH_SHORT).show();
                    if (mActionPlaying != null){
                        mActionPlaying.playPauseBtnClick();
                    }
                    break;
                case "next":
                    Toast.makeText(this,"Next"
                            , Toast.LENGTH_SHORT).show();
                    if (mActionPlaying != null){
                        mActionPlaying.nextBtnClicked();
                    }
                    break;
                case "previous":
                    Toast.makeText(this,"Previous"
                            , Toast.LENGTH_SHORT).show();
                    if (mActionPlaying != null){
                        mActionPlaying.prevBtnClicked();
                    }
                    break;
            }
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
    public void createMediaPlayer(int posititonInner){
        position = posititonInner;
        uri = Uri.parse(mMusicFiles.get(position).getPath());
        SharedPreferences.Editor editor = getSharedPreferences(MUSIC_FILE_LAST_PLAYED, MODE_PRIVATE)
                .edit();
        editor.putString(MUSIC_FILE, uri.toString());
        editor.apply();
        editor.putString(SONG_NAME, mMusicFiles.get(position).getTitle() );
        editor.apply();
        editor.putString(ARTIST_NAME, mMusicFiles.get(position).getArtist());
        editor.apply();
        editor.putInt(LAST_SONG, position);
        editor.apply();
        mMediaPlayer = MediaPlayer.create(getBaseContext(), uri);
    }
    public void OnCompleted(){
        mMediaPlayer.setOnCompletionListener(this);
    }
    @Override
    public void onCompletion(MediaPlayer mediaPlayer) {
        if (mActionPlaying != null){
            mActionPlaying.nextBtnClicked();
            if(mMediaPlayer != null){
                createMediaPlayer(position);
                mMediaPlayer.start();
                OnCompleted();
            }
        }
    }
    public void setCallBack(ActionPlaying actionPlaying){
        this.mActionPlaying = actionPlaying;
    }

    public void showNotification(int playPauseBtn){
        Intent intent = new Intent(this, PlayerActivity.class);
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0, intent
                ,0);
        Intent prevIntent = new Intent(this, NotificationReceiver.class)
                .setAction(ACTION_PREVIOUS);
        PendingIntent prevPending = PendingIntent.getBroadcast(this, 0, prevIntent
                , FLAG_UPDATE_CURRENT);

        Intent pauseIntent = new Intent(this, NotificationReceiver.class)
                .setAction(ACTION_PLAY);
        PendingIntent pausePending = PendingIntent.getBroadcast(this, 0, pauseIntent
                , FLAG_UPDATE_CURRENT);

        Intent nextIntent = new Intent(this, NotificationReceiver.class)
                .setAction(ACTION_NEXT);
        PendingIntent nextPending = PendingIntent.getBroadcast(this, 0, nextIntent
                , FLAG_UPDATE_CURRENT);
        byte[] picture = null;
        picture = getAlbumArt(mMusicFiles.get(position).getPath());
        Bitmap thumb = null;
        if(picture != null){
            thumb = BitmapFactory.decodeByteArray(picture, 0, picture.length);
        } else {
            thumb = BitmapFactory.decodeResource(getResources(), R.drawable.author_image);
        }
        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID_2)
                .setSmallIcon(playPauseBtn)
                .setLargeIcon(thumb)
                .setContentTitle(mMusicFiles.get(position).getTitle())
                .setContentText(mMusicFiles.get(position).getArtist())
                .addAction(R.drawable.ic_baseline_skip_previous, "Previous", prevPending)
                .addAction(playPauseBtn, "Pause", pausePending)
                .addAction(R.drawable.ic_baseline_skip_next, "Next", nextPending)
                .setStyle(new androidx.media.app.NotificationCompat.MediaStyle()
                        .setMediaSession(mMediaSessionCompat.getSessionToken()))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setOnlyAlertOnce(true)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .build();

        // Service is still killed by system
        // after app is destroyed
        startForeground(1,notification);

    }
    private byte[] getAlbumArt(String uri){
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        retriever.setDataSource(uri);
        byte[] art = retriever.getEmbeddedPicture();
        retriever.release();
        return art;
    }
}
