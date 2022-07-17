package com.example.contactapp.utils;

import com.google.api.services.drive.model.File;

import java.util.List;

public interface IGetListFileCallback {
    void onGetListFileSuccess(List<File> files);
    void onGetListFileFailure(String message);
}
