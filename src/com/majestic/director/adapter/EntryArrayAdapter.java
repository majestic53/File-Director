/**
 * 	File Director -- EntryArrayAdapter.java
 *
 *      Author: David Jolly 
 *      		[jollyd@onid.oregonstate.edu]
 *
 */

package com.majestic.director.adapter;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import com.majestic.director.R;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class EntryArrayAdapter extends ArrayAdapter<Entry> {

	private List<Entry> entries = new ArrayList<Entry>();
	private ImageView accessIcon;
	private TextView fileName;
	private TextView fileSize;
	private TextView filePerm;
	
	/**
	 * EntryArrayAdapter constructor
	 * @param context Context
	 * @param viewId Integer
	 * @param entries List<Entry>
	 */
	public EntryArrayAdapter(Context context, int viewId, List<Entry> entries) {
		super(context, viewId, entries);
		this.entries = entries;
	}
	
	public View getView(int position, View view, ViewGroup parent) {
		View row = view;
		if(row == null) {
			LayoutInflater inflater = (LayoutInflater) this.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			row = inflater.inflate(R.layout.entry, parent, false);
		}
		Entry entry = entries.get(position);
		accessIcon = (ImageView) row.findViewById(R.id.access_icon);
		accessIcon.setImageResource(entry.getIcon());
		fileName = (TextView) row.findViewById(R.id.file_name);
		fileName.setText(entry.getName());
		fileSize = (TextView) row.findViewById(R.id.file_size);
		if(entry.isDirectory())
			fileSize.setText(new DecimalFormat("#0.00").format(Entry.getNestedSize(entry) / 1000.0) + " KB");
		else
			fileSize.setText(new DecimalFormat("#0.00").format(entry.getSize() / 1000.0) + " KB");
		filePerm = (TextView) row.findViewById(R.id.file_perm);
		filePerm.setText(entry.getPermissions());
		return row;
	}
}