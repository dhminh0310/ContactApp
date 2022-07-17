package com.example.contactapp.ui.adapter;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.contactapp.R;
import com.example.contactapp.data.model.FileFromDrive;

import java.util.List;

public class RecyclerViewFileFromDriveAdapter extends RecyclerView.Adapter<RecyclerViewFileFromDriveAdapter.FileViewHolder> {

    private List<FileFromDrive> listFiles = null;
    private IFileFromDriveAdapterCallback callback = null;

    @NonNull
    @Override
    public FileViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new FileViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.file_from_drive_item, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull FileViewHolder holder, int position) {
        FileFromDrive currentFile = listFiles.get(position);
        holder.tvFileFromDriveName.setText(currentFile.getFile().getName());
        holder.tvFileFromDriveName.setOnClickListener(view -> {
            setSelectedFile(position);

            if (callback != null) {
                callback.onClickFileItem(currentFile, position);
            }
        });
        if (currentFile.isSelected()) {
            holder.tvFileFromDriveName.setBackgroundColor(Color.parseColor("#00ffff"));
        } else {
            holder.tvFileFromDriveName.setBackgroundColor(Color.parseColor("#ffffff"));
        }
    }

    @Override
    public int getItemCount() {
        return listFiles.size();
    }

    public class FileViewHolder extends RecyclerView.ViewHolder {
        TextView tvFileFromDriveName;

        public FileViewHolder(@NonNull View itemView) {
            super(itemView);
            tvFileFromDriveName = itemView.findViewById(R.id.tvFileFromDriveName);
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    public void setListFiles(List<FileFromDrive> listFiles) {
        this.listFiles = listFiles;
        notifyDataSetChanged();
    }

    public void setCallback(IFileFromDriveAdapterCallback callback) {
        this.callback = callback;
    }

    public interface IFileFromDriveAdapterCallback {
        void onClickFileItem(FileFromDrive file, int position);
    }

    @SuppressLint("NotifyDataSetChanged")
    private void setSelectedFile(int position) {
        for (int i = 0; i < listFiles.size(); i++) {
            listFiles.get(i).setSelected(false);
        }
        listFiles.get(position).setSelected(true);
        notifyDataSetChanged();
    }
}
