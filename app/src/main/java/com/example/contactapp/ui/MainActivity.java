package com.example.contactapp.ui;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.contactapp.R;
import com.example.contactapp.data.db.AppDatabase;
import com.example.contactapp.data.model.Contact;
import com.example.contactapp.ui.adapter.RecyclerViewContactAdapter;

import java.util.List;

public class MainActivity extends AppCompatActivity implements RecyclerViewContactAdapter.IAdapterCallback {

    private RecyclerView rvContact;
    private ConstraintLayout emptyLayout;
    private RecyclerViewContactAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mappingView();
        getContactFromDB();
    }


    private void mappingView() {
        rvContact = findViewById(R.id.rvContact);
        emptyLayout = findViewById(R.id.emptyLayout);
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
                    adapter.setListContact(_contacts);
                    rvContact.smoothScrollToPosition(_contacts.size() - 1);
                } else {
                    Toast.makeText(this, "Something went wrong !", Toast.LENGTH_SHORT).show();
                }
            }
        });

        btnCancel.setOnClickListener(view -> {
            dialog.dismiss();
        });

        dialog.show();
    }

    private void importContact() {
    }

    private void exportContact() {
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

        btnCancel.setOnClickListener(view -> {
            dialog.dismiss();
        });

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

        builder.setPositiveButton("Cancel", (dialog, which) -> {
            dialog.dismiss();
        });

        AlertDialog alert = builder.create();
        alert.show();
        alert.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(getResources().getColor(R.color.red));
    }
}