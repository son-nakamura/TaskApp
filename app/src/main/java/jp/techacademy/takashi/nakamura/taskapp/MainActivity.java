package jp.techacademy.takashi.nakamura.taskapp;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Spinner;

import io.realm.Realm;
import io.realm.RealmChangeListener;
import io.realm.RealmResults;
import io.realm.Sort;

public class MainActivity extends AppCompatActivity {
    public final static String EXTRA_TASK = "jp.techacademy.takashi.nakamura.taskapp.TASK";

    private Realm mRealm;
//    private RealmChangeListener mRealmChangeListener;
    private Spinner mCategorySpinner;
    private ArrayAdapter<String> mSpinnerAdapter;
    private Task mTask;
    private Category mCategory;
    private ListView mListView;
    private TaskAdapter mTaskAdapter;

    // Realm に ChangeListener を設定(Realmが変更された場合、全Taskを表示)
    private RealmChangeListener mRealmListener= new RealmChangeListener() {
        @Override
        public void onChange(Object o) {
            reloadAllListView();
        }
    };

    // Spinnerによるカテゴリの選択と選択されたタスクの表示
    private AdapterView.OnItemSelectedListener mItemSelectedListener = new AdapterView.OnItemSelectedListener() {
        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            Spinner spinner = (Spinner) parent;
            String category = (String) spinner.getSelectedItem();

            if (category.equals(Category.ZENKATEGORI)) {
                // 全カテゴリが選択された場合、すべてのタスクを抽出
                reloadAllListView();
            } else {
                // 個々のカテゴリが選択された場合、Realmからカテゴリが一致するTaskを抽出
                // 選択されたカテゴリのIDを取得
                mRealm = Realm.getDefaultInstance();
                Category categoryResult = mRealm.where(Category.class).equalTo("category", category).findFirst();
                int categoryId = categoryResult.getId();
                mRealm.close();
                // 取得したカテゴリIDに一致するTaskをすべて抽出
                mRealm = Realm.getDefaultInstance();
//                RealmResults<Task> sortedResults = mRealm.where(Task.class).findAllSorted("date", Sort.ASCENDING);
                RealmResults<Task> sortedResults = mRealm.where(Task.class).equalTo("categoryId", categoryId)
                        .findAllSorted("date", Sort.ASCENDING);
                // 抽出したTaskをTaskAdapterにセット
                mTaskAdapter.setTaskList(mRealm.copyFromRealm(sortedResults));
                mRealm.close();
                // TaskAdapterをListviewにセット
                mListView.setAdapter(mTaskAdapter);
                // 表示の更新
                mTaskAdapter.notifyDataSetChanged();
            }
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {
            // 何も選択されなかったときの処理
//            mCategory = null; TODO
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // FloatingActionButtonの生成
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, InputActivity.class);
                startActivity(intent);
            }
        });

        // Realmの設定
        mRealm = Realm.getDefaultInstance();
        mRealm.addChangeListener(mRealmListener);

        // カテゴリ検索のための Spinner と Adapterの設定
        mCategorySpinner = (Spinner) findViewById(R.id.category_select_spinner);
        mCategorySpinner.setOnItemSelectedListener(mItemSelectedListener);

        // ListViewの設定
        mTaskAdapter = new TaskAdapter(MainActivity.this);
        mListView = (ListView) findViewById(R.id.listView1);

        // ListViewをタップしたときの処理
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // 入力・編集する画面に遷移させる
                Task task = (Task) parent.getAdapter().getItem(position);

                Intent intent = new Intent(MainActivity.this, InputActivity.class);
                intent.putExtra(EXTRA_TASK, task.getId());

                startActivity(intent);
            }
        });

        // ListViewを長押ししたときの処理
        mListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {

                // タスクを削除する

                final Task task = (Task) parent.getAdapter().getItem(position);

                // ダイアログを表示する
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);

                builder.setTitle("削除");
                builder.setMessage(task.getTitle() + "を削除しますか");
                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        mRealm = Realm.getDefaultInstance();
                        RealmResults<Task> results = mRealm.where(Task.class).equalTo("id", task.getId()).findAll();
                        mRealm.beginTransaction();
                        results.deleteAllFromRealm();
                        mRealm.commitTransaction();
                        mRealm.close();


                        Intent resultIntent = new Intent(getApplicationContext(), TaskAlarmReceiver.class);
                        PendingIntent resultPendingIntent = PendingIntent.getBroadcast(
                                MainActivity.this,
                                task.getId(),
                                resultIntent,
                                PendingIntent.FLAG_UPDATE_CURRENT
                        );

                        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
                        alarmManager.cancel(resultPendingIntent);

                        // ListVewの再表示
                        reloadAllListView();
                    }
                });
                builder.setNegativeButton("CANCEL", null);

                AlertDialog dialog = builder.create();
                dialog.show();

                return true;
            }
        });
        // 全タスクの再表示
//        reloadAllListView();
    } // End of onCreate()

    // InputActivityから戻ったとき、onCreate()が実行されないので、
    // onResume()でカテゴリをmSpinnerに登録しなおし、全タスクを表示
    @Override
    protected void onResume() {
        super.onResume();

        if (mSpinnerAdapter == null) {
            // SpinnerAdapterがnullの場合、新規作成
            mSpinnerAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item);
            mSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        } else {
            // nullでない場合、SpinnerAdapterをクリア
            mSpinnerAdapter.clear();
        }
        // 「全カテゴリ」をSpinnerAdapterの先頭に登録
        mSpinnerAdapter.add(Category.ZENKATEGORI);
        // Realmからすべての登録済みカテゴリを取得
        mRealm = Realm.getDefaultInstance();
        RealmResults<Category> results = mRealm.where(Category.class).findAllSorted("category", Sort.ASCENDING);
        for (Category category: results) {
            mSpinnerAdapter.add(category.getCategory());
        }
        // SpinnerAdapterをCategorySpinnerにセット
        mCategorySpinner.setAdapter(mSpinnerAdapter);
        mRealm.close();
        // 全タスクを表示
        reloadAllListView();
    }

    // MainActivityが破棄されるときにRealmをクローズする
    @Override
    protected void onDestroy() {
        super.onDestroy();
        mRealm.close();
    }

    // Realmデータベースから全てのTaskを取得し、ListView用のアダプタに渡すメソッド
    private void reloadAllListView() {
        // Realmデータベースから、「全てのデータを取得して新し日時順に並べた」結果を取得
        mRealm = Realm.getDefaultInstance();
        RealmResults<Task> taskRealmResults = mRealm.where(Task.class).findAllSorted("date", Sort.DESCENDING);
        // 上記の結果を、TaskList としてセットする
        if (taskRealmResults.size() > 0) {
            mTaskAdapter.setTaskList(mRealm.copyFromRealm(taskRealmResults));
            // TaskのListView用のアダプタに渡す
            mListView.setAdapter(mTaskAdapter);
        }
        mRealm.close();
        // 表示を更新するために、アダプターにデータが変更されたことを知らせる
        mTaskAdapter.notifyDataSetChanged();
    }

}
