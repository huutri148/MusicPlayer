package com.homie.musicplayer.Adapter;

import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.snackbar.Snackbar;
import com.homie.musicplayer.Activity.PlayerActivity;
import com.homie.musicplayer.Model.MusicFiles;
import com.homie.musicplayer.R;

import java.io.File;
import java.util.ArrayList;


public class SongAdapter extends RecyclerView.Adapter<SongAdapter.ViewHolder> {

    private Context mContext;
    public static ArrayList<MusicFiles> mFiles;


    public SongAdapter(Context mContext, ArrayList<MusicFiles> musicFiles){
        this.mFiles = musicFiles;
        this.mContext = mContext;
    }
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.music_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, final int position) {
        holder.file_name.setText(mFiles.get(position).getTitle());
        byte[] image = getAlbumArt(mFiles.get(position).getPath());
        if(image != null){
            Glide.with(mContext).asBitmap()
                    .load(image)
                    .into(holder.album_art);
        } else {
            Glide.with(mContext)
                    .load(R.drawable.music);

        }
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(mContext, PlayerActivity.class);
                intent.putExtra("position", position);
                mContext.startActivity(intent);
            }
        });
        holder.menuMore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {
                PopupMenu popupMenu = new PopupMenu(mContext, view);
                popupMenu.getMenuInflater().inflate(R.menu.popup_menu, popupMenu.getMenu());
                popupMenu.show();
                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem menuItem) {
                       switch ((menuItem.getItemId())){
                           case R.id.delete:
                               //Log.e("Temp Extreme Before ->", temp + "");
                               Toast.makeText(mContext, "Delete Clicked!", Toast.LENGTH_SHORT).show();
                               //temp = new Model(list.get(position).getName(), list.get(position).getImg());
                               //Log.e("Temp before -> ", temp + "");
                               deleteFile(position, view);
                               break;
                       }
                       return true;
                    }
                });
            }
        });

    }
    private void deleteFile(int position, View view){
        Uri contentUri = ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, Long.parseLong(mFiles.get(position).getId()));

        File file = new File(mFiles.get(position).getPath());
        boolean deleted = file.delete();
        if(deleted){
            mContext.getContentResolver().delete(contentUri, null,null );
            mFiles.remove(position);
            notifyItemRemoved(position);
            notifyItemRangeChanged(position, mFiles.size());
            Snackbar.make(view, "File deleted: ", Snackbar.LENGTH_LONG)
                    //.setActionTextColor(mContext.getResources().getColor(android.R.color.holo_blue_dark))
                    .show();
        } else {
            Snackbar.make(view, "Can't be deleted: ", Snackbar.LENGTH_LONG)
                    .show();
        }

    }
    @Override
    public int getItemCount() {
        return mFiles.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView file_name;
        ImageView album_art, menuMore;
        public ViewHolder(@NonNull View itemView){
            super(itemView);
            file_name = itemView.findViewById(R.id.music_file_name);
            album_art = itemView.findViewById(R.id.music_img);
            menuMore = itemView.findViewById(R.id.menuMore);
        }
    }
    private byte[] getAlbumArt(String uri){
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        retriever.setDataSource(uri);
        byte[] art = retriever.getEmbeddedPicture();
        retriever.release();
        return art;
    }
    public void updateList(ArrayList<MusicFiles> musicFilesArrayList){
        mFiles = new ArrayList<>();
        mFiles.addAll(musicFilesArrayList);
        notifyDataSetChanged();
    }
}