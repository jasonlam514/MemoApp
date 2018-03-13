package com.example.jasonlam.memoapp;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.text.format.DateUtils;
import android.text.format.Time;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;


public class MainActivity extends ActionBarActivity {

    DBAdapter myDb;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        openDB();
        checkFirstRun();
        /*myDb.insertRow("Thank you for using the Memo App! Click here for guideYou can create a list of memos! \n \n \n \n" +
                        "You can add a new memo by clicking the add button at the top left corner on the main page,\" +\n" +
                        "                        \" you can edit or delete the memo when you click on a specific memo. Time alert can be set for urgent memo........................................................................................................................s", "",
                "", "1");*/
        populateListView();
        Thread t = new Thread() {

            @Override
            public void run() {
                try {
                    while (!isInterrupted()) {
                        Thread.sleep(60000);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                populateListView();
                            }
                        });
                    }
                } catch (InterruptedException e) {
                }
            }
        };

        t.start();
    }

    public void addMemo(View view){
        Intent intent = new Intent(MainActivity.this, CreateMemoActivity.class);
        startActivityForResult(intent, 1);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
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

    public void showAuthor(View view){
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        dialogBuilder.setTitle("About");
        dialogBuilder.setMessage("Author: Jason Lam");
        dialogBuilder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //populateListView();
            }
        });
        AlertDialog alertDialog = dialogBuilder.create();
        alertDialog.show();
    }

    private void populateListView(){
        Cursor cursor = myDb.getAllRows();
        //startManagingCursor(cursor);
        String[] fromFieldNames = new String[] {DBAdapter.KEY_TYPE, DBAdapter.KEY_TASK, DBAdapter.KEY_DATE, DBAdapter.KEY_CONTENT};
        int[] toViewIDs = new int[] {R.id.typeList, R.id.titleList, R.id.dateText, R.id.contentView};
        SimpleCursorAdapter myCursorAdaptor;
        myCursorAdaptor = new SimpleCursorAdapter(getBaseContext(),R.layout.row_layout, cursor, fromFieldNames,toViewIDs,0);
        myCursorAdaptor.setViewBinder(new SimpleCursorAdapter.ViewBinder() {

            public boolean setViewValue(View aView, Cursor aCursor, int aColumnIndex) {
                String[] type = new String[]{ "Normal", "Important", "Urgent"};
                if (aColumnIndex == 4) {
                    int pos = Integer.parseInt(aCursor.getString(aColumnIndex));

                    TextView textView = (TextView) aView;
                    textView.setText(type[pos]);
                    return true;
                }
                if(aColumnIndex == 2 && !aCursor.getString(aColumnIndex).equals("")) {
                    SimpleDateFormat simpleDateFormat =
                            new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");

                    Date date1 = null;
                    Date date2 = null;
                    try {

                        date1 = simpleDateFormat.parse(simpleDateFormat.format(new Date()));
                        date2 = simpleDateFormat.parse(aCursor.getString(aColumnIndex)+":00");
                        //Date date2 = simpleDateFormat.parse(aCursor.getString(aColumnIndex));

                        TextView textView = (TextView) aView;
                        textView.setText(printDifference(date1, date2));
                        //textView.setText(aCursor.getString(aColumnIndex)+":00");

                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                    return true;
                }

                return false;
            }
        });
        ListView myList = (ListView)findViewById(R.id.listView);
        myList.setAdapter(myCursorAdaptor);
        //.makeText(MainActivity.this, cursor.getCount()+"", Toast.LENGTH_SHORT).show();
        myList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //String picked = "You selected " + String.valueOf(parent.getItemAtPosition(position));
                Cursor cursor =(Cursor)parent.getItemAtPosition(position);
                int _id = cursor.getInt(0);
                String picked = "You selected " + _id;
                //Toast.makeText(MainActivity.this, picked, Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(MainActivity.this, EditMemoActivity.class);
                intent.putExtra("ID",_id+"");
                startActivityForResult(intent, 1);
            }
        });
    }

    private void openDB(){
        myDb = new DBAdapter(this);
        myDb.open();
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        populateListView();
    }

    public void checkFirstRun() {
        boolean isFirstRun = getSharedPreferences("PREFERENCE", MODE_PRIVATE).getBoolean("isFirstRun", true);
        if (isFirstRun){
            myDb.insertRow("Thank you for using the Memo App! Click here for guide", "",
                    "You can create a list of memos! \nYou can add a new memo by clicking the add button at the top left corner on the main page," +
                            " you can edit or delete the memo when you click on a specific memo. Time alert can be set for urgent memos.", "1");
            getSharedPreferences("PREFERENCE", MODE_PRIVATE)
                    .edit()
                    .putBoolean("isFirstRun", false)
                    .apply();
        }
    }

    public String printDifference(Date startDate, Date endDate){

        //milliseconds
        long different = endDate.getTime() - startDate.getTime();

        long secondsInMilli = 1000;
        long minutesInMilli = secondsInMilli * 60;
        long hoursInMilli = minutesInMilli * 60;
        long daysInMilli = hoursInMilli * 24;

        long elapsedDays = different / daysInMilli;
        different = different % daysInMilli;

        long elapsedHours = different / hoursInMilli;
        different = different % hoursInMilli;

        long elapsedMinutes = different / minutesInMilli;
        different = different % minutesInMilli;

        String daypast = elapsedDays + " d ";
        String hourpast = elapsedHours + " h ";
        String minpast = elapsedMinutes + " m ";
        if(elapsedDays == 0){
            daypast = "";
            if(elapsedHours == 0){
                hourpast = "";
            }
        }
        if(different < 0) {
            return "finished";
        }
        else
        {
            return (daypast + hourpast + minpast + "left");
        }
    }

    @Override
    public void onBackPressed()
    {
        this.recreate();
    }
}
