/**
 * 	File Director -- Information.java
 *
 *      Author: David Jolly 
 *      		[jollyd@onid.oregonstate.edu]
 *
 */

package com.majestic.director.dialog;

import java.util.ArrayList;

import com.majestic.director.Director;
import com.majestic.director.R;
import com.majestic.director.adapter.InfoArrayAdapter;
import com.majestic.director.adapter.InfoEntry;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;

public class Information extends Activity {
	
	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.information);
        ImageView icon = (ImageView) this.findViewById(R.id.file_info_image);
        ListView infoList = (ListView) this.findViewById(R.id.file_info_view);
        Button closeButton = (Button) this.findViewById(R.id.file_info_close_button);
        closeButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				finish();
			}
		});
        SharedPreferences info = this.getSharedPreferences(Director.SHARE_FILE, 0);
        ArrayList<InfoEntry> entries = new ArrayList<InfoEntry>();
        entries.add(new InfoEntry("Title", info.getString("title", "")));
        entries.add(new InfoEntry("Path", info.getString("path", "")));
        entries.add(new InfoEntry("Size", info.getString("size", "")));
        entries.add(new InfoEntry("Last Modified", info.getString("modified", "")));
        entries.add(new InfoEntry("Hidden", info.getString("hidden", "")));
        entries.add(new InfoEntry("Permissions", info.getString("permissions", "")));
        if(info.getBoolean("directory", false))
        	entries.add(new InfoEntry("Contains", info.getString("contains", "")));
        InfoArrayAdapter adapter = new InfoArrayAdapter(getApplicationContext(), R.layout.information, entries);
        infoList.setAdapter(adapter);
        icon.setImageResource(info.getInt("icon", R.drawable.file_ico));
	}
}
