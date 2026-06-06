package com.example.accountbook.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "account_book.db";
    private static final int DATABASE_VERSION = 2;

    private static final String TABLE_USERS = "users";
    private static final String TABLE_RECORDS = "records";
    private static final String COL_ID = "id";
    private static final String COL_USER_ID = "user_id";
    private static final String COL_USERNAME = "username";
    private static final String COL_PASSWORD = "password";
    private static final String COL_CREATED_AT = "created_at";
    private static final String COL_TYPE = "type";
    private static final String COL_CATEGORY = "category";
    private static final String COL_AMOUNT = "amount";
    private static final String COL_RECORD_DATE = "record_date";
    private static final String COL_NOTE = "note";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        createUsersTable(db);
        createRecordsTable(db);
    }

    private void createUsersTable(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE_USERS + " ("
                + COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COL_USERNAME + " TEXT UNIQUE NOT NULL, "
                + COL_PASSWORD + " TEXT NOT NULL, "
                + COL_CREATED_AT + " TEXT)");
    }

    private void createRecordsTable(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE_RECORDS + " ("
                + COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COL_USER_ID + " INTEGER NOT NULL, "
                + COL_TYPE + " TEXT NOT NULL, "
                + COL_CATEGORY + " TEXT NOT NULL, "
                + COL_AMOUNT + " REAL NOT NULL, "
                + COL_RECORD_DATE + " TEXT NOT NULL, "
                + COL_NOTE + " TEXT, "
                + COL_CREATED_AT + " TEXT)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 2) {
            createRecordsTable(db);
        }
    }

    public boolean isUsernameExists(String username) {
        SQLiteDatabase db = getReadableDatabase();
        try (Cursor cursor = db.query(
                TABLE_USERS,
                new String[]{COL_ID},
                COL_USERNAME + "=?",
                new String[]{username},
                null,
                null,
                null)) {
            return cursor.moveToFirst();
        }
    }

    public long registerUser(String username, String password) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_USERNAME, username);
        values.put(COL_PASSWORD, password);
        values.put(COL_CREATED_AT, String.valueOf(System.currentTimeMillis()));
        return db.insert(TABLE_USERS, null, values);
    }

    public int validateLogin(String username, String password) {
        SQLiteDatabase db = getReadableDatabase();
        try (Cursor cursor = db.query(
                TABLE_USERS,
                new String[]{COL_ID},
                COL_USERNAME + "=? AND " + COL_PASSWORD + "=?",
                new String[]{username, password},
                null,
                null,
                null)) {
            if (cursor.moveToFirst()) {
                return cursor.getInt(cursor.getColumnIndexOrThrow(COL_ID));
            }
            return -1;
        }
    }

    public long addRecord(int userId, String type, String category, double amount, String recordDate, String note) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_USER_ID, userId);
        values.put(COL_TYPE, type);
        values.put(COL_CATEGORY, category);
        values.put(COL_AMOUNT, amount);
        values.put(COL_RECORD_DATE, recordDate);
        values.put(COL_NOTE, note);
        values.put(COL_CREATED_AT, String.valueOf(System.currentTimeMillis()));
        return db.insert(TABLE_RECORDS, null, values);
    }

    public Cursor getRecordsByUser(int userId) {
        SQLiteDatabase db = getReadableDatabase();
        return db.query(
                TABLE_RECORDS,
                new String[]{COL_ID, COL_TYPE, COL_CATEGORY, COL_AMOUNT, COL_RECORD_DATE, COL_NOTE},
                COL_USER_ID + "=?",
                new String[]{String.valueOf(userId)},
                null,
                null,
                COL_RECORD_DATE + " DESC, " + COL_ID + " DESC");
    }

    public Cursor searchRecords(int userId, String type, String category, String keyword) {
        SQLiteDatabase db = getReadableDatabase();
        StringBuilder selection = new StringBuilder(COL_USER_ID + "=?");
        java.util.ArrayList<String> args = new java.util.ArrayList<>();
        args.add(String.valueOf(userId));

        if (type != null && !type.isEmpty()) {
            selection.append(" AND ").append(COL_TYPE).append("=?");
            args.add(type);
        }

        if (category != null && !category.isEmpty()) {
            selection.append(" AND ").append(COL_CATEGORY).append("=?");
            args.add(category);
        }

        if (keyword != null && !keyword.isEmpty()) {
            selection.append(" AND ").append(COL_NOTE).append(" LIKE ?");
            args.add("%" + keyword + "%");
        }

        return db.query(
                TABLE_RECORDS,
                new String[]{COL_ID, COL_TYPE, COL_CATEGORY, COL_AMOUNT, COL_RECORD_DATE, COL_NOTE},
                selection.toString(),
                args.toArray(new String[0]),
                null,
                null,
                COL_RECORD_DATE + " DESC, " + COL_ID + " DESC");
    }

    public Cursor getRecordById(int recordId, int userId) {
        SQLiteDatabase db = getReadableDatabase();
        return db.query(
                TABLE_RECORDS,
                new String[]{COL_ID, COL_TYPE, COL_CATEGORY, COL_AMOUNT, COL_RECORD_DATE, COL_NOTE},
                COL_ID + "=? AND " + COL_USER_ID + "=?",
                new String[]{String.valueOf(recordId), String.valueOf(userId)},
                null,
                null,
                null);
    }

    public int updateRecord(int recordId, int userId, String type, String category, double amount,
                            String recordDate, String note) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_TYPE, type);
        values.put(COL_CATEGORY, category);
        values.put(COL_AMOUNT, amount);
        values.put(COL_RECORD_DATE, recordDate);
        values.put(COL_NOTE, note);
        return db.update(
                TABLE_RECORDS,
                values,
                COL_ID + "=? AND " + COL_USER_ID + "=?",
                new String[]{String.valueOf(recordId), String.valueOf(userId)});
    }

    public int deleteRecord(int recordId, int userId) {
        SQLiteDatabase db = getWritableDatabase();
        return db.delete(
                TABLE_RECORDS,
                COL_ID + "=? AND " + COL_USER_ID + "=?",
                new String[]{String.valueOf(recordId), String.valueOf(userId)});
    }

    public double getMonthlyTotal(int userId, String type, String monthPrefix) {
        SQLiteDatabase db = getReadableDatabase();
        try (Cursor cursor = db.rawQuery(
                "SELECT SUM(" + COL_AMOUNT + ") FROM " + TABLE_RECORDS
                        + " WHERE " + COL_USER_ID + "=? AND " + COL_TYPE + "=? AND "
                        + COL_RECORD_DATE + " LIKE ?",
                new String[]{String.valueOf(userId), type, monthPrefix + "%"})) {
            if (cursor.moveToFirst()) {
                return cursor.getDouble(0);
            }
            return 0;
        }
    }
}
