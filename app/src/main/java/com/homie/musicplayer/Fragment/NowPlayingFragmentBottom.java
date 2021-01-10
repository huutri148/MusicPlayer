package com.homie.musicplayer.Fragment;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.os.IBinder;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.homie.musicplayer.ActionPlaying;
import com.homie.musicplayer.Activity.PlayerActivity;
import com.homie.musicplayer.MainActivity;
import com.homie.musicplayer.MusicService;
import com.homie.musicplayer.R;

import javax.xml.xpath.XPath;


import static com.homie.musicplayer.MainActivity.ARTIST_TO_FRAG;
import static com.homie.musicplayer.MainActivity.PATH_TO_FRAG;
import static com.homie.musicplayer.MainActivity.SHOW_MINI_PLAYER;
import static com.homie.musicplayer.MainActivity.SONG_NAME;
import static com.homie.musicplayer.MainActivity.SONG_NAME_TO_FRAG;
import static com.homie.musicplayer.MainActivity.lastSong;

public class NowPlayingFragmentBottom extends Fragment implements ServiceConnection, ActionPlaying {
    ImageView nextBtn, albumArt;
    TextView artistName, songName;
    FloatingActionButton playPauseBtn;
    MusicService mMusicService;
    static Uri uri;
    View view;



    public NowPlayingFragmentBottom() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_now_playing_bottom,
                container, false);
        artistName = view.findViewById(R.id.song_artist_miniPlayer);
        songName = view.findViewById(R.id.song_name_miniPlayer);
        albumArt = view.findViewById(R.id.bottom_album_art);
        nextBtn = view.findViewById(R.id.skip_next_bottom);
        playPauseBtn = view.findViewById(R.id.play_pause_miniPlayer);

//        Intent intent = new Intent(this.getContext(), MusicService.class );
//        intent.putExtra("servicePosition", lastSong);
//        this.getContext().startService(intent);


        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(v.getContext(), PlayerActivity.class);
                if(MainActivity.lastSong != -1)
                {
                    intent.putExtra("position", lastSong);
                    v.getContext().startActivity(intent);
                }
            }
        });
        nextBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
            // TODO: add action in here

            }
        }
        );
        playPauseBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // TODO: add action in here
                playPauseBtnClick();

            }
        });
        return view;
    }

    private byte[] getAlbumArt(String uri){
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        retriever.setDataSource(uri);
        byte[] art = retriever.getEmbeddedPicture();
        retriever.release();
        return art;
    }

    @Override
    public void onResume() {
        super.onResume();
        if(SHOW_MINI_PLAYER){
            if (PATH_TO_FRAG != null) {
                byte[] art = getAlbumArt(PATH_TO_FRAG);
                if (art != null){
                    Glide.with(getContext()).load(art)
                            .into(albumArt);

                } else {
                    Glide.with(getContext()).load(R.drawable.author_image)
                            .into(albumArt);
                }
                songName.setText(SONG_NAME_TO_FRAG);
                artistName.setText(ARTIST_TO_FRAG);

            }
        }
    }

    @Override
    public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
        MusicService.MyBinder myBinder = (MusicService.MyBinder) iBinder;
        mMusicService = myBinder.getService();
        mMusicService.setCallBack(this);
        mMusicService.OnCompleted();
        mMusicService.showNotification(R.drawable.ic_baseline_pause);
    }

    @Override
    public void onServiceDisconnected(ComponentName componentName) {
        mMusicService = null;
    }

    @Override
    public void nextBtnClicked() {

    }

    @Override
    public void prevBtnClicked() {

    }

    @Override
    public void playPauseBtnClick() {
        if (mMusicService.isPlaying()){
            playPauseBtn.setImageResource(R.drawable.ic_baseline_play);
            mMusicService.showNotification(R.drawable.ic_baseline_play);
            mMusicService.pause();
        } else {
            mMusicService.showNotification(R.drawable.ic_baseline_pause);
            playPauseBtn.setImageResource(R.drawable.ic_baseline_pause);
            mMusicService.start();
        }
    }
}