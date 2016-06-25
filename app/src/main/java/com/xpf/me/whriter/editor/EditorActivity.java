package com.xpf.me.whriter.editor;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Typeface;
import android.os.Bundle;
import android.preference.PreferenceCategory;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.SharedPreferencesCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebSettings;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.bowyer.app.fabtoolbar.FabToolbar;
import com.cocosw.bottomsheet.BottomSheet;
import com.github.mr5.icarus.Callback;
import com.github.mr5.icarus.Icarus;
import com.github.mr5.icarus.TextViewToolbar;
import com.github.mr5.icarus.button.Button;
import com.github.mr5.icarus.button.FontScaleButton;
import com.github.mr5.icarus.button.TextViewButton;
import com.github.mr5.icarus.entity.Options;
import com.github.mr5.icarus.popover.FontScalePopoverImpl;
import com.github.mr5.icarus.popover.HtmlPopoverImpl;
import com.github.mr5.icarus.popover.ImagePopoverImpl;
import com.github.mr5.icarus.popover.LinkPopoverImpl;
import com.xpf.me.whriter.R;
import com.xpf.me.whriter.common.AppData;
import com.xpf.me.whriter.common.RealmProvider;
import com.xpf.me.whriter.model.WhriterFile;
import com.xpf.me.whriter.utils.HTMLUtil;
import com.xpf.me.whriter.widget.ObservableWebView;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import io.realm.Realm;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

/**
 * Created by pengfeixie on 16/5/23.
 */
public class EditorActivity extends AppCompatActivity {

    private static final String TAG = EditorActivity.class.getName();
    private static final String EXTRA_ID = "file_id";

    private FabToolbar fabToolbar;
    private FloatingActionButton fab;
    private ObservableWebView webView;
    private DrawerLayout drawerLayout;
    private EditText titleEdit;
    private ActionBarDrawerToggle toggle;

    private Icarus icarus;

    private String content = "";
    private WhriterFile mFile;

    public static void enterEditor(Context start, String id) {
        Intent intent = new Intent(start, EditorActivity.class);
        intent.putExtra(EXTRA_ID, id);
        start.startActivity(intent);
    }

    private void check() {
        if (getIntent().getStringExtra(EXTRA_ID) != null) {
            mFile = RealmProvider.getInstance().getRealm().where(WhriterFile.class)
                    .equalTo("id", getIntent().getStringExtra(EXTRA_ID)).findFirst();
            content = mFile.getContent();
        }
    }

    private void setUpToolbar(Toolbar toolbar) {
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
    }

