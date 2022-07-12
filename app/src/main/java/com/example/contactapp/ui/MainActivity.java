package com.example.contactapp.ui;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.example.contactapp.R;
import com.example.contactapp.data.db.AppDatabase;
import com.example.contactapp.data.model.Contact;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    private RecyclerView rvContact;
    private ConstraintLayout emptyLayout;

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
        if(contacts.size() > 0){
            hideEmptyView();
        }else{
            showEmptyView();
        }
    }

    private void showEmptyView(){
        rvContact.setVisibility(View.GONE);
        emptyLayout.setVisibility(View.VISIBLE);
    }

    private void hideEmptyView(){
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

    private void addNewContact(){}
    private void importContact(){}
    private void exportContact(){}
}