package com.example.accountbook.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "account_book.db";
    private static final int DATABASE_VERSION = 3;

    private static final String TABLE_USERS = "users";
    private static final String TABLE_RECORDS = "records";
    private static final String TABLE_CATEGORIES = "categories";
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
    private static final String COL_NAME = "name";
    private static final String COL_IS_DEFAULT = "is_default";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        createUsersTable(db);
        createRecordsTable(db);
        createCategoriesTable(db);
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

    private void createCategoriesTable(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE_CATEGORIES + " ("
                + COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COL_USER_ID + " INTEGER NOT NULL, "
                + COL_NAME + " TEXT NOT NULL, "
                + COL_TYPE + " TEXT NOT NULL, "
                + COL_IS_DEFAULT + " INTEGER NOT NULL DEFAULT 0, "
                + "UNIQUE(" + COL_USER_ID + ", " + COL_NAME + ", " + COL_TYPE + "))");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 2) {
            createRecordsTable(db);
        }
        if (oldVersion < 3) {
            createCategoriesTable(db);
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
        long userId = db.insert(TABLE_USERS, null, values);
        if (userId != -1) {
            ensureDefaultCategories((int) userId);
        }
        return userId;
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
                int userId = cursor.getInt(cursor.getColumnIndexOrThrow(COL_ID));
                ensureDefaultCategories(userId);
                return userId;
            }
            return -1;
        }
    }

    public boolean checkPassword(int userId, String password) {
        SQLiteDatabase db = getReadableDatabase();
        try (Cursor cursor = db.query(
                TABLE_USERS,
                new String[]{COL_ID},
                COL_ID + "=? AND " + COL_PASSWORD + "=?",
                new String[]{String.valueOf(userId), password},
                null,
                null,
                null)) {
            return cursor.moveToFirst();
        }
    }

    public int updatePassword(int userId, String newPassword) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_PASSWORD, newPassword);
        return db.update(
                TABLE_USERS,
                values,
                COL_ID + "=?",
                new String[]{String.valueOf(userId)});
    }

    public void ensureDefaultCategories(int userId) {
        if (userId == -1 || hasCategories(userId)) {
            return;
        }

        String[] expenses = {"餐饮", "交通", "购物", "生活缴费", "其他支出"};
        String[] incomes = {"工资", "奖金", "兼职", "其他收入"};
        for (String name : expenses) {
            addCategory(userId, name, "expense", true);
        }
        for (String name : incomes) {
            addCategory(userId, name, "income", true);
        }
    }

    private boolean hasCategories(int userId) {
        SQLiteDatabase db = getReadableDatabase();
        try (Cursor cursor = db.query(
                TABLE_CATEGORIES,
                new String[]{COL_ID},
                COL_USER_ID + "=?",
                new String[]{String.valueOf(userId)},
                null,
                null,
                null)) {
            return cursor.moveToFirst();
        }
    }

    public long addCategory(int userId, String name, String type, boolean isDefault) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_USER_ID, userId);
        values.put(COL_NAME, name);
        values.put(COL_TYPE, type);
        values.put(COL_IS_DEFAULT, isDefault ? 1 : 0);
        return db.insertWithOnConflict(TABLE_CATEGORIES, null, values, SQLiteDatabase.CONFLICT_IGNORE);
    }

    public Cursor getCategoriesByType(int userId, String type) {
        ensureDefaultCategories(userId);
        SQLiteDatabase db = getReadableDatabase();
        return db.query(
                TABLE_CATEGORIES,
                new String[]{COL_ID, COL_NAME, COL_TYPE, COL_IS_DEFAULT},
                COL_USER_ID + "=? AND " + COL_TYPE + "=?",
                new String[]{String.valueOf(userId), type},
                null,
                null,
                COL_IS_DEFAULT + " DESC, " + COL_NAME + " ASC");
    }

    public Cursor getAllCategories(int userId) {
        ensureDefaultCategories(userId);
        SQLiteDatabase db = getReadableDatabase();
        return db.query(
                TABLE_CATEGORIES,
                new String[]{COL_ID, COL_NAME, COL_TYPE, COL_IS_DEFAULT},
                COL_USER_ID + "=?",
                new String[]{String.valueOf(userId)},
                null,
                null,
                COL_TYPE + " ASC, " + COL_IS_DEFAULT + " DESC, " + COL_NAME + " ASC");
    }

    public int deleteCustomCategory(int userId, int categoryId) {
        SQLiteDatabase db = getWritableDatabase();
        return db.delete(
                TABLE_CATEGORIES,
                COL_ID + "=? AND " + COL_USER_ID + "=? AND " + COL_IS_DEFAULT + "=0",
                new String[]{String.valueOf(categoryId), String.valueOf(userId)});
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

    public Cursor getMonthlyCategoryTotals(int userId, String type, String monthPrefix) {
        SQLiteDatabase db = getReadableDatabase();
        return db.rawQuery(
                "SELECT " + COL_CATEGORY + ", SUM(" + COL_AMOUNT + ") AS total FROM " + TABLE_RECORDS
                        + " WHERE " + COL_USER_ID + "=? AND " + COL_TYPE + "=? AND "
                        + COL_RECORD_DATE + " LIKE ?"
                        + " GROUP BY " + COL_CATEGORY
                        + " ORDER BY total DESC",
                new String[]{String.valueOf(userId), type, monthPrefix + "%"});
    }
}
