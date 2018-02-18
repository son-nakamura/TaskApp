package jp.techacademy.takashi.nakamura.taskapp;

import android.app.Application;

import io.realm.Realm;

// アプリケーション名が TaskApp であり、
// このクラスの onCreate() で Realm を初期化する。
// 呼び出されるためには、AndroidManifest.xml の application に、
// android:name="TaskApp" を追加する。

public class TaskApp extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        Realm.init(this);
    }
}
