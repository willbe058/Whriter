package com.xpf.me.whriter.home;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.otto.Subscribe;
import com.xpf.me.whriter.R;
import com.xpf.me.whriter.common.AppData;
import com.xpf.me.whriter.event.BusProvider;
import com.xpf.me.whriter.settings.SettingsActivity;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getName();

    private long mExitTime;
    private TextView title;
    private FilesFragment fragment = new FilesFragment();
    private boolean backEnabled = true;

    private void setUpToolbar(Toolbar toolbar) {
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setHomeButtonEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);
    }

    private void setUpContent() {
        title = (TextView) findViewById(R.id.toolbar_title);
        title.setText(AppData.getString(R.string.app_name));
        getSupportFragmentManager().beginTransaction().replace(R.id.files_container, fragment).commit();
    }

    @Subscribe
    public void onTitleChanged(BusProvider.FolderChangeEvent event) {
        if (event.getCurrentFolder() == null) {
            title.setText(R.string.app_name);
            getSupportActionBar().setHomeButtonEnabled(false);
            getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        } else {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            title.setText(event.getCurrentFolder().getTitle());
            getSupportActionBar().setHomeButtonEnabled(true);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_arrow_back_black_24dp);
        }
    }

    @Subscribe
    public void onLongClickEvent(BusProvider.LongClickEvent event) {
        backEnabled = event.isDismiss();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        BusProvider.getInstance().register(this);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setUpToolbar(toolbar);
        setUpContent();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        BusProvider.getInstance().unregister(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            startActivity(new Intent(this, SettingsActivity.class));

        }
        if (id == android.R.id.home) {
            if (backEnabled) {
                return fragment.onBackPressed();
            }
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if (!fragment.onBackPressed()) {
            if ((System.currentTimeMillis() - mExitTime) > 2000) {
                Toast.makeText(this, AppData.getString(R.string.press_back), Toast.LENGTH_SHORT).show();
                mExitTime = System.currentTimeMillis();
            } else {
                finish();
            }
        }
    }
}
