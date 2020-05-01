package fr.florianlallier.notaresto;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class WebsiteRestaurantActivity extends AppCompatActivity {

    private String website;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_website_restaurant);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        toolbar.setNavigationIcon(R.drawable.ic_menu_back);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed(); // Retourne à l'activité précédente
            }
        });

        website = getIntent().getStringExtra(RestaurantActivity.EXTRA_WEBSITE); // Récupère l'adresse du site web

        WebView webView = (WebView) findViewById(R.id.webview);
        webView.setWebViewClient(new WebViewClient());
        webView.loadUrl(website);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_website_restaurant, menu); // Ajoute le menu

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_navigator:
                Intent navigator = new Intent(Intent.ACTION_VIEW);
                navigator.setData(Uri.parse(website));
                startActivity(navigator); // Ouvre le site web dans le navigateur web
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
