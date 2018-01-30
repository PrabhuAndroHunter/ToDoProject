package com.pub.todo.activity;

import android.app.Dialog;
import android.content.ContentValues;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.pub.todo.R;
import com.pub.todo.adapter.TaskViewAdapter;
import com.pub.todo.database.DBHelper;
import com.pub.todo.utils.CommonUtilities;
import com.pub.todo.utils.Constants;
import com.pub.todo.utils.RecyclerViewItemDecorator;

public class HomeScreenActivity extends AppCompatActivity {
    private final String TAG = HomeScreenActivity.class.toString();
    private EditText mtitleEdt, mdescriptionEdt;
    private Button mSaveBtn, mCancelBtn;
    private DatePicker mDatePicker;
    private TextView mStatusTv;
    private RecyclerView mRecyclerView;
    private DBHelper dbHelper;
    private TaskViewAdapter taskViewAdapter;
    private String title, description, date;
    private long exitTime;
    private int year;
    private int month;
    private int day;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_screen);
        dbHelper = CommonUtilities.getDBObject(this);
        mRecyclerView = (RecyclerView) findViewById(R.id.recycler_View_employee);
        mStatusTv = (TextView) findViewById(R.id.textview_no_result);
        dbHelper = CommonUtilities.getDBObject(this); // get database reference
        getSupportActionBar().setTitle("Task List"); //  set tittle
        taskViewAdapter = new TaskViewAdapter(this);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mRecyclerView.addItemDecoration(new RecyclerViewItemDecorator(this, 0));
        mRecyclerView.setAdapter(taskViewAdapter); // set adapter
    }

    @Override
    protected void onStart() {
        super.onStart();
        int count = dbHelper.getFullCount(Constants.TASK_TABLE, null);
        if (count == 0) { // check for record count
            mStatusTv.setVisibility(View.VISIBLE); // if 0 then make 'no records' text as visible
        } else {
            mStatusTv.setVisibility(View.INVISIBLE);
        }
        taskViewAdapter.refreshUI();
    }

    @Override
    public void onBackPressed() {
        if ((System.currentTimeMillis() - exitTime) > 2000) {
            Toast.makeText(getApplicationContext(), "Press back again to exit",
                    Toast.LENGTH_SHORT).show();
            exitTime = System.currentTimeMillis();
        } else {
            finish();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.add_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.add_task:
                // Create custom dialog object
                final Dialog dialog = new Dialog(HomeScreenActivity.this);
                // Include dialog.xml file
                dialog.setContentView(R.layout.add_dialog);
                // Set dialog title
                dialog.setTitle("New Task");

                mtitleEdt = (EditText) dialog.findViewById(R.id.edit_text_title);
                mdescriptionEdt = (EditText) dialog.findViewById(R.id.edit_text_description);
                mSaveBtn = (Button) dialog.findViewById(R.id.button_add);
                mCancelBtn = (Button) dialog.findViewById(R.id.button_cancle);
                mDatePicker = (DatePicker) dialog.findViewById(R.id.datePicker);
                long now = System.currentTimeMillis() - 1000;
                mDatePicker.setMinDate(now);
                dialog.show();
                // set onclick listener to save button
                mSaveBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // get all values
                        title = mtitleEdt.getText().toString();
                        description = mdescriptionEdt.getText().toString();
                        date = mDatePicker.getDayOfMonth() + "-" + mDatePicker.getMonth() + 1 + "-" + mDatePicker.getYear();
                        Log.d(TAG, "onClick: value" + title + description + date);
                        if (validateAndSave()) {            // if all values are current and save in DB
                            mStatusTv.setVisibility(View.INVISIBLE);
                            taskViewAdapter.refreshUI();
                            dialog.dismiss();
                        }

                    }
                });

                mCancelBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.dismiss();
                    }
                });
                break;

            case R.id.completed_task:
                Intent intent = new Intent(this, CompletedTaskActivity.class);
                startActivity(intent);
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                break;
        }
        return true;
    }

    // this method will check all fields are having values or not
    private boolean validateAndSave() {
        if (validate(title, mtitleEdt, "Please enter Title"))
            if (validate(description, mdescriptionEdt, "Please Enter Description")) {
                final ContentValues contentValues = new ContentValues();
                contentValues.put(Constants.TASK_TITLE, title);
                contentValues.put(Constants.TASK_DESCRIPTION, description);
                contentValues.put(Constants.TASK_DATE, date);
                contentValues.put(Constants.TASK_STATUS, 0);
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        long result = dbHelper.insertContentVals(Constants.TASK_TABLE, contentValues);
                        Log.d(TAG, "run: " + result);
                    }
                }).start();
                Toast.makeText(getApplicationContext(), "Task Added", Toast.LENGTH_SHORT).show();
                return true;
            }
        return false;
    }

    // this method will check whether the fields are having values or not, if not return false
    private boolean validate(String value, EditText view, String error) {
        if (value.equalsIgnoreCase("")) {
            view.setError(error);
            return false;
        } else {
            return true;
        }
    }
}
