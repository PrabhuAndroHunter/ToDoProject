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

import com.pub.todo.R;
import com.pub.todo.activity.CompletedTaskActivity;
import com.pub.todo.activity.HomeScreenActivity;
import com.pub.todo.database.DBHelper;
import com.pub.todo.model.Task;
import com.pub.todo.utils.CommonUtilities;
import com.pub.todo.utils.Constants;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by prabhu on 25/1/18.
 */

public class CompletedTaskViewAdapter extends RecyclerView.Adapter <CompletedTaskViewAdapter.MyViewAdapter> {
    private final String TAG = CompletedTaskViewAdapter.class.toString();
    Context context;
    private DBHelper dbHelper;
    private List <Task> taskList = new ArrayList <Task>();
    private int position;

    public CompletedTaskViewAdapter(Context context) {
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

        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                Log.d(TAG, "onLongClick: ");
                setPosition(holder.getAdapterPosition());
                if (clickedTask.isTaskCompleted())
                    dbHelper.deleteTask(clickedTask.getId());
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

    public void refreshUI() {
        taskList.clear();
        taskList = dbHelper.getCompletedTaskList();
        if (taskList.size() == 0){
            ((CompletedTaskActivity)context).showStatusText();
        }
        ((CompletedTaskActivity) context).runOnUiThread(new Runnable() {
            @Override
            public void run() {
                notifyDataSetChanged();
            }
        });
    }
}