    private void setUpContent() {
        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        fabToolbar = ((FabToolbar) findViewById(R.id.fabtoolbar));
        fab = ((FloatingActionButton) findViewById(R.id.fab));
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fabToolbar.expandFab();
            }
        });
        fabToolbar.setFab(fab);

        DisplayMetrics metric = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metric);
        final int widthScreen = metric.widthPixels;     // 屏幕宽度（像素）

        toggle = new ActionBarDrawerToggle(this, drawerLayout, R.string.hello_world, R.string.hello_world);
        toggle.setDrawerIndicatorEnabled(true);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();
        drawerLayout.addDrawerListener(new DrawerLayout.DrawerListener() {
            @Override
            public void onDrawerSlide(View drawerView, float slideOffset) {
                int width = webView.getWidth() - widthScreen * 3 / 4;
                webView.setTranslationX(slideOffset * width);
            }

            @Override
            public void onDrawerOpened(View drawerView) {

            }

            @Override
            public void onDrawerClosed(View drawerView) {

            }

            @Override
            public void onDrawerStateChanged(int newState) {

            }
        });

    }

    private void setUpEditor() {
        webView = (ObservableWebView) findViewById(R.id.edit_lines);
        titleEdit = ((EditText) findViewById(R.id.editor_title));
        titleEdit.setText(mFile.getTitle());
        webView.setOnScrollChangedCallback(new ObservableWebView.OnScrollChangedCallback() {
            @Override
            public void onScroll(int dx, int dy) {
                if (dy > 0) {
                    //up
                    fab.hide();
                } else {
                    if (!fabToolbar.isFabExpanded()) {
                        fab.show();
                    }
                }
            }
        });

        WebSettings settings = webView.getSettings();
        settings.setSupportZoom(false);
        TextViewToolbar toolbar = new TextViewToolbar();
        Options options = new Options();
        options.setPlaceholder(AppData.getString(R.string.place_holder_text));
        //  img: ['src', 'alt', 'width', 'height', 'data-non-image']
        // a: ['href', 'target']
        options.addAllowedAttributes("img", Arrays.asList("data-type", "data-id", "class", "src", "alt", "width", "height", "data-non-image"));
        options.addAllowedAttributes("iframe", Arrays.asList("data-type", "data-id", "class", "src", "width", "height"));
        options.addAllowedAttributes("a", Arrays.asList("data-type", "data-id", "class", "href", "target", "title"));

        icarus = new Icarus(toolbar, options, webView);
        prepareToolbar(toolbar, icarus);
        icarus.render();
        icarus.setContent(content);

    }

    private void setUpDrawerButtons() {
        ImageButton deleteBtn = (ImageButton) findViewById(R.id.button_delete);
        deleteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                icarus.setContent("");
            }
        });

        ImageButton shareBtn = (ImageButton) findViewById(R.id.button_share);
        shareBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                drawerLayout.closeDrawers();
                new BottomSheet.Builder(EditorActivity.this)
                        .title(R.string.share)
                        .sheet(R.menu.menu_share)
                        .listener(new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                switch (which) {

                                }
                            }
                        }).show();
            }
        });
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
    }


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        check();
        setContentView(R.layout.activity_editor);
        setUpToolbar((Toolbar) findViewById(R.id.toolbar));
        setUpContent();
        setUpEditor();
        setUpDrawerButtons();
    }


    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        toggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        toggle.onConfigurationChanged(newConfig);
    }

    private com.github.mr5.icarus.Toolbar prepareToolbar(TextViewToolbar toolbar, Icarus icarus) {
        Typeface iconfont = Typeface.createFromAsset(getAssets(), "fonts/Simditor.ttf");
        HashMap<String, Integer> generalButtons = new HashMap<>();
        generalButtons.put(Button.NAME_BOLD, R.id.button_bold);
        generalButtons.put(Button.NAME_OL, R.id.button_list_ol);
        generalButtons.put(Button.NAME_BLOCKQUOTE, R.id.button_blockquote);
        generalButtons.put(Button.NAME_HR, R.id.button_hr);
        generalButtons.put(Button.NAME_UL, R.id.button_list_ul);
        generalButtons.put(Button.NAME_ALIGN_LEFT, R.id.button_align_left);
        generalButtons.put(Button.NAME_ALIGN_CENTER, R.id.button_align_center);
        generalButtons.put(Button.NAME_ALIGN_RIGHT, R.id.button_align_right);
        generalButtons.put(Button.NAME_ITALIC, R.id.button_italic);
        generalButtons.put(Button.NAME_INDENT, R.id.button_indent);
        generalButtons.put(Button.NAME_OUTDENT, R.id.button_outdent);
        generalButtons.put(Button.NAME_CODE, R.id.button_math);
        generalButtons.put(Button.NAME_UNDERLINE, R.id.button_underline);
        generalButtons.put(Button.NAME_STRIKETHROUGH, R.id.button_strike);

        for (String name : generalButtons.keySet()) {
            TextView textView = (TextView) findViewById(generalButtons.get(name));
            if (textView == null) {
                continue;
            }
            textView.setTypeface(iconfont);
            TextViewButton button = new TextViewButton(textView, icarus);
            button.setName(name);
            toolbar.addButton(button);
        }
        TextView linkButtonTextView = (TextView) findViewById(R.id.button_link);
        linkButtonTextView.setTypeface(iconfont);
        TextViewButton linkButton = new TextViewButton(linkButtonTextView, icarus);
        linkButton.setName(Button.NAME_LINK);
        linkButton.setPopover(new LinkPopoverImpl(linkButtonTextView, icarus));
        toolbar.addButton(linkButton);

        TextView imageButtonTextView = (TextView) findViewById(R.id.button_image);
        imageButtonTextView.setTypeface(iconfont);
        TextViewButton imageButton = new TextViewButton(imageButtonTextView, icarus);
        imageButton.setName(Button.NAME_IMAGE);
        imageButton.setPopover(new ImagePopoverImpl(imageButtonTextView, icarus));
        toolbar.addButton(imageButton);

        TextView htmlButtonTextView = (TextView) findViewById(R.id.button_html5);
        htmlButtonTextView.setTypeface(iconfont);
        TextViewButton htmlButton = new TextViewButton(htmlButtonTextView, icarus);
        htmlButton.setName(Button.NAME_HTML);
        htmlButton.setPopover(new HtmlPopoverImpl(htmlButtonTextView, icarus));
        toolbar.addButton(htmlButton);

        TextView fontScaleTextView = (TextView) findViewById(R.id.button_scale);
        fontScaleTextView.setTypeface(iconfont);
        TextViewButton fontScaleButton = new FontScaleButton(fontScaleTextView, icarus);
        fontScaleButton.setPopover(new FontScalePopoverImpl(fontScaleTextView, icarus));
        toolbar.addButton(fontScaleButton);
        return toolbar;
    }

//    @Override
//    protected void attachBaseContext(Context newBase) {
//        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
//    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                drawerLayout.openDrawer(GravityCompat.START);  // OPEN DRAWER
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
            return;
        }
        if (fabToolbar.isFabExpanded()) {
            fabToolbar.slideOutFab();
        } else {
            final String id = mFile.getId();
            if (mFile.getCurrentFolder() != null) {
                RealmProvider.getInstance().getRealm().beginTransaction();
                mFile.getCurrentFolder().setCreateDate(System.currentTimeMillis());
                RealmProvider.getInstance().getRealm().commitTransaction();
            }
            icarus.getContent(new Callback() {
                @Override
                public void run(final String params) {
                    //convert json to object
                    final Map<String, String> map = new HashMap<>();
                    try {
                        JSONObject jsonObj = new JSONObject(params);
                        String key;
                        String value;
                        Iterator<String> i = jsonObj.keys();
                        while (i.hasNext()) {
                            key = i.next();
                            value = jsonObj.getString(key);
                            map.put(key, value);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    mFile = Realm.getInstance(RealmProvider.getConfig())
                            .where(WhriterFile.class)
                            .equalTo("id", id)
                            .findFirst();
                    Realm.getInstance(RealmProvider.getConfig()).executeTransaction(new Realm.Transaction() {
                        @Override
                        public void execute(Realm realm) {
                            if (!TextUtils.isEmpty(titleEdit.getText().toString())) {
                                mFile.setTitle(titleEdit.getText().toString());
                            }
                            mFile.setContent(map.get("content"));
                            String preview = HTMLUtil.removeTag(map.get("content"));
                            mFile.setPreview(preview.length() > 30 ? preview.substring(0, 29) : preview);
                            mFile.setCreateDate(System.currentTimeMillis());
                            realm.copyToRealmOrUpdate(mFile);
                        }
                    });

                }
            });
            Toast.makeText(
                    EditorActivity.this,
                    AppData.getString(R.string.saved),
                    Toast.LENGTH_LONG).
                    show();
            finish();
        }
    }
}
