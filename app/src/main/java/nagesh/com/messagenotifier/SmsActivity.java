package nagesh.com.messagenotifier;

import android.Manifest;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;

public class SmsActivity extends RuntimePermissionsActivity  implements OnItemClickListener {

    private static final int REQUEST_PERMISSIONS = 20;
    private static SmsActivity inst;
    ArrayList<String> smsMessagesList = new ArrayList<String>();
    ListView smsListView;
    ArrayAdapter arrayAdapter;
    SessionManager session;
    int MY_PERMISSION_READ_SMS = 111;

    public static SmsActivity instance() {
        return inst;
    }

    @Override
    public void onStart() {
        super.onStart();
        inst = this;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        session = new SessionManager(getApplicationContext());
        Intent intent = getIntent();
        boolean skiplogin = false;
        if (intent != null && intent.getStringExtra("skip") != null) {
            if (intent.getStringExtra("skip").equals("Yes"))
                skiplogin = true;
        }
        if (!skiplogin)
            session.checkLogin();
        setContentView(R.layout.activity_sms);
        smsListView = (ListView) findViewById(R.id.SMSList);
        arrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, smsMessagesList);
        smsListView.setAdapter(arrayAdapter);
        smsListView.setOnItemClickListener(this);
        SmsActivity.super.requestAppPermissions(new
                        String[]{Manifest.permission.READ_SMS,
                        Manifest.permission.SEND_SMS, Manifest.permission.RECEIVE_SMS, Manifest.permission.READ_CONTACTS}, R.string
                        .runtime_permissions_txt
                , REQUEST_PERMISSIONS);

    }

    @Override
    public void onPermissionsGranted(int requestCode) {

        refreshSmsInbox();
        tableStructure();
    }

    public void refreshSmsInbox() {
        ContentResolver contentResolver = getContentResolver();
        Cursor smsInboxCursor = contentResolver.query(Uri.parse("content://sms/inbox"), null, null, null, null);
        int indexBody = smsInboxCursor.getColumnIndex("body");
        int indexAddress = smsInboxCursor.getColumnIndex("address");
        if (indexBody < 0 || !smsInboxCursor.moveToFirst()) return;
        String address;
        arrayAdapter.clear();
        do {
            String str = "SMS From: " + smsInboxCursor.getString(indexAddress) +
                    "\n" + smsInboxCursor.getString(indexBody) + "\n";
            arrayAdapter.add(str);
        } while (smsInboxCursor.moveToNext());
    }

    public void updateList(final String smsMessage) {
        arrayAdapter.insert(smsMessage, 0);
        arrayAdapter.notifyDataSetChanged();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.activity_sms_action_menu, menu);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_logout:
                session.logoutUser();
                return true;
            case R.id.setting:
                Intent i = new Intent(this, SettingsActivity.class);
                startActivity(i);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void onItemClick(AdapterView<?> parent, View view, int pos, long id) {
        try {
            String[] smsMessages = smsMessagesList.get(pos).split("\n");
            String address = smsMessages[0];
            String smsMessage = "";
            for (int i = 1; i < smsMessages.length; ++i) {
                smsMessage += smsMessages[i];
            }

            String smsMessageStr = address + " is -- -- -- --  \n";
            smsMessageStr += smsMessage;
            Log.d("Message Email Start", smsMessageStr);

            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.
                    Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);

            try {
                new SendMailAsynTask(this).execute();

            } catch (Exception ex) {
                Log.e("Messages", ex + "");
                ex.printStackTrace();
                Toast.makeText(this, ex.toString(), Toast.LENGTH_LONG).show();
            }
            Log.d("Message Email End", smsMessageStr);
            Toast.makeText(this, smsMessageStr, Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void tableStructure() {
        Uri SMS_INBOX = Uri.parse("content://sms/conversations/");
        Cursor c = getContentResolver().query(SMS_INBOX, null, null, null, "date desc");

        startManagingCursor(c);
        String[] count = new String[c.getCount()];
        String[] snippet = new String[c.getCount()];
        String[] thread_id = new String[c.getCount()];

        c.moveToFirst();
        for (int i = 0; i < c.getCount(); i++) {
            count[i] = c.getString(c.getColumnIndexOrThrow("msg_count"));
            // .toString();
            thread_id[i] = c.getString(c.getColumnIndexOrThrow("thread_id"));
            //.toString();
            snippet[i] = c.getString(c.getColumnIndexOrThrow("snippet"));
            //  .toString();
            //Toast.makeText(getApplicationContext(), count[i] + " - " + thread_id[i]+" - "+snippet[i] , Toast.LENGTH_LONG).show();
            c.moveToNext();
        }
        c.close();
        /*for(int i=0;i<count.length;i++)
            Log.d("Message","count = "+count[i]+" snippet = "+snippet[i]+" thread_id = "+thread_id[i]+"\n");
*/
        for (int ad = 0; ad < thread_id.length; ad++) {
            Uri uri = Uri.parse("content://sms/inbox");
            String where = "thread_id=" + thread_id[ad];
            Cursor mycursor = getContentResolver().query(uri, null, where, null, null);
            startManagingCursor(mycursor);

            String[] number = new String[mycursor.getCount()];


            if (mycursor.moveToFirst()) {
                for (int i = 0; i < mycursor.getCount(); i++) {
                    number[i] = mycursor.getString(mycursor.getColumnIndexOrThrow("address"));

                    mycursor.moveToNext();
                }
            }
            mycursor.close();
        }

    }
}
