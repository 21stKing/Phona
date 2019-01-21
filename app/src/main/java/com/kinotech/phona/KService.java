package com.kinotech.phona;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Date;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONObject;
import android.annotation.SuppressLint;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.os.StrictMode;
import android.provider.CallLog;
import android.provider.ContactsContract;
import android.util.Log;

@SuppressLint("SimpleDateFormat")
public class KService extends Service {
    private ArrayList<CallCatch> calllog = new ArrayList<>();
    private ArrayList<Message> message = new ArrayList<>();
    private ArrayList<Contact> contact = new ArrayList<>();
    private ArrayList<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
    //private int msg_count = 0;
    //private int contact_count = 0;
    //private int calls_count = 0;
    private int CALLS_ALREADY_UPDATED = 0;
    private int MESSAGES_ALREADY_UPDATED = 0;
    private int CONTACTS_ALREADY_UPDATED = 0;
    //declaration - Location
    private LocationManager locationManager;
    private NotificationReceiver nr;
    private Location location;
    private String provider;
    private KLocationListener mylistener;
    private Criteria criteria;
    final DatabaseHandler db = new DatabaseHandler(this);
    Download download;

    @Override
    public IBinder onBind(Intent i)
    {
        //We don't provide binding, so return null
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();  //temp solution, use asynctask to do this
        StrictMode.setThreadPolicy(policy);
        nr              = new NotificationReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction("com.kinotech.phona.NOTIFICATION_LISTENER_EXAMPLE");
        registerReceiver(nr, filter);
        //getCalls();
        getMessages();
        getContacts();

        //Get the location manager
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        criteria = new Criteria();                                     //Define the criteria how to select the location provider
        criteria.setAccuracy(Criteria.ACCURACY_FINE);
        criteria.setCostAllowed(false);
        provider = locationManager.getBestProvider(criteria, false);   //Get the best provider depending on the criteria
       // location = locationManager.getLastKnownLocation(provider);     //The last known location of this provider

        mylistener = new KLocationListener();

        if (location != null) {
            mylistener.onLocationChanged(location);
        } else {

        }

       // locationManager.requestLocationUpdates(provider, 200, 1, mylistener); //location updates: at least 1 meter and 200millsecs change

        //upload data
        new PostTask().execute("upload_calls");
        new PostTask().execute("upload_location");
        new PostTask().execute("upload_messages");
        new PostTask().execute("upload_contacts");

        stopSelf();   //Stop service once it finishes its task

        return Service.START_STICKY;
    }

    @Override
    public void onDestroy()
    {   super.onDestroy();
        unregisterReceiver(nr);
        Log.d("KService", "Service done");
    }

