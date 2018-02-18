package jp.techacademy.takashi.nakamura.taskapp;

import java.io.Serializable;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class Category extends RealmObject implements Serializable {

    static final String ZENKATEGORI ="全カテゴリ";
    static final int ALL_CATEGORIES = 0;

    private String category;

    @PrimaryKey
    private int id;

    // カテゴリの取得
    public String getCategory() {
        return category;
    }

    // カテゴリの設定
    public void setCategory(String category) {
        this.category = category;
    }

    // id の取得
    public int getId() {
        return id;
    }

    // id の設定
    public void setId(int id) {
        this.id = id;
    }
}
