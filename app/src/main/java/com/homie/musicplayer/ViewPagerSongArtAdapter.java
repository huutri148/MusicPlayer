package com.homie.musicplayer;

import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.lifecycle.Lifecycle;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.homie.musicplayer.Model.MusicFiles;

import java.io.Serializable;
import java.util.List;

public class ViewPagerSongArtAdapter extends FragmentStatePagerAdapter {
    private List<MusicFiles> songsPager;

    public ViewPagerSongArtAdapter(@NonNull FragmentManager fm, int behavior, List<MusicFiles> musicFilesList) {
        super(fm, behavior);
        this.songsPager = musicFilesList;
    }


    @NonNull
    @Override
    public Fragment getItem(int position) {
        if(songsPager == null || songsPager.isEmpty())
            return null;
        MusicFiles musicFiles = songsPager.get(position);
        Uri uri = Uri.parse(musicFiles.getPath());
        String _uri = musicFiles.getPath();
        SongArtFragment songArtFragment = new SongArtFragment();
        Bundle bundle = new Bundle();
        bundle.putSerializable("song_uri", _uri);
        songArtFragment.setArguments(bundle);
        return songArtFragment;
    }

    @Override
    public int getCount() {

        if(songsPager != null)
            return songsPager.size();
        return 0;
    }
}
