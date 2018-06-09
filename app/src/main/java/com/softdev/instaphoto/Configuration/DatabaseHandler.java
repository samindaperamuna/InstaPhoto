package com.softdev.instaphoto.Configuration;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.softdev.instaphoto.Dataset.Feed;
import com.softdev.instaphoto.Dataset.Notification;

import java.util.ArrayList;

public class DatabaseHandler extends SQLiteOpenHelper {

    private static final int DBVersion = 1;
    private static final String DBName = "instaDatabase";

    public DatabaseHandler(Context context)
    {
        super(context, DBName, null, DBVersion);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Table Query.
        Log.d("SQLite", "Creating tables ...");
        String notificationTable = "CREATE TABLE notifications (id INTEGER PRIMARY KEY, user INTEGER, postId INTEGER, userId INTEGER, username VARCHAR(255), name VARCHAR(255), icon TEXT, action INTEGER, creation DATETIME)";

        // Executing Query.
        db.execSQL(notificationTable);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS notifications");
        onCreate(db);
    }

    public void resetDatabase() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("DELETE FROM notifications");
        db.close();
    }

    public long addNotification(Notification notification) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("id", notification.id);
        values.put("user", notification.user);
        values.put("postId", notification.postId);
        values.put("userId", notification.userId);
        values.put("username", notification.username);
        values.put("name", notification.name);
        values.put("icon", notification.icon);
        values.put("action", notification.action);
        values.put("creation", notification.creation);
        return db.insertWithOnConflict("notifications", null, values, SQLiteDatabase.CONFLICT_REPLACE);
    }

    public ArrayList<Notification> getNotifications() {
        ArrayList<Notification> notificationArrayList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM notifications ORDER BY creation desc", null);
        if (cursor.moveToFirst()) {
            do {
                Notification n = new Notification();
                n.id = cursor.getString(0);
                n.user = cursor.getString(1);
                n.postId = cursor.getString(2);
                n.userId = cursor.getString(3);
                n.username = cursor.getString(4);
                n.name = cursor.getString(5);
                n.icon = cursor.getString(6);
                n.action = cursor.getString(7);
                n.creation = cursor.getString(8);
                notificationArrayList.add(n);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return notificationArrayList;
    }
}
