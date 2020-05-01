package fr.florianlallier.notaresto.provider;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;

import androidx.annotation.NonNull;

import fr.florianlallier.notaresto.database.DatabaseHandler;
import fr.florianlallier.notaresto.database.Restaurant;

public class NotarestoProvider extends ContentProvider {

    private DatabaseHandler database; // Base de données

    private static final String AUTHORITY = "fr.florianlallier.notaresto.provider"; // Autorité

    public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/" + Restaurant.RESTAURANT_TABLE);

    private static final int ALL_RESTAURANTS = 1; // Tous les restaurants
    private static final int SINGLE_RESTAURANT = 2; // Un seul restaurant

    private static final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
    static {
        matcher.addURI(AUTHORITY, Restaurant.RESTAURANT_TABLE, ALL_RESTAURANTS); // content://fr.florianlallier.notaresto.provider/restaurant
        matcher.addURI(AUTHORITY, Restaurant.RESTAURANT_TABLE + "/#", SINGLE_RESTAURANT); // content://fr.florianlallier.notaresto.provider/restaurant/#
    }

    public static final String TYPE_DIR = "vnd.android.cursor.dir/vnd." + AUTHORITY + "." + Restaurant.RESTAURANT_TABLE; // Tous les éléments
    public static final String TYPE_ITEM = "vnd.android.cursor.item/vnd." + AUTHORITY + "." + Restaurant.RESTAURANT_TABLE; // Un seul élément

    @Override
    public boolean onCreate() {
        database = new DatabaseHandler(getContext());

        return true;
    }

    @Override
    public String getType(@NonNull Uri uri) {
        switch (matcher.match(uri)) {
            case ALL_RESTAURANTS:
                return TYPE_DIR;
            case SINGLE_RESTAURANT:
                return TYPE_ITEM;
            default:
                throw new IllegalArgumentException("Unsupported URI: " + uri);
        }
    }

    @SuppressWarnings("ConstantConditions")
    @Override
    public Cursor query(@NonNull Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        SQLiteDatabase db = database.getReadableDatabase(); // Ouvre en lecture
        SQLiteQueryBuilder builder = new SQLiteQueryBuilder();

        builder.setTables(Restaurant.RESTAURANT_TABLE);
        switch (matcher.match(uri)) { // Gère les URI
            case ALL_RESTAURANTS:
                // Ne fait rien
                break;
            case SINGLE_RESTAURANT:
                String id = uri.getPathSegments().get(1); // Récupère l'ID
                builder.appendWhere(Restaurant.RESTAURANT_KEY_ROWID + " = " + id);
                break;
            default:
                throw new IllegalArgumentException("Unsupported URI: " + uri);
        }
        Cursor cursor = builder.query(db, projection, selection, selectionArgs, null, null, sortOrder); // Fait la requête
        cursor.setNotificationUri(getContext().getContentResolver(), uri); // Indique des changements

        return cursor;
    }

    @SuppressWarnings("ConstantConditions")
    @Override
    public Uri insert(@NonNull Uri uri, ContentValues values) {
        SQLiteDatabase db = database.getWritableDatabase(); // Ouvre en écriture

        switch (matcher.match(uri)) { // Gère les URI
            case ALL_RESTAURANTS:
                // Ne fait rien
                break;
            default:
                throw new IllegalArgumentException("Unsupported URI: " + uri);
        }
        long id = db.insert(Restaurant.RESTAURANT_TABLE, null, values); // Fait l'insertion
        if (id > -1) { // Ne trouve pas d'erreur dans le insert
            Uri uriRes = ContentUris.withAppendedId(CONTENT_URI, id);
            getContext().getContentResolver().notifyChange(uri, null); // Indique des changements
            return uriRes; // Renvoie l'URI de la nouvelle ligne
        } else {
            throw new SQLException("Unsupported URI: " + uri);
        }
    }

    @SuppressWarnings("ConstantConditions")
    @Override
    public int update(@NonNull Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        SQLiteDatabase db = database.getWritableDatabase(); // Ouvre en écriture

        switch (matcher.match(uri)) { // Gère les URI
            case ALL_RESTAURANTS:
                // Ne fait rien
                break;
            case SINGLE_RESTAURANT:
                String id = uri.getPathSegments().get(1); // Récupère l'ID
                selection = Restaurant.RESTAURANT_KEY_ROWID + " = " + id + (!TextUtils.isEmpty(selection) ? " AND (" + selection + ")" : "");
                break;
            default:
                throw new IllegalArgumentException("Unsupported URI: " + uri);
        }
        int count = db.update(Restaurant.RESTAURANT_TABLE, values, selection, selectionArgs);
        getContext().getContentResolver().notifyChange(uri, null); // Indique des changements

        return count; // Renvoie le nombre de lignes modifiées
    }

    @SuppressWarnings("ConstantConditions")
    @Override
    public int delete(@NonNull Uri uri, String selection, String[] selectionArgs) {
        SQLiteDatabase db = database.getWritableDatabase(); // Ouvre en écriture

        switch (matcher.match(uri)) { // Gère les URI
            case ALL_RESTAURANTS:
                // Ne fait rien
                break;
            case SINGLE_RESTAURANT:
                String id = uri.getPathSegments().get(1); // Récupère l'ID
                selection = Restaurant.RESTAURANT_KEY_ROWID + " = " + id + (!TextUtils.isEmpty(selection) ? " AND (" + selection + ")" : "");
                break;
            default:
                throw new IllegalArgumentException("Unsupported URI: " + uri);
        }
        int count = db.delete(Restaurant.RESTAURANT_TABLE, selection, selectionArgs);
        getContext().getContentResolver().notifyChange(uri, null); // Indique des changements

        return count; // Renvoie le nombre de lignes supprimées
    }
}
