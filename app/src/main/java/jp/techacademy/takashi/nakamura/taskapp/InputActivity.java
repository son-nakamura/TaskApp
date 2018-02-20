package jp.techacademy.takashi.nakamura.taskapp;

import android.app.AlarmManager;
import android.app.DatePickerDialog;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TimePicker;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import io.realm.Realm;
import io.realm.RealmResults;
import io.realm.Sort;

public class InputActivity extends AppCompatActivity {

    public final static String EXTRA_CATEGORY = "jp.techacademy.takashi.nakamura.taskapp.CATEGORY";

    private Realm mRealm;
    private int mYear, mMonth, mDay, mHour, mMinute;
    private int mCategoryId;
    private Button mDateButton, mTimeButton, mNewCategoryButton;
    private EditText mTitleEdit, mContentEdit;
    private Spinner mCategorySpinner;
    private ArrayAdapter<String> mSpinnerAdapter;
    private Task mTask;

    // spinner によるカテゴリの選択
    private AdapterView.OnItemSelectedListener mItemSelectedListener = new AdapterView.OnItemSelectedListener() {
        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            // 選択されたカテゴリのIDを取得
            Spinner spinner = (Spinner) parent;
            String category = (String) spinner.getSelectedItem();
            Category categoryResult = mRealm.where(Category.class).equalTo("category", category).findFirst();
            mCategoryId = categoryResult.getId();
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {
            // 何も選択されなかったとき何もしない
        }
    };

