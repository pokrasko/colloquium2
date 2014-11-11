package ru.ifmo.md.colloquium2;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ListActivity;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;


public class MainActivity extends ListActivity {
    Dialog addDialog;
    Dialog renameDialog;
    ListView listView;
    TextView emptyView;

    private VoteAdapter adapter;
    private VoteResultReceiver receiver;

    private volatile boolean votesRunning = false;

    public static final int PERSON_RENAME_ID = 1;
    public static final int PERSON_DELETE_ID = 2;

    private long personId;
    private int currentPosition;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        dialog = createDialog();
        listView = (ListView) findViewById(android.R.id.list);
        emptyView = (TextView) findViewById(R.id.noPersons);

        registerForContextMenu(listView);

        receiver = new VoteResultReceiver(new Handler(), this);

        listView.setVisibility(View.GONE);
        emptyView.setVisibility(View.VISIBLE);

        adapter = new VoteAdapter(new PersonList());
        setListAdapter(adapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Person person = (Person) adapter.getItem(position);
                long personId = person.getId();
                Uri uri = Uri.withAppendedPath(VoteContentProvider.CONTENT_PERSONS_URI, "" + personId);
                getContentResolver().update(uri, new ContentValues(), null, null);
                adapter.vote(position);
            }
        });
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
        if (id == R.id.add_candidate) {
            if (votesRunning) {
                return false;
            }
            addDialog.show();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void createDialogs() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        final EditText nameView = new EditText(this);
        nameView.setInputType(InputType.TYPE_TEXT_VARIATION_PERSON_NAME);

        builder.setTitle(R.string.add_candidate)
               .setView(nameView);
        builder.setPositiveButton(R.string.OK, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String name = nameView.getText().toString();
                addCandidate(name);
            }
        })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
        addDialog = builder.create();

        builder = new AlertDialog.Builder(this);

        final EditText nameView = new EditText(this);
        nameView.setInputType(InputType.TYPE_TEXT_VARIATION_PERSON_NAME);

        builder.setTitle(R.string.rename_candidate)
                .setView(nameView);
        builder.setPositiveButton(R.string.OK, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String name = nameView.getText().toString();
                renameCandidate(name);
            }
        })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
        renameDialog = builder.create();
    }

    private void addCandidate(String name) {
        Cursor cursor = getContentResolver().query(VoteContentProvider.CONTENT_PERSONS_URI, null,
                VoteContentProvider.NAME_FIELD + "=" + name, null, null);
        if (cursor.getCount() != 0) {
            receiver.send(VoteResultReceiver.PERSON_EXISTS_ERROR, Bundle.EMPTY);
            return;
        }
        ContentValues values = new ContentValues();
        values.put(VoteContentProvider.NAME_FIELD, name);
        values.put(VoteContentProvider.VOTES_FIELD, 0);
        Uri uri = getContentResolver().insert(VoteContentProvider.CONTENT_PERSONS_URI, values);
        long id = Long.parseLong(uri.getLastPathSegment());
        ((VoteAdapter) getListAdapter()).add(new Person(id, name, 0));
    }

    private void renameCandidate(String name) {
        Uri uri = Uri.parse(VoteContentProvider.CONTENT_PERSONS_URI + "/" + personId);
        ContentValues values = new ContentValues();
        values.put("action", VoteContentProvider.RENAME_ACTION);
        values.put("name", name);
        getContentResolver().update(uri, values, null, null);
        adapter.rename(currentPosition, name);
    }


    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        menu.add(0, PERSON_RENAME_ID, 0, R.string.person_rename);
        menu.add(0, PERSON_DELETE_ID, 1, R.string.person_delete);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        if (item.getItemId() == PERSON_RENAME_ID) {
            if (votesRunning) {
                return false;
            }
            AdapterView.AdapterContextMenuInfo acmi = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
            Person person = (Person) adapter.getItem(acmi.position);
            currentPosition = acmi.position;
            personId = person.getId();
            renameDialog.show();
        } else if (item.getItemId() == PERSON_DELETE_ID) {
            if (votesRunning) {
                return false;
            }
            AdapterView.AdapterContextMenuInfo acmi = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
            Person person = (Person) adapter.getItem(acmi.position);
            long personId = person.getId();

            Uri uri = Uri.parse(VoteContentProvider.CONTENT_PERSONS_URI + "/" + personId);
            getContentResolver().delete(uri, null, null);
            adapter.remove(acmi.position);
        }
        return super.onContextItemSelected();
    }
}
