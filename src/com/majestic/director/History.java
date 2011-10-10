/**
 * 	File Director -- History.java
 *
 *      Author: David Jolly 
 *      		[jollyd@onid.oregonstate.edu]
 *
 */

package com.majestic.director;

import com.majestic.director.adapter.HistoryEntryArrayAdapter;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

public class History extends Activity {

	private ListView histView;
	
	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.history);
        histView = (ListView) findViewById(R.id.history_file_view);
        histView.setOnItemClickListener(new ListView.OnItemClickListener() {
        	public void onItemClick(AdapterView<?> parent, View view, int pos, long id) {
        		returnResults(pos);
			}
        });
        HistoryEntryArrayAdapter adapter = new HistoryEntryArrayAdapter(getApplicationContext(), R.layout.entry, Director.history);
        histView.setAdapter(adapter);
	}
	
	public boolean onCreateOptionsMenu(Menu menu) {
	    MenuInflater inflater = getMenuInflater();
	    inflater.inflate(R.menu.history_menu, menu);
	    return true;
	}
	
	public boolean onOptionsItemSelected(MenuItem item) {
    	switch(item.getItemId()) {
	    	case R.id.clear_history:
	    		Director.history.clear();
	    		HistoryEntryArrayAdapter adapter = new HistoryEntryArrayAdapter(getApplicationContext(), R.layout.entry, Director.history);
	            histView.setAdapter(adapter);
	    		return true;
	    	case R.id.back_history:
	    		finish();
	    		return true;
    	}
    	return false;
    }
	
	/**
	 * Returns the selected entry in the history
	 * @param pos The position of the selected entry in the history
	 */
	private void returnResults(int pos) {
		Intent resultIntent = new Intent(this, History.class);
		resultIntent.putExtra("historyResults", Director.history.get(pos).getFile().getAbsolutePath());
		setResult(Activity.RESULT_OK, resultIntent);
		finish();
	}
}

