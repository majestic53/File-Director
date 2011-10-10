/**
 * 	File Director -- Alert.java
 *
 *      Author: David Jolly 
 *      		[jollyd@onid.oregonstate.edu]
 *
 */

package com.majestic.director.dialog;

import com.majestic.director.Director;
import com.majestic.director.R;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

public class Alert extends Activity {

	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.info);
        SharedPreferences message = this.getSharedPreferences(Director.SHARE_MESSAGE, 0);
        ImageView image = (ImageView) this.findViewById(R.id.info_image);
		image.setImageResource(message.getInt("icon", R.drawable.alert));
		TextView text = (TextView) this.findViewById(R.id.info_text);
		text.setText(message.getString("message", ""));
		Button button = (Button) this.findViewById(R.id.info_close_button);
		button.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				finish();
			}
		});
	}
}
