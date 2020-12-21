package com.homie.musicplayer;

import android.content.Context;
import android.content.Intent;
import android.media.MediaMetadataRetriever;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.zip.Inflater;

import static com.homie.musicplayer.MainActivity.musicFiles;

public class AlbumDetailsAdapter extends RecyclerView.Adapter<AlbumDetailsAdapter.ViewHolder>{
    private Context mContext;
    public static ArrayList<MusicFiles> musicAlbumFiles;
    View view;
    public AlbumDetailsAdapter(Context context, ArrayList<MusicFiles> musicAlbumFiles) {
        mContext = context;
        this.musicAlbumFiles = musicAlbumFiles;
    }

    private byte[] getAlbumArt(String uri){
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        retriever.setDataSource(uri);
        byte[] art = retriever.getEmbeddedPicture();
        retriever.release();
        return art;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        view = LayoutInflater.from(mContext).inflate(R.layout.music_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, final int position) {
        holder.song_name.setText(musicAlbumFiles.get(position).getTitle());
        byte[] image = getAlbumArt(musicAlbumFiles.get(position).getPath());
        if (image != null){
            Glide.with(mContext).asBitmap()
                    .load(image)
                    .into(holder.song_image);
        } else {
            Glide.with(mContext)
                    .load(R.drawable.no_music_tianyi)
                    .into(holder.song_image);
        }
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(mContext, PlayerActivity.class);
                intent.putExtra("sender", "albumDetails");
                intent.putExtra("position", position);
                mContext.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return musicAlbumFiles.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        ImageView song_image;
        TextView song_name;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            song_image = itemView.findViewById(R.id.music_img);
            song_name = itemView.findViewById(R.id.music_file_name);
        }
    }
}
