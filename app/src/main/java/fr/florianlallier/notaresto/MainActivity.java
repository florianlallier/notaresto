package fr.florianlallier.notaresto;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import android.app.AlertDialog;
import android.app.LoaderManager;
import android.app.SearchManager;
import android.content.Context;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import fr.florianlallier.notaresto.database.Restaurant;
import fr.florianlallier.notaresto.provider.NotarestoProvider;

public class MainActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final String EXTRA_URI = "fr.florianlallier.notaresto.extra.uri";

    private SimpleCursorAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setLogo(R.drawable.ic_menu_logo); // Affiche le logo dans la toolbar

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent insertRestaurant = new Intent(MainActivity.this, InsertRestaurantActivity.class);
                startActivity(insertRestaurant);
            }
        });

        displayListView();
    }

    @Override
    protected void onResume() {
        super.onResume();
        getLoaderManager().restartLoader(0, null, this); // Relance le loader
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu); // Ajoute le menu

        // Ouvre la barre de recherche directement dans la toolbar en cliquant sur l'icône correspondante :
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView = (SearchView) menu.findItem(R.id.menu_search).getActionView();
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_advanced_search:
                Intent advancedSearchRestaurants = new Intent(this, AdvancedSearchRestaurantsActivity.class);
                startActivity(advancedSearchRestaurants);
                return true;
            case R.id.menu_settings:
                Intent preferencesRestaurants = new Intent(this, PreferencesActivity.class);
                startActivity(preferencesRestaurants);
                return true;
            case R.id.menu_about:
                dialogAbout();
                return true;
            case R.id.menu_exit:
                dialogExit();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /**
     * Ouvre une boîte de dialogue affichant les informations concernant l'application.
     */
    private void dialogAbout() {
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_about, null); // Récupère le layout res/layout/dialog_about.xml

        String version = null;
        try {
            version = getPackageManager().getPackageInfo(getPackageName(), 0).versionName; // Récupère la version de l'application
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        if (version != null) {
            ((TextView) view.findViewById(R.id.about_version)).setText(version);
        }

        AlertDialog dialogAbout = new AlertDialog.Builder(this).setView(view).create();
        dialogAbout.setTitle(R.string.about_title);
        dialogAbout.setButton(AlertDialog.BUTTON_NEGATIVE, getString(R.string.about_ok),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Ferme la boîte de dialogue
                    }
                });
        dialogAbout.show();
    }

    /**
     * Ouvre une boîte de dialogue demandant une confirmation pour fermer l'application.
     */
    private void dialogExit() {
        AlertDialog dialogExit = new AlertDialog.Builder(this).create();
        dialogExit.setTitle(R.string.exit_title);
        dialogExit.setButton(AlertDialog.BUTTON_NEGATIVE, getString(R.string.exit_no),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Ferme la boîte de dialogue
                    }
                });
        dialogExit.setButton(AlertDialog.BUTTON_POSITIVE, getString(R.string.exit_yes),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish(); // Ferme l'application
                    }
                });
        dialogExit.show();
    }

    /**
     * Permet d'afficher la liste des restaurants dans l'activité principale. En cas de clic sur un
     * élément de la liste, on appelle l'activité de visualisation de restaurant afin d'afficher
     * l'élément sélectionné.
     */
    private void displayListView() {
        String[] columns = new String[] {Restaurant.RESTAURANT_KEY_NAME, Restaurant.RESTAURANT_KEY_ADDRESS}; // Informations à afficher
        int[] to = new int[] {R.id.name, R.id.address}; // Endroit où afficher ces informations
        adapter = new SimpleCursorAdapter(this, R.layout.list_item, null, columns, to, 0);
        ListView listview = (ListView) findViewById(R.id.listview);
        listview.setAdapter(adapter);
        getLoaderManager().initLoader(0, null, this); // Initialise le loader

        listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> listView, View view, int position, long id) {
                Cursor cursor = (Cursor) listView.getItemAtPosition(position); // Récupère le curseur
                String rowid = cursor.getString(cursor.getColumnIndexOrThrow(Restaurant.RESTAURANT_KEY_ROWID)); // Récupère l'ID
                String uri = NotarestoProvider.CONTENT_URI + "/" + rowid;
                Intent restaurant = new Intent(MainActivity.this, RestaurantActivity.class);
                restaurant.putExtra(EXTRA_URI, uri); // Envoie l'URI à RestaurantActivity
                startActivity(restaurant);
            }
        });
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(this, NotarestoProvider.CONTENT_URI, null, null, null, null);
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
