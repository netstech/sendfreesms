package smsapi.netstech.net;

import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;



public class log extends AppCompatActivity {
    ListView list;
    SQLite db;
    AlertDialog.Builder builder;
    ArrayAdapter adb;
    List<SMS> sms;
    List<String> arry = new ArrayList<String>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log);
        list=(ListView)findViewById(R.id.list);
        db=new SQLite(getApplicationContext());
           sms=db.get();
        for(SMS a : sms){
            arry.add("To:"+a.getTo().toString()+"\n"+"Message:"+a.getMessage().toString()+"\nTimestamp:"+a.getTimestamp().toString()+"~"+a.getId());
        }
        adb = new ArrayAdapter(log.this,R.layout.support_simple_spinner_dropdown_item,arry);
        list.setAdapter(adb);
        list.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
                final int jj=i;
                new AlertDialog.Builder(log.this)
                        .setTitle("Confirm?")
                        .setMessage("Are you Sure you want to delete this?")
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {

                            public void onClick(DialogInterface dialog, int whichButton) {
                                int g= Integer.parseInt(arry.get(jj).split("~")[1]);
                                db.delete(g);
                                Toast.makeText(log.this,"Deleted Successfull", Toast.LENGTH_SHORT).show();
                                sms=db.get();
                                arry.clear();
                                for(SMS a : sms){
                                    arry.add("To:"+a.getTo().toString()+"\n"+"Message:"+a.getMessage().toString()+"\nTimestamp:"+a.getTimestamp().toString()+"~"+a.getId());
                                }
                                adb.notifyDataSetChanged();
                                list.invalidateViews();
                            }
                        })
                        .setNegativeButton(android.R.string.no, null).show();
                return true;
            }
            });
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                AlertDialog.Builder alert = new AlertDialog.Builder(log.this);
                alert.setMessage(arry.get(i));
                alert.show();
            }
        });
        }
    }
