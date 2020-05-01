package fr.florianlallier.notaresto;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import fr.florianlallier.notaresto.database.Restaurant;

public class UpdateRestaurantActivity extends AppCompatActivity {

    private static final String EXTRA_URI = "fr.florianlallier.notaresto.extra.uri";

    private String uri;

    private EditText restaurantName = null;
    private EditText restaurantAddress = null;
    private EditText restaurantPhone = null;
    private EditText restaurantWebsite = null;
    private EditText restaurantMail = null;
    private EditText restaurantCuisine = null;
    private EditText restaurantComment = null;

    private String price;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_restaurant);
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

        restaurantName = (EditText) findViewById(R.id.restaurant_name);
        restaurantAddress = (EditText) findViewById(R.id.restaurant_address);
        restaurantPhone = (EditText) findViewById(R.id.restaurant_phone);
        restaurantWebsite = (EditText) findViewById(R.id.restaurant_website);
        restaurantMail = (EditText) findViewById(R.id.restaurant_mail);
        Spinner restaurantPrice = (Spinner) findViewById(R.id.restaurant_price);
        restaurantCuisine = (EditText) findViewById(R.id.restaurant_cuisine);
        restaurantComment = (EditText) findViewById(R.id.restaurant_comment);

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.restaurant_price, R.layout.spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        restaurantPrice.setAdapter(adapter);

        restaurantName.setText(cursor.getString(1));
        restaurantAddress.setText(cursor.getString(2));
        restaurantPhone.setText(clearNotDefined(cursor.getString(3)));
        restaurantWebsite.setText(clearNotDefined(cursor.getString(4)));
        restaurantMail.setText(clearNotDefined(cursor.getString(5)));
        restaurantPrice.setSelection(findPrice(cursor.getString(6)));
        restaurantCuisine.setText(clearNotDefined(cursor.getString(7)));
        restaurantComment.setText(clearNotDefined(cursor.getString(8)));

        cursor.close();

        restaurantPrice.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            @SuppressWarnings("deprecation")
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position != 0) {
                    ((TextView) parent.getChildAt(0)).setTextColor(Color.BLACK);
                    price = (String) parent.getItemAtPosition(position);
                } else {
                    ((TextView) parent.getChildAt(0)).setTextColor(getResources().getColor(R.color.colorHint));
                    price = "n.d.";
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_update_restaurant, menu); // Ajoute le menu

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_save:
                updateRestaurant();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /**
     * Permet de modifier un restaurant dans la base de données. Après validation, on appelle
     * l'activité de visualisation de restaurant afin d'afficher l'élément modifié.
     */
    private void updateRestaurant() {
        String name = restaurantName.getText().toString();
        String address = restaurantAddress.getText().toString();
        String phone = check(restaurantPhone.getText().toString());
        String website = checkWebsite(restaurantWebsite.getText().toString());
        String mail = check(restaurantMail.getText().toString());
        String cuisine = check(restaurantCuisine.getText().toString());
        String comment = check(restaurantComment.getText().toString());
        if (!name.equals("") && !address.equals("")) {
            ContentResolver resolver = getContentResolver();
            ContentValues values = new ContentValues();
            values.put(Restaurant.RESTAURANT_KEY_NAME, name);
            values.put(Restaurant.RESTAURANT_KEY_ADDRESS, address);
            values.put(Restaurant.RESTAURANT_KEY_PHONE, phone);
            values.put(Restaurant.RESTAURANT_KEY_WEBSITE, website);
            values.put(Restaurant.RESTAURANT_KEY_MAIL, mail);
            values.put(Restaurant.RESTAURANT_KEY_PRICE, price);
            values.put(Restaurant.RESTAURANT_KEY_CUISINE, cuisine);
            values.put(Restaurant.RESTAURANT_KEY_COMMENT, comment);
            resolver.update(Uri.parse(uri), values, null, null); // Modifie le restaurant dans la base de données
            Toast.makeText(this, R.string.updated_restaurant, Toast.LENGTH_SHORT).show();
            finish(); // Termine l'activité pour empêcher d'y revenir avec le bouton back
            Intent restaurant = new Intent(this, RestaurantActivity.class);
            restaurant.putExtra(EXTRA_URI, uri); // Envoie l'URI à RestaurantActivity
            startActivity(restaurant);
        } else {
            Toast.makeText(this, R.string.incomplete_form, Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Vérifie si la chaîne de caractères contient le caractère "non défini". Dans le cas échéant,
     * le supprimer.
     *
     * @param chaine - le format reçu.
     * @return le bon format.
     */
    private String clearNotDefined(String chaine) {
        if (chaine.equals("n.d.")) {
            return "";
        } else {
            return chaine;
        }
    }

    /**
     *  Trouve la position exacte dans le spinner du prix reçu avec la requête.
     *
     *  @param price - le prix reçu.
     *  @return sa position dans le spinner.
     */
    int findPrice(String price) {
        String[] restaurantPrice = getResources().getStringArray(R.array.restaurant_price);
        for (int i = 1; i < restaurantPrice.length; i++) {
            if (price.equals(restaurantPrice[i])) {
                return i;
            }
        }
        return 0;
    }

    /**
     * Vérifie si le format du site web est correct.
     *
     * @param website - le format reçu.
     * @return le bon format.
     */
    private String checkWebsite(String website) {
        if (website.equals("")) {
            return "n.d.";
        } else if (website.startsWith("http://") || website.startsWith("https://")) {
            return website;
        } else {
            return "http://" + website; // Ajoute le schéma de l'URI
        }
    }

    /**
     * Vérifie si le format de la chaîne de caractères est correct.
     *
     * @param chaine - le format reçu.
     * @return le bon format.
     */
    private String check(String chaine) {
        if (chaine.equals("")) {
            return "n.d.";
        } else {
            return chaine;
        }
    }
}