package com.kinotech.phona;


import java.util.ArrayList;
import java.util.List;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;


public class DatabaseHandler extends SQLiteOpenHelper
{
    //Database and version
    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "PhonaDB.db";
    //Database tables
    private static final String DATABASE_TABLE_CALLLOG = "call_log";
    private static final String DATABASE_TABLE_MESSAGE = "message";
    private static final String DATABASE_TABLE_CONTACT = "contact";
    //Database table call_log with column names
    private static final String LOG_COLUMN_CALLERID 	   = "callerid";
    private static final String LOG_COLUMN_CNUMBER  	   = "cnumber";
    private static final String LOG_COLUMN_CNAME 	   = "cname";
    private static final String LOG_COLUMN_CDURATION    = "cduration";
    private static final String LOG_COLUMN_CDATE 	   = "cdate";
    private static final String LOG_COLUMN_CTYPE 	   = "ctype";
    //Database table message with column names
    private static final String MSG_COLUMN_MSGID        = "_id";
    private static final String MSG_COLUMN_MSGTYPE 	   = "msgtype";
    private static final String MSG_COLUMN_MSGADDRESS   = "msgaddress";
    private static final String MSG_COLUMN_MSGDATE 	   = "msgdate";
    private static final String MSG_COLUMN_MSGDATE_SENT = "msgdate_sent";
    private static final String MSG_COLUMN_MSGBODY 	   = "msgbody";
    //Database table contact with column names
    private static final String CNT_COLUMN_CONTACTID	   = "contactid";
    private static final String CNT_COLUMN_CONTNAME	   = "contname";
    private static final String CNT_COLUMN_CONTVALUE	   = "contvalue";
    private static final String CNT_COLUMN_CONTDESCRIPTION  = "contdescription";


