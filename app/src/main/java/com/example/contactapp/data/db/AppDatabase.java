package com.example.contactapp.data.db;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.contactapp.data.model.Contact;

import java.util.ArrayList;
import java.util.List;

public class AppDatabase extends SQLiteOpenHelper {
    private static AppDatabase instance;

    private static final String DB_NAME = "database";
    private static final int DB_VERSION = 1;
    private static final String TABLE_CONTACT_NAME = "contact";
    private static final String ID = "id";
    private static final String NAME = "name";
    private static final String PHONE_NUMBER = "phone_number";

    private AppDatabase(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    public static AppDatabase getInstance(Context context){
        if(instance == null){
            synchronized (AppDatabase.class){
                instance = new AppDatabase(context.getApplicationContext());
            }
        }
        return instance;
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        String QUERY_CREATE_TABLE_CONTACT = "CREATE TABLE " + TABLE_CONTACT_NAME + " ( " +
                ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                NAME + " TEXT, " +
                PHONE_NUMBER + " TEXT )";

        sqLiteDatabase.execSQL(QUERY_CREATE_TABLE_CONTACT);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {

    }

    public boolean insertContact(Contact contact) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues contentValues = new ContentValues();
        contentValues.put(NAME, contact.getName());
        contentValues.put(PHONE_NUMBER, contact.getPhoneNumber());

        long id = db.insert(
                TABLE_CONTACT_NAME,
                null,
                contentValues
        );
        db.close();
        return id > -1;
    }

    @SuppressLint("Recycle")
    public List<Contact> getAllContacts() {
        List<Contact> contacts = new ArrayList<>();
        String query = "SELECT * FROM " + TABLE_CONTACT_NAME;

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(query, null);

        if(cursor.moveToFirst()){
            do{
                Contact contact = new Contact(
                        cursor.getInt(0),
                        cursor.getString(1),
                        cursor.getString(2));
                contacts.add(contact);
            }while (cursor.moveToNext());
        }
        return contacts;
    }

    public boolean updateContact(int contactId, String name, String number) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues contentValues = new ContentValues();
        contentValues.put(NAME, name);
        contentValues.put(PHONE_NUMBER, number);

        long id = db.update(
                TABLE_CONTACT_NAME,
                contentValues,
                "id = " + contactId,
                null
        );
        db.close();
        return id > -1;
    }

    public boolean deleteContact(int contactId) {
        SQLiteDatabase db = this.getWritableDatabase();

        long id = db.delete(
                TABLE_CONTACT_NAME,
                ID + " = " + contactId,
                null
        );
        db.close();
        return id > -1;
    }
}


