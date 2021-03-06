/**
 * 	File Director -- Search.java
 *
 *      Author: David Jolly 
 *      		[jollyd@onid.oregonstate.edu]
 *
 */

package com.majestic.director;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import com.majestic.director.adapter.Entry;
import com.majestic.director.adapter.EntryArrayAdapter;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnKeyListener;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;

public class Search extends Activity {

	/**
	 * Dialogs
	 */
	private static final int PROG_DIALOG = 0;
	private static final int SEARCH_UPDATE = 0;
	
	/**
	 * Depth to recursively search for files
	 */
	public static final int NESTED_FILE_DEPTH = 2;
	
	private ListView searchView;
	private EditText searchText;
	private Thread searchThread;
	
	private Handler handle = new Handler() {
		
		public void handleMessage(Message msg) {
			switch(msg.what) {
				case SEARCH_UPDATE:
					EntryArrayAdapter adapter = new EntryArrayAdapter(getApplicationContext(), R.layout.entry, Director.search);
			        searchView.setAdapter(adapter);
					break;
			}
		}
	};
	
	protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle("Search in " + Director.curr.getPath());
        setContentView(R.layout.search);
        searchView = (ListView) findViewById(R.id.search_file_view);
        searchView.setOnItemClickListener(new ListView.OnItemClickListener() {
        	public void onItemClick(AdapterView<?> parent, View view, int pos, long id) {
        		returnResults(pos);
			}
        });
        searchText = (EditText) findViewById(R.id.search_text);
        searchText.setText(Director.searchTerm);
        searchText.setSelection(searchText.length());
		searchText.setOnKeyListener(new OnKeyListener() {
			public boolean onKey(View v, int keyCode, KeyEvent event) {
				if ((event.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
					Director.search.clear();
					Director.searchTerm = searchText.getText().toString();
					searchText.setText(Director.searchTerm);
		    		EntryArrayAdapter adapter = new EntryArrayAdapter(getApplicationContext(), R.layout.entry, Director.search);
			        searchView.setAdapter(adapter);
					showDialog(PROG_DIALOG);
					searchThread = new Thread(){
						public void run(){
							search(Director.searchTerm);
							handle.sendEmptyMessage(SEARCH_UPDATE);
							dismissDialog(PROG_DIALOG);
						}
					};
					searchThread.start();
					return true;
				}
				return false;
			}
		});
		EntryArrayAdapter adapter = new EntryArrayAdapter(getApplicationContext(), R.layout.entry, Director.search);
        searchView.setAdapter(adapter);
	}
	
	public Dialog onCreateDialog(int id) {
		if(id == PROG_DIALOG){
			ProgressDialog prog = new ProgressDialog(this);
			prog.setMessage("Searching...");
			prog.setIndeterminate(true);
			prog.setCancelable(true);
			return prog;
		}
		return super.onCreateDialog(id);
	}
	
	public boolean onCreateOptionsMenu(Menu menu) {
	    MenuInflater inflater = getMenuInflater();
	    inflater.inflate(R.menu.search_menu, menu);
	    return true;
	}
	
	public boolean onOptionsItemSelected(MenuItem item) {
    	switch(item.getItemId()) {
	    	case R.id.clear_search:
	    		Director.search.clear();
	    		Director.searchTerm = new String();
	    		searchText.setText(new String());
	    		EntryArrayAdapter adapter = new EntryArrayAdapter(getApplicationContext(), R.layout.entry, Director.search);
		        searchView.setAdapter(adapter);
	    		return true;
	    	case R.id.back_search:
	    		finish();
	    		return true;
    	}
    	return false;
    }
	
	protected void onStop() {
		super.onStop();
		if(searchThread != null && searchThread.isAlive())
			searchThread.stop();
	}
	
	/**
	 * Returns the selected entry in the search
	 * @param pos The position of the selected entry in the search
	 */
	private void returnResults(int pos) {
		Intent resultIntent = new Intent(this, History.class);
		resultIntent.putExtra("searchResults", Director.search.get(pos).getFile().getAbsolutePath());
		setResult(Activity.RESULT_OK, resultIntent);
		finish();
	}
	
	/**
	 * Searches for a given search term within the current entry
	 * @param term The search term
	 */
	private void search(String term) {
		if(term.length() == 0)
			return;
		try {
			Pattern pat = Pattern.compile(term);
			searchHelper(pat, Director.curr, 0);
		} catch(PatternSyntaxException e) {
			return;
		}
	}
	
	/**
	 * Search helper method
	 * @param term The search term as a pattern
	 * @param entry The current entry to search from
	 * @param level The current level being searched at
	 */
	private void searchHelper(Pattern pat, Entry entry, int level) {
		if(level == NESTED_FILE_DEPTH)
			return;
		Matcher mat = pat.matcher(entry.getName());
		if(mat.find())
			Director.search.add(entry);
		if(entry.isDirectory() && entry.getAccessable() == Director.ACCESS)
			for(Entry e: entry.getSubFiles())
				searchHelper(pat, e, level + 1);
	}
}
