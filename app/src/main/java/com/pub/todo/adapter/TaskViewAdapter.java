package com.pub.todo.adapter;

import android.app.Dialog;
import android.content.ContentValues;
import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import com.pub.todo.activity.HomeScreenActivity;
import com.pub.todo.R;
import com.pub.todo.database.DBHelper;
import com.pub.todo.model.Task;
import com.pub.todo.utils.CommonUtilities;
import com.pub.todo.utils.Constants;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by prabhu on 25/1/18.
 */

public class TaskViewAdapter extends RecyclerView.Adapter <TaskViewAdapter.MyViewAdapter> {
    private final String TAG = TaskViewAdapter.class.toString();
    Context context;
    private EditText mtitleEdt, mdescriptionEdt;
    private Button mUpdateBtn, mCancelBtn;
    private DatePicker mDatePicker;
    private DBHelper dbHelper;
    private String title, description, date;
    private List <Task> taskList = new ArrayList <Task>();
    private int position;

    public TaskViewAdapter(Context context) {
        this.context = context;
        this.dbHelper = CommonUtilities.getDBObject(context);
    }

    @Override
    public MyViewAdapter onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_task, parent, false);
        return new MyViewAdapter(view);
    }

    @Override
    public void onBindViewHolder(final MyViewAdapter holder, final int position) {
        final Task clickedTask = taskList.get(position);
        holder.mNameTv.setText(taskList.get(position).getTitle());    // set name
        holder.mPhoneNumberTv.setText(taskList.get(position).getDescription());  // set phoneNumber
        holder.mDoBTv.setText(taskList.get(position).getDate());  // set doteofbirth
        if (clickedTask.isTaskCompleted()) {
            holder.mStatusBtn.setImageResource(R.drawable.complete);
        } else {
            holder.mStatusBtn.setImageResource(R.drawable.incomplete);
        }

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                showDialogue(clickedTask);
            }
        });

        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                Log.d(TAG, "onLongClick: ");
                setPosition(holder.getAdapterPosition());
                if (clickedTask.isTaskCompleted())
                    dbHelper.changeTaskStatus(clickedTask.getId(), false);
                else
                    dbHelper.changeTaskStatus(clickedTask.getId(), true);
                refreshUI();
                return true;
            }
        });

    }

    // return total record count
    @Override
    public int getItemCount() {
        Log.d(TAG, "getItemCount: " + taskList.size());
        return taskList.size();
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    class MyViewAdapter extends RecyclerView.ViewHolder {
        private TextView mNameTv, mPhoneNumberTv, mDoBTv;
        private ImageButton mStatusBtn;

        public MyViewAdapter(View itemView) {
            super(itemView);
            mNameTv = (TextView) itemView.findViewById(R.id.text_view_title);
            mPhoneNumberTv = (TextView) itemView.findViewById(R.id.text_view_description);
            mDoBTv = (TextView) itemView.findViewById(R.id.text_view_date);
            mStatusBtn = (ImageButton) itemView.findViewById(R.id.image_button_status);
        }
    }

    public void updateTaskList(List <Task> taskList) {
        this.taskList = taskList;
    }

    private void showDialogue(final Task task) {
        // Create custom dialog object
        final Dialog dialog = new Dialog(context);
        // Include dialog.xml file
        dialog.setContentView(R.layout.add_dialog);
        // Set dialog title
        dialog.setTitle("Update Task");

        mtitleEdt = (EditText) dialog.findViewById(R.id.edit_text_title);
        mdescriptionEdt = (EditText) dialog.findViewById(R.id.edit_text_description);
        mUpdateBtn = (Button) dialog.findViewById(R.id.button_add);
        mUpdateBtn.setText("Update");
        mCancelBtn = (Button) dialog.findViewById(R.id.button_cancle);
        mDatePicker = (DatePicker) dialog.findViewById(R.id.datePicker);
        long now = System.currentTimeMillis() - 1000;
        mDatePicker.setMinDate(now);
        mtitleEdt.setHint(task.getTitle());
        mdescriptionEdt.setHint(task.getDescription());
        final String[] newDate = task.getDate().split("-");   // need to update date in dialog
        dialog.show();
        // set onclick listener to save button
        mUpdateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // get all values
                title = mtitleEdt.getText().toString();
                description = mdescriptionEdt.getText().toString();
                date = mDatePicker.getDayOfMonth() + "-" + mDatePicker.getMonth() + 1 + "-" + mDatePicker.getYear();
                Log.d(TAG, "onClick: value" + title + description + date);
                if (validateAndSave(task)) {            // if all values are current and save in DB
//                            mStatusTv.setVisibility(View.INVISIBLE);
                    refreshUI();
                    dialog.dismiss();
                }
            }
        });
    }

    private boolean validateAndSave(final Task task) {
        if (validate(task)) {
            final ContentValues contentValues = new ContentValues();
            contentValues.put(Constants.TASK_TITLE, title);
            contentValues.put(Constants.TASK_DESCRIPTION, description);
            contentValues.put(Constants.TASK_DATE, date);
            new Thread(new Runnable() {
                @Override
                public void run() {
                    long result = dbHelper.updateRecords(task.getId(), contentValues);
                }
            }).start();
        }
        return true;
    }

    // this method will check whether the fields are having values or not if not return false
    private boolean validate(Task task) {
        if (title.equalsIgnoreCase("")) {
            title = task.getTitle();
        }
        if (description.equalsIgnoreCase("")) {
            description = task.getDescription();
        }
        return true;
    }

    public void refreshUI() {
        taskList.clear();
        taskList = dbHelper.getTaskList();
        ((HomeScreenActivity) context).runOnUiThread(new Runnable() {
            @Override
            public void run() {
                notifyDataSetChanged();
            }
        });
    }
}
