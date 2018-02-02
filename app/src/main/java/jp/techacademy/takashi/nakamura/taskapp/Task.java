package jp.techacademy.takashi.nakamura.taskapp;

import java.io.Serializable;
import java.util.Date;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class Task extends RealmObject implements Serializable {
    private String title;
    private String contents;
    private String category;
    // クラス Date は年、月、日、時、分、秒、ミリ秒の値をあつかう
    // https://docs.oracle.com/javase/jp/6/api/java/util/Date.html
    private Date date;

    // idをプライマリーキーとして設定
    @PrimaryKey
    private int id;

    // タイトルの設定
    public String getTitle() {
        return title;
    }

    // タイトルの取得
    public void setTitle(String title) {
        this.title = title;
    }

    // コンテンツの設定
    public String getContents() {
        return contents;
    }

    // コンテンツの取得
    public void setContents(String contents) {
        this.contents = contents;
    }

    // カテゴリーの設定
    public String getCategory() {
        return category;
    }

    // カテゴリーの取得
    public void setCategory(String category) {
        this.category = category;
    }

    // 日時の設定
    public Date getDate() {
        return date;
    }

    // 日時の取得
    public void setDate(Date date) {
        this.date = date;
    }

    // IDの設定
    public int getId() {
        return id;
    }

    // IDの取得
    public void setId(int id) {
        this.id = id;
    }

}
