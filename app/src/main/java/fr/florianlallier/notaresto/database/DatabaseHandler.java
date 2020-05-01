package fr.florianlallier.notaresto.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHandler extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "notaresto.db"; // Nom de la base de données
    private static final int DATABASE_VERSION = 1; // Version de la base de données

    public DatabaseHandler(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        Restaurant.onCreate(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Restaurant.onUpgrade(db, oldVersion, newVersion);
    }
}
