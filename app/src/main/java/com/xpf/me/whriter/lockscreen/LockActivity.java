package com.xpf.me.whriter.lockscreen;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

import com.afollestad.digitus.Digitus;
import com.afollestad.digitus.DigitusCallback;
import com.afollestad.digitus.DigitusErrorType;
import com.xpf.me.whriter.R;
import com.xpf.me.whriter.widget.lockscreen.FingerprintView;

/**
 * Created by pengfeixie on 16/6/28.
 */
public class LockActivity extends AppCompatActivity implements DigitusCallback {

    private FingerprintView fingerprintView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lock);
        fingerprintView = (FingerprintView) findViewById(R.id.view_fingerprint);

    }

    @Override
    protected void onResume() {
        super.onResume();
        Digitus.init(this, getString(R.string.app_name), 69, this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        Digitus.deinit();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        Digitus.get().handleResult(requestCode, permissions, grantResults);
    }

    @Override
    public void onDigitusReady(Digitus digitus) {
        digitus.startListening();
    }

    @Override
    public void onDigitusListening(boolean newFingerprint) {
        fingerprintView.setStatusText("请使用指纹解锁");
    }

    @Override
    public void onDigitusAuthenticated(Digitus digitus) {
        finish();
    }

    @Override
    public void onDigitusError(Digitus digitus, DigitusErrorType type, Exception e) {
        switch (type) {
            case FINGERPRINT_NOT_RECOGNIZED:
                fingerprintView.setStatusText(getString(R.string.status_error, e.getMessage()));
                break;
            case FINGERPRINTS_UNSUPPORTED:
                fingerprintView.setStatusText(getString(R.string.status_error, e.getMessage()));
                break;
            case HELP_ERROR:
                fingerprintView.setStatusText(getString(R.string.status_error, e.getMessage()));
                break;
            case PERMISSION_DENIED:
                fingerprintView.setStatusText(getString(R.string.status_error, e.getMessage()));
                break;
            case REGISTRATION_NEEDED:
                fingerprintView.setStatusText(getString(R.string.status_error, e.getMessage()));

                break;
            case UNRECOVERABLE_ERROR:
                fingerprintView.setStatusText(getString(R.string.status_error, e.getMessage()));
                break;
        }
    }
}
