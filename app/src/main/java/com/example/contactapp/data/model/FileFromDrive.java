package com.example.contactapp.data.model;

import com.google.api.services.drive.model.File;

public class FileFromDrive {
    private boolean isSelected;
    private File file;

    public FileFromDrive(boolean isSelected, File file) {
        this.isSelected = isSelected;
        this.file = file;
    }

    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean selected) {
        isSelected = selected;
    }

    public File getFile() {
        return file;
    }

    public void setFile(File file) {
        this.file = file;
    }
}
