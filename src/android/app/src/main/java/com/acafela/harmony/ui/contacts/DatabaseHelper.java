package com.acafela.harmony.ui.contacts;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper
{
    private static final String LOG_TAG = "SQL_HELPER";
    private static final String TABLE_NAME = "HarmonyContacts";

    public static DatabaseHelper createContactDatabaseHelper(Context context)
    {
        return new DatabaseHelper(context, "ContactDB", null, 1);
    }

    public DatabaseHelper(
                        Context context,
                        String name,
                        SQLiteDatabase.CursorFactory factory,
                        int version)
    {
        super(context, name, factory, version);
        Log.d(LOG_TAG, "DataBaseHelper()");
    }

    @Override
    public void onCreate(SQLiteDatabase db)
    {
        Log.d(LOG_TAG, "onCreate()");

        StringBuilder strBuilder = new StringBuilder();
        strBuilder.append("CREATE TABLE ");
        strBuilder.append(          TABLE_NAME);
        strBuilder.append("(");
        strBuilder.append(      "id INTEGER PRIMARY KEY AUTOINCREMENT,");
        strBuilder.append(      "name TEXT NOT NULL,");
        strBuilder.append(      "phone TEXT,");
        strBuilder.append(      "email TEXT");
        strBuilder.append(");");
        db.execSQL(strBuilder.toString());
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
    {
    }

    public long insert(
                    String name,
                    String phone,
                    String email)
    {
        Log.d(LOG_TAG, "insert Data " + name);

        ContentValues contentValues = new ContentValues();
        contentValues.put("name", name);
        contentValues.put("phone", phone);
        contentValues.put("email", email);

        SQLiteDatabase db = getWritableDatabase();
        long id = db.insert(TABLE_NAME, null, contentValues);
        Log.d(LOG_TAG, "  inserted: " + id);

        return id;
    }

    public int update(
                    long id,
                    String name,
                    String phone,
                    String email)
    {
        Log.d(LOG_TAG, "update() " + id + " " + name);

        ContentValues contentValues = new ContentValues();
        contentValues.put("name", name);
        contentValues.put("phone", phone);
        contentValues.put("email", email);

        String ids[] = { String.valueOf(id) };

        SQLiteDatabase db = getWritableDatabase();
        int n = db.update(TABLE_NAME, contentValues, "id = ?", ids);
        Log.d(LOG_TAG, "  updated: " + n);

        return n;
    }

    public int delete(long id)
    {
        Log.d(LOG_TAG, "delete() " + id);

        String ids[] = { String.valueOf(id) };

        SQLiteDatabase db = getWritableDatabase();
        int n = db.delete(TABLE_NAME, "id = ?", ids);
        Log.d(LOG_TAG, "  deleted: " + n);

        return n;
    }

    public String query(String phone)
    {
        String name = "";
        Cursor cursor = null;
        try {
            SQLiteDatabase db = getReadableDatabase();
            cursor = db.query(TABLE_NAME, new String[] {"name"}, "phone=?", new String[] {phone}, null, null, null);
            if (cursor != null) {
                while (cursor.moveToNext()) {
                    name = cursor.getString(cursor.getColumnIndex("name"));
                    Log.e(LOG_TAG, "name: " + name);
                    break;
                }
            }
        } finally {
            if (cursor != null) { cursor.close(); }
        }
        return name;
    }

    public List<ContactEntry> query()
    {
        List<ContactEntry> contacts = new ArrayList<>();
        Cursor cursor = null;
        try {
            SQLiteDatabase db = getReadableDatabase();
            cursor = db.query(TABLE_NAME, null, null, null, null, null, null);
            if (cursor != null) {
                while (cursor.moveToNext()) {
                    Log.e(LOG_TAG, cursor.getString(cursor.getColumnIndex("id"))
                            + " " + cursor.getString(cursor.getColumnIndex("name"))
                            + " " + cursor.getString(cursor.getColumnIndex("phone"))
                            + " " + cursor.getString(cursor.getColumnIndex("email")));
                    contacts.add(
                        new ContactEntry(
                            cursor.getString(cursor.getColumnIndex("id")),
                            cursor.getString(cursor.getColumnIndex("name")),
                            cursor.getString(cursor.getColumnIndex("phone")),
                            cursor.getString(cursor.getColumnIndex("email"))
                        )
                    );
                }
            }
        } finally {
            if (cursor != null) { cursor.close(); }
        }
        return contacts;
    }
}
