package com.example.contactapp.utils;

import android.os.AsyncTask;
import android.util.Log;

import com.google.api.services.drive.model.File;

import java.io.IOException;
import java.util.List;

public class GetListFileAsyncTask extends AsyncTask<Void, Void, List<File>> {

    IGetListFileCallback callback = null;

    public GetListFileAsyncTask(DriveServiceHelper serviceHelper, IGetListFileCallback callback){
        this.driveServiceHelper = serviceHelper;
        this.callback = callback;
    }

    private static final String TAG = "GoogleDrive";
    List<File> fileList;
    DriveServiceHelper driveServiceHelper;

    @Override
    protected List<File> doInBackground(Void... voids) {

        try {
            fileList = driveServiceHelper.listDriveImageFiles();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return fileList;

    }

    @Override
    protected void onPostExecute(List<File> files) {
        super.onPostExecute(files);

        if(fileList == null) {
            Log.i(TAG, "list file null");
            callback.onGetListFileFailure("Something went wrong when get files from Drive");
            return;
        }

        callback.onGetListFileSuccess(files);
    }
}
