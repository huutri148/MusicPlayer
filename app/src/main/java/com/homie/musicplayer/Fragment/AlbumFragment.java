package com.homie.musicplayer.Fragment;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.homie.musicplayer.Adapter.AlbumAdapter;
import com.homie.musicplayer.R;

import static com.homie.musicplayer.MainActivity.albums;


public class AlbumFragment extends Fragment {
    RecyclerView mRecyclerView;
    AlbumAdapter mAlbumAdapter;

    public AlbumFragment() {
        // Required empty public constructor
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_album, container,false);
        mRecyclerView = view.findViewById(R.id.recyclerView);
        mRecyclerView.setHasFixedSize(true);

        if(!(albums.size() < 1)){
            mAlbumAdapter = new AlbumAdapter(getContext(), albums);
            mRecyclerView.setAdapter(mAlbumAdapter);
            mRecyclerView.setLayoutManager(new GridLayoutManager(getContext()
                    ,2));
        }

        return view;
    }
}