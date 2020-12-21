package com.homie.musicplayer;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import static com.homie.musicplayer.MainActivity.*;
import static com.homie.musicplayer.SongAdapter.mFiles;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link SongFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class SongFragment extends Fragment {

    RecyclerView mRecyclerView;
    static SongAdapter mSongAdapter;
    public static SongFragment newInstance(String param1, String param2) {
        SongFragment fragment = new SongFragment();

        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_song, container,false);
        mRecyclerView = view.findViewById(R.id.recyclerView);
        mRecyclerView.setHasFixedSize(true);

        if(!(musicFiles.size() < 1)){
            mSongAdapter = new SongAdapter(getContext(), musicFiles);
            mRecyclerView.setAdapter(mSongAdapter);
            mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()
            , RecyclerView.VERTICAL, false));
        }

        return view;
    }
}