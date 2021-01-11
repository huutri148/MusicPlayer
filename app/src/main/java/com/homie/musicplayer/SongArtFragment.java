package com.homie.musicplayer;

import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;

import static com.homie.musicplayer.MainActivity.PATH_TO_FRAG;


public class SongArtFragment extends Fragment {

    ImageView art_cover;
    private View mView;

    public SongArtFragment() {
        // Required empty public constructor
    }



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        mView = inflater.inflate(R.layout.fragment_song_art, container, false);
        art_cover = mView.findViewById(R.id.cover_art);
        Bundle receive = getArguments();
        if (receive != null) {
            String uri = (String) receive.get("song_uri");
            if (uri != null) {
                byte[] art = getAlbumArt(uri);
                if (art != null) {
                    Glide.with(getContext()).load(art)
                            .into(art_cover);
                } else {
                    Glide.with(getContext()).load(R.drawable.author_image)
                            .into(art_cover);
                }
            }
        }
        return mView;
    }
    private byte[] getAlbumArt(String uri){
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        retriever.setDataSource(uri);
        byte[] art = retriever.getEmbeddedPicture();
        retriever.release();
        return art;
    }
}