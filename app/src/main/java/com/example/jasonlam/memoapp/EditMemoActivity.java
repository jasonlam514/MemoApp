package com.example.jasonlam.memoapp;

import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.ActionBarActivity;
import android.text.format.DateFormat;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TimePicker;
import android.widget.Toast;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;


public class EditMemoActivity extends ActionBarActivity {

    static EditText DateEdit;
    String id, date;
    private String[] arraySpinner;
    DBAdapter myDb;
    EditText title, content;
        CheckBox cb;
        Spinner s;

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.edit_layout);
            id =  getIntent().getExtras().getString("ID");
            openDB();

        //retrieve from database
        Cursor cursor = myDb.getRow(Integer.parseInt(id));
        title = (EditText) findViewById(R.id.titleEt);
        content = (EditText) findViewById(R.id.content);
        title.setText(cursor.getString(1));
        content.setText(cursor.getString(3));
        cb = (CheckBox)findViewById(R.id.checkBox);
        DateEdit = (EditText) findViewById(R.id.timeField);
        this.arraySpinner = new String[] {
                "Normal", "Important", "Urgent"
        };
        s = (Spinner) findViewById(R.id.spinnerEt);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item, arraySpinner);
        s.setAdapter(adapter);
        s.setSelection(Integer.parseInt(cursor.getString(4)));
        s.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> arg0, View arg1,
                                       int arg2, long arg3) {
                if(s.getSelectedItemPosition()==2){
                    cb.setVisibility(View.VISIBLE);
                    DateEdit.setVisibility(View.VISIBLE);
                    findViewById(R.id.notificationed).setVisibility(View.VISIBLE);
                } else {
                    cb.setVisibility(View.GONE);
                    DateEdit.setVisibility(View.GONE);
                    findViewById(R.id.notificationed).setVisibility(View.GONE);
                }
            }
            public void onNothingSelected(AdapterView<?> arg0) {
                cb.setVisibility(View.GONE);
                DateEdit.setVisibility(View.GONE);
                findViewById(R.id.notificationed).setVisibility(View.GONE);
            }
        });
        if(!cursor.getString(2).equals("")){
            cb.setChecked(true);
            DateEdit.setText(cursor.getString(2));
        }

        //guide cannot be deleted
        if(id.equals("1")) {
            findViewById(R.id.save_button).setVisibility(View.INVISIBLE);
            findViewById(R.id.del_button).setVisibility(View.INVISIBLE);
            findViewById(R.id.textView).setVisibility(View.GONE);
            s.setVisibility(View.GONE);
        }
/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        //DateEdit = (EditText) findViewById(R.id.timeField);
        DateEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(cb.isChecked()) {
                    showTruitonTimePickerDialog(v);
                    showTruitonDatePickerDialog(v);
                }
            }
        });
    }

    public void showTruitonDatePickerDialog(View v) {
        DialogFragment newFragment = new DatePickerFragment();
        newFragment.show(getSupportFragmentManager(), "datePicker");
    }

    public static class DatePickerFragment extends DialogFragment implements
            DatePickerDialog.OnDateSetListener {
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            // Use the current date as the default date in the picker
            final Calendar c = Calendar.getInstance();
            int year = c.get(Calendar.YEAR);
            int month = c.get(Calendar.MONTH);
            int day = c.get(Calendar.DAY_OF_MONTH);

            // Create a new instance of DatePickerDialog and return it
            return new DatePickerDialog(getActivity(), this, year, month, day);
        }

        public void onDateSet(DatePicker view, int year, int month, int day) {
            // Do something with the date chosen by the user
            Date date = new Date(year, month, day);
            SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
            //date = String.format("%02d/%02d/%04d", date);
            DateEdit.setText(sdf.format(date));
        }
    }

    public void showTruitonTimePickerDialog(View v) {
        DialogFragment newFragment = new TimePickerFragment();
        newFragment.show(getSupportFragmentManager(), "timePicker");
    }

    public static class TimePickerFragment extends DialogFragment implements
            TimePickerDialog.OnTimeSetListener {

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            // Use the current time as the default values for the picker
            final Calendar c = Calendar.getInstance();
            int hour = c.get(Calendar.HOUR_OF_DAY);
            int minute = c.get(Calendar.MINUTE);

            // Create a new instance of TimePickerDialog and return it
            return new TimePickerDialog(getActivity(), this, hour, minute,
                    DateFormat.is24HourFormat(getActivity()));
        }

        public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
            // Do something with the time chosen by the user
            if(!DateEdit.getText().toString().equals("")) {
                int year = Integer.parseInt(DateEdit.getText().toString().substring(1, 4));
                int month = Integer.parseInt(DateEdit.getText().toString().substring(4, 6));
                int day = Integer.parseInt(DateEdit.getText().toString().substring(6, 8));
                Date date = new Date(year-800, month-1, day, hourOfDay, minute);
                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm");
                DateEdit.setText(sdf.format(date));
            }
            //DateEdit.setText(DateEdit.getText() + " " + hourOfDay + ":" + minute);
        }
    }
