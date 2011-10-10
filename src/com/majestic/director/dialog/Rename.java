/**
 * 	File Director -- Rename.java
 *
 *      Author: David Jolly 
 *      		[jollyd@onid.oregonstate.edu]
 *
 */

package com.majestic.director.dialog;

import com.majestic.director.Director;
import com.majestic.director.R;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

public class Rename extends Activity {
	
	private EditText renameTxt;
	
	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.rename);
        SharedPreferences rename = this.getSharedPreferences(Director.SHARE_ENTRY, 0);
        renameTxt = (EditText) this.findViewById(R.id.rename_text);
		renameTxt.setText(rename.getString("name", ""));
		renameTxt.setSelection(renameTxt.length());
		Button renameButton = (Button) this.findViewById(R.id.rename_rename_button);
		renameButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				returnResults();
			}
		});
		Button closeButton = (Button) this.findViewById(R.id.rename_close_button);
		closeButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				finish();
			}
		});
	}
	
	/**
	 * Returns results to the main activity
	 */
	private void returnResults() {
		Intent resultIntent = new Intent(this, Rename.class);
		resultIntent.putExtra("renameResults", renameTxt.getText().toString());
		setResult(Activity.RESULT_OK, resultIntent);
		finish();
	}
}
