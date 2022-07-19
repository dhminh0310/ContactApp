package com.example.contactapp.ui;

import static com.example.contactapp.utils.Constant.DRIVE_PERMISSION_CODE;
import static com.example.contactapp.utils.Constant.SIGN_IN_CODE;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.example.contactapp.R;
import com.example.contactapp.data.db.AppDatabase;
import com.example.contactapp.data.model.Contact;
import com.example.contactapp.data.model.FileFromDrive;
import com.example.contactapp.ui.adapter.RecyclerViewContactAdapter;
import com.example.contactapp.ui.adapter.RecyclerViewFileFromDriveAdapter;
import com.example.contactapp.utils.ActionType;
import com.example.contactapp.utils.DriveServiceHelper;
import com.example.contactapp.utils.GetListFileAsyncTask;
import com.example.contactapp.utils.IGetListFileCallback;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.Scopes;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.tasks.Task;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.drive.Drive;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

public class MainActivity extends AppCompatActivity implements
        RecyclerViewContactAdapter.IAdapterCallback,
        IGetListFileCallback,
        RecyclerViewFileFromDriveAdapter.IFileFromDriveAdapterCallback {

    private RecyclerView rvContact;
    private ConstraintLayout emptyLayout;
    private RecyclerViewContactAdapter adapter;
    private ConstraintLayout userLayout;
    private TextView tvUserName;
    private TextView btnSignOut;
    private FrameLayout loadingView;
    private ProgressBar pbLoading;
    private ActionType currentActionType = null;

    private GoogleSignInClient googleSignInClient;
    private GoogleSignInAccount account = null;
    private DriveServiceHelper driveServiceHelper = null;
    private List<FileFromDrive> listFileFromDrive = null;
    private RecyclerViewFileFromDriveAdapter fileFromDriveAdapter = null;
    private FileFromDrive currentSelectedFileFromDrive = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mappingView();
        getContactFromDB();

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();

        googleSignInClient = GoogleSignIn.getClient(this, gso);

        handleActionClick();

    }

    @Override
    protected void onStart() {
        super.onStart();

        account = GoogleSignIn.getLastSignedInAccount(this);
        if (account != null) {
            userLayout.setVisibility(View.VISIBLE);
            tvUserName.setText(account.getDisplayName());
        } else {
            userLayout.setVisibility(View.GONE);
        }
    }

    private void mappingView() {
        rvContact = findViewById(R.id.rvContact);
        emptyLayout = findViewById(R.id.emptyLayout);
        userLayout = findViewById(R.id.userLayout);
        tvUserName = findViewById(R.id.tvUserName);
        btnSignOut = findViewById(R.id.btnSignOut);
        loadingView = findViewById(R.id.loadingView);
        pbLoading = findViewById(R.id.pbLoading);
    }

    private void handleActionClick() {
        btnSignOut.setOnClickListener(view -> googleSignInClient.signOut()
                .addOnCompleteListener(this, task -> {
                    userLayout.setVisibility(View.GONE);
                    account = null;
                    Toast.makeText(this, "Sign out successfully", Toast.LENGTH_SHORT).show();
                }));
    }

    private void getContactFromDB() {
        List<Contact> contacts = AppDatabase.getInstance(this).getAllContacts();
        if (contacts.size() > 0) {
            hideEmptyView();
            adapter = new RecyclerViewContactAdapter();
            adapter.setCallback(this);
            adapter.setListContact(contacts);
            rvContact.setAdapter(adapter);
        } else {
            showEmptyView();
        }
    }

    private void showEmptyView() {
        rvContact.setVisibility(View.GONE);
        emptyLayout.setVisibility(View.VISIBLE);
    }

    private void hideEmptyView() {
        rvContact.setVisibility(View.VISIBLE);
        emptyLayout.setVisibility(View.GONE);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return true;
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_add:
                addNewContact();
                return true;
            case R.id.action_import:
                importContact();
                return true;
            case R.id.action_export:
                exportContact();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void addNewContact() {
        final Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCanceledOnTouchOutside(true);
        dialog.setContentView(R.layout.dialog_add_contact);

        EditText edtName = dialog.findViewById(R.id.edtName);
        EditText edtNumber = dialog.findViewById(R.id.edtPhoneNumber);
        TextView btnAdd = dialog.findViewById(R.id.btnAdd);
        TextView btnCancel = dialog.findViewById(R.id.btnCancel);

        btnAdd.setOnClickListener(view -> {
            String name = edtName.getText().toString().trim();
            String number = edtNumber.getText().toString().trim();
            if (name.isEmpty() || number.isEmpty()) {
                Toast.makeText(this, "Please enter name and number", Toast.LENGTH_SHORT).show();
            } else {
                boolean isSuccess = AppDatabase.getInstance(this).insertContact(new Contact(name, number));
                if (isSuccess) {
                    Toast.makeText(this, "Add contact successfully", Toast.LENGTH_SHORT).show();
                    dialog.dismiss();
                    List<Contact> _contacts = AppDatabase.getInstance(this).getAllContacts();
                    if (adapter == null) {
                        adapter = new RecyclerViewContactAdapter();
                        adapter.setCallback(this);
                        rvContact.setAdapter(adapter);
                        hideEmptyView();
                    }
                    adapter.setListContact(_contacts);
                    rvContact.smoothScrollToPosition(_contacts.size() - 1);
                } else {
                    Toast.makeText(this, "Something went wrong !", Toast.LENGTH_SHORT).show();
                }
            }
        });

        btnCancel.setOnClickListener(view -> dialog.dismiss());

        dialog.show();
    }

    private void importContact() {
        if (account == null) {
            signInToDriveAccount();
        } else {
            currentActionType = ActionType.IMPORT;
            checkForGooglePermissions();
        }
    }

    private void exportContact() {
        if (account == null) {
            signInToDriveAccount();
        } else {
            currentActionType = ActionType.EXPORT;
            checkForGooglePermissions();
        }
    }

    private void signInToDriveAccount() {
        Log.d("TAG", "account null ");
        final Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCanceledOnTouchOutside(true);
        dialog.setContentView(R.layout.dialog_sign_in);

        SignInButton btnSignIn = dialog.findViewById(R.id.btnSignIn);
        TextView btnCancel = dialog.findViewById(R.id.btnCancelSignIn);

        btnSignIn.setOnClickListener(view -> {
            Intent signInIntent = googleSignInClient.getSignInIntent();
            startActivityForResult(signInIntent, SIGN_IN_CODE);
            dialog.dismiss();
        });

        btnCancel.setOnClickListener(view -> dialog.dismiss());

        dialog.show();
    }

    private void checkForGooglePermissions() {

        Scope ACCESS_DRIVE_SCOPE = new Scope(Scopes.DRIVE_FILE);
        Scope SCOPE_EMAIL = new Scope(Scopes.EMAIL);

        if (!GoogleSignIn.hasPermissions(
                account,
                ACCESS_DRIVE_SCOPE,
                SCOPE_EMAIL)) {

            GoogleSignIn.requestPermissions(
                    MainActivity.this,
                    DRIVE_PERMISSION_CODE,
                    account,
                    ACCESS_DRIVE_SCOPE,
                    SCOPE_EMAIL);

            Toast.makeText(this, "request permission", Toast.LENGTH_SHORT).show();
        } else {
            driveSetUp();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == SIGN_IN_CODE) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            handleSignInResult(task);
        } else if (requestCode == DRIVE_PERMISSION_CODE) {
            if (resultCode == RESULT_OK) {
                Toast.makeText(this, "Permission was granted, now you can import/export contacts to you Drive", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Permission deny", Toast.LENGTH_SHORT).show();
            }

        }
    }

    private void handleSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {
            account = completedTask.getResult(ApiException.class);
            userLayout.setVisibility(View.VISIBLE);
            tvUserName.setText(account.getDisplayName());
            Toast.makeText(this, "Sign in successfully", Toast.LENGTH_SHORT).show();
            checkForGooglePermissions();
        } catch (ApiException e) {
            Toast.makeText(this, "Sign in fail", Toast.LENGTH_SHORT).show();
            userLayout.setVisibility(View.GONE);
        }
    }

    private void driveSetUp() {

        GoogleSignInAccount mAccount = GoogleSignIn.getLastSignedInAccount(MainActivity.this);

        GoogleAccountCredential credential =
                GoogleAccountCredential.usingOAuth2(
                        getApplicationContext(), Collections.singleton(Scopes.DRIVE_FILE));
        credential.setSelectedAccount(mAccount.getAccount());
        Drive googleDriveService =
                new com.google.api.services.drive.Drive.Builder(
                        AndroidHttp.newCompatibleTransport(),
                        new GsonFactory(),
                        credential)
                        .setApplicationName("GoogleDriveIntegration 3")
                        .build();
        driveServiceHelper = new DriveServiceHelper(googleDriveService);

        //case import
        if (currentActionType == ActionType.IMPORT) {
            showLoadingView();
            new GetListFileAsyncTask(driveServiceHelper, this).execute();
        }

        //case export
        if (currentActionType == ActionType.EXPORT) {
            Log.d(TAG, "case export contacts ");
            showLoadingView();
            doExportContacts();
        }
    }

    private void doExportContacts() {
        List<Contact> contacts = AppDatabase.getInstance(this).getAllContacts();
        if (contacts != null && contacts.size() > 0) {
            String jsonData = new Gson().toJson(contacts);

            String fileName = getFileName(ActionType.EXPORT);
            File path = this.getExternalCacheDir();
            File file = new File(path, fileName);

            //write file
            FileOutputStream stream;
            try {
                stream = new FileOutputStream(file);
                stream.write(jsonData.getBytes());
                stream.flush();
                stream.close();
                uploadContact(file);
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(this, "Export contacts fail", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "There is no contact to export", Toast.LENGTH_SHORT).show();
        }
    }

    @SuppressLint("SimpleDateFormat")
    private String getFileName(ActionType actionType) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd-MM-yyy-hh-mm-ss");
        String dateNow = simpleDateFormat.format(new Date());

        if (actionType == ActionType.EXPORT) return "Contact-" + dateNow + ".json";
        if (actionType == ActionType.IMPORT) return "Download-" + dateNow + ".json";
        return "empty";
    }

    @Override
    public void onClickContactItem(Contact contact) {

    }

    @Override
    public void onEditContact(Contact contact) {
        final Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCanceledOnTouchOutside(true);
        dialog.setContentView(R.layout.dialog_add_contact);

        EditText edtName = dialog.findViewById(R.id.edtName);
        EditText edtNumber = dialog.findViewById(R.id.edtPhoneNumber);
        TextView btnUpdate = dialog.findViewById(R.id.btnAdd);
        TextView btnCancel = dialog.findViewById(R.id.btnCancel);

        edtName.setText(contact.getName());
        edtNumber.setText(contact.getPhoneNumber());
        btnUpdate.setText("Update");

        btnUpdate.setOnClickListener(view -> {
            String name = edtName.getText().toString().trim();
            String number = edtNumber.getText().toString().trim();
            if (name.isEmpty() || number.isEmpty()) {
                Toast.makeText(this, "Please enter name and number", Toast.LENGTH_SHORT).show();
            } else {
                boolean isSuccess = AppDatabase.getInstance(this).updateContact(contact.getId(), name, number);
                if (isSuccess) {
                    Toast.makeText(this, "Update contact successfully", Toast.LENGTH_SHORT).show();
                    dialog.dismiss();
                    List<Contact> _contacts = AppDatabase.getInstance(this).getAllContacts();
                    adapter.setListContact(_contacts);
                } else {
                    Toast.makeText(this, "Something went wrong !", Toast.LENGTH_SHORT).show();
                }
            }
        });

        btnCancel.setOnClickListener(view -> dialog.dismiss());

        dialog.show();
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onDeleteContact(Contact contact) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setTitle("Delete contact");
        builder.setMessage("Are you sure ?");

        builder.setNegativeButton("Ok", (dialog, i) -> {
            boolean isSuccess = AppDatabase.getInstance(this).deleteContact(contact.getId());
            if (!isSuccess) {
                Toast.makeText(this,
                        "Something went wrong when delete feed",
                        Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Delete contact successfully", Toast.LENGTH_SHORT).show();
                List<Contact> _contacts = AppDatabase.getInstance(this).getAllContacts();
                if (_contacts != null && _contacts.size() > 0) {
                    adapter.setListContact(_contacts);
                } else {
                    showEmptyView();
                }
            }
            dialog.dismiss();
        });

        builder.setPositiveButton("Cancel", (dialog, which) -> dialog.dismiss());

        AlertDialog alert = builder.create();
        alert.show();
        alert.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(getResources().getColor(R.color.red));
    }

    @Override
    public void onClickSms(Contact contact) {
        Intent smsIntent = new Intent(Intent.ACTION_VIEW);
        smsIntent.setData(Uri.parse("sms:"));
        smsIntent.putExtra("address", contact.getPhoneNumber());
        smsIntent.putExtra("sms_body","Hello " + contact.getName() + ", this message is sent from Contact app");
        startActivity(smsIntent);
    }

    @Override
    public void onClickCall(Contact contact) {
        Intent intent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + contact.getPhoneNumber()));
        startActivity(intent);
    }

    private static final String TAG = "GoogleDrive";

    private void uploadContact(File file) {

        try {
            driveServiceHelper.uploadFile(file, "application/json", null)
                    .addOnSuccessListener(googleDriveFileHolder -> {
                        hideLoadingView();
                        Toast.makeText(MainActivity.this, "Upload contacts successfully", Toast.LENGTH_SHORT).show();
                    })
                    .addOnFailureListener(e -> {
                        hideLoadingView();
                        Toast.makeText(MainActivity.this, "Upload contacts fail", Toast.LENGTH_SHORT).show();
                    });
        } catch (Exception e) {
            hideLoadingView();
            Toast.makeText(MainActivity.this, "Upload contacts fail", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

    private void showLoadingView() {
        loadingView.setVisibility(View.VISIBLE);
        pbLoading.setVisibility(View.VISIBLE);
        loadingView.setOnClickListener(view -> {
        });
    }

    private void hideLoadingView() {
        loadingView.setVisibility(View.GONE);
        pbLoading.setVisibility(View.GONE);
    }

    @Override
    public void onGetListFileSuccess(List<com.google.api.services.drive.model.File> files) {
        hideLoadingView();
        if (files.size() == 0) {
            Toast.makeText(MainActivity.this, "There are no .txt file in Drive", Toast.LENGTH_SHORT).show();
        } else {
            listFileFromDrive = new ArrayList<>();
            for (com.google.api.services.drive.model.File file : files) {
                listFileFromDrive.add(new FileFromDrive(false, file));
            }
            listFileFromDrive.get(0).setSelected(true);

            final Dialog dialog = new Dialog(this);
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialog.setCanceledOnTouchOutside(false);
            dialog.setContentView(R.layout.dialog_file_from_drive);

            RecyclerView rvFileFromDrive = dialog.findViewById(R.id.rvFileFromDrive);
            TextView btnAddFileFromDrive = dialog.findViewById(R.id.btnAddFileFromDrive);
            TextView btnCancelFileFromDrive = dialog.findViewById(R.id.btnCancelFileFromDrive);

            fileFromDriveAdapter = new RecyclerViewFileFromDriveAdapter();
            fileFromDriveAdapter.setCallback(this);
            rvFileFromDrive.setAdapter(fileFromDriveAdapter);
            fileFromDriveAdapter.setListFiles(listFileFromDrive);

            btnCancelFileFromDrive.setOnClickListener(view -> {
                clearDataDrive(dialog);
            });

            btnAddFileFromDrive.setOnClickListener(view -> {
                showLoadingView();
                if (currentSelectedFileFromDrive != null) {
                    File file = new File(getExternalCacheDir(), getFileName(ActionType.IMPORT));
                    driveServiceHelper.downloadFile(file, currentSelectedFileFromDrive.getFile().getId())
                            .addOnSuccessListener(aVoid -> {
                                readAndImportFile(file, dialog);
                            })
                            .addOnFailureListener(e -> {
                                        hideLoadingView();
                                        clearDataDrive(dialog);
                                        e.printStackTrace();
                                        Toast.makeText(this, "Import contacts fail", Toast.LENGTH_SHORT).show();
                                    }
                            );
                } else {
                    hideLoadingView();
                    clearDataDrive(dialog);
                    Toast.makeText(this, "Please select a file to add", Toast.LENGTH_SHORT).show();
                }
                clearDataDrive(dialog);
            });

            dialog.show();
        }
    }

    private void clearDataDrive(Dialog dialog){
        listFileFromDrive = null;
        fileFromDriveAdapter = null;
        dialog.dismiss();
    }

    private void readAndImportFile(File file, Dialog dialog) {
        int length = (int) file.length();

        byte[] bytes = new byte[length];

        FileInputStream in;
        try {
            in = new FileInputStream(file);
            in.read(bytes);
        } catch (Exception e) {
            hideLoadingView();
            clearDataDrive(dialog);
            e.printStackTrace();
            Toast.makeText(this, "Read file fail", Toast.LENGTH_SHORT).show();
        }

        String contactsJSON = new String(bytes);
        if (contactsJSON.trim().isEmpty()) {
            hideLoadingView();
            clearDataDrive(dialog);
            Toast.makeText(this, "Selected file is empty", Toast.LENGTH_SHORT).show();
        }else{
            try {
                List<Contact> driveContacts = new Gson().fromJson(
                        contactsJSON,
                        new TypeToken<List<Contact>>() {}.getType()
                );
                AppDatabase db = AppDatabase.getInstance(this);
                for(Contact contact : driveContacts){
                    db.insertContact(contact);
                }

                hideLoadingView();
                clearDataDrive(dialog);

                List<Contact> _contacts = AppDatabase.getInstance(this).getAllContacts();
                if (adapter == null) {
                    adapter = new RecyclerViewContactAdapter();
                    adapter.setCallback(this);
                    rvContact.setAdapter(adapter);
                    hideEmptyView();
                }
                adapter.setListContact(_contacts);
                rvContact.smoothScrollToPosition(_contacts.size() - 1);

            }catch (Exception e){
                hideLoadingView();
                clearDataDrive(dialog);
                e.printStackTrace();
                Toast.makeText(this, "Selected file is invalid Contacts File", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onGetListFileFailure(String message) {
        hideLoadingView();
        Toast.makeText(MainActivity.this, message, Toast.LENGTH_SHORT).show();
    }

    @SuppressLint("NotifyDataSetChanged")
    @Override
    public void onClickFileItem(FileFromDrive file, int position) {
        if (listFileFromDrive != null && listFileFromDrive.size() > 0) {
            currentSelectedFileFromDrive = file;
        }
    }
}