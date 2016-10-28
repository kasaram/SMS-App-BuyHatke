package com.buyhutkesmsapp.activities;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.buyhutkesmsapp.database.UploadOnDrive;
import com.smsapplication.R;
import com.buyhutkesmsapp.utils.ItemOnClickListener;
import com.buyhutkesmsapp.adapters.SMSAdapter;
import com.buyhutkesmsapp.models.SMS;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author Vishwajeet
 * @since 27 October 2016
 */

public class MainActivity extends AppCompatActivity {

    private FloatingActionButton fab;
    private RecyclerView recyclerView;
    private SMSAdapter smsAdapter;
    private ArrayList<SMS> smsList;
    private Cursor cursor;
    private UploadToDrive uploadSMS;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        recyclerView = (RecyclerView) findViewById(R.id.messages_list);
        smsList = new ArrayList<>();

        fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, SendSMSActivity.class);
                startActivity(intent);
            }
        });

        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(layoutManager);

    }

    @Override
    protected void onResume() {
        super.onResume();
        final ArrayList<SMS> sms_list, smslist_group;
        sms_list = new ArrayList<>();
        smslist_group = new ArrayList<>();

        int REQUEST_CODE_ASK_PERMISSIONS = 123;
        try {
            //Permission for API >=23
            if (ContextCompat.checkSelfPermission(getBaseContext(), "android.permission.READ_SMS") == PackageManager.PERMISSION_GRANTED) {
            } else {
                ActivityCompat.requestPermissions(MainActivity.this, new String[]{"android.permission.READ_SMS"}, REQUEST_CODE_ASK_PERMISSIONS);
            }

            cursor = getContentResolver().query(Uri.parse("content://sms/inbox"), new String[]{"_id", "address", "date", "body"}, null, null, null);
            StringBuilder sb = new StringBuilder();

            if (cursor != null && cursor.getCount() > 0) {
                while (cursor.moveToNext()) {
                    String address = cursor.getString(cursor.getColumnIndexOrThrow(String.valueOf(R.string.address)));
                    String date = cursor.getString(cursor.getColumnIndexOrThrow(String.valueOf(R.string.body)));
                    String body = cursor.getString(cursor.getColumnIndexOrThrow(String.valueOf(R.string.date)));
                    sb.append(address).append("\n");
                    sb.append(body).append("\n");
                    sb.append(date).append("\n");
                    sb.append("\n");
                    sms_list.add(new SMS(address, date, body));
                }
                cursor.close();
            }

            smsList = sms_list;
            //MAP to store the details of all the messages associated with one sender
            Map<String, SMS> map = new LinkedHashMap<>();

            for (SMS message : sms_list) {

                SMS existingValue = map.get(message.address);
                if (existingValue == null) {
                    map.put(message.address, message);
                }
            }

            smslist_group.clear();
            smslist_group.addAll(map.values());

            smsAdapter = new SMSAdapter(smslist_group);
            recyclerView.setAdapter(smsAdapter);

            recyclerView.addOnItemTouchListener(
                    new ItemOnClickListener(getApplicationContext(), new ItemOnClickListener.OnItemClickListener() {
                        @Override
                        public void onItemClick(View view, int position) {

                            ArrayList<SMS> smsList_inside = new ArrayList<>();
                            String n = smslist_group.get(position).address;

                            for (int i = 0; i < sms_list.size(); i++) {
                                if (sms_list.get(i).address.equals(n))
                                    smsList_inside.add(sms_list.get(i));
                            }
                            Intent i = new Intent(MainActivity.this, ReadSMSActivity.class);
                            i.putParcelableArrayListExtra("messages", smsList_inside);
                            startActivity(i);
                        }
                    })
            );

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    class UploadToDrive extends AsyncTask<Void, Integer, Uri> {

        ProgressDialog pDialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(MainActivity.this);
            pDialog.setMessage(String.valueOf(R.string.exporting_message));
            pDialog.setIndeterminate(false);
            pDialog.setMax(100);
            pDialog.setProgress(0);
            pDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            pDialog.setCancelable(false);
            pDialog.show();
        }

        @Override
        protected Uri doInBackground(Void... params) {
            FileOutputStream fos = null;
            try {
                String file_name = "SmsList.txt";
                File file = new File(getFilesDir(), file_name);
                if (!file.exists())
                    file.createNewFile();
                //Storing the file inside the internal storage of the device since all device may no have external storage
                fos = openFileOutput(file_name, Context.MODE_PRIVATE);
                cursor = getContentResolver().query(Uri.parse("content://sms/inbox"), null, null, null, null);
                int count = cursor.getCount(), i = 0;

                StringBuilder sb = new StringBuilder();
                if (cursor.moveToFirst()) {
                    do {
                        sb.append(cursor.getString(cursor.getColumnIndex(String.valueOf(R.string.address))))
                                .append("\n");
                        sb.append(cursor.getString(cursor.getColumnIndex(String.valueOf(R.string.body))))
                                .append("\n");
                        sb.append(cursor.getString(cursor.getColumnIndex(String.valueOf(R.string.date))))
                                .append("\n");
                        sb.append("\n");
                        publishProgress(++i * 100 / count);
                    } while (!isCancelled() && cursor.moveToNext());
                }
                fos.write(sb.toString().getBytes());
                return Uri.fromFile(file);
            } catch (Exception e) {
            } finally {
                if (fos != null) {
                    try {
                        fos.close();
                    } catch (IOException e) {
                    }
                }
            }
            return null;
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
            pDialog.setProgress(values[0]);
        }

        @Override
        protected void onPostExecute(Uri result) {
            super.onPostExecute(result);
            pDialog.dismiss();
            if (result == null) {
                Toast.makeText(MainActivity.this, String.valueOf(R.string.export_failed),
                        Toast.LENGTH_LONG).show();
                return;
            }
            Intent i = new Intent(MainActivity.this, UploadOnDrive.class);
            startActivity(i);
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_search) {
            Intent i = new Intent(MainActivity.this, SearchSMSActivity.class);
            i.putParcelableArrayListExtra("search", smsList);
            startActivity(i);
            return true;
        }
        if (id == R.id.action_share) {
            uploadSMS = new UploadToDrive();
            uploadSMS.execute();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onPause() {
        if (uploadSMS != null) {
            uploadSMS.cancel(false);
            uploadSMS.pDialog.dismiss();
        }
        super.onPause();
    }

}
