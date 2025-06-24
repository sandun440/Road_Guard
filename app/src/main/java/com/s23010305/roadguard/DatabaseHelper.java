package com.s23010305.roadguard;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String TAG = "DatabaseHelper";
    private static final String DATABASE_NAME = "RoadGuard.db";
    private static final int DATABASE_VERSION = 2; // Incremented version due to schema change

    // Table name
    private static final String TABLE_USERS = "users";

    // Column names
    private static final String COLUMN_ID = "id";
    private static final String COLUMN_FIRST_NAME = "first_name";
    private static final String COLUMN_LAST_NAME = "last_name";
    private static final String COLUMN_EMAIL = "email";
    private static final String COLUMN_USERNAME = "username";
    private static final String COLUMN_PASSWORD = "password";
    private static final String COLUMN_FINGERPRINT_ENABLED = "fingerprint_enabled"; // New column

    private static DatabaseHelper instance;
    private final Context context; // Added to store context for SharedPreferences

    private DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.context = context.getApplicationContext();
    }

    public static synchronized DatabaseHelper getInstance(Context context) {
        if (instance == null) {
            instance = new DatabaseHelper(context.getApplicationContext());
        }
        return instance;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_USERS_TABLE = "CREATE TABLE " + TABLE_USERS + " (" +
                COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                COLUMN_FIRST_NAME + " TEXT NOT NULL," +
                COLUMN_LAST_NAME + " TEXT NOT NULL," +
                COLUMN_EMAIL + " TEXT NOT NULL UNIQUE," +
                COLUMN_USERNAME + " TEXT NOT NULL UNIQUE," +
                COLUMN_PASSWORD + " TEXT NOT NULL," +
                COLUMN_FINGERPRINT_ENABLED + " INTEGER DEFAULT 0)"; // New column, default false (0)

        db.execSQL(CREATE_USERS_TABLE);
        Log.d(TAG, "Database table created successfully");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 2) {
            // Add fingerprint_enabled column for version 2
            String ALTER_TABLE = "ALTER TABLE " + TABLE_USERS + " ADD COLUMN " +
                    COLUMN_FINGERPRINT_ENABLED + " INTEGER DEFAULT 0";
            try {
                db.execSQL(ALTER_TABLE);
                Log.d(TAG, "Added fingerprint_enabled column to users table");
            } catch (Exception e) {
                Log.e(TAG, "Error adding fingerprint_enabled column: " + e.getMessage());
            }
        }
        Log.d(TAG, "Database upgraded from version " + oldVersion + " to " + newVersion);
    }

    // Add a new user to the database
    public boolean addUser(String firstName, String lastName, String email, String username, String password) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(COLUMN_FIRST_NAME, firstName);
        values.put(COLUMN_LAST_NAME, lastName);
        values.put(COLUMN_EMAIL, email);
        values.put(COLUMN_USERNAME, username);
        values.put(COLUMN_PASSWORD, password);
        values.put(COLUMN_FINGERPRINT_ENABLED, 0); // Default to false for new users

        try {
            long result = db.insert(TABLE_USERS, null, values);
            if (result != -1) {
                Log.d(TAG, "User added successfully: " + username);
                return true;
            } else {
                Log.e(TAG, "Failed to add user: " + username);
                return false;
            }
        } catch (Exception e) {
            Log.e(TAG, "Error adding user: " + e.getMessage());
            return false;
        } finally {
            db.close();
        }
    }

    // Check if user credentials are valid
    public boolean checkUser(String username, String password) {
        SQLiteDatabase db = this.getReadableDatabase();
        String[] columns = {COLUMN_ID};
        String selection = COLUMN_USERNAME + "=? AND " + COLUMN_PASSWORD + "=?";
        String[] selectionArgs = {username, password};

        Cursor cursor = null;
        try {
            cursor = db.query(TABLE_USERS, columns, selection, selectionArgs, null, null, null);
            int count = cursor.getCount();
            Log.d(TAG, "Login attempt for user: " + username + ", Found: " + count);
            return count > 0;
        } catch (Exception e) {
            Log.e(TAG, "Error checking user: " + e.getMessage());
            return false;
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            db.close();
        }
    }

    // Check if username is already taken
    public boolean isUsernameTaken(String username) {
        SQLiteDatabase db = this.getReadableDatabase();
        String[] columns = {COLUMN_ID};
        String selection = COLUMN_USERNAME + "=?";
        String[] selectionArgs = {username};

        Cursor cursor = null;
        try {
            cursor = db.query(TABLE_USERS, columns, selection, selectionArgs, null, null, null);
            int count = cursor.getCount();
            return count > 0;
        } catch (Exception e) {
            Log.e(TAG, "Error checking username: " + e.getMessage());
            return false;
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            db.close();
        }
    }

    // Check if email is already taken
    public boolean isEmailTaken(String email) {
        SQLiteDatabase db = this.getReadableDatabase();
        String[] columns = {COLUMN_ID};
        String selection = COLUMN_EMAIL + "=?";
        String[] selectionArgs = {email};

        Cursor cursor = null;
        try {
            cursor = db.query(TABLE_USERS, columns, selection, selectionArgs, null, null, null);
            int count = cursor.getCount();
            return count > 0;
        } catch (Exception e) {
            Log.e(TAG, "Error checking email: " + e.getMessage());
            return false;
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            db.close();
        }
    }

    // Get user information by username
    public User getUserByUsername(String username) {
        SQLiteDatabase db = this.getReadableDatabase();
        String[] columns = {COLUMN_ID, COLUMN_FIRST_NAME, COLUMN_LAST_NAME,
                COLUMN_EMAIL, COLUMN_USERNAME, COLUMN_PASSWORD, COLUMN_FINGERPRINT_ENABLED};
        String selection = COLUMN_USERNAME + "=?";
        String[] selectionArgs = {username};

        Cursor cursor = null;
        try {
            cursor = db.query(TABLE_USERS, columns, selection, selectionArgs, null, null, null);
            if (cursor != null && cursor.moveToFirst()) {
                User user = new User();
                user.setId(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ID)));
                user.setFirstName(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_FIRST_NAME)));
                user.setLastName(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_LAST_NAME)));
                user.setEmail(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_EMAIL)));
                user.setUsername(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_USERNAME)));
                user.setPassword(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PASSWORD)));
                user.setFingerprintEnabled(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_FINGERPRINT_ENABLED)) == 1);

                Log.d(TAG, "User retrieved: " + user.getUsername());
                return user;
            }
        } catch (Exception e) {
            Log.e(TAG, "Error getting user: " + e.getMessage());
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            db.close();
        }
        return null;
    }

    // Get all users (for debugging purposes)
    public void printUsers() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;
        try {
            cursor = db.rawQuery("SELECT * FROM " + TABLE_USERS, null);
            Log.d(TAG, "=== ALL USERS IN DATABASE ===");
            if (cursor.moveToFirst()) {
                do {
                    int id = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ID));
                    String firstName = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_FIRST_NAME));
                    String lastName = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_LAST_NAME));
                    String email = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_EMAIL));
                    String username = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_USERNAME));
                    String password = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PASSWORD));
                    int fingerprintEnabled = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_FINGERPRINT_ENABLED));

                    Log.d(TAG, "ID: " + id + ", Name: " + firstName + " " + lastName +
                            ", Email: " + email + ", Fingerprint Enabled: " + (fingerprintEnabled == 1 ? "true" : "false"));
                } while (cursor.moveToNext());
            } else {
                Log.d(TAG, "No users found in database");
            }
            Log.d(TAG, "=== END OF USER LIST ===");
        } catch (Exception e) {
            Log.e(TAG, "Error printing users: " + e.getMessage());
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            db.close();
        }
    }

    // Update user information
    public boolean updateUser(String username, String firstName, String lastName, String email, String password) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(COLUMN_FIRST_NAME, firstName);
        values.put(COLUMN_LAST_NAME, lastName);
        values.put(COLUMN_EMAIL, email);
        values.put(COLUMN_PASSWORD, password);

        String selection = COLUMN_USERNAME + " = ?";
        String[] selectionArgs = {username};

        try {
            int rowsAffected = db.update(TABLE_USERS, values, selection, selectionArgs);
            if (rowsAffected > 0) {
                Log.d(TAG, "User updated successfully: " + username);
                return true;
            } else {
                Log.e(TAG, "Failed to update user: " + username);
                return false;
            }
        } catch (Exception e) {
            Log.e(TAG, "Error updating user: " + e.getMessage());
            return false;
        } finally {
            db.close();
        }
    }

    // Delete user
    public boolean deleteUser(String username) {
        SQLiteDatabase db = this.getWritableDatabase(); // Changed to getWritableDatabase for deletion
        String selection = COLUMN_USERNAME + " = ?";
        String[] selectionArgs = {username};

        try {
            int rowsDeleted = db.delete(TABLE_USERS, selection, selectionArgs);
            if (rowsDeleted > 0) {
                Log.d(TAG, "User deleted successfully: " + username);
                return true;
            } else {
                Log.e(TAG, "Failed to delete user: " + username);
                return false;
            }
        } catch (Exception e) {
            Log.e(TAG, "Error deleting user: " + e.getMessage());
            return false;
        } finally {
            db.close();
        }
    }

    public boolean saveFingerprintStatus(boolean status) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_FINGERPRINT_ENABLED, status ? 1 : 0);

        // Retrieve the current username from SharedPreferences
        SharedPreferences prefs = context.getSharedPreferences("RoadGuardPrefs", Context.MODE_PRIVATE);
        String username = prefs.getString("currentUsername", null);

        if (username == null) {
            Log.e(TAG, "No current user found for saving fingerprint status");
            db.close();
            return false;
        }

        String selection = COLUMN_USERNAME + "=?";
        String[] selectionArgs = {username};

        try {
            int rowsAffected = db.update(TABLE_USERS, values, selection, selectionArgs);
            if (rowsAffected > 0) {
                Log.d(TAG, "Fingerprint status updated successfully for user: " + username);
                return true;
            } else {
                Log.e(TAG, "Failed to update fingerprint status for user: " + username);
                return false;
            }
        } catch (Exception e) {
            Log.e(TAG, "Error updating fingerprint status: " + e.getMessage());
            return false;
        } finally {
            db.close();
        }
    }
}