package com.xpf.me.whriter.common;

import io.realm.Realm;
import io.realm.RealmConfiguration;

/**
 * Created by pengfeixie on 16/6/3.
 */
public class RealmProvider {

    private ThreadLocal<Realm> realmThreadLocal = new ThreadLocal<>();
    private static Realm realm;
    private static RealmProvider realmProvider = new RealmProvider();

    private RealmProvider() {
        RealmConfiguration configuration = new RealmConfiguration.Builder(AppData.getContext())
                .name(RealmConfig.REALM_NAME)
                .schemaVersion(RealmConfig.REALM_VERSION)
                .build();
        Realm.setDefaultConfiguration(configuration);
        realm = Realm.getDefaultInstance();
        realmThreadLocal.set(realm);
    }

    public static RealmConfiguration getConfig() {
        return new RealmConfiguration.Builder(AppData.getContext())
                .name(RealmConfig.REALM_NAME)
                .schemaVersion(RealmConfig.REALM_VERSION)
                .build();
    }

    public static RealmProvider getInstance() {
        return realmProvider;
    }

    public Realm getRealm() {
        if (realmThreadLocal.get() == null) {
            new RealmProvider();
        }
        return realmThreadLocal.get();
    }
}
