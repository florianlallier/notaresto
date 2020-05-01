package fr.florianlallier.notaresto;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

public class PreferencesActivity extends AppCompatActivity {

    public static final String PREFERENCE_CURRENCY = "fr.florianlallier.notaresto.preference.currency";

    private TextView preferenceCurrency = null;

    private String currency;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preferences);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        toolbar.setNavigationIcon(R.drawable.ic_menu_back);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed(); // Retourne à l'activité précédente
            }
        });

        LinearLayout layout = (LinearLayout) findViewById(R.id.preferences_layout);
        layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialogCurrency();
            }
        });

        preferenceCurrency = (TextView) findViewById(R.id.preference_currency);
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        preferenceCurrency.setText(preferences.getString(PREFERENCE_CURRENCY, getResources().getString(R.string.currency)));
    }

    /**
     * Ouvre une boîte de dialogue demandant une préférence pour la monnaie.
     */
    private void dialogCurrency() {
        final CharSequence[] currencies = {"Euro", "Dollar", "円", "Pound"};
        AlertDialog.Builder dialogCurrency = new AlertDialog.Builder(this);
        dialogCurrency.setTitle(R.string.currency_title);
        dialogCurrency.setSingleChoiceItems(currencies, -1, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                currency = currencies[which].toString();
            }
        });
        dialogCurrency.setNegativeButton(R.string.currency_no, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Ferme la boîte de dialogue
            }
        });
        dialogCurrency.setPositiveButton(R.string.currency_yes, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                preferenceCurrency.setText(currency);
                SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(PreferencesActivity.this);
                SharedPreferences.Editor editor = preferences.edit();
                editor.putString(PREFERENCE_CURRENCY, currency);
                editor.apply();
            }
        });
        dialogCurrency.show();
    }
}
