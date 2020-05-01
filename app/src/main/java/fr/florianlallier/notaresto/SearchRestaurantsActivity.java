package fr.florianlallier.notaresto;

import android.app.LoaderManager;
import android.app.SearchManager;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;

import fr.florianlallier.notaresto.database.Restaurant;
import fr.florianlallier.notaresto.provider.NotarestoProvider;

public class SearchRestaurantsActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final String EXTRA_URI = "fr.florianlallier.notaresto.extra.uri";

    private SimpleCursorAdapter adapter;

    private boolean isAdvancedSearch;

    private String name;
    private String address;
    private String price;
    private String cuisine;
    private String noteMin;
    private String noteMax;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_restaurants);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        toolbar.setNavigationIcon(R.drawable.ic_menu_back);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed(); // Retourne à l'activité précédente
            }
        });

        handleIntent(getIntent());
        displayListView();
    }

    @Override
    protected void onResume() {
        super.onResume();
        getLoaderManager().restartLoader(0, null, this); // Relance le loader
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_search_restaurants, menu); // Ajoute le menu

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_insert:
                Intent insertRestaurant = new Intent(this, InsertRestaurantActivity.class);
                startActivity(insertRestaurant);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        handleIntent(intent);
    }

    /**
     * Configure la recherche en fonction de l'intent reçu.
     *
     * @param intent - l'intent reçu.
     */
    private void handleIntent(Intent intent) {
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) { // Intent via MainActivity
            isAdvancedSearch = false;
            name = intent.getStringExtra(SearchManager.QUERY);
            address = intent.getStringExtra(SearchManager.QUERY);
            cuisine = intent.getStringExtra(SearchManager.QUERY);
        } else { // Intent via AdvancedSearchRestaurantsActivity
            isAdvancedSearch = true;
            name = intent.getStringExtra(AdvancedSearchRestaurantsActivity.EXTRA_NAME);
            address = intent.getStringExtra(AdvancedSearchRestaurantsActivity.EXTRA_ADDRESS);
            price = checkPrice(intent.getStringExtra(AdvancedSearchRestaurantsActivity.EXTRA_PRICE));
            cuisine = intent.getStringExtra(AdvancedSearchRestaurantsActivity.EXTRA_CUISINE);
            noteMin = checkNoteMin(intent.getStringExtra(AdvancedSearchRestaurantsActivity.EXTRA_NOTE_MIN));
            noteMax = checkNoteMax(intent.getStringExtra(AdvancedSearchRestaurantsActivity.EXTRA_NOTE_MAX));
        }
    }

    /**
     * Vérifie si le format de la catégorie de prix est correct.
     *
     * @param price - le format reçu.
     * @return le bon format.
     */
    public String checkPrice(String price) {
        if (price.equals("")) {
            return "%"; // Joker
        } else {
            return price;
        }
    }

    /**
     * Vérifie si le format de la note minimale est correct.
     *
     * @param noteMin - le format reçu.
     * @return le bon format.
     */
    public String checkNoteMin(String noteMin) {
        if (noteMin.equals("")) {
            return "0";
        } else {
            return noteMin;
        }
    }

    /**
     * Vérifie si le format de la note maximale est correct.
     *
     * @param noteMax - le format reçu.
     * @return le bon format.
     */
    public String checkNoteMax(String noteMax) {
        if (noteMax.equals("")) {
            return "5";
        } else {
            return noteMax;
        }
    }

    /**
     * Permet d'afficher la liste des restaurants dans l'activité de recherche. En cas de clic sur
     * un élément de la liste, on appelle l'activité de visualisation de restaurant afin d'afficher
     * l'élément sélectionné.
     */
    private void displayListView() {
        String[] columns = new String[] {Restaurant.RESTAURANT_KEY_NAME, Restaurant.RESTAURANT_KEY_ADDRESS}; // Les informations à afficher
        int[] to = new int[] {R.id.name, R.id.address}; // Endroit où afficher ces informations
        adapter = new SimpleCursorAdapter(this, R.layout.list_item, null, columns, to, 0);
        ListView listview = (ListView) findViewById(R.id.listview);
        listview.setAdapter(adapter);
        getLoaderManager().initLoader(0, null, this); // Initialise le loader

        listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> listView, View view, int position, long id) {
                Cursor cursor = (Cursor) listView.getItemAtPosition(position); // Récupère le curseur
                String rowid = cursor.getString(cursor.getColumnIndexOrThrow(Restaurant.RESTAURANT_KEY_ROWID));
                String uri = NotarestoProvider.CONTENT_URI + "/" + rowid; // Récupère l'ID
                Intent restaurant = new Intent(SearchRestaurantsActivity.this, RestaurantActivity.class);
                restaurant.putExtra(EXTRA_URI, uri); // Envoie l'URI à RestaurantActivity
                startActivity(restaurant);
            }
        });
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        if(!isAdvancedSearch) {
            String selection = "INSTR(LOWER(" + Restaurant.RESTAURANT_KEY_NAME + "), LOWER(?)) > 0 OR " +
                                "INSTR(LOWER(" + Restaurant.RESTAURANT_KEY_ADDRESS + "), LOWER(?)) > 0 OR " +
                                "INSTR(LOWER(" + Restaurant.RESTAURANT_KEY_CUISINE + "), LOWER(?)) > 0";
            return new CursorLoader(this, NotarestoProvider.CONTENT_URI, null, selection, new String[]{name, address, cuisine}, null);
        } else {
            String selection = "INSTR(LOWER(" + Restaurant.RESTAURANT_KEY_NAME + "), LOWER(?)) > 0 AND " +
                                "INSTR(LOWER(" + Restaurant.RESTAURANT_KEY_ADDRESS + "), LOWER(?)) > 0 AND " +
                                Restaurant.RESTAURANT_KEY_PRICE + " LIKE ? AND " +
                                "INSTR(LOWER(" + Restaurant.RESTAURANT_KEY_CUISINE + "), LOWER(?)) > 0 AND " +
                                Restaurant.RESTAURANT_KEY_NOTE + " >= ? AND " +
                                Restaurant.RESTAURANT_KEY_NOTE + " <= ?";
            return new CursorLoader(this, NotarestoProvider.CONTENT_URI, null, selection, new String[]{name, address, price, cuisine, noteMin, noteMax}, null);
        }
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        adapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        adapter.swapCursor(null);
    }
}
