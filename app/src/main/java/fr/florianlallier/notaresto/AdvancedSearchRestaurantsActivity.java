package fr.florianlallier.notaresto;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

public class AdvancedSearchRestaurantsActivity extends AppCompatActivity {

    public static final String EXTRA_NAME = "fr.florianlallier.notaresto.extra.name";
    public static final String EXTRA_ADDRESS = "fr.florianlallier.notaresto.extra.address";
    public static final String EXTRA_PRICE = "fr.florianlallier.notaresto.extra.price";
    public static final String EXTRA_CUISINE = "fr.florianlallier.notaresto.extra.cuisine";
    public static final String EXTRA_NOTE_MIN = "fr.florianlallier.notaresto.extra.note_min";
    public static final String EXTRA_NOTE_MAX = "fr.florianlallier.notaresto.extra.note_max";

    private EditText restaurantName = null;
    private EditText restaurantAddress = null;
    private EditText restaurantCuisine = null;
    private EditText restaurantNoteMin = null;
    private EditText restaurantNoteMax = null;

    private String price;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_advanced_search_restaurants);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        toolbar.setNavigationIcon(R.drawable.ic_menu_back);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed(); // Retourne à l'activité précédente
            }
        });

        restaurantName = (EditText) findViewById(R.id.restaurant_name);
        restaurantAddress = (EditText) findViewById(R.id.restaurant_address);
        Spinner restaurantPrice = (Spinner) findViewById(R.id.restaurant_price);
        restaurantCuisine = (EditText) findViewById(R.id.restaurant_cuisine);
        restaurantNoteMin = (EditText) findViewById(R.id.restaurant_note_min);
        restaurantNoteMax = (EditText) findViewById(R.id.restaurant_note_max);
        Button advancedSearchRestaurantSearch = (Button) findViewById(R.id.advanced_search_restaurants_search);

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.restaurant_price, R.layout.spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        restaurantPrice.setAdapter(adapter);

        restaurantPrice.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position != 0) {
                    ((TextView) parent.getChildAt(0)).setTextColor(Color.BLACK);
                    price = (String) parent.getItemAtPosition(position);
                } else {
                    ((TextView) parent.getChildAt(0)).setTextColor(getResources().getColor(R.color.colorHint));
                    price = "";
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        advancedSearchRestaurantSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent searchRestaurants = new Intent(AdvancedSearchRestaurantsActivity.this, SearchRestaurantsActivity.class);
                searchRestaurants.putExtra(EXTRA_NAME, restaurantName.getText().toString());
                searchRestaurants.putExtra(EXTRA_ADDRESS, restaurantAddress.getText().toString());
                searchRestaurants.putExtra(EXTRA_PRICE, price);
                searchRestaurants.putExtra(EXTRA_CUISINE, restaurantCuisine.getText().toString());
                searchRestaurants.putExtra(EXTRA_NOTE_MIN, restaurantNoteMin.getText().toString());
                searchRestaurants.putExtra(EXTRA_NOTE_MAX, restaurantNoteMax.getText().toString());
                startActivity(searchRestaurants); // Envoie la recherche à SearchRestaurantsActivity
            }
        });
    }
}
