//package com.s23010305.roadguard;
//
//import android.content.ContentValues;
//import android.content.Context;
//import android.database.Cursor;
//import android.database.sqlite.SQLiteDatabase;
//import android.database.sqlite.SQLiteOpenHelper;
//
//public class DatabaseHelper extends SQLiteOpenHelper {
//    private static final String DATABASE_NAME = "RoadGuard.db";
//    private static final int DATABASE_VERSION = 1;
//    private static final String TABLE_USERS = "users";
//    private static final String COLUMN_ID = "id";
//    private static final String COLUMN_FIRST_NAME = "first_name";
//    private static final String COLUMN_LAST_NAME = "last_name";
//    private static final String COLUMN_EMAIL = "email";
//    private static final String COLUMN_USERNAME = "username";
//    private static final String COLUMN_PASSWORD = "password";
//
//    private static DatabaseHelper instance;
//
//    private DatabaseHelper(Context context) {
//        super(context, DATABASE_NAME, null, DATABASE_VERSION);
//    }
//
//    public static synchronized DatabaseHelper getInstance(Context context) {
//        if (instance == null) {
//            instance = new DatabaseHelper(context.getApplicationContext());
//        }
//        return instance;
//    }
//
//    @Override
//    public void onCreate(SQLiteDatabase db) {
//        String CREATE_USERS_TABLE = "CREATE TABLE " + TABLE_USERS + " (" +
//                COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
//                COLUMN_FIRST_NAME + " TEXT," +
//                COLUMN_LAST_NAME + " TEXT," +
//                COLUMN_EMAIL + " TEXT," +
//                COLUMN_USERNAME + " TEXT," +
//                COLUMN_PASSWORD + " TEXT)";
//        db.execSQL(CREATE_USERS_TABLE);
//    }
//
//    @Override
//    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
//        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);
//        onCreate(db);
//    }
//
//    public boolean addUser(String firstName, String lastName, String email, String username, String password) {
//        SQLiteDatabase db = this.getWritableDatabase();
//        ContentValues values = new ContentValues();
//        values.put(COLUMN_FIRST_NAME, firstName);
//        values.put(COLUMN_LAST_NAME, lastName);
//        values.put(COLUMN_EMAIL, email);
//        values.put(COLUMN_USERNAME, username);
//        values.put(COLUMN_PASSWORD, password);
//        long result = db.insert(TABLE_USERS, null, values);
//        db.close();
//        return result != -1;
//    }
//
//    public boolean checkUser(String username, String password) {
//        SQLiteDatabase db = this.getReadableDatabase();
//        String[] columns = {COLUMN_ID};
//        String selection = COLUMN_USERNAME + "=? AND " + COLUMN_PASSWORD + "=?";
//        String[] selectionArgs = {username, password};
//        Cursor cursor = db.query(TABLE_USERS, columns, selection, selectionArgs, null, null, null);
//        int count = cursor.getCount();
//        cursor.close();
//        db.close();
//        return count > 0;
//    }
//
//    public boolean isUsernameTaken(String username) {
//        SQLiteDatabase db = this.getReadableDatabase();
//        String[] columns = {COLUMN_ID};
//        String selection = COLUMN_USERNAME + "=?";
//        String[] selectionArgs = {username};
//        Cursor cursor = db.query(TABLE_USERS, columns, selection, selectionArgs, null, null, null);
//        int count = cursor.getCount();
//        cursor.close();
//        db.close();
//        return count > 0;
//    }
//
//    public boolean isEmailTaken(String email) {
//        SQLiteDatabase db = this.getReadableDatabase();
//        String[] columns = {COLUMN_ID};
//        String selection = COLUMN_EMAIL + "=?";
//        String[] selectionArgs = {email};
//        Cursor cursor = db.query(TABLE_USERS, columns, selection, selectionArgs, null, null, null);
//        int count = cursor.getCount();
//        cursor.close();
//        db.close();
//        return count > 0;
//    }
//}