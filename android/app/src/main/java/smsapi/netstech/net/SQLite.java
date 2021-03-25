package smsapi.netstech.net;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Muhammad Ashir on 1/20/2017.
 */

public class SQLite extends SQLiteOpenHelper{

    public static final String db = "URL";


    public static final String tbl_SMS = "SMS";

    public static final String SMS_id = "ID";
    public static final String SMS_to = "sto";
    public static final String SMS_Message = "smessage";
    public static final String SMS_STatus = "status";
    public static final String SMS_TIMESTAMP = "timestamp";



    public SQLite(Context context) {
        super(context, db, null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL("create table "+tbl_SMS+" ("+SMS_id+" INTEGER PRIMARY KEY AUTOINCREMENT, "+SMS_to+" TEXT,"+SMS_Message+" TEXT,"+SMS_STatus+" TEXT,"+SMS_TIMESTAMP+"  TIMESTAMP DEFAULT CURRENT_TIMESTAMP)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

    }
    public long insert (List<SMS> all){
        for (SMS c:all
             ) {
            SQLiteDatabase db = this.getWritableDatabase();
            ContentValues val = new ContentValues();
            val.put(SMS_to,c.getTo());
            val.put(SMS_Message,c.getMessage());

           db.insert(tbl_SMS,null,val);
        }
        return 0;
    }


    public List<SMS> get(){

        List<SMS> contect = new ArrayList<SMS>();
        String selectQuery = "SELECT  * FROM " + tbl_SMS+" Order by ID DESC";


        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (c.moveToFirst()) {
            do {
                SMS one = new SMS();
                one.setId(c.getInt((c.getColumnIndex(SMS_id))));
                one.setTo((c.getString(c.getColumnIndex(SMS_to))));
                one.setMessage(c.getString(c.getColumnIndex(SMS_Message)));
                one.setStatus(c.getString(c.getColumnIndex(SMS_STatus)));
                one.setTimestamp(c.getString(c.getColumnIndex(SMS_TIMESTAMP)));

                // adding to todo list
                contect.add(one);
            } while (c.moveToNext());
        }

        return contect;
    }
    public void delete(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("delete  FROM " + tbl_SMS + " where " + SMS_id + "=" + id);
        db.close();
    }
}
