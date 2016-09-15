package com.xpf.me.whriter.editor;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
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
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.safety.Whitelist;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.realm.Realm;
import tk.zielony.naturaldateformat.AbsoluteDateFormat;
import tk.zielony.naturaldateformat.NaturalDateFormat;

/**
 * Created by pengfeixie on 16/5/23.
 */
public class EditorActivity extends AppCompatActivity implements Shareable {

    // TODO: 16/6/27 import text
    private static final String TAG = EditorActivity.class.getName();
    private static final String EXTRA_ID = "file_id";
    private static final int WRITE_EXTERNAL_STORAGE_REQUEST_CODE = 1;

    @BindView(R.id.fabtoolbar)
    FabToolbar fabToolbar;
    @BindView(R.id.fab)
    FloatingActionButton fab;
    @BindView(R.id.edit_lines)
    ObservableWebView webView;
    @BindView(R.id.drawer_layout)
    DrawerLayout drawerLayout;
    @BindView(R.id.editor_title)
    EditText titleEdit;
    @BindView(R.id.button_share)
    ImageButton shareBtn;
    @BindView(R.id.button_delete)
    ImageButton deleteBtn;
    @BindView(R.id.button_info)
    ImageButton infoBtn;

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

    /**
     * Init drawer
     */
    private void setUpContent() {
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

    /**
     * Init editor WebView
     */
    private void setUpEditor() {
        titleEdit.setText(mFile.getTitle());
        titleEdit.requestFocus();
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

    /**
     * Remove all tags from html text except <br><p>
     *
     * @param bodyHtml HTML string
     * @return Clean string
     */
    public static String cleanPreserveLineBreaks(String bodyHtml) {
        // get pretty printed html with preserved br and p tags
        String prettyPrintedBodyFragment = Jsoup.clean(bodyHtml, ""
                , Whitelist.none().addTags("br", "p")
                , new Document.OutputSettings().prettyPrint(true));
        // get plain text with preserved line breaks by disabled prettyPrint
        return Jsoup.clean(prettyPrintedBodyFragment
                , ""
                , Whitelist.none()
                , new Document.OutputSettings().prettyPrint(false))
                + "\n\n"
                + AppData.getString(R.string.form_whriter);
    }

    public static String clean(String bodyHtml) {
        // get plain text with preserved line breaks by disabled prettyPrint
        String temp = Jsoup.clean(bodyHtml
                , ""
                , Whitelist.none()
                , new Document.OutputSettings().prettyPrint(false));
        return temp.replace("&nbsp;", " ");
    }

    private static float countWords(String str) {
        if (str == null || str.length() <= 0) {
            return 0;
        }
        float len = 0;
        char c;
        for (int i = str.length() - 1; i >= 0; i--) {
            c = str.charAt(i);
            if ((c >= '0' && c <= '9') || (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z')) {
                // 字母, 数字
                len += 0.5;
            } else {
                if (Character.isLetter(c)) { // 中文
                    len++;
                } else { // 符号或控制字符
                    len += 0.5;
                }
            }
        }
        return len;
    }

    /**
     * Share text through system share
     *
     * @param text
     */
    private void shareText(String text) {
        File destDir = new File(Environment.getExternalStorageDirectory()
                + File.separator
                + "Whriter"
                + File.separator
                + "Text");
        if (!destDir.exists()) {
            destDir.mkdirs();
        }
        try {
            FileOutputStream fos;
            fos = new FileOutputStream(Environment.getExternalStorageDirectory()
                    + File.separator
                    + "Whriter"
                    + File.separator
                    + "Text"
                    + File.separator
                    + new Date().toString() + ".txt");
            fos.write(text.getBytes());
            fos.close();
            Toast.makeText(this, AppData.getString(R.string.save_to)
                    + Environment.getExternalStorageDirectory()
                    + File.separator
                    + "Whriter"
                    + File.separator
                    + "Text", Toast.LENGTH_LONG).show();
        } catch (IOException e) {
            e.printStackTrace();
        }
        Intent shareIntent = new Intent();
        shareIntent.setAction(Intent.ACTION_SEND);
        shareIntent.putExtra(Intent.EXTRA_TEXT, text);
        shareIntent.setType("text/plain");

        startActivity(Intent.createChooser(shareIntent, AppData.getString(R.string.share_to)));
    }


    /**
     * Share image after asking for permissions
     * @param requestCode
     * @param grantResults
     */
    private void shareImageHavingPermission(int requestCode, int[] grantResults) {
        if (requestCode == WRITE_EXTERNAL_STORAGE_REQUEST_CODE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                shareImage();
                // Permission Granted
            } else {
                Toast.makeText(this, "Error", Toast.LENGTH_LONG).show();
                // Permission Denied
            }
        }
    }

    /**
     * Save a WebView content to a bitmap
     *
     * @param webView Editor WebView
     * @return Saved bitmap
     */
    public static Bitmap screenshot(WebView webView) {
        try {
            float scale = webView.getScale();
            Log.i(TAG, "screenshot: " + scale);
            int height = (int) (webView.getContentHeight() * scale + 0.5);
            Bitmap bitmap = Bitmap.createBitmap(webView.getWidth(), height, Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(bitmap);
            webView.draw(canvas);
            return bitmap;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Save bitmap in SD card in png format
     *
     * @return Path to the image
     */
    private String saveBitmap2Picture() {
        Bitmap b = screenshot(webView);
        String path = null;
        try {
            if (b != null) {
                File destDir = new File(Environment.getExternalStorageDirectory()
                        + File.separator
                        + "Whriter"
                        + File.separator
                        + "Image");
                if (!destDir.exists()) {
                    destDir.mkdirs();
                }
                path = Environment.getExternalStorageDirectory()
                        + File.separator
                        + "Whriter"
                        + File.separator
                        + "Image"
                        + File.separator
                        + new Date().toString() + ".png";
                b.compress(Bitmap.CompressFormat.JPEG, 95, new FileOutputStream(path));
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        return path;
    }

    /**
     * Return the json value of editor content of user's article
     *
     * @param params JSON format content
     * @return
     */
    private Map<String, String> getEditorContent(String params) {
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
        return map;
    }

    @OnClick(R.id.fab)
    void setFab() {
        fabToolbar.expandFab();
    }

    @OnClick(R.id.button_delete)
    void setDeleteBtn() {
        drawerLayout.closeDrawers();
        new MaterialDialog.Builder(this)
                .title(R.string.clear)
                .positiveText(android.R.string.ok)
                .positiveColor(AppData.getColor(android.R.color.holo_red_dark))
                .negativeColor(AppData.getColor(android.R.color.black))
                .negativeText(android.R.string.cancel)
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        RealmProvider.getInstance().getRealm().executeTransaction(new Realm.Transaction() {
                            @Override
                            public void execute(Realm realm) {
                                icarus.setContent("");
                            }
                        });
                    }
                })
                .show();
    }

    @OnClick(R.id.button_share)
    void setShareBtn() {
        drawerLayout.closeDrawers();
        new BottomSheet.Builder(EditorActivity.this)
                .title(R.string.share)
                .sheet(R.menu.menu_share)
                .listener(new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which) {
                            case R.id.share_text:
                                if (ContextCompat.checkSelfPermission(EditorActivity.this
                                        , Manifest.permission.WRITE_EXTERNAL_STORAGE)
                                        != PackageManager.PERMISSION_GRANTED) {
                                    ActivityCompat.requestPermissions(EditorActivity.this
                                            , new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}
                                            , WRITE_EXTERNAL_STORAGE_REQUEST_CODE);
                                } else {
                                    sharePlainText();
                                }
                                break;
                            case R.id.share_picture:
                                if (ContextCompat.checkSelfPermission(EditorActivity.this
                                        , Manifest.permission.WRITE_EXTERNAL_STORAGE)
                                        != PackageManager.PERMISSION_GRANTED) {
                                    ActivityCompat.requestPermissions(EditorActivity.this
                                            , new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}
                                            , WRITE_EXTERNAL_STORAGE_REQUEST_CODE);
                                } else {
                                    shareImage();
                                }
                                break;
                            case R.id.share_html:
                                if (ContextCompat.checkSelfPermission(EditorActivity.this
                                        , Manifest.permission.WRITE_EXTERNAL_STORAGE)
                                        != PackageManager.PERMISSION_GRANTED) {
                                    ActivityCompat.requestPermissions(EditorActivity.this
                                            , new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}
                                            , WRITE_EXTERNAL_STORAGE_REQUEST_CODE);
                                } else {
                                    shareHTMLText();
                                }
                                break;
                        }
                    }
                }).show();
    }

    @OnClick(R.id.button_info)
    void setInfoBtn() {
        // TODO: 16/6/27 count
        drawerLayout.closeDrawers();
        final String titleStr = mFile.getTitle();
        final long createDate = mFile.getCreateDate();
        final long modifyDate = mFile.getModifyDate();
        final View infoView = LayoutInflater.from(EditorActivity.this).inflate(R.layout.dialog_info, null);
        final TextView title = (TextView) infoView.findViewById(R.id.title_filed);
        final TextView count = (TextView) infoView.findViewById(R.id.count_field);
        final TextView modified = (TextView) infoView.findViewById(R.id.modify_field);
        final TextView create = (TextView) infoView.findViewById(R.id.create_field);
        icarus.getContent(new Callback() {
            @Override
            public void run(String params) {
                final float c = countWords(clean(getEditorContent(params).get("content")));
                title.setText(titleStr);
                count.setText(c + " ");
                AbsoluteDateFormat d = new AbsoluteDateFormat(EditorActivity.this
                        , NaturalDateFormat.DATE
                        | NaturalDateFormat.HOURS
                        | NaturalDateFormat.MINUTES
                        | NaturalDateFormat.SECONDS);
                modified.setText(d.format(modifyDate));
                create.setText(d.format(createDate));
                MaterialDialog dialog = new MaterialDialog.Builder(EditorActivity.this)
                        .title(R.string.detail)
                        .customView(infoView, true)
                        .build();
                dialog.show();
            }
        });

    }

    @Override
    public void sharePlainText() {
        icarus.getContent(new Callback() {
            @Override
            public void run(String params) {
                shareText(cleanPreserveLineBreaks(getEditorContent(params).get("content")));
            }
        });
    }

    @Override
    public void shareHTMLText() {
        icarus.getContent(new Callback() {
            @Override
            public void run(String params) {
                shareText(getEditorContent(params).get("content")
                        + "\n\n"
                        + AppData.getString(R.string.form_whriter));
            }
        });
    }

    @Override
    public void shareImage() {
        //save and get the path of image
        String imagePath = saveBitmap2Picture();

        if (imagePath != null) {
            Toast.makeText(this, AppData.getString(R.string.save_to)
                    + Environment.getExternalStorageDirectory()
                    + File.separator
                    + "Whriter"
                    + File.separator
                    + "Image", Toast.LENGTH_LONG).show();
            //use the path to get uri
            Uri imageUri = Uri.fromFile(new File(imagePath));
            //then share with uri
            Intent shareIntent = new Intent();
            shareIntent.setAction(Intent.ACTION_SEND);
            shareIntent.putExtra(Intent.EXTRA_STREAM, imageUri);
            shareIntent.setType("image/*");
            startActivity(Intent.createChooser(shareIntent, AppData.getString(R.string.share_to)));
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        shareImageHavingPermission(requestCode, grantResults);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
    }


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            WebView.enableSlowWholeDocumentDraw();
        }
        check();
        setContentView(R.layout.activity_editor);
        ButterKnife.bind(this);
        setUpToolbar((Toolbar) findViewById(R.id.toolbar));
        setUpContent();
        setUpEditor();
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
                mFile.getCurrentFolder().setModifyDate(System.currentTimeMillis());
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
                            mFile.setModifyDate(System.currentTimeMillis());
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