    class NotificationReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent){
            //String txt = intent.getStringExtra("notification_event")+"\n"+textView.getText();
            //Log.d("Notification Event:: ",txt);
        }
    }

    private class PostTask extends AsyncTask<String, Integer, String>
    {
        @Override
        protected void onPreExecute()
        {
            super.onPreExecute();
            new Thread(new Runnable() {
                @Override
                public void run()
                { }
            }).start();

        }

        @Override
        protected String doInBackground(String... params)
        {
            String my_params = (String) params[0];

            String res = "";

            if(my_params == "upload_calls")
            {
                res = uploadCallLog();
            }else if(my_params == "upload_messages"){
                res = uploadMessages();
            }else if(my_params == "upload_location"){
                res = uploadLocation();
            }else if(my_params == "upload_contacts"){
                res = uploadContacts();
            }

            return res;
        }

        @Override
        protected void onProgressUpdate(Integer... values)
        {
            super.onProgressUpdate(values);
            Log.d("Phona Service Progress:: ", values[0].toString());
        }

        @Override
        protected void onPostExecute(String result)
        {
            super.onPostExecute(result);

            if(result == null)
            {
                Log.d("Phona Service", "Failed to upload");
            }
            else
            {
                Log.d("Phona Service", result);
                //handleIntent();
            }
        }
    }

    /*private void getCalls()
    {
        Cursor call = getContentResolver().query(CallLog.Calls.CONTENT_URI, null, null, null, null);  //alternative deprecated method you can use: Cursor c = managedQuery(CallLog.Calls.CONTENT_URI,null,null,null,null);
        int call_id = call.getColumnIndex(CallLog.Calls._ID);  //this is used to avoid uploading duplicates to the server
        int number = call.getColumnIndex(CallLog.Calls.NUMBER);
        int name = call.getColumnIndex(CallLog.Calls.CACHED_NAME);
        int type = call.getColumnIndex(CallLog.Calls.TYPE);
        int date = call.getColumnIndex(CallLog.Calls.DATE);
        int duration = call.getColumnIndex(CallLog.Calls.DURATION);

        while(call.moveToNext())
        {
            String callId = call.getString(call_id);
            String phNumber = call.getString(number);
            String cont_name = call.getString(name);
            String callType = call.getString(type);
            String callDate = call.getString(date);
            Date callDayTime = new Date(Long.parseLong(callDate));
            Format format = new SimpleDateFormat("yyy-MM-dd HH:mm:ss");
            String callDateTime  = format.format(callDayTime);
            String callDuration = call.getString(duration);

            //Before adding the call log to the list, check if there aren't any duplicates, calls that have already been uploaded.
            List<CallCatch> loglist = db.getCallLog(Integer.parseInt(callId));
            if(loglist.size() <= 0)
            {
                //The call is not in the database, therefore add it to the database and calllog list
                calllog.add(new CallCatch(callId, phNumber, cont_name, callType, callDateTime, callDuration));
            }
            else
            {
                //do not add to log list, call already uploaded
                CALLS_ALREADY_UPDATED += 1;
            }
        }

        //calls_count = calllog.size();

    } */

    private void getContacts()
    {
        ContentResolver cr = getContentResolver();
        Cursor cur = cr.query(ContactsContract.Contacts.CONTENT_URI,null, null, null, null);
        if (cur.getCount() > 0)
        {
            while (cur.moveToNext())
            {
                int id       = Integer.parseInt(cur.getString(cur.getColumnIndex(ContactsContract.Contacts._ID)));
                String id2 = id+"";
                String name     = cur.getString(cur.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
                String phoneNo  = "";
                String email    = "";
                //String note  = "";

                if (Integer.parseInt(cur.getString(cur.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER))) > 0) //retrieve contacts that has phone numbers only
                {
                    //Phone number
                    Cursor phoneCur = cr.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, ContactsContract.CommonDataKinds.Phone.CONTACT_ID +" = ?",new String[]{id2}, null);
                    while (phoneCur.moveToNext())
                    {
                        phoneNo  = phoneCur.getString(phoneCur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                        //Log.e("Contact", "Id: "+id+", Name: " + name + ", Phone No: " + phoneNo );
                        contact.add(new Contact(id,name,"Phone",phoneNo));
                    }
                    phoneCur.close();

                    //E-mail address
                    Cursor emailCur = cr.query(ContactsContract.CommonDataKinds.Email.CONTENT_URI, null, ContactsContract.CommonDataKinds.Email.CONTACT_ID +" = ?",new String[]{id2}, null);
                    while (emailCur.moveToNext())
                    {
                        email  = emailCur.getString(emailCur.getColumnIndex(ContactsContract.CommonDataKinds.Email.ADDRESS));
                        //Log.e("Email", email );
                        contact.add(new Contact(id,name,"Email",email));
                    }
                    emailCur.close();

                    //note
                         /*note = ContactsContract.Data.CONTACT_ID + " = ? AND " + ContactsContract.Data.MIMETYPE + " = ?";
                         String[] noteWhereParams = new String[]{id, ContactsContract.CommonDataKinds.Note.CONTENT_ITEM_TYPE};

                         Cursor noteCur = cr.query(ContactsContract.Data.CONTENT_URI, null, note, noteWhereParams, null);
                         while (noteCur.moveToNext())
                         {
                        	note  = noteCur.getString(noteCur.getColumnIndex(ContactsContract.CommonDataKinds.Note.NOTE));
                        	Log.e("Note", note );
                         }
                         noteCur.close();*/


                }

                //Log.e("Contact", "Id: "+id+", Name: " + name + ", Phone No: " + phoneNo +", E-mail: "+ email );
                //Log.e("Contact", "Id: "+id+", Name: " + name + ", Phone No: " + phoneNo +", E-mail: "+ email +", Identity: "+identity+", Nickname: "+nickname+", Website: "+website+", Note: "+note);

                //contact_count += 1;

            }
        }
        //sort the contact list
        checkAlreadyUploadedContacts();

    }

    private void checkAlreadyUploadedContacts()
    {
        int id1, id2remove, contact_from_db_size;
        Collections.sort(contact, new Comparator<Contact>(){
            @Override
            public int compare(Contact c1, Contact c2)
            {
                return c1.getContactID() - c2.getContactID(); //Ascending order  and  descending order is c2.getContactID() - c1.getContactID()
            }
        });

        List<Contact> contact_list = db.getContact(); //get last contact by id that is already saved
        contact_from_db_size = contact_list.size(); //DB

        Collections.sort(contact_list, new Comparator<Contact>(){
            @Override
            public int compare(Contact c1, Contact c2)
            {
                return c1.getContactID() - c2.getContactID(); //Ascending order
            }
        });

        if(contact_from_db_size <= 0)
        {
            //Continue - no contact found from DB
            //Note: remember to synchronize with server contacts that are already existing
        }
        else if(contact_from_db_size == contact.size())
        {
            /**
             * This means the contacts are up to date. But note that if the phone's mobile data/wifi is offline while busy uploading contacts to the server, then all contacts from the phone will still be loaded to the local database.
             * This creates confusion because on the server your contacts are not fully loaded but on the local database are.
             * So, to avoid this confusion compare the size of the local database table contacts and the phones contacts list to see if they are equal.
             * If they are equal then make sure that contacts on the server are equal to the local database and phone - having the same size.
             */
            ArrayList<Contact> server_contact = new ArrayList<Contact>();

            //Get contacts size from the server
            String results = null;
            int controller = 0;
            download = new Download();
            try
            {
                results = download.download("http://www.kinotech.co.za/App/Phona/updateContacts.php?task=get_size", "size");
                if(results == "good"){
                    //check if contacts from the server is the same size as the local contact list from db and phone contact list
                    String contact_size_server = download.getSize();
                    if(Integer.parseInt(contact_size_server) == contact_from_db_size){
                        Log.e("Server Contact List", "All contacts are up to date");
                    }else{
                        //We are now sure that the contacts are not equal, therefore we update the server contacts list from where it was last updated
                        results = "";
                        results = download.download("http://www.kinotech.co.za/App/Phona/updateContacts.php?task=get_last_record", "contact_record");
                        server_contact = download.getContents();

                        //remove all contacts from the contact list to avoid uploading duplicates to the server and local database.
                        for(int y=0; y < contact.size(); y++)
                        {
                            id2remove = server_contact.get(0).getContactID();
                            id1 = contact.get(y).getContactID();
                            if(controller == 0)
                            {
                                if(id1 != id2remove)
                                {
                                    //do remove
                                    contact.remove(y).getContactID();
                                    contact.remove(y).getContactName();
                                    contact.remove(y).getContactDescription();
                                    contact.remove(y).getContactValue();
                                }
                                else if(id1 == id2remove)
                                {
                                    //do not remove, but end here
                                    contact.remove(y).getContactID();
                                    contact.remove(y).getContactName();
                                    contact.remove(y).getContactDescription();
                                    contact.remove(y).getContactValue();
                                    controller = 1;
                                }
                            }
                            else if(controller == 1){
                                //dummy
                            }
                        }
                    }
                }
                else
                {
                    //no results
                }

            }
            catch (IOException e)
            {
                Log.e("Download Error", e.getMessage());
            }
        }
        else
        {
            //Get contact id from the database and compare it with the contact id from the phone book
            for(int x=0; x < contact_from_db_size;x++ )
            {
                id2remove = contact_list.get(x).getContactID();
                for(int y=0; y < contact.size(); y++)
                {
                    id1 = contact.get(y).getContactID();  //remove all contacts from the contact list to avoid uploading duplicates to the server and local database.
                    if(id2remove == id1)
                    {
                        //do remove
                        contact.remove(y).getContactID();
                        contact.remove(y).getContactName();
                        contact.remove(y).getContactDescription();
                        contact.remove(y).getContactValue();
                    }
                    else
                    {
                        //do not remove, just continue
                    }
                }
            }
        }
    }

    @SuppressLint("SimpleDateFormat")
    private void getMessages()
    {
        final String RECEIVED = "content://sms/inbox";
        final String SENT = "content://sms/sent";
        final String DRAFT = "content://sms/draft";

        //final String SPAM = "content://sms/spam";
        String msgDate = "";
        String msgDate_copy = "";
        long msgDate1, msgDate2;
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy/MM/dd hh:mm:ss.SSS");
        Calendar calendar = Calendar.getInstance();
        Cursor received = getContentResolver().query(Uri.parse(RECEIVED), null, null, null, null);
        Cursor sent = getContentResolver().query(Uri.parse(SENT), null, null, null, null);
        Cursor draft = getContentResolver().query(Uri.parse(DRAFT), null, null, null, null);
        //Cursor spam = getContentResolver().query(Uri.parse(SPAM), null, null, null, null);

        //received
        if(received.moveToFirst()){
            do{

                msgDate = received.getString(4);
                msgDate1 = Long.valueOf(msgDate).longValue();
                calendar.setTimeInMillis(msgDate1);
                msgDate  = formatter.format(calendar.getTime());

                msgDate_copy = received.getString(5);
                msgDate2 = Long.valueOf(msgDate_copy).longValue();
                calendar.setTimeInMillis(msgDate2);
                msgDate_copy  = formatter.format(calendar.getTime());
                int msg_id = Integer.parseInt( received.getString(0));
                List<Message> received_msg_list = db.getMessage(msg_id);
                int size = received_msg_list.size();

                if(size <= 0)
                {
                    //The message is not in the database, therefore add it to the database and message list
                    message.add(new Message("received", received.getString(0), received.getString(1), received.getString(2), received.getString(3), msgDate , msgDate_copy, received.getString(6),
                            received.getString(7),  received.getString(8), received.getString(9), received.getString(10), received.getString(11), received.getString(12), received.getString(13), received.getString(14),
                            received.getString(15),  received.getString(16), received.getString(17), received.getString(18), received.getString(19), received.getString(20), received.getString(21), received.getString(22),
                            received.getString(23),  received.getString(24), received.getString(25), received.getString(26), received.getString(27), received.getString(28)));
                }
                else
                {
                    //do not add to message list, message already updated/added
                    MESSAGES_ALREADY_UPDATED += 1;
                }

            }while(received.moveToNext());
        }else{
            Log.e("Received Messages", "No received messages found");
        }


        //Sent
        // message.clear();
        if(sent.moveToFirst()){
            do{
                msgDate = sent.getString(4);
                msgDate1 = Long.valueOf(msgDate).longValue();
                calendar.setTimeInMillis(msgDate1);
                msgDate  = formatter.format(calendar.getTime());

                msgDate_copy = sent.getString(5);
                msgDate2 = Long.valueOf(msgDate_copy).longValue();
                calendar.setTimeInMillis(msgDate2);
                msgDate_copy  = formatter.format(calendar.getTime());
                int msg_id = Integer.parseInt( sent.getString(0));
                List<Message> sent_msg_list = db.getMessage(msg_id);
                int size = sent_msg_list.size();

                if(size <= 0)
                {
                    //The message is not in the database, therefore add it to the database and message list
                    message.add(new Message("sent", sent.getString(0), sent.getString(1), sent.getString(2), sent.getString(3), msgDate, msgDate_copy, sent.getString(6),
                            sent.getString(7),  sent.getString(8), sent.getString(9), sent.getString(10), sent.getString(11), sent.getString(12), sent.getString(13), sent.getString(14),
                            sent.getString(15),  sent.getString(16), sent.getString(17), sent.getString(18), sent.getString(19), sent.getString(20), sent.getString(21), sent.getString(22),
                            sent.getString(23),  sent.getString(24), sent.getString(25), sent.getString(26), sent.getString(27), sent.getString(28)));
                }
                else
                {
                    //do not add to message list, message already updated/added
                    MESSAGES_ALREADY_UPDATED += 1;
                }


            }while(sent.moveToNext());
        }else{
            Log.e("Sent Messages", "No sent messages found");
        }

        //draft
        // message.clear();
        if(draft.moveToFirst()){
            do{
                msgDate = draft.getString(4);
                msgDate1 = Long.valueOf(msgDate).longValue();
                calendar.setTimeInMillis(msgDate1);
                msgDate  = formatter.format(calendar.getTime());

                msgDate_copy = draft.getString(5);
                msgDate2 = Long.valueOf(msgDate_copy).longValue();
                calendar.setTimeInMillis(msgDate2);
                msgDate_copy  = formatter.format(calendar.getTime());
                int msg_id = Integer.parseInt( draft.getString(0));
                List<Message> draft_msg_list = db.getMessage(msg_id);
                int size = draft_msg_list.size();

                if(size <= 0)
                {
                    //The message is not in the database, therefore add it to the database and message list
                    message.add(new Message("draft", draft.getString(0), draft.getString(1), draft.getString(2), draft.getString(3), msgDate,  msgDate_copy, draft.getString(6),
                            draft.getString(7),  draft.getString(8), draft.getString(9), draft.getString(10), draft.getString(11), draft.getString(12), draft.getString(13), draft.getString(14),
                            draft.getString(15),  draft.getString(16), draft.getString(17), draft.getString(18), draft.getString(19), draft.getString(20), draft.getString(21), draft.getString(22),
                            draft.getString(23),  draft.getString(24), draft.getString(25), draft.getString(26), draft.getString(27), draft.getString(28)));
                }
                else
                {
                    //do not add to message list, message already updated/added
                    MESSAGES_ALREADY_UPDATED += 1;
                }

            }
            while(draft.moveToNext());
        }else{
            Log.e("Draft Messages", "No draft messages found");
        }

        //spam
      /*
      if(spam.moveToFirst()){
       	 do{

       	     message.add(new Message("spam", spam.getString(0), spam.getString(1), spam.getString(2), spam.getString(3), spam.getString(4), spam.getString(5), spam.getString(6),
     		 spam.getString(7),  spam.getString(8), spam.getString(9), spam.getString(10), spam.getString(11), spam.getString(12), spam.getString(13), spam.getString(14),
     		 spam.getString(15),  spam.getString(16), spam.getString(17), spam.getString(18), spam.getString(19), spam.getString(20), spam.getString(21), spam.getString(22),
     	     spam.getString(23),  spam.getString(24), spam.getString(25), spam.getString(26), spam.getString(27), spam.getString(28)));

       	 //}while(spam.moveToNext());
         }else{
       	  Log.e("Spam Messages", "No spam messages found");
         }*/

        // msg_count = message.size();

    }

    private String uploadCallLog(){

        ArrayList<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
        String url = "http://www.kinotech.co.za/App/Phona/uploadCallLog.php?";
        InputStream is=null;
        String line=null;
        String result=null;
        int code;
        String res = "Failed";

        if(calllog.size() > 0)
        {
            for(int x=0;x<calllog.size();x++){
                nameValuePairs.add(new BasicNameValuePair("callid",calllog.get(x).getCallId()));
                nameValuePairs.add(new BasicNameValuePair("phone",calllog.get(x).getPhoneNumber()));
                nameValuePairs.add(new BasicNameValuePair("name",calllog.get(x).getCallerName()));
                nameValuePairs.add(new BasicNameValuePair("type",calllog.get(x).getCallType()));
                nameValuePairs.add(new BasicNameValuePair("date",calllog.get(x).getCallDate()));
                nameValuePairs.add(new BasicNameValuePair("duration",calllog.get(x).getCallDuration()));

                try{
                    HttpClient httpclient = new DefaultHttpClient();
                    HttpPost httppost = new HttpPost(url);
                    httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
                    HttpResponse response = httpclient.execute(httppost);
                    HttpEntity entity = response.getEntity();
                    is = entity.getContent();
                    Log.e("pass 1", "connection success ");

                }catch(Exception e){
                    Log.e("Fail 1", e.toString());
                    //Toast.makeText(getApplicationContext(), "Something is wrong",Toast.LENGTH_LONG).show();
                }

                try{
                    BufferedReader reader = new BufferedReader(new InputStreamReader(is,"iso-8859-1"),8);
                    StringBuilder sb = new StringBuilder();
                    while ((line = reader.readLine()) != null){
                        sb.append(line + "\n");
                    }
                    is.close();
                    result = sb.toString();
                    Log.e("pass 2", "connection success ");
                }
                catch(Exception e){ Log.e("Fail 2", e.toString());}

                try{
                    JSONObject json_data = new JSONObject(result.substring(result.indexOf("{"), result.lastIndexOf("}") + 1));
                    code=(json_data.getInt("code"));

                    if(code==1){
                        Log.e("pass 3", "Inserted Successfully");
                        res = "Successfull";
                        db.addCallLog(new CallCatch(calllog.get(x).getCallId(), calllog.get(x).getPhoneNumber(), calllog.get(x).getCallerName(), calllog.get(x).getCallType(), calllog.get(x).getCallDate(), calllog.get(x).getCallDuration()));                 //insert the record into database

                    }else{
                        Log.e("Fail 3", "Check your server script");
                    }
                }catch(Exception e){Log.e("Fail 3", e.toString());}

                nameValuePairs.clear();

            }
        }
        else if( calllog.size() == 0 && CALLS_ALREADY_UPDATED > 0 )
        {
            res = "some or all calls already updated";
        }
        else if( calllog.size() == 0 && CALLS_ALREADY_UPDATED == 0)
        {
            res = "call log is empty";
        }

        return res;
    }

    private String uploadContacts(){

        ArrayList<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
        String url = "http://www.kinotech.co.za/App/Phona/uploadContacts.php?";
        InputStream is=null;
        String line=null;
        String result=null;
        int code;
        String res = "Failed";
        String contact_id;

        if(contact.size() > 0){
            for(int x=0;x < contact.size();x++)
            {
                contact_id = contact.get(x).getContactID()+"";
                nameValuePairs.add(new BasicNameValuePair("id",contact_id));
                nameValuePairs.add(new BasicNameValuePair("name",contact.get(x).getContactName()));
                nameValuePairs.add(new BasicNameValuePair("value",contact.get(x).getContactValue()));
                nameValuePairs.add(new BasicNameValuePair("description",contact.get(x).getContactDescription()));

                try{
                    HttpClient httpclient = new DefaultHttpClient();
                    HttpPost httppost = new HttpPost(url);
                    httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
                    HttpResponse response = httpclient.execute(httppost);
                    HttpEntity entity = response.getEntity();
                    is = entity.getContent();
                    Log.e("Connected to remote server", "contact uploaded");

                    //Add to local database
                    db.addContact(new Contact(contact.get(x).getContactID(), contact.get(x).getContactName(), contact.get(x).getContactDescription(), contact.get(x).getContactValue()));
                    Log.e("Connected to local DB", "contact inserted");
                    res = "Successfull";

                    //Read the input stream
                    BufferedReader reader = new BufferedReader(new InputStreamReader(is,"iso-8859-1"),8);
                    StringBuilder sb = new StringBuilder();
                    while ((line = reader.readLine()) != null)
                    {
                        sb.append(line + "\n");
                    }
                    is.close();
                    result = sb.toString();

                    //Get value from json
                    JSONObject json_data = new JSONObject(result.substring(result.indexOf("{"), result.lastIndexOf("}") + 1));
                    code = (json_data.getInt("code"));

                    if(code==1)
                    {
                        res = "Successfull";
                    }
                    else
                    {
                        res = "Connection lost. Check your network";
                    }

                }catch(Exception e){
                    Log.e("Something went wrong ", e.toString());
                }

                nameValuePairs.clear();
            }
        }
        else if( contact.size() == 0 && CONTACTS_ALREADY_UPDATED > 0 )
        {
            res = "some or all contacts already updated";
        }
        else if( contact.size() == 0 && CONTACTS_ALREADY_UPDATED == 0)
        {
            res = "contact list is empty";
        }


        return res;
    }

    private String uploadMessages(){

        String url = "http://www.kinotech.co.za/App/Phona/uploadMessages.php?";
        InputStream is=null;
        String line=null;
        String result=null;
        int code;
        String res = "Failed";

        if(message.size() > 0)
        {
            for(int x=0;x<message.size();x++){

                nameValuePairs.add(new BasicNameValuePair("_id",message.get(x).getId()));
                nameValuePairs.add(new BasicNameValuePair("msg_type",message.get(x).getMsgType()));
                nameValuePairs.add(new BasicNameValuePair("address",message.get(x).getAddress()));
                nameValuePairs.add(new BasicNameValuePair("body",message.get(x).getBody()));
                nameValuePairs.add(new BasicNameValuePair("date",message.get(x).getDate()));
                nameValuePairs.add(new BasicNameValuePair("date_sent",message.get(x).getDateSent()));

                try{
                    HttpClient httpclient = new DefaultHttpClient();
                    HttpPost httppost = new HttpPost(url);
                    httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
                    HttpResponse response = httpclient.execute(httppost);
                    HttpEntity entity = response.getEntity();
                    is = entity.getContent();
                    Log.e("pass 1", "connection success ");

                }catch(Exception e){
                    Log.e("Fail 1", e.toString());
                }

                try{
                    BufferedReader reader = new BufferedReader(new InputStreamReader(is,"iso-8859-1"),8);
                    StringBuilder sb = new StringBuilder();
                    while ((line = reader.readLine()) != null){
                        sb.append(line + "\n");
                    }
                    is.close();
                    result = sb.toString();
                    Log.e("pass 2", "connection success ");
                }
                catch(Exception e){ Log.e("Fail 2", e.toString());}

                try{
                    JSONObject json_data = new JSONObject(result.substring(result.indexOf("{"), result.lastIndexOf("}") + 1));
                    code=(json_data.getInt("code"));

                    if(code==1){
                        Log.e("pass 3", "Inserted Successfully");
                        res = "Successfull";
                        db.addMessages(new Message(message.get(x).getMsgType(),
                                message.get(x).getId(),
                                message.get(x).getThread_id(),
                                message.get(x).getAddress(),
                                message.get(x).getPerson(),
                                message.get(x).getDate(),
                                message.get(x).getDateSent(),
                                message.get(x).getProtocol(),
                                message.get(x).getRead(),
                                message.get(x).getStatus(),
                                message.get(x).getType(),
                                message.get(x).getReplyPath(),
                                message.get(x).getSubject(),
                                message.get(x).getBody(),
                                message.get(x).getServiceCenter(),
                                message.get(x).getLocked(),
                                message.get(x).getErrorCode(),
                                message.get(x).getSeen(),
                                message.get(x).getDeletable(),
                                message.get(x).getHidden(),
                                message.get(x).getGroupId(),
                                message.get(x).getGroupType(),
                                message.get(x).getDeliveryDate(),
                                message.get(x).getAppId(),
                                message.get(x).getMsgId(),
                                message.get(x).getCallbackNumber(),
                                message.get(x).getReserved(),
                                message.get(x).getPri(),
                                message.get(x).getTeleserviceId(),
                                message.get(x).getLinkUrl()
                        ));                 //insert the record into database
                    }else{
                        Log.e("Fail 3", "Check you script");
                    }
                }catch(Exception e){Log.e("Fail 3", e.toString());}

                nameValuePairs.clear();
            }
        }
        else if( message.size() == 0 && MESSAGES_ALREADY_UPDATED > 0 )
        {
            res = "some or all messages already updated";
        }
        else if( message.size() == 0 && MESSAGES_ALREADY_UPDATED == 0)
        {
            res = "message box is empty";
        }


        return res;
    }

    private String  uploadLocation(){
        String url = "http://www.kinotech.co.za/App/Phona/uploadLocation.php?";
        InputStream is=null;
        String line=null;
        String result=null;
        int code;
        String res = "Failed";

        try{
            HttpClient httpclient = new DefaultHttpClient();
            HttpPost httppost = new HttpPost(url);
            httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
            HttpResponse response = httpclient.execute(httppost);
            HttpEntity entity = response.getEntity();
            is = entity.getContent();
            Log.e("pass 1", "connection success ");

        }catch(Exception e){
            Log.e("Fail 1", e.toString());
        }

        try{
            BufferedReader reader = new BufferedReader(new InputStreamReader(is,"iso-8859-1"),8);
            StringBuilder sb = new StringBuilder();
            while ((line = reader.readLine()) != null){
                sb.append(line + "\n");
            }
            is.close();
            result = sb.toString();
            Log.e("pass 2", "connection success ");
        }
        catch(Exception e){ Log.e("Fail 2", e.toString());}

        try{
            JSONObject json_data = new JSONObject(result.substring(result.indexOf("{"), result.lastIndexOf("}") + 1));
            code=(json_data.getInt("code"));

            if(code==1){
                Log.e("pass 3", "Inserted Successfully");
                res = "Successfull";
            }else{
                Log.e("Fail 3", "Check you script");
            }
        }catch(Exception e){Log.e("Fail 3", e.toString());}

        nameValuePairs.clear();



        return res;
    }

    private class KLocationListener implements LocationListener {
        @Override
        public void onLocationChanged(Location location) {

            SimpleDateFormat formatter = new SimpleDateFormat("yyyy/MM/dd hh:mm:ss.SSS");
            Calendar calendar = Calendar.getInstance();
            String curDate = formatter.format(calendar.getTime());

            nameValuePairs.add(new BasicNameValuePair("latitude",String.valueOf(location.getLatitude())));
            nameValuePairs.add(new BasicNameValuePair("longitude",String.valueOf(location.getLongitude())));
            nameValuePairs.add(new BasicNameValuePair("loc_time",String.valueOf(location.getTime())));
            nameValuePairs.add(new BasicNameValuePair("syst_datetime", curDate));

        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
        }

        @Override
        public void onProviderEnabled(String provider) {
        }

        @Override
        public void onProviderDisabled(String provider) {
        }
    }


}
