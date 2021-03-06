package com.example.tarun.auxilium;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Handler;

public class EmergencyMessages extends AppCompatActivity
{
    private TextView tv;
    ArrayList<String> messages=new ArrayList<>();;
    private ArrayAdapter myAdapter;
    private Spinner messageType;

    String TAG,content;
    private String shared_messages;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        overridePendingTransition(android.R.anim.fade_in,android.R.anim.fade_out);
        setContentView(R.layout.activity_emergency_messages);

        final EmergencyMessagesDatabaseHandler messages_handler=new EmergencyMessagesDatabaseHandler(this);
        messageType = (Spinner) findViewById(R.id.message_type);
        myAdapter=new ArrayAdapter(this,android.R.layout.simple_list_item_1,messages);
        messageType.setAdapter(myAdapter);

        Cursor cursor=messages_handler.getAllMessages();
        if(cursor!=null && cursor.moveToFirst())
        {   do
            {
                content=cursor.getString(cursor.getColumnIndex("name"));
                TAG=cursor.getString(cursor.getColumnIndex("tag"));
                messages.add(TAG);
                myAdapter.notifyDataSetChanged();
                cursor.moveToNext();
            }while (!cursor.isAfterLast());
            cursor.close();
        }
        else
        {
            messages_handler.addMessages("Create your own Messsage", "Default message will be displayed here");
            messages_handler.addMessages("Sample Message One", "HELP!!\nI need Help");
            messages_handler.addMessages("Sample Message Two", "EMERGENCY!!\nI need Help urgently");
            messages_handler.addMessages("Sample Message Three", "I am in Emergency. I need help now");
            messages_handler.addMessages("Sample Message Four", "Please Help me. ");

            messages.add("Create your own Messsage");
            messages.add("Sample Message One");
            messages.add("Sample Message Two");
            messages.add("Sample Message Three");
            messages.add("Sample Message Four");
            myAdapter.notifyDataSetChanged();
        }

        tv= (TextView)findViewById(R.id.message_print);
        tv.setText(getSharedPreferences());
    }

    public void previewClicked(View view)
    {
        final EmergencyMessagesDatabaseHandler addition_handler=new EmergencyMessagesDatabaseHandler(this);
        String item_selected=String.valueOf(messageType.getSelectedItem());

        Cursor mCursor=addition_handler.getMessageContent(item_selected);
        if(mCursor!=null && mCursor.moveToFirst())
        {   do
            {
                content=mCursor.getString(mCursor.getColumnIndex("name"));
          //      TAG=mCursor.getString(mCursor.getColumnIndex("_id"));
                mCursor.moveToNext();
            }while (!mCursor.isAfterLast());
            mCursor.close();
        }

        if(content.equals("Default message will be displayed here"))
        {
            showInputDialog();
        }
        else
        {
            tv.setText(content);
            saveSharedPreferences(content);
            Toast.makeText(this, "Message Successfully Selected As Your Default Message", Toast.LENGTH_SHORT).show();
        }
    }

    public void deleteClicked(View view)
    {
        final EmergencyMessagesDatabaseHandler delete_handler = new EmergencyMessagesDatabaseHandler(this);

        final String item_selected = String.valueOf(messageType.getSelectedItem());

        Cursor mCursor=delete_handler.getMessageContent(item_selected);
        if(mCursor!=null && mCursor.moveToFirst())
        {   do
            {
                content=mCursor.getString(mCursor.getColumnIndex("name"));
           //     TAG=mCursor.getString(mCursor.getColumnIndex("_id"));
                mCursor.moveToNext();
            }while (!mCursor.isAfterLast());
            mCursor.close();
        }


        if(content.equals("Default message will be displayed here"))
        {
            AlertDialog.Builder ab =new AlertDialog.Builder(EmergencyMessages.this);
            ab.setMessage("This option cant be deleted")
                    .setCancelable(false).setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which)
                {
                    dialog.cancel();
                }
            });
            AlertDialog alert=ab.create();
            alert.setTitle("MESSAGE");
            alert.show();
        }

        else
        {
            AlertDialog.Builder ab =new AlertDialog.Builder(EmergencyMessages.this);
            ab.setMessage("Do you want to delete this Message")
                    .setCancelable(false).setPositiveButton("yes", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which)
                {

                    delete_handler.deleteMessages(item_selected);
                    messages.remove(item_selected);
                    myAdapter.notifyDataSetChanged();

                    if(content.equals(tv.getText()))
                    {
                        tv.setText("PLEASE SELECT THE MESSAGE YOU WANT TO SEND");
                        content=tv.getText().toString();
                    }
                    saveSharedPreferences(content);
                    Toast.makeText(getApplicationContext(),"Message Successfully Deleted",Toast.LENGTH_SHORT).show();
                }
            }).setNegativeButton("No", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which)
                {
                    dialog.cancel();
                }
            });
            AlertDialog alert=ab.create();
            alert.setTitle("DELETION CONFIRMATION");
            alert.show();
        }

    }
    protected void showInputDialog()
    {
        final EmergencyMessagesDatabaseHandler handler=new EmergencyMessagesDatabaseHandler(this);
        // get prompts.xml view
        LayoutInflater layoutInflater = LayoutInflater.from(EmergencyMessages.this);
        View promptView = layoutInflater.inflate(R.layout.dialog_layout, null);
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(EmergencyMessages.this);
        alertDialogBuilder.setView(promptView);

        final EditText tag = (EditText) promptView.findViewById(R.id.tag);
        final EditText message=(EditText)promptView.findViewById(R.id.message);
        // setup a dialog window
        alertDialogBuilder.setCancelable(false)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id)
                    {
                        TAG=tag.getText().toString();
                        content=message.getText().toString();
                        if(TAG.length()==0)
                        {
                            Toast.makeText(getApplicationContext(),"Unable to add message\nPlease enter the TAG for your message",
                                                                Toast.LENGTH_LONG).show();
                        }
                        else
                        {
                            handler.addMessages(TAG,content);
                            messages.add(TAG);
                            myAdapter.notifyDataSetChanged();
                            tv.setText(content);
                            saveSharedPreferences(content);
                            Toast.makeText(getApplicationContext(), "Message Successfully Selected As Your Default Message", Toast.LENGTH_SHORT).show();
                        }
                    }
                })
                .setNegativeButton("Cancel",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        });

        // create an alert dialog
        AlertDialog alert = alertDialogBuilder.create();
        alert.show();
    }


    public void saveSharedPreferences(String text)
    {
/*
        SharedPreferences sharedPreferences=getSharedPreferences("IMPORT_MESSAGE", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor=sharedPreferences.edit();
        editor.putString("MESSAGE_LIST",""+text);
        editor.apply();
*/

        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(EmergencyMessages.this);
        SharedPreferences.Editor editor = pref.edit();
        editor.putString("idid", ""+text);
        editor.commit();
    }

    public String getSharedPreferences()
    {
     /*   SharedPreferences sharedPreferences=getSharedPreferences("IMPORT_MESSAGE", Context.MODE_PRIVATE);
        shared_messages = sharedPreferences.getString("MESSAGE_LIST","");
     */
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(EmergencyMessages.this);
        shared_messages=pref.getString("idid", "null");

        return shared_messages;
    }


}
