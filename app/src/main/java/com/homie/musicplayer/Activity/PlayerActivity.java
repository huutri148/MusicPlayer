package com.homie.musicplayer.Activity;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NavUtils;
import androidx.core.app.NotificationCompat;
import androidx.palette.graphics.Palette;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.media.session.MediaSessionCompat;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.homie.musicplayer.ActionPlaying;
import com.homie.musicplayer.MainActivity;
import com.homie.musicplayer.Model.MusicFiles;
import com.homie.musicplayer.MusicService;
import com.homie.musicplayer.MusicService.MyBinder;
import com.homie.musicplayer.NotificationReceiver;
import com.homie.musicplayer.R;

import java.util.ArrayList;
import java.util.Random;

import static android.app.PendingIntent.FLAG_UPDATE_CURRENT;
import static com.homie.musicplayer.Adapter.AlbumDetailsAdapter.musicAlbumFiles;
import static com.homie.musicplayer.ApplicationClass.ACTION_NEXT;
import static com.homie.musicplayer.ApplicationClass.ACTION_PLAY;
import static com.homie.musicplayer.ApplicationClass.ACTION_PREVIOUS;
import static com.homie.musicplayer.ApplicationClass.CHANNEL_ID_2;
import static com.homie.musicplayer.MainActivity.*;
import static com.homie.musicplayer.Adapter.SongAdapter.mFiles;

