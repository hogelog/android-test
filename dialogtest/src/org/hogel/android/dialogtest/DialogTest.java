package org.hogel.android.dialogtest;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

public class DialogTest extends Activity {
	static final int DIALOG_HELLO_ID = 0;
	static final int DIALOG_LIST_ID = 1;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
    }

    private Dialog createHelloDialog() {
    	AlertDialog.Builder builder = new AlertDialog.Builder(this);
    	TextView view = new TextView(this);
    	view.setText("Hello, world!");
    	builder.setView(view);
    	return builder.create();
    }

    private ListView createSampleList() {
    	String[] items = new String[]{"hoge", "fuga", "moge"};
    	ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, items);

    	LayoutInflater inflater = (LayoutInflater) getLayoutInflater();
    	ListView list = (ListView) inflater.inflate(R.layout.samplelist, null);
    	list.setAdapter(adapter);
    	return list;
    }

    private Dialog createListDialog() {
    	AlertDialog.Builder builder = new AlertDialog.Builder(this);
    	builder.setView(createSampleList());
    	return builder.create();
    }
	
    @Override
    protected Dialog onCreateDialog(int id) {
    	switch (id) {
    	case DIALOG_HELLO_ID:
    		return createHelloDialog();
    	case DIALOG_LIST_ID:
    		return createListDialog();
    	}
    	return null;
    }

    public void clickHelloButton(View view) {
    	showDialog(DIALOG_HELLO_ID);
    }

    public void clickListButton(View view) {
    	showDialog(DIALOG_LIST_ID);
    }
}
