package fr.florianlallier.notaresto.database;

import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public class Restaurant {

    // Nom de la table :
    public static final String RESTAURANT_TABLE = "restaurant";

    // Attributs de la table :
    public static final String RESTAURANT_KEY_ROWID = "_id"; // ID
    public static final String RESTAURANT_KEY_NAME = "name"; // Nom
    public static final String RESTAURANT_KEY_ADDRESS = "address"; // Adresse
    public static final String RESTAURANT_KEY_PHONE = "phone"; // Numéro de téléphone
    public static final String RESTAURANT_KEY_WEBSITE = "website"; // Site web
    public static final String RESTAURANT_KEY_MAIL = "mail"; // Adresse e-mail
    public static final String RESTAURANT_KEY_PRICE = "price"; // Catégorie de prix
    public static final String RESTAURANT_KEY_CUISINE = "cuisine"; // Type de cuisine
    public static final String RESTAURANT_KEY_COMMENT = "comment"; // Commentaire personnel
    public static final String RESTAURANT_KEY_NOTE = "note"; // Note personnelle

    private static final String RESTAURANT_CREATE =
            "CREATE TABLE IF NOT EXISTS " + RESTAURANT_TABLE + " (" +
                    RESTAURANT_KEY_ROWID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    RESTAURANT_KEY_NAME + " TEXT NOT NULL DEFAULT '', " +
                    RESTAURANT_KEY_ADDRESS + " TEXT NOT NULL DEFAULT '', " +
                    RESTAURANT_KEY_PHONE + " TEXT NOT NULL DEFAULT '', " +
                    RESTAURANT_KEY_WEBSITE + " TEXT NOT NULL DEFAULT '', " +
                    RESTAURANT_KEY_MAIL + " TEXT NOT NULL DEFAULT '', " +
                    RESTAURANT_KEY_PRICE + " TEXT NOT NULL DEFAULT '', " +
                    RESTAURANT_KEY_CUISINE + " TEXT NOT NULL DEFAULT '', " +
                    RESTAURANT_KEY_COMMENT + " TEXT NOT NULL DEFAULT '', " +
                    RESTAURANT_KEY_NOTE + " REAL NOT NULL DEFAULT '0.0');";

    private static final String RESTAURANT_DROP = "DROP TABLE IF EXISTS " + RESTAURANT_TABLE + ";";

    public static void onCreate(SQLiteDatabase db) {
        db.execSQL(RESTAURANT_CREATE);
    }

    public static void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.i(Restaurant.class.getName(), "Upgrading database from version " + oldVersion + " to " + newVersion + "...");
        db.execSQL(RESTAURANT_DROP);
        onCreate(db);
    }
}
