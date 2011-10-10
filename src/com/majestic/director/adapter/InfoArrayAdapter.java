/**
 * 	File Director -- InfoArrayAdapter.java
 *
 *      Author: David Jolly 
 *      		[jollyd@onid.oregonstate.edu]
 *
 */

package com.majestic.director.adapter;

import java.util.ArrayList;
import java.util.List;

import com.majestic.director.R;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class InfoArrayAdapter extends ArrayAdapter<InfoEntry> {

	private List<InfoEntry> fields = new ArrayList<InfoEntry>();
	private TextView title;
	private TextView attrib;
	
	/**
	 * InfoArrayAdapter constructor
	 * @param context Context
	 * @param viewId Integer
	 * @param entries List<Entry>
	 */
	public InfoArrayAdapter(Context context, int viewId, List<InfoEntry> fields) {
		super(context, viewId, fields);
		this.fields = fields;
	}

	public View getView(int position, View view, ViewGroup parent) {
		View row = view;
		if(row == null) {
			LayoutInflater inflater = (LayoutInflater) this.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			row = inflater.inflate(R.layout.information_entry, parent, false);
		}
		InfoEntry field = fields.get(position);
		title = (TextView) row.findViewById(R.id.information_entry_title);
		title.setText(field.getTitle());
		attrib = (TextView) row.findViewById(R.id.information_entry_rel);
		attrib.setText(field.getAttribute());
		return row;
	}
}
