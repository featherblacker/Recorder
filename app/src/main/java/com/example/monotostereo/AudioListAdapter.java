package com.example.monotostereo;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.io.File;
import java.sql.Time;

public class AudioListAdapter extends RecyclerView.Adapter<AudioListAdapter.AudioViewHolder> {

    private File[] allFiles;
    private TimeAgo timeAgo;
    private onItemListClick onItemListClick;

    public AudioListAdapter(File[] allFiles, onItemListClick onItemListClick){
        this.allFiles = allFiles;
        this.onItemListClick = onItemListClick;
    }
    @NonNull
    @Override
    public AudioViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.single_list_item, parent, false);
        timeAgo = new TimeAgo();
        return new AudioViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AudioViewHolder holder, int position) {
        holder.list_title.setText(allFiles[position].getName().substring(allFiles[position].getName().length()-18, allFiles[position].getName().length()));
        holder.list_date.setText(timeAgo.getTimeAgo(allFiles[position].lastModified()));
    }

    @Override
    public int getItemCount() {
        return allFiles.length;
    }
//    public int getPosition(){return getA}

    public class AudioViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private TextView list_title;
        private TextView list_date;

        public AudioViewHolder(@NonNull View itemView) {
            super(itemView);

            list_date = itemView.findViewById(R.id.list_date);
            ImageView list_image = itemView.findViewById(R.id.list_image_view);
            list_title = itemView.findViewById(R.id.list_title);
            itemView.setOnClickListener(this);
        }

        public int getAbsolutePosition(){
            return getAbsoluteAdapterPosition();
        }

        @Override
        public void onClick(View view) {
            onItemListClick.onClickListener(allFiles[getAbsoluteAdapterPosition()], getAbsoluteAdapterPosition());
        }
    }

    public interface onItemListClick{
        void onClickListener(File file, int position);
    }
}