    // カテゴリ新規作成ボタンの処理
    private View.OnClickListener mOnNewCategoryClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            // インテントを作成してカテゴリ作成画面に遷移
            Intent intent = new Intent(InputActivity.this, CategoryActivity.class);
            startActivity(intent);
        }
    };


    // 日付設定ボタンが押されたときの処理
    private View.OnClickListener mOnDateClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            DatePickerDialog datePickerDialog = new DatePickerDialog(InputActivity.this,
                    new DatePickerDialog.OnDateSetListener() {
                        @Override
                        public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                            mYear = year;
                            mMonth = monthOfYear;
                            mDay = dayOfMonth;
                            String dateString = mYear + "/" + String.format("%02d", mMonth + 1) + "/" + String.format("%02d", mDay);
                            mDateButton.setText(dateString);
                        }
                    }, mYear, mMonth, mDay);
            datePickerDialog.show();
        }
    };


    // 時間設定ボタンが押されたときの処理
    private View.OnClickListener mOnTimeClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            TimePickerDialog timePickerDialog = new TimePickerDialog(InputActivity.this,
                    new TimePickerDialog.OnTimeSetListener() {
                        @Override
                        public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                            mHour = hourOfDay;
                            mMinute = minute;
                            String timeString = String.format("%02d", mHour) + ":" + String.format("%02d", mMinute);
                            mTimeButton.setText(timeString);
                        }
                    }, mHour, mMinute, false);
            timePickerDialog.show();
        }
    };


    // 決定ボタンが押されたときの処理
    private View.OnClickListener mOnDoneClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            addTask();
            finish();
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_input);

        // 日付設定ボタンの設定
        mDateButton = (Button) findViewById(R.id.date_button);
        mDateButton.setOnClickListener(mOnDateClickListener);

        // 時刻設定ボタンの設定
        mTimeButton = (Button) findViewById(R.id.times_button);
        mTimeButton.setOnClickListener(mOnTimeClickListener);

        // 決定ボタンの設定
        findViewById(R.id.done_button).setOnClickListener(mOnDoneClickListener);

        // 新規カテゴリボタンの設定
        mNewCategoryButton = (Button) findViewById(R.id.new_category_button);
        mNewCategoryButton.setOnClickListener(mOnNewCategoryClickListener);

        // EditTextの設定
        mTitleEdit = (EditText) findViewById(R.id.title_edit_text);
        mContentEdit = (EditText) findViewById(R.id.content_edit_text);

        // CategorySpinnerの設定
        mCategorySpinner = (Spinner) findViewById(R.id.category_spinner);
        mCategorySpinner.setOnItemSelectedListener(mItemSelectedListener);

        // MainActivity.EXTRA_TASKからTaskのidを取得して、idからTaskのインスタンスを取得する
        // taskId = -1 のとき mTask = null となり、新規作成となる
        Intent intent = getIntent();
        int taskId = intent.getIntExtra(MainActivity.EXTRA_TASK, -1);
        mRealm = Realm.getDefaultInstance();
        mTask = mRealm.where(Task.class).equalTo("id", taskId).findFirst();

        if (mTask == null) {
            // 新規作成の場合、現在日時を設定
            Calendar calender = Calendar.getInstance();
            mYear = calender.get(Calendar.YEAR);
            mMonth = calender.get(Calendar.MONTH);
            mDay = calender.get(Calendar.DAY_OF_MONTH);
            mHour = calender.get(Calendar.HOUR_OF_DAY);
            mMinute = calender.get(Calendar.MINUTE);
        } else {
            // 更新の場合、すでに登録されている値をEditTextと日時ボタンに表示
            mTitleEdit.setText(mTask.getTitle());
            mContentEdit.setText(mTask.getContents());

            Calendar calender = Calendar.getInstance();
            calender.setTime(mTask.getDate());
            mYear = calender.get(Calendar.YEAR);
            mMonth = calender.get(Calendar.MONTH);
            mDay = calender.get(Calendar.DAY_OF_MONTH);
            mHour = calender.get(Calendar.HOUR_OF_DAY);
            mMinute = calender.get(Calendar.MINUTE);

            String dateString = mYear + "/" + String.format("%02d", (mMonth + 1)) + "/" + String.format("%02d", mDay);
            String timeString = String.format("%02d", mHour) + String.format("%02d", mMinute);
            mDateButton.setText(dateString);
            mTimeButton.setText(timeString);
        }
    } // end of onCreate


    // 他のActivityから遷移してきたとき、onCreate()が実行されていないので、
    // onResume()でカテゴリをmSpinnerAdapterに登録する
    @Override
    protected void onResume() {
        super.onResume();

        if (mSpinnerAdapter == null) {
            // SpinnerAdapterがnullの場合、新規作成
            mSpinnerAdapter = new ArrayAdapter<String>(InputActivity.this, android.R.layout.simple_spinner_item);
            mSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        } else {
            // nullでない場合、SpinnerAdapterをクリア
            mSpinnerAdapter.clear();
        }
        // Realmからすべてのカテゴリを取得
        RealmResults<Category> results = mRealm.where(Category.class).findAllSorted("category", Sort.ASCENDING);
        // すべてのカテゴリをSpinnerAdapterに登録しなおす
        for (Category category: results) {
            mSpinnerAdapter.add(category.getCategory());
        }
        // SpinnerAdapterをCategorySpinnerにセット
        mCategorySpinner.setAdapter(mSpinnerAdapter);
    }


    // onDestroy()でRealmをクローズ
    @Override
    protected void onDestroy() {
        super.onDestroy();
        mRealm.close();
    }


    // Task を追加するメソッド
    private void addTask() {
        mRealm.beginTransaction();

        if (mTask == null) {
            // タスクが新規作成の場合、新しいtaskIdを設定
            mTask = new Task();
            RealmResults<Task> taskRealmResults = mRealm.where(Task.class).findAll();
            int identifier;
            if (taskRealmResults.max("id") != null) {
                // 追加登録の場合
                identifier = taskRealmResults.max("id").intValue() + 1;
            } else {
                // 全く新規の登録の場合
                identifier = 0;
            }
            mTask.setId(identifier);
        }

        // タイトルを登録
        String title = mTitleEdit.getText().toString();
        if (title.equals("")) {
            // タイトルが入力されていない場合、Taskを追加しない
            return;
        }
        mTask.setTitle(title);

        // コンテントを登録
        String content = mContentEdit.getText().toString();
        mTask.setContents(content);

        // カテゴリIDを登録
        mTask.setCategoryId(mCategoryId);

        // 日時を登録
        GregorianCalendar calendar = new GregorianCalendar(mYear, mMonth, mDay, mHour, mMinute);
        Date date = calendar.getTime();
        mTask.setDate(date);

        // Realmに書き込み/更新
        mRealm.copyToRealmOrUpdate(mTask);
        mRealm.commitTransaction();

        // PendingIntentを作成してアラームをセット
        Intent resultIntent = new Intent(getApplicationContext(), TaskAlarmReceiver.class);
        resultIntent.putExtra(MainActivity.EXTRA_TASK, mTask.getId());
        PendingIntent resultPendingIntent = PendingIntent.getBroadcast(
                this,
                mTask.getId(),
                resultIntent,
                PendingIntent.FLAG_UPDATE_CURRENT
        );
        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        alarmManager.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), resultPendingIntent);
    } // End of addTask()

}
