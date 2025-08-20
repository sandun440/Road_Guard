package com.s23010305.roadguard;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {

    // --- DB meta ---
    private static final String DATABASE_NAME = "roadguard.db";
    private static final int DATABASE_VERSION = 1;

    private static DatabaseHelper instance;

    // --- Table & columns ---
    private static final String TABLE_USERS   = "users";
    private static final String COL_ID        = "id";
    private static final String COL_FIRSTNAME = "firstName";
    private static final String COL_LASTNAME  = "lastName";
    private static final String COL_EMAIL     = "email";
    private static final String COL_USERNAME  = "username";
    private static final String COL_PASSWORD  = "password";
    private static final String COL_PHONE     = "phone";

    // Singleton
    public static synchronized DatabaseHelper getInstance(Context context) {
        if (instance == null) {
            instance = new DatabaseHelper(context.getApplicationContext());
        }
        return instance;
    }

    private DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    // --- Lifecycle ---
    @Override
    public void onCreate(SQLiteDatabase db) {
        String createUsersTable =
                "CREATE TABLE " + TABLE_USERS + " ("
                        + COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                        + COL_FIRSTNAME + " TEXT, "
                        + COL_LASTNAME + " TEXT, "
                        + COL_EMAIL + " TEXT UNIQUE, "
                        + COL_USERNAME + " TEXT UNIQUE, "
                        + COL_PASSWORD + " TEXT, "
                        + COL_PHONE + " TEXT"
                        + ")";
        db.execSQL(createUsersTable);

        // Optional: indexes for faster reads (UNIQUE already implies an index, so these are optional)
        // db.execSQL("CREATE INDEX IF NOT EXISTS idx_users_username ON " + TABLE_USERS + "(" + COL_USERNAME + ")");
        // db.execSQL("CREATE INDEX IF NOT EXISTS idx_users_email ON " + TABLE_USERS + "(" + COL_EMAIL + ")");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);
        onCreate(db);
    }

    // --- Helpers ---
    private boolean exists(String selection, String[] args) {
        SQLiteDatabase db = this.getReadableDatabase();
        try (Cursor c = db.query(
                TABLE_USERS,
                new String[]{"1"},     // lightweight projection
                selection,
                args,
                null, null, null,
                "1"                    // LIMIT 1
        )) {
            return c.moveToFirst();
        }
    }

    // --- CRUD ---
    public boolean addUser(String firstName, String lastName, String email, String username, String password) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_FIRSTNAME, firstName);
        values.put(COL_LASTNAME, lastName);
        values.put(COL_EMAIL, email);
        values.put(COL_USERNAME, username);
        values.put(COL_PASSWORD, password);  // Consider hashing before storing
        values.put(COL_PHONE, "");           // default empty phone
        long result = db.insert(TABLE_USERS, null, values);
        return result != -1;
    }

    public boolean checkUser(String username, String password) {
        return exists(COL_USERNAME + "=? AND " + COL_PASSWORD + "=?", new String[]{ username, password });
    }

    public boolean isUsernameTaken(String username) {
        return exists(COL_USERNAME + "=?", new String[]{ username });
    }

    public boolean isEmailTaken(String email) {
        return exists(COL_EMAIL + "=?", new String[]{ email });
    }

    public User getUserByUsername(String username) {
        SQLiteDatabase db = this.getReadableDatabase();

        String[] projection = {
                COL_FIRSTNAME, COL_LASTNAME, COL_EMAIL, COL_USERNAME, COL_PASSWORD, COL_PHONE
        };

        try (Cursor cursor = db.query(
                TABLE_USERS,
                projection,
                COL_USERNAME + "=?",
                new String[]{ username },
                null, null, null
        )) {
            if (cursor.moveToFirst()) {
                String firstName = cursor.getString(cursor.getColumnIndexOrThrow(COL_FIRSTNAME));
                String lastName  = cursor.getString(cursor.getColumnIndexOrThrow(COL_LASTNAME));
                String email     = cursor.getString(cursor.getColumnIndexOrThrow(COL_EMAIL));
                String pwd       = cursor.getString(cursor.getColumnIndexOrThrow(COL_PASSWORD));
                String phone     = cursor.getString(cursor.getColumnIndexOrThrow(COL_PHONE));
                return new User(firstName, lastName, email, username, pwd, phone);
            }
        }
        return null;
    }

    public boolean updatePhone(String username, String phone) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_PHONE, phone);
        int rows = db.update(TABLE_USERS, values, COL_USERNAME + "=?", new String[]{ username });
        return rows > 0;
    }
}