    public DatabaseHandler(Context context)
    {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db)
    {
        String CREATE_LOG_TABLE = "CREATE TABLE "+DATABASE_TABLE_CALLLOG+"("+LOG_COLUMN_CALLERID+" TEXT,"+LOG_COLUMN_CNUMBER +" TEXT,"+LOG_COLUMN_CNAME+" TEXT,"+LOG_COLUMN_CDURATION+" TEXT,"+LOG_COLUMN_CDATE+" TEXT, "+LOG_COLUMN_CTYPE+")";
        db.execSQL(CREATE_LOG_TABLE);

        String CREATE_MESSAGE_TABLE = "CREATE TABLE "+DATABASE_TABLE_MESSAGE+"("+MSG_COLUMN_MSGID+" TEXT,"+MSG_COLUMN_MSGTYPE +" TEXT,"+MSG_COLUMN_MSGADDRESS+" TEXT,"+MSG_COLUMN_MSGDATE+" TEXT,"+MSG_COLUMN_MSGDATE_SENT+" TEXT,"+MSG_COLUMN_MSGBODY+")";
        db.execSQL(CREATE_MESSAGE_TABLE);

        String CREATE_CONTACT_TABLE = "CREATE TABLE "+DATABASE_TABLE_CONTACT+"("+CNT_COLUMN_CONTACTID+" TEXT,"+CNT_COLUMN_CONTNAME +" TEXT,"+CNT_COLUMN_CONTVALUE+" TEXT,"+CNT_COLUMN_CONTDESCRIPTION+" )";
        db.execSQL(CREATE_CONTACT_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
    {
        db.execSQL("DROP TABLE IF EXISTS " + DATABASE_TABLE_CALLLOG);
        db.execSQL("DROP TABLE IF EXISTS " + DATABASE_TABLE_MESSAGE);
        db.execSQL("DROP TABLE IF EXISTS " + DATABASE_TABLE_CONTACT);
        //Create new tables
        onCreate(db);
    }

    public void addCallLog(CallCatch cc)
    {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(LOG_COLUMN_CALLERID, cc.getCallId());
        values.put(LOG_COLUMN_CNUMBER, cc.getPhoneNumber());
        values.put(LOG_COLUMN_CNAME, cc.getCallerName());
        values.put(LOG_COLUMN_CDURATION, cc.getCallDuration());
        values.put(LOG_COLUMN_CDATE, cc.getCallDate());
        values.put(LOG_COLUMN_CTYPE, cc.getCallType());

        //insert row
        db.insert(DATABASE_TABLE_CALLLOG, null, values);
        db.close();
    }

    public void addMessages(Message msg)
    {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(MSG_COLUMN_MSGID, msg.getId());
        values.put(MSG_COLUMN_MSGTYPE, msg.getMsgType());
        values.put(MSG_COLUMN_MSGADDRESS, msg.getAddress());
        values.put(MSG_COLUMN_MSGDATE, msg.getDate());
        values.put(MSG_COLUMN_MSGDATE_SENT, msg.getDateSent());
        values.put(MSG_COLUMN_MSGBODY, msg.getBody());

        //insert row
        db.insert(DATABASE_TABLE_MESSAGE, null, values);
        db.close();
    }

    public void addContact(Contact cont)
    {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(CNT_COLUMN_CONTACTID, cont.getContactID());
        values.put(CNT_COLUMN_CONTNAME, cont.getContactName());
        values.put(CNT_COLUMN_CONTVALUE, cont.getContactValue());
        values.put(CNT_COLUMN_CONTDESCRIPTION, cont.getContactDescription());

        //insert row
        db.insert(DATABASE_TABLE_CONTACT, null, values);
        db.close();
    }

    public void emptyCallLog()
    {
        String query = "DELETE FROM "+ DATABASE_TABLE_CALLLOG;

        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL(query);
        db.close();
    }

    public void deleteCallById(int id){
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("DELETE FROM "+DATABASE_TABLE_CALLLOG+" WHERE "+LOG_COLUMN_CALLERID+"="+id);
        db.close();
    }

    public void deleteMessagegById(int id){
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("DELETE FROM "+DATABASE_TABLE_MESSAGE+" WHERE "+MSG_COLUMN_MSGID+"="+id);
        db.close();
    }

    public List<CallCatch> getCallLog(int idin)
    {

        List<CallCatch> logList = new ArrayList<CallCatch>();
        String query ;

        if(idin < 1)
        {
            query = "SELECT * FROM " + DATABASE_TABLE_CALLLOG +" WHERE "+LOG_COLUMN_CALLERID+"="+(idin+1) ;
        }
        else
        {
            query = "SELECT * FROM " + DATABASE_TABLE_CALLLOG +" WHERE "+LOG_COLUMN_CALLERID+"="+idin ;

        }

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(query, null);

        //loop through all rows and add to list
        if (cursor.moveToFirst())
        {
            do
            {
                CallCatch cc = new CallCatch();
                cc.setCallId(cursor.getString(0));
                cc.setPhoneNumber(cursor.getString(1));
                cc.setCallerName(cursor.getString(2));
                cc.setCallDuration(cursor.getString(3));
                cc.setCallDate(cursor.getString(4));
                cc.setCallType(cursor.getString(5));
                logList.add(cc);     //add to log list

            }while (cursor.moveToNext());
        }

        cursor.close();
        db.close();

        return logList;
    }

    public List<Message> getMessage(int idin)
    {

        List<Message> msgList = new ArrayList<Message>();
        String query ;

        if(idin < 1)
        {
            query = "SELECT * FROM " + DATABASE_TABLE_MESSAGE +" WHERE "+MSG_COLUMN_MSGID+"="+(idin+1) ;
        }
        else
        {
            query = "SELECT * FROM " + DATABASE_TABLE_MESSAGE +" WHERE "+MSG_COLUMN_MSGID+"="+idin ;

        }

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(query, null);

        //loop through all rows and add to list
        if (cursor.moveToFirst())
        {
            do
            {
                Message msg = new Message();
                msg.setId(cursor.getString(0));
                msg.setMsgType(cursor.getString(1));
                msg.setAddress(cursor.getString(2));
                msg.setDate(cursor.getString(3));
                msg.setDateSent(cursor.getString(4));
                msg.setBody(cursor.getString(5));
                msgList.add(msg);     //add to log list

            }while (cursor.moveToNext());
        }

        cursor.close();
        db.close();

        return msgList;
    }

    public List<Contact> getContact(int idin)
    {

        List<Contact> conList = new ArrayList<Contact>();
        String query ;

        if(idin < 1)
        {
            query = "SELECT * FROM " + DATABASE_TABLE_CONTACT +" WHERE "+CNT_COLUMN_CONTACTID+"="+(idin+1) ;
        }
        else
        {
            query = "SELECT * FROM " + DATABASE_TABLE_CONTACT +" WHERE "+CNT_COLUMN_CONTACTID+"="+idin ;

        }

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(query, null);

        //loop through all rows and add to list
        if (cursor.moveToFirst())
        {
            do
            {
                Contact cont = new Contact();
                cont.setContactID(Integer.parseInt(cursor.getString(0)));
                cont.setContactName(cursor.getString(1));
                cont.setContactValue(cursor.getString(2));
                cont.setContactDescription(cursor.getString(3));
                conList.add(cont);     //add to log list

            }while (cursor.moveToNext());
        }

        cursor.close();
        db.close();

        return conList;
    }

    public List<CallCatch> getCallLog()
    {
        List<CallCatch> logList = new ArrayList<CallCatch>();
        String query = "SELECT * FROM " + DATABASE_TABLE_CALLLOG;
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(query, null);

        //loop through all rows and add to list
        if (cursor.moveToFirst())
        {
            do
            {
                CallCatch cc = new CallCatch();
                cc.setCallId(cursor.getString(0));
                cc.setPhoneNumber(cursor.getString(1));
                cc.setCallerName(cursor.getString(2));
                cc.setCallDuration(cursor.getString(3));
                cc.setCallDate(cursor.getString(4));
                cc.setCallType(cursor.getString(5));
                logList.add(cc);     //add to log list

            }while (cursor.moveToNext());
        }

        cursor.close();
        db.close();

        return logList;
    }

    public List<Message> getMessages()
    {

        List<Message> msgList = new ArrayList<Message>();
        String query = "SELECT * FROM " + DATABASE_TABLE_CALLLOG;
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(query, null);

        //loop through all rows and add to list
        if (cursor.moveToFirst())
        {
            do
            {
                Message msg = new Message();
                msg.setId(cursor.getString(0));
                msg.setMsgType(cursor.getString(1));
                msg.setAddress(cursor.getString(2));
                msg.setDate(cursor.getString(3));
                msg.setDateSent(cursor.getString(4));
                msg.setBody(cursor.getString(5));
                msgList.add(msg);     //add to log list

            }while (cursor.moveToNext());
        }

        cursor.close();
        db.close();

        return msgList;
    }

    public List<Contact> getContact()
    {

        List<Contact> conList = new ArrayList<Contact>();
        String query = "SELECT * FROM " + DATABASE_TABLE_CONTACT;
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(query, null);


        //loop through all rows and add to list
        if (cursor.moveToFirst())
        {
            do
            {
                Contact cont = new Contact();
                cont.setContactID(Integer.parseInt(cursor.getString(0)));
                cont.setContactName(cursor.getString(1));
                cont.setContactValue(cursor.getString(2));
                cont.setContactDescription(cursor.getString(3));
                conList.add(cont);     //add to contact list

            }while (cursor.moveToNext());
        }

        cursor.close();
        db.close();

        return conList;
    }

}

