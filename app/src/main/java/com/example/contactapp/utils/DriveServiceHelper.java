package com.example.contactapp.utils;

import androidx.annotation.Nullable;

import com.example.contactapp.data.model.GoogleDriveFileHolder;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.api.client.http.FileContent;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class DriveServiceHelper {

    private final Executor mExecutor = Executors.newSingleThreadExecutor();
    private final Drive mDriveService;
    private final String TAG = "DRIVE_TAG";


    public DriveServiceHelper(Drive driveService) {

        mDriveService = driveService;
    }

    public Task<Void> downloadFile(java.io.File targetFile, String fileId) {
        return Tasks.call(mExecutor, () -> {

            // Retrieve the metadata as a File object.
            OutputStream outputStream = new FileOutputStream(targetFile);
            mDriveService.files().get(fileId).executeMediaAndDownloadTo(outputStream);
            return null;
        });
    }

// TO LIST FILES

    public List<File> listDriveImageFiles() throws IOException{

        FileList result;
        String pageToken = null;
        do {
            result = mDriveService.files().list()
/*.setQ("mimeType='image/png' or mimeType='text/plain'")This si to list both image and text files. Mind the type of image(png or jpeg).setQ("mimeType='image/png' or mimeType='text/plain'") */
                    .setQ("mimeType='text/plain'") //only get file txt
                    .setSpaces("drive")
                    .setFields("nextPageToken, files(id, name)")
                    .setPageToken(pageToken)
                    .execute();

            pageToken = result.getNextPageToken();
        } while (pageToken != null);

        return result.getFiles();
    }

// TO UPLOAD A FILE ONTO DRIVE

    public Task<GoogleDriveFileHolder> uploadFile(final java.io.File localFile,
                                                  final String mimeType, @Nullable final String folderId) {
        return Tasks.call(mExecutor, new Callable<GoogleDriveFileHolder>() {
            @Override
            public GoogleDriveFileHolder call() throws Exception {
                // Retrieve the metadata as a File object.

                List<String> root;
                if (folderId == null) {
                    root = Collections.singletonList("root");
                } else {

                    root = Collections.singletonList(folderId);
                }

                File metadata = new File()
                        .setParents(root)
                        .setMimeType(mimeType)
                        .setName(localFile.getName());

                FileContent fileContent = new FileContent(mimeType, localFile);

                File fileMeta = mDriveService.files().create(metadata,
                        fileContent).execute();
                GoogleDriveFileHolder googleDriveFileHolder = new
                        GoogleDriveFileHolder();
                googleDriveFileHolder.setId(fileMeta.getId());
                googleDriveFileHolder.setName(fileMeta.getName());
                return googleDriveFileHolder;
            }
        });
    }
}
