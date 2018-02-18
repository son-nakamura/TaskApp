package jp.techacademy.takashi.nakamura.taskapp;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import io.realm.Realm;
import io.realm.RealmChangeListener;
import io.realm.RealmResults;
import io.realm.Sort;

public class CategoryActivity extends AppCompatActivity {

    private Category mCategory;
    private ListView mListView;
    private CategoryAdapter mCategoryAdapter;
    private Realm mRealm;
    private RealmChangeListener mRealmListener = new RealmChangeListener() {
        @Override
        public void onChange(Object o) {
            reloadListView();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_category);

        final EditText newCategoryEditText = (EditText) findViewById(R.id.new_category_edit_text);
        final Button categoryRegisterButton = (Button) findViewById(R.id.category_register_button);
        final Button categoryCloseButton = (Button) findViewById(R.id.category_close_button);

        // 戻るボタンが押されたときの処理
        categoryCloseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        // カテゴリ登録ボタンが押されたときの処理
        categoryRegisterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // EditTextからテキストを取得
                String newCategory = newCategoryEditText.getText().toString();
                // 取得したテキストが空でないときのみ処理
                if (!newCategory.equals("")) {
                    // 登録済みカテゴリとテキストが一致するものを探索
                    Realm realm = Realm.getDefaultInstance();
                    RealmResults<Category> results = realm.where(Category.class).equalTo("category", newCategory).findAll();
                    if (results.size() == 0) {
                        // 一致するものがないとき、カテゴリを登録
//                        mCategory.setCategory(newCategory);
                        addCategory(newCategory);
                    } else {
                        // 一致するものがあるとき、AlertDialog でユーザーに通知
                        AlertDialog.Builder builder = new AlertDialog.Builder(CategoryActivity.this);
                        builder.setTitle("登録できません");
                        builder.setMessage(newCategory + " はすでに登録されています");
                        // OKのみ表示し、押されたとき何もしない
                        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {}
                        });
                        AlertDialog alertDialog = builder.create();
                        builder.show();
                    }
                    // EditTextのテキストを削除
                    newCategoryEditText.setText("");
                }
            }
        });

        // Realmの設定
        mRealm = Realm.getDefaultInstance();
        mRealm.addChangeListener(mRealmListener);

        // ListViewの設定
        mCategoryAdapter = new CategoryAdapter(CategoryActivity.this);
        mListView = (ListView) findViewById(R.id.category_list_view);

        // CategoryActivity生成時に全カテゴリを表示
        reloadListView();

        // listViewを長押ししたとき、そのカテゴリを削除
        mListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                // カテゴリが使われているかチェック
                // 引数で渡された id のカテゴリを取得
                final Category category = (Category) parent.getAdapter().getItem(position);
                // Taskの中からカテゴリの一致するものを取得
                RealmResults<Task> TaskResults = mRealm.where(Task.class).equalTo("id", category.getId()).findAll();
                // 取得した結果をチェック
                if (TaskResults.size() == 0) {
                    // 一致するものがない場合、ダイアログを表示してユーザーに確認
                    AlertDialog.Builder builder = new AlertDialog.Builder(CategoryActivity.this);
                    builder.setTitle("削除");
                    builder.setMessage(category.getCategory() + "を削除しますか");
                    // OK が押されたときカテゴリを削除
                    builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            RealmResults<Category> categoryResults = mRealm.where(Category.class).equalTo("id", category.getId()).findAll();
                            mRealm.beginTransaction();
                            categoryResults.deleteAllFromRealm();
                            mRealm.commitTransaction();
                            reloadListView();
                        }
                    });
                    // CANCEL が押されたき
                    builder.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            // ダイアログを閉じるだけで何もしない
                        }
                    });
                    AlertDialog alertDialog = builder.create();
                    builder.show();

                } else if (TaskResults.size() >= 1) {
                    // 一致するものがある場合、まだそのカテゴリが使用されていることをダイアログで表示
                    AlertDialog.Builder builder = new AlertDialog.Builder(CategoryActivity.this);
                    builder.setTitle("削除できません");
                    builder.setMessage("このカテゴリを使用している予定があります");
                    builder.setPositiveButton("CLOSE", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            // ダイアログを閉じるだけで何もしない
                        }
                    });
                    AlertDialog alertDialog = builder.create();
                    builder.show();
                }

                return true;
            }
        }); // listViewを長押ししたときの処理の終わり

    } // onCreate()の終わり

    // CategoryActivityが消滅するときにRealmをクローズする
    @Override
    protected void onDestroy() {
        super.onDestroy();
        mRealm.close();
    }

    // RealmからすべてのCategoryを取得して表示するメソッド
    private void reloadListView() {
        // Realmからカテゴリをソートして取得
        RealmResults<Category> results  = mRealm.where(Category.class).findAllSorted("category", Sort.ASCENDING);
        // RealmResults を Adapter にセット
        mCategoryAdapter.setCategoryList(mRealm.copyFromRealm(results));
        // Adapter を ListView にセット
        mListView.setAdapter(mCategoryAdapter);
        // 変更を通知
        mCategoryAdapter.notifyDataSetChanged();
    }

    // Category を追加するメソッド
    private void addCategory(String strCategory) {
        Realm realm = Realm.getDefaultInstance();
        RealmResults<Category> results = realm.where(Category.class).findAll();
        int id;
        if (results.size() == Category.ALL_CATEGORIES) {
            // 何も登録されていない場合
            id = Category.ALL_CATEGORIES + 1;
        } else {
            // 何か登録されている場合
            id = results.max("id").intValue() + 1;
        }

        // 新しいカテゴリを生成
        Category newCategory = new Category();
        newCategory.setId(id);
        newCategory.setCategory(strCategory);

        // realトランザクション
        realm.beginTransaction();
        realm.copyToRealmOrUpdate(newCategory);
        realm.commitTransaction();
        realm.close();
    }

}
