package com.homie.musicplayer;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.ViewPager;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.TableLayout;
import android.widget.Toast;

import com.google.android.material.tabs.TabLayout;

import java.util.ArrayList;

import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

public class MainActivity extends AppCompatActivity {
    private RecyclerView mRecyclerView;
    private SongAdapter mSongAdapter;
    public static final int REQUEST_CODE = 1;
    public static ArrayList<MusicFiles> musicFiles;
    public static ArrayList<MusicFiles> albums = new ArrayList<>();
    static boolean shuffleBoolean = false, repeatBoolean = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initViewPager();
        permission();
//        mRecyclerView = (RecyclerView)findViewById(R.id.tracks);
//        mSongAdapter = new SongAdapter();
//        mRecyclerView.setAdapter(mSongAdapter);
    }

    private void permission() {
        if(ContextCompat.checkSelfPermission(getApplicationContext(), WRITE_EXTERNAL_STORAGE)
        != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{WRITE_EXTERNAL_STORAGE}
            ,REQUEST_CODE);
        } else {
            Toast.makeText(this, "Permission granted", Toast.LENGTH_SHORT).show();
            musicFiles = getAllAudio(this);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode == REQUEST_CODE){
            if(grantResults[0] == PackageManager.PERMISSION_GRANTED){
                musicFiles = getAllAudio(this);
            } else {
                ActivityCompat.requestPermissions(MainActivity.this, new String[]{WRITE_EXTERNAL_STORAGE},
                        REQUEST_CODE);
            }
        }
    }

    private void initViewPager() {
        ViewPager viewPager = findViewById(R.id.viewpager);
        TabLayout tableLayout = findViewById(R.id.tab_layout);
        ViewPagerAdapter viewPagerAdapter = new ViewPagerAdapter(getSupportFragmentManager());
        viewPagerAdapter.addFragment(new SongFragment(),"Songs");
        viewPagerAdapter.addFragment(new AlbumFragment(),"Albums");
        viewPager.setAdapter(viewPagerAdapter);
        tableLayout.setupWithViewPager(viewPager);
    }
    public static ArrayList<MusicFiles> getAllAudio(Context context){
        ArrayList<MusicFiles> tempAudioList = new ArrayList<>();
        ArrayList<String> duplicate = new ArrayList<>();
        Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        String[] projection = {
                MediaStore.Audio.Media.ALBUM,
                MediaStore.Audio.Media.TITLE,
                MediaStore.Audio.Media.DURATION,
                MediaStore.Audio.Media.DATA,
                MediaStore.Audio.Media.ARTIST,
                MediaStore.Audio.Media._ID

        };
        Cursor cursor = context.getContentResolver().query(uri,projection,
                null, null,null);
        if(cursor != null){
            while(cursor.moveToNext()){
                String album = cursor.getString(0);
                String title = cursor.getString(1);
                String duration = cursor.getString(2);
                String path = cursor.getString(3);
                String artist = cursor.getString(4);
                String id = cursor.getString(5);
                MusicFiles musicFiles = new MusicFiles(path, title, artist, album, duration,id);
                Log.e("Path: " + path, "Album: " + album);
                tempAudioList.add(musicFiles);
                if(!duplicate.contains(album)) {
                    albums.add(musicFiles);
                    duplicate.add(album);
                }
            }
            cursor.close();
        }
        return tempAudioList;
    }
    public static class ViewPagerAdapter extends FragmentPagerAdapter{
        private ArrayList<Fragment> mFragments;
        private ArrayList<String> titles;

        public ViewPagerAdapter(@NonNull FragmentManager fm) {
            super(fm);
            this.mFragments = new ArrayList<>();
            this.titles = new ArrayList<>();
        }
        public void addFragment(Fragment fragment, String title){
            mFragments.add(fragment);
            titles.add(title);
        }

        @NonNull
        @Override
        public Fragment getItem(int position) {
            return mFragments.get(position);
        }

        @Override
        public int getCount() {
            return mFragments.size();
        }

        @Nullable
        @Override
        public CharSequence getPageTitle(int position) {
            return titles.get(position);
        }
    }
}