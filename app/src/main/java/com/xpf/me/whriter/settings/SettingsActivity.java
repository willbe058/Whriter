package com.xpf.me.whriter.settings;

import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.xpf.me.whriter.R;
import com.xpf.me.whriter.common.AppData;
import com.xpf.me.whriter.common.RealmProvider;
import com.xpf.me.whriter.model.WhriterFile;

import io.realm.Realm;
import io.realm.RealmResults;

/**
 * Created by pengfeixie on 16/6/25.
 */
public class SettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(true);

        if (savedInstanceState == null) {
            getFragmentManager().beginTransaction()
                    .add(R.id.container, new SettingsFragment())
                    .commit();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public static class SettingsFragment extends PreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.preference);

            findPreference("clear_key").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    new MaterialDialog.Builder(getActivity())
                            .title(R.string.clear_title)
                            .content(R.string.clear_content)
                            .positiveText(android.R.string.ok)
                            .positiveColor(AppData.getColor(android.R.color.holo_red_dark))
                            .negativeText(android.R.string.cancel)
                            .negativeColor(AppData.getColor(android.R.color.black))
                            .onPositive(new MaterialDialog.SingleButtonCallback() {
                                @Override
                                public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                    final RealmResults<WhriterFile> realmResults =
                                            RealmProvider.getInstance().getRealm().where(WhriterFile.class).findAll();
                                    RealmProvider.getInstance().getRealm().executeTransaction(new Realm.Transaction() {
                                        @Override
                                        public void execute(Realm realm) {
                                            realmResults.deleteAllFromRealm();
                                        }
                                    });
                                }
                            })
                            .show();

                    return false;
                }
            });
        }
    }

}
