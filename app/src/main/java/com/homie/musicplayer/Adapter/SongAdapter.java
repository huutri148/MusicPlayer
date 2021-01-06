package com.homie.musicplayer.Adapter;

import android.annotation.SuppressLint;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.palette.graphics.Palette;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.github.siyamed.shapeimageview.RoundedImageView;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.snackbar.Snackbar;
import com.homie.musicplayer.Activity.PlayerActivity;
import com.homie.musicplayer.Model.MusicFiles;
import com.homie.musicplayer.MusicService;
import com.homie.musicplayer.R;

import java.io.File;
import java.util.ArrayList;

import static com.homie.musicplayer.R.*;


public class SongAdapter extends RecyclerView.Adapter<SongAdapter.ViewHolder> {

    private Context mContext;
    public static ArrayList<MusicFiles> mFiles;
    private int lastPosition = -1;
    private int currentSong = 1;


    public SongAdapter(Context mContext, ArrayList<MusicFiles> musicFiles){
        this.mFiles = musicFiles;
        this.mContext = mContext;
        currentSong = MusicService.position;
    }
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(layout.music_item, parent, false);
        return new ViewHolder(view);
    }


    @SuppressLint("ResourceAsColor")
    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, final int position) {
        holder.file_name.setText(mFiles.get(position).getTitle());
        byte[] image = getAlbumArt(mFiles.get(position).getPath());
        if(image != null){
            Glide.with(mContext).asBitmap()
                    .load(image)
                    .into(holder.album_art);
        } else {
            Glide.with(mContext)
                    .load(drawable.music);
        }
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(mContext, PlayerActivity.class);
                intent.putExtra("position", position);
                mContext.startActivity(intent);
                int temp = currentSong;
                currentSong = position;
                notifyItemChanged(temp);
                notifyItemChanged(currentSong);
            }
        });
        holder.menuMore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {
                PopupMenu popupMenu = new PopupMenu(mContext, view);
                popupMenu.getMenuInflater().inflate(menu.popup_menu, popupMenu.getMenu());
                popupMenu.show();
                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem menuItem) {
                       switch ((menuItem.getItemId())){
                           case id.delete:
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

        setAnimation(holder.itemView, position);
        if(currentSong == position){
//            holder.mCardView.setCardBackgroundColor(Color.argb(100,7,81,155));
//            holder.file_name.setTextColor(Color.argb(100,0,0,0));
//            holder.menuMore.setColorFilter(Color.argb(100,255,255,255));
            if(image != null) {
                Bitmap bitmap = BitmapFactory.decodeByteArray(image, 0, image.length);

                Palette.from(bitmap).generate(new Palette.PaletteAsyncListener() {
                    @Override
                    public void onGenerated(@Nullable Palette palette) {
                        Palette.Swatch swatch = palette.getDominantSwatch();
                        if (swatch != null) {
                            GradientDrawable gradientDrawable = new GradientDrawable(GradientDrawable.Orientation.RIGHT_LEFT,
                                    new int[]{swatch.getRgb(), swatch.getRgb()});
                            holder.mCardView.setBackground(gradientDrawable);
                            holder.file_name.setTextColor(swatch.getTitleTextColor());
                            holder.menuMore.setColorFilter(Color.argb(100, 255, 255, 255));
                        } else {
                            GradientDrawable gradientDrawable = new GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT,
                                    new int[]{0xFF5C88F7, 0xFF5C88F7});
                            holder.mCardView.setBackground(gradientDrawable);
                            holder.file_name.setTextColor(swatch.getTitleTextColor());
                            holder.menuMore.setColorFilter(Color.argb(100, 255, 255, 255));
                        }
                    }
                });
            } else {
                GradientDrawable gradientDrawable = new GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT,
                        new int[]{0xFF5C88F7, 0xFF5C88F7});
                holder.mCardView.setBackground(gradientDrawable);
                holder.file_name.setTextColor(Color.argb(100,255,255,255));
                holder.menuMore.setColorFilter(Color.argb(100, 255, 255, 255));
            }
        } else {
            GradientDrawable gradientDrawable = new GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT,
                    new int[]{0xffffffff, 0xffffffff});
            holder.mCardView.setBackground(gradientDrawable);
            holder.file_name.setTextColor(Color.argb(100,51,60,102));
            holder.menuMore.setColorFilter(Color.argb(100,0,0,0));
        }
    }

    private void setAnimation(View viewToAnim, int position){
        // If the bound view wasn't previously displayed on screen, it's animated
        if(position > lastPosition){
            LayoutAnimationController animation = AnimationUtils.loadLayoutAnimation(mContext, anim.layout_animation);
            viewToAnim.startAnimation(animation.getAnimation());
            lastPosition = position;
        }

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
        ImageView menuMore;
        RoundedImageView album_art;
        MaterialCardView mCardView;
        public ViewHolder(@NonNull View itemView){
            super(itemView);
            file_name = itemView.findViewById(id.music_file_name);
            album_art = itemView.findViewById(id.music_img);
            menuMore = itemView.findViewById(id.menuMore);
            mCardView = itemView.findViewById(id.audio_item);
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