/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    public void goBack(View view){
        finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_create_memo, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void delete(View view){
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        dialogBuilder.setTitle("Confirmation");
        dialogBuilder.setMessage("Do you really want to delete this memo? ");
        dialogBuilder.setNegativeButton("Cancel", null);
        dialogBuilder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if(myDb.deleteRow(Integer.parseInt(id))) {
                    finish();
                }
            }
        });
        AlertDialog alertDialog = dialogBuilder.create();
        alertDialog.show();
    }

    private void openDB(){
        myDb = new DBAdapter(this);
        myDb.open();
    }

    public void edit(View view){
        if(s.getSelectedItemPosition() == 2 && cb.isChecked() && DateEdit.getText().length() == 16){
            date = DateEdit.getText().toString();
            SimpleDateFormat simpleDateFormat =
                    new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
            try {
                long time = (long)(simpleDateFormat.parse(date + ":00").getTime() - simpleDateFormat.parse(simpleDateFormat.format(new Date())).getTime()
                        - (1000 * 60 * 60));
                if(time >= 0) {
                    scheduleNotification(getNotification("1 hour left - " + title.getText().toString()),
                            time, Integer.parseInt(id));
                }
            } catch (ParseException e) {
                e.printStackTrace();
            }
        } else {
             delNotification(getNotification("1 hour left - " + title.getText().toString()),
                        Integer.parseInt(id));
            date = "";
        }
        if(myDb.updateRow(Integer.parseInt(id),title.getText().toString(), date, content.getText().toString(), s.getSelectedItemPosition()+"")) {
            finish();
        }
    }

    private void scheduleNotification(Notification notification, long delay, int id) {

        Intent notificationIntent = new Intent(this, NotificationPublisher.class);
        notificationIntent.putExtra(NotificationPublisher.NOTIFICATION_ID, id);
        notificationIntent.putExtra(NotificationPublisher.NOTIFICATION, notification);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, id, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        long futureInMillis = SystemClock.elapsedRealtime() + delay;
        //long futureInMillis = delay;
        AlarmManager alarmManager = (AlarmManager)getSystemService(Context.ALARM_SERVICE);
        alarmManager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, futureInMillis, pendingIntent);
    }

    private void delNotification(Notification notification, int id){
        Intent notificationIntent = new Intent(this, NotificationPublisher.class);
        notificationIntent.putExtra(NotificationPublisher.NOTIFICATION_ID, id);
        notificationIntent.putExtra(NotificationPublisher.NOTIFICATION, notification);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, id, notificationIntent, PendingIntent.FLAG_CANCEL_CURRENT);
        AlarmManager alarmManager = (AlarmManager)getSystemService(Context.ALARM_SERVICE);
        alarmManager.cancel(pendingIntent);
    }

    private Notification getNotification(String content) {
        Notification.Builder builder = new Notification.Builder(this);
        builder.setContentTitle("Scheduled Notification");
        builder.setContentText(content);
        builder.setSmallIcon(R.mipmap.icmemo_launcher);
        return builder.build();
    }
}
