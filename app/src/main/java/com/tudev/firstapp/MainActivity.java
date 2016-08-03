package com.tudev.firstapp;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.SparseBooleanArray;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.tudev.firstapp.data.Contact;
import com.tudev.firstapp.data.ContactAccessor;
import com.tudev.firstapp.data.Contacts;
import com.tudev.firstapp.data.SQLContactHelper;
import com.tudev.firstapp.data.SimpleContactDatabase;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import static android.R.color.holo_red_dark;

public class MainActivity extends AppCompatActivity {

    private static final int TAG_ID = 1;
    private static final int CONTACT_ACTIVITY_RCODE = 1;
    public static final String CONTACT_EDIT_INTENT_KEY = "CONTACT_EDIT_INTENT_KEY";

    private ContactAccessor accessor;
    private BaseAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final ListView listView = (ListView) findViewById(R.id.listViewOne);
        listView.setEmptyView(getLayoutInflater().inflate(R.layout.empty_view, listView));
        final Button actionButton = (Button) findViewById(R.id.listActionButton);
        accessor = new SimpleContactDatabase(new SQLContactHelper(getApplicationContext()));
        adapter = new ContactAdapter(getLayoutInflater(), accessor.getSimpleContacts());

        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Intent intent = new Intent(MainActivity.this, ContactActivity.class);
                Contact.ContactSimple origin = (Contact.ContactSimple) adapterView.getItemAtPosition(i);
                Contact toSend = accessor.getContact(origin.getId());
                intent.putExtra(ContactActivity.CONTACT_BUNDLE_KEY, toSend);
                MainActivity.this.startActivity(intent);
            }
        });

        listView.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                actionButton.setText(MainActivity.this.getString(R.string.remove_button));
                actionButton.setTag(TAG_ID, AddButtonIntent.REMOVE);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                actionButton.setText(MainActivity.this.getString(R.string.add_button));
                actionButton.setTag(TAG_ID, AddButtonIntent.ADD);
            }
        });
        actionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AddButtonIntent intent = (AddButtonIntent) actionButton.getTag(TAG_ID);
                switch (intent){
                    case ADD:{
                        // TODO: start activity_edit view with CREATE param
                        Intent createIntent = new Intent(MainActivity.this, EditContactActivity.class);
                        createIntent.putExtra(CONTACT_EDIT_INTENT_KEY, ContactActionIntent.CREATE);
                        MainActivity.this.startActivityForResult(createIntent,CONTACT_ACTIVITY_RCODE);
                    }
                    case REMOVE:{
                        SparseBooleanArray array = listView.getCheckedItemPositions();
                        List<Contact.ContactSimple> removeList = new ArrayList<>();
                        for (int i = 0; i < array.size(); ++i) {
                            if(array.get(i)){
                                removeList.add((Contact.ContactSimple) listView.getItemAtPosition(i));
                            }
                        }
                        accessor.removeContacts(removeList);
                        adapter.notifyDataSetChanged();
                    }
                }
            }
        });
    }

    @Override
    protected void onStart() {
        // user re-enters activity here
        SimpleApplication app = (SimpleApplication)getApplication();
        if(app.getState() == ContactDBState.MODIFIED) {
            accessor.invalidate();
            adapter.notifyDataSetChanged();
            app.setState(ContactDBState.INTACT);
        }
        super.onStart();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            accessor.close();
        }catch (IOException ex){
            Toast.makeText(this, ex.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
}
