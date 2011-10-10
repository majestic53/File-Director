/**
 * 	File Director -- NewFolder.java
 *
 *      Author: David Jolly 
 *      		[jollyd@onid.oregonstate.edu]
 *
 */

package com.majestic.director.dialog;

import com.majestic.director.R;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

public class NewFolder extends Activity {
	
	private EditText folderTxt;
	
	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.new_folder);
		folderTxt = (EditText) this.findViewById(R.id.new_folder_text);
		Button newFoldBtn = (Button) this.findViewById(R.id.new_folder_create_button);
		newFoldBtn.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				returnResults();
			}
		});
		Button newFolderCloseButton = (Button) this.findViewById(R.id.new_folder_close_button);
		newFolderCloseButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				finish();
			}
		});
	}
	
	/**
	 * Returns results to the main activity
	 */
	private void returnResults() {
		Intent resultIntent = new Intent(this, NewFolder.class);
		resultIntent.putExtra("newFolderResults", folderTxt.getText().toString());
		setResult(Activity.RESULT_OK, resultIntent);
		finish();
	}
}