public class PlayerActivity extends AppCompatActivity
        implements ActionPlaying, ServiceConnection {

    TextView songName, artistName, durationPlayed, durationTotal;
    ImageView coverArt, nextBtn,prevBtn, backBtn, shuffleBtn, repeatBtn;
    FloatingActionButton playPauseBtn;
    SeekBar seekBar;
    int position = -1;
    static Uri uri;
    public static ArrayList<MusicFiles> listSongs = new ArrayList<>();
    //static MediaPlayer mediaPlayer;
    public MusicService mMusicService;
    private Handler handler = new Handler();
    private Thread playThread, prevThread, nextThread;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setFullScreen();
        setContentView(R.layout.activity_player);
        getSupportActionBar().hide();
        overridePendingTransition(R.anim.sliding_down, R.anim.slide_up);
        initViews();
        getIntentMethod();

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                if(mMusicService!= null && b){
                    mMusicService.seekTo(i * 1000);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        PlayerActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if(mMusicService != null){
                    int mCurrentPosition = mMusicService.getCurrentPosition() / 1000;
                    seekBar.setProgress(mCurrentPosition);
                    durationPlayed.setText(formattedTime(mCurrentPosition));
                }
                handler.postDelayed(this, 1000);
            }
        });
        shuffleBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(shuffleBoolean){
                    shuffleBoolean = false;
                    shuffleBtn.setImageResource(R.drawable.ic_baseline_shuffle);
                } else {
                    shuffleBoolean = true;
                    shuffleBtn.setImageResource(R.drawable.ic_baseline_shuffle_on);
                }
            }
        });
        repeatBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(repeatBoolean){
                    repeatBoolean = false;
                    repeatBtn.setImageResource(R.drawable.ic_baseline_repeat);
                } else {
                    repeatBoolean = true;
                    repeatBtn.setImageResource(R.drawable.ic_baseline_repeat_on);
                }
            }
        });
        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                backBtn_click();
            }
        });
        OnBackPressedCallback callback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {

            }
        };
    }

    private void setFullScreen() {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
    }

    @Override
    protected void onResume() {
        Intent intent = new Intent(this, MusicService.class);
        bindService(intent, this, BIND_AUTO_CREATE);
        playThreadBtn();
        nextThreadBtn();
        prevThreadBtn();
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        unbindService(this);
    }

    private void prevThreadBtn() {
        prevThread = new Thread(){
            @Override
            public void run() {
                super.run();
                prevBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        prevBtnClicked();
                    }
                });
            }
        };
        prevThread.start();

    }

    private void backBtn_click() {
        NavUtils.navigateUpFromSameTask(this);
    }

    public void prevBtnClicked() {
        if (mMusicService.isPlaying()){
            mMusicService.stop();
            mMusicService.release();
            if(shuffleBoolean && !repeatBoolean){
                position = getRandom(listSongs.size() - 1);
            } else if (!shuffleBoolean && !repeatBoolean){
                position = ((position - 1) < 0 ? (listSongs.size() - 1) : (position -1));
            }
            //position = ((position - 1) < 0 ? (listSongs.size() - 1) : (position -1));
            uri = Uri.parse(listSongs.get(position).getPath());
            mMusicService.createMediaPlayer(position);
            metaData(uri);
            songName.setText(listSongs.get(position).getTitle());
            artistName.setText(listSongs.get(position).getArtist());
            seekBar.setMax(mMusicService.getDuration() / 1000);
            PlayerActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if(mMusicService != null){
                        int mCurrentPosition = mMusicService.getCurrentPosition() / 1000;
                        seekBar.setProgress(mCurrentPosition);
                    }
                    handler.postDelayed(this, 1000);
                }
            });
            mMusicService.OnCompleted();
            mMusicService.showNotification(R.drawable.ic_baseline_pause);
            playPauseBtn.setBackgroundResource(R.drawable.ic_baseline_pause);
            mMusicService.start();
        } else {
            mMusicService.stop();
            mMusicService.release();
            if(shuffleBoolean && !repeatBoolean){
                position = getRandom(listSongs.size() - 1);
            } else if (!shuffleBoolean && !repeatBoolean){
                position = ((position - 1) < 0 ? (listSongs.size() - 1) : (position -1));
            }
            //position = ((position - 1) < 0 ? (listSongs.size() - 1) : (position -1));
            uri = Uri.parse(listSongs.get(position).getPath());
            //mediaPlayer = MediaPlayer.create(getApplicationContext(), uri);
            mMusicService.createMediaPlayer(position);
            metaData(uri);
            songName.setText(listSongs.get(position).getTitle());
            artistName.setText(listSongs.get(position).getArtist());
            seekBar.setMax(mMusicService.getDuration() / 1000);
            PlayerActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if(mMusicService != null){
                        int mCurrentPosition = mMusicService.getCurrentPosition() / 1000;
                        seekBar.setProgress(mCurrentPosition);
                    }
                    handler.postDelayed(this, 1000);
                }
            });
            mMusicService.OnCompleted();
            mMusicService.showNotification(R.drawable.ic_baseline_pause);
            playPauseBtn.setBackgroundResource(R.drawable.ic_baseline_play);
        }
    }

    private void nextThreadBtn() {
        nextThread = new Thread(){
            @Override
            public void run() {
                super.run();
                nextBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        nextBtnClicked();
                    }
                });
            }
        };
        nextThread.start();
    }

    public void nextBtnClicked() {
        if (mMusicService.isPlaying()){
            mMusicService.stop();
            mMusicService.release();
            if(shuffleBoolean && !repeatBoolean){
                position = getRandom(listSongs.size() - 1);
            } else if (!shuffleBoolean && !repeatBoolean){
                position = ((position + 1) % listSongs.size());
            }
            //position = ((position + 1) % listSongs.size());
            uri = Uri.parse(listSongs.get(position).getPath());
            //mediaPlayer = MediaPlayer.create(getApplicationContext(), uri);
            mMusicService.createMediaPlayer(position);
            metaData(uri);
            songName.setText(listSongs.get(position).getTitle());
            artistName.setText(listSongs.get(position).getArtist());
            seekBar.setMax(mMusicService.getDuration() / 1000);
            PlayerActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if(mMusicService != null){
                        int mCurrentPosition = mMusicService.getCurrentPosition() / 1000;
                        seekBar.setProgress(mCurrentPosition);
                    }
                    handler.postDelayed(this, 1000);
                }
            });
            mMusicService.OnCompleted();
            mMusicService.showNotification(R.drawable.ic_baseline_pause);
            playPauseBtn.setBackgroundResource(R.drawable.ic_baseline_pause);
            mMusicService.start();
        } else {
            mMusicService.stop();
            mMusicService.release();
            if(shuffleBoolean && !repeatBoolean){
                position = getRandom(listSongs.size() - 1);
            } else if (!shuffleBoolean && !repeatBoolean){
                position = ((position + 1) % listSongs.size());
            }
            uri = Uri.parse(listSongs.get(position).getPath());
            //mediaPlayer = MediaPlayer.create(getApplicationContext(), uri);
            mMusicService.createMediaPlayer(position);
            metaData(uri);
            songName.setText(listSongs.get(position).getTitle());
            artistName.setText(listSongs.get(position).getArtist());
            seekBar.setMax(mMusicService.getDuration() / 1000);
            PlayerActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if(mMusicService != null){
                        int mCurrentPosition = mMusicService.getCurrentPosition() / 1000;
                        seekBar.setProgress(mCurrentPosition);
                    }
                    handler.postDelayed(this, 1000);
                }
            });
            mMusicService.OnCompleted();
            mMusicService.showNotification(R.drawable.ic_baseline_play);
            playPauseBtn.setBackgroundResource(R.drawable.ic_baseline_play);
        }

    }

    private int getRandom(int i) {
        Random random = new Random();
        return  random.nextInt(i + 1);
    }

    private void playThreadBtn() {
        playThread = new Thread(){
            @Override
            public void run() {
                super.run();
                playPauseBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        playPauseBtnClick();
                    }
                });
            }
        };
        playThread.start();
    }

    public void playPauseBtnClick() {
        if (mMusicService.isPlaying()){
            playPauseBtn.setImageResource(R.drawable.ic_baseline_play);
            mMusicService.showNotification(R.drawable.ic_baseline_play);
            mMusicService.pause();
            seekBar.setMax(mMusicService.getDuration() / 1000);
            PlayerActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if(mMusicService != null){
                        int mCurrentPosition = mMusicService.getCurrentPosition() / 1000;
                        seekBar.setProgress(mCurrentPosition);
                    }
                    handler.postDelayed(this, 1000);
                }
            });
        } else {
            mMusicService.showNotification(R.drawable.ic_baseline_pause);
            playPauseBtn.setImageResource(R.drawable.ic_baseline_pause);
            mMusicService.start();
            seekBar.setMax(mMusicService.getDuration() /1000);
            PlayerActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if(mMusicService != null){
                        int mCurrentPosition = mMusicService.getCurrentPosition() / 1000;
                        seekBar.setProgress(mCurrentPosition);
                    }
                    handler.postDelayed(this, 1000);
                }
            });
        }
    }

    private String formattedTime(int mCurrentPosition) {
        String totalout = "";
        String totalNew = "";
        String seconds = String.valueOf(mCurrentPosition % 60);
        String minutes = String.valueOf(mCurrentPosition / 60);
        totalout = minutes + ":" + seconds;
        totalNew = minutes + ":" + "0" + seconds;
        if(seconds.length() == 1){
            return totalNew;
        } else {
            return totalout;
        }
    }

    private void getIntentMethod() {
        position = getIntent().getIntExtra("position", -1);
        String sender = getIntent().getStringExtra("sender");
        if (sender != null && sender.equals("albumDetails")){
            listSongs = musicAlbumFiles;
        } else {
            listSongs = mFiles;
        }
        if(listSongs != null){
            playPauseBtn.setImageResource(R.drawable.ic_baseline_pause);
            uri = Uri.parse(listSongs.get(position).getPath());
        }
        Intent intent = new Intent(this, MusicService.class );
        intent.putExtra("servicePosition", position);
        startService(intent);

    }

    private void initViews() {
        songName = findViewById(R.id.song_name);
        artistName = findViewById(R.id.song_artist);
        durationPlayed = findViewById(R.id.durationPlayed);
        durationTotal = findViewById(R.id.durationTotal);
        coverArt = findViewById(R.id.cover_art);
        nextBtn = findViewById(R.id.id_next);
        prevBtn = findViewById(R.id.id_prev);
        backBtn = findViewById(R.id.back_btn);
        shuffleBtn = findViewById(R.id.id_shuffle);
        repeatBtn = findViewById(R.id.id_repeat);
        playPauseBtn = findViewById(R.id.play_pause);
        seekBar = findViewById(R.id.seekBar);
        backBtn = findViewById(R.id.back_btn);
    }
    private void metaData(Uri uri){
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        retriever.setDataSource(uri.toString());
        int durationTotal = Integer.parseInt(listSongs.get(position).getDuration()) /1000;
        this.durationTotal.setText(formattedTime(durationTotal));
        byte[] art = retriever.getEmbeddedPicture();
        Bitmap bitmap = null;
        if(art != null){


            bitmap = BitmapFactory.decodeByteArray(art, 0, art.length);
            ImageAnimation(this,coverArt,bitmap);
            Palette.from(bitmap).generate(new Palette.PaletteAsyncListener() {
                @Override
                public void onGenerated(@Nullable Palette palette) {
                    Palette.Swatch swatch = palette.getDominantSwatch();
                    if (swatch!= null){
                        RelativeLayout mContainer  = findViewById(R.id.mContainer);
                        mContainer.setBackgroundResource(R.drawable.main_bg);
                        GradientDrawable gradientDrawable = new GradientDrawable(GradientDrawable.Orientation.BOTTOM_TOP,
                                new int[]{swatch.getRgb(), 0x00000000});
                        GradientDrawable gradientDrawableBg = new GradientDrawable(GradientDrawable.Orientation.BOTTOM_TOP,
                                new int[]{swatch.getRgb(), swatch.getRgb()});
                        songName.setTextColor(swatch.getRgb());
                        artistName.setTextColor(swatch.getTitleTextColor());
                        playPauseBtn.setBackgroundColor(swatch.getRgb());
                        playPauseBtn.setBackground(gradientDrawable);
                    } else {
                        songName.setTextColor(Color.GRAY);
                        artistName.setTextColor(Color.GRAY);
                        playPauseBtn.setBackgroundColor(Color.argb(100,92,136,247));
                    }
                }
            });
        } else {
            Glide.with(this)
                    .asBitmap()
                    .load(R.drawable.author_image)
                    .into(coverArt);
            RelativeLayout mContainer  = findViewById(R.id.mContainer);
            mContainer.setBackgroundResource(R.drawable.main_bg);
            songName.setTextColor(Color.GRAY);
            artistName.setTextColor(Color.DKGRAY);
            playPauseBtn.setBackgroundColor(Color.argb(100,92,136,247));

        }
    }
    public void ImageAnimation(final Context context, final ImageView imageView, final Bitmap bitmap){
        Animation animOut = AnimationUtils.loadAnimation(context, android.R.anim.fade_out);
        final Animation animIn = AnimationUtils.loadAnimation(context, android.R.anim.fade_in);
        animOut.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                Glide.with(context).load(bitmap).into(imageView);
                animIn.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {

                    }

                    @Override
                    public void onAnimationEnd(Animation animation) {

                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {

                    }
                });
                imageView.startAnimation(animIn);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        imageView.startAnimation(animOut);
    }


    @Override
    public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
        MyBinder myBinder = (MyBinder) iBinder;
        mMusicService = myBinder.getService();
        mMusicService.setCallBack(this);
//        Toast.makeText(this, "Connected" + mMusicService,
//                Toast.LENGTH_SHORT).show();
        seekBar.setMax(mMusicService.getDuration() / 1000);
        metaData(uri);
        songName.setText((listSongs.get(position).getTitle()));
        artistName.setText(listSongs.get(position).getArtist());
        mMusicService.OnCompleted();
        mMusicService.showNotification(R.drawable.ic_baseline_pause);
    }

    @Override
    public void onServiceDisconnected(ComponentName componentName) {
        mMusicService = null;
    }


}