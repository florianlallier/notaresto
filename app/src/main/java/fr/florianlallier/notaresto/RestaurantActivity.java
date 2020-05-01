package fr.florianlallier.notaresto;

import android.Manifest;
import android.app.AlertDialog;
import android.content.ContentProviderOperation;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.CalendarContract;
import android.provider.ContactsContract;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;
import java.util.ArrayList;

import fr.florianlallier.notaresto.database.Restaurant;

public class RestaurantActivity extends AppCompatActivity {

    private static final String EXTRA_URI = "fr.florianlallier.notaresto.extra.uri";
    public static final String EXTRA_WEBSITE = "fr.florianlallier.notaresto.extra.website";
    private static final int PERMISSIONS_REQUEST_WRITE_CONTACTS = 1;

    private String uri;

    private TextView restaurantName = null;
    private TextView restaurantAddress = null;
    private TextView restaurantPhone = null;
    private TextView restaurantWebsite = null;
    private TextView restaurantMail = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_restaurant);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        toolbar.setNavigationIcon(R.drawable.ic_menu_back);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed(); // Retourne à l'activité précédente
            }
        });

        uri = getIntent().getStringExtra(EXTRA_URI); // Récupère l'URI

        ContentResolver resolver = getContentResolver();
        Cursor cursor = resolver.query(Uri.parse(uri), null, null, null, null); // Récupère le restaurant
        assert cursor != null;
        cursor.moveToFirst();

        restaurantName = (TextView) findViewById(R.id.restaurant_name);
        restaurantAddress = (TextView) findViewById(R.id.restaurant_address);
        restaurantPhone = (TextView) findViewById(R.id.restaurant_phone);
        restaurantWebsite = (TextView) findViewById(R.id.restaurant_website);
        restaurantMail = (TextView) findViewById(R.id.restaurant_mail);
        TextView restaurantPrice = (TextView) findViewById(R.id.restaurant_price);
        TextView restaurantCuisine = (TextView) findViewById(R.id.restaurant_cuisine);
        TextView restaurantComment = (TextView) findViewById(R.id.restaurant_comment);
        RatingBar restaurantNote = (RatingBar) findViewById(R.id.restaurant_note);

        restaurantName.setText(cursor.getString(1));
        restaurantAddress.setText(cursor.getString(2));
        restaurantPhone.setText(cursor.getString(3));
        restaurantWebsite.setText(cursor.getString(4));
        restaurantMail.setText(cursor.getString(5));
        restaurantPrice.setText(cursor.getString(6));
        restaurantCuisine.setText(cursor.getString(7));
        restaurantComment.setText(cursor.getString(8));
        restaurantNote.setRating(cursor.getFloat(9));

        cursor.close();

        italicNotDefined(restaurantPhone);
        italicNotDefined(restaurantWebsite);
        italicNotDefined(restaurantMail);
        italicNotDefined(restaurantPrice);
        italicNotDefined(restaurantCuisine);
        italicNotDefined(restaurantComment);

        if (!restaurantPrice.getText().toString().equals("n.d.")) {
            TextView currency = (TextView) findViewById(R.id.currency);
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
            String preferenceCurrency = preferences.getString(PreferencesActivity.PREFERENCE_CURRENCY, getResources().getString(R.string.currency));
            currency.setText(findCurrency(preferenceCurrency));
        }

        restaurantNote.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {
            @Override
            public void onRatingChanged(RatingBar ratingBar, float rating, boolean fromUser) {
                ContentResolver resolver = getContentResolver();
                ContentValues values = new ContentValues();
                values.put(Restaurant.RESTAURANT_KEY_NOTE, rating);
                resolver.update(Uri.parse(uri), values, null, null); // Modifie la note du restaurant dans la base de données
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_restaurant, menu); // Ajoute le menu

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_update:
                Intent updateRestaurant = new Intent(this, UpdateRestaurantActivity.class);
                updateRestaurant.putExtra(EXTRA_URI, uri); // Envoie l'URI à UpdateRestaurantActivity
                startActivity(updateRestaurant);
                return true;
            case R.id.menu_delete:
                dialogDeleteRestaurant();
                return true;
            case R.id.menu_map:
                String name = restaurantName.getText().toString();
                String address = restaurantAddress.getText().toString();
                Intent map = new Intent(Intent.ACTION_VIEW, Uri.parse("geo:0,0?q=" + name + "+" + address));
                map.setPackage("com.google.android.apps.maps");
                startActivity(map);
                return true;
            case R.id.menu_call:
                String phone = restaurantPhone.getText().toString();
                if (!phone.equals("n.d.")) {
                    Intent call = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + phone));
                    startActivity(call);
                } else {
                    Toast.makeText(this, R.string.not_phone, Toast.LENGTH_SHORT).show();
                }
                return true;
            case R.id.menu_contact:
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && checkSelfPermission(Manifest.permission.WRITE_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
                    requestPermissions(new String[]{Manifest.permission.WRITE_CONTACTS}, PERMISSIONS_REQUEST_WRITE_CONTACTS);
                } else {
                    insertContact(getContentResolver());
                }
                return true;
            case R.id.menu_website:
                String website = restaurantWebsite.getText().toString();
                if (!website.equals("n.d.")) {
                    Intent websiteRestaurant = new Intent(this, WebsiteRestaurantActivity.class);
                    websiteRestaurant.putExtra(EXTRA_WEBSITE, website); // Envoie l'adresse du site web à WebsiteRestaurantActivity
                    startActivity(websiteRestaurant);
                } else {
                    Toast.makeText(this, R.string.not_website, Toast.LENGTH_SHORT).show();
                }
                return true;
            case R.id.menu_mail:
                String mail = restaurantMail.getText().toString();
                if (!mail.equals("n.d.")) {
                    Intent email = new Intent(Intent.ACTION_VIEW, Uri.parse("mailto:"+mail));
                    startActivity(email);
                } else {
                    Toast.makeText(this, R.string.not_mail, Toast.LENGTH_SHORT).show();
                }
                return true;
            case R.id.menu_invite:
                Intent invite = new Intent(Intent.ACTION_SEND);
                invite.setType("text/plain"); // Détermine le type MIME à envoyer
                invite.putExtra(Intent.EXTRA_SUBJECT, R.string.invite_subject);
                invite.putExtra(Intent.EXTRA_TEXT, getResources().getString(R.string.invite_text, restaurantName.getText()));
                startActivity(Intent.createChooser(invite, getResources().getString(R.string.invite_application)));
                return true;
            case R.id.menu_calendar:
                Intent calendar = new Intent(Intent.ACTION_INSERT, CalendarContract.Events.CONTENT_URI);
                calendar.putExtra(CalendarContract.Events.TITLE, "[Notaresto] " + restaurantName.getText());
                calendar.putExtra(CalendarContract.Events.EVENT_LOCATION, restaurantAddress.getText());
                startActivity(calendar);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
     public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == PERMISSIONS_REQUEST_WRITE_CONTACTS) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) { // Permission accordée
                insertContact(getContentResolver());
            } else {
                Toast.makeText(this, R.string.not_inserted_contact, Toast.LENGTH_SHORT).show();
            }
        }
    }

    /**
     * Ouvre une boîte de dialogue demandant une confirmation pour supprimer le restaurant.
     */
    private void dialogDeleteRestaurant() {
        AlertDialog dialogDeleteRestaurant = new AlertDialog.Builder(this).create();
        dialogDeleteRestaurant.setTitle(R.string.delete_title);
        dialogDeleteRestaurant.setButton(AlertDialog.BUTTON_NEGATIVE, getString(R.string.delete_no),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Ferme la boîte de dialogue
                    }
                });
        dialogDeleteRestaurant.setButton(AlertDialog.BUTTON_POSITIVE, getString(R.string.delete_yes),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        ContentResolver resolver = getContentResolver();
                        resolver.delete(Uri.parse(uri), null, null); // Supprime le restaurant de la base de données
                        Toast.makeText(RestaurantActivity.this, R.string.deleted_restaurant, Toast.LENGTH_SHORT).show();
                        Intent main = new Intent(RestaurantActivity.this, MainActivity.class);
                        main.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP); // Retourne à l'activité principale de manière propre
                        startActivity(main);
                    }
                });
        dialogDeleteRestaurant.show();
    }

    /**
     * Permet d'ajouter un contact à la liste de contacts du téléphone.
     *
     * @param resolver - le ContentResolver reçu.
     */
    private void insertContact(ContentResolver resolver) {
        String name = restaurantName.getText().toString();
        String address = restaurantAddress.getText().toString();
        String phone = restaurantPhone.getText().toString();
        String mail = restaurantMail.getText().toString();
        String website = restaurantWebsite.getText().toString();
        ArrayList<ContentProviderOperation> operation = new ArrayList<>();

        operation.add(ContentProviderOperation.newInsert(ContactsContract.RawContacts.CONTENT_URI)
                .withValue(ContactsContract.RawContacts.ACCOUNT_TYPE, null)
                .withValue(ContactsContract.RawContacts.ACCOUNT_NAME, null)
                .build());
        // Nom :
        operation.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
                .withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE)
                .withValue(ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME, name)
                .build());
        // Adresse :
        operation.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
                .withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.StructuredPostal.CONTENT_ITEM_TYPE)
                .withValue(ContactsContract.CommonDataKinds.StructuredPostal.FORMATTED_ADDRESS, address)
                .withValue(ContactsContract.CommonDataKinds.StructuredPostal.TYPE, ContactsContract.CommonDataKinds.StructuredPostal.TYPE_OTHER)
                .build());
        if (!phone.equals("n.d.")) {
            operation.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                    .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
                    .withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE)
                    .withValue(ContactsContract.CommonDataKinds.Phone.NUMBER, phone)
                    .withValue(ContactsContract.CommonDataKinds.Phone.TYPE, ContactsContract.CommonDataKinds.Phone.TYPE_MAIN)
                    .build());
        }
        if (!mail.equals("n.d.")) {
            operation.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                    .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
                    .withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Email.CONTENT_ITEM_TYPE)
                    .withValue(ContactsContract.CommonDataKinds.Email.ADDRESS, mail)
                    .withValue(ContactsContract.CommonDataKinds.Email.TYPE, ContactsContract.CommonDataKinds.Email.TYPE_OTHER)
                    .build());
        }
        if (!website.equals("n.d.")) {
            operation.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                    .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
                    .withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Website.CONTENT_ITEM_TYPE)
                    .withValue(ContactsContract.CommonDataKinds.Website.URL, website)
                    .build());
        }
        try {
            resolver.applyBatch(ContactsContract.AUTHORITY, operation); // Ajoute le contact à la liste de contacts
            Toast.makeText(this, R.string.inserted_contact, Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Vérifie si le TextView contient le caractère "non défini". Dans le cas échéant,
     * le mettre en italic.
     *
     * @param textView - le TextView reçu.
     */
    void italicNotDefined(TextView textView) {
        if (textView.getText().toString().equals("n.d.")) {
            textView.setTypeface(null, Typeface.ITALIC);
        }
    }

    /**
     *  Trouve le symbole de la monnaie en fonction de son nom.
     *
     *  @param currency - le nom de la monnaie.
     *  @return son symbole.
     */
    String findCurrency(String currency) {
        switch (currency) {
            case "Dollar":
                return "$";
            case "円":
                return "¥";
            case "Pound":
                return "£";
            default:
                return "€";
        }
    }
}
