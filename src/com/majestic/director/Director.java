/**
 * 	File Director -- Director.java
 *
 *      Author: David Jolly 
 *      		[jollyd@onid.oregonstate.edu]
 *
 */

package com.majestic.director;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import com.majestic.director.adapter.Entry;
import com.majestic.director.adapter.EntryArrayAdapter;
import com.majestic.director.dialog.Alert;
import com.majestic.director.dialog.Information;
import com.majestic.director.dialog.NewFolder;
import com.majestic.director.dialog.Rename;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

public class Director extends Activity {
	
	/**
	 * Context menu options
	 */
	public static final int ENTER_POS = 0;
	public static final int NEW_FOLD_POS = 1;
	public static final int INFO_POS = 2;
	public static final int CUT_POS = 3;
	public static final int COPY_POS = 4;
	public static final int PASTE_POS = 5;
	public static final int RENAME_POS = 6;
	public static final int DELETE_POS = 7;
	
	/**
	 * Sorting options
	 */
	public static final int SORT_BY_TYPE = 0;
	public static final int SORT_BY_NAME = 1;
	public static final int SORT_BY_SIZE = 2;
	public static final int SORT_BY_DATE = 3;
	
	/**
	 * File types
	 */
	public static final byte ACCESS = 0;
	public static final byte RESTRICT = 1;
	public static final byte NON_DIR = 2;
	
	/**
	 * File icons
	 */
	public static final int ACCESS_ICON = R.drawable.ok_ico;
	public static final int RESTRICT_ICON = R.drawable.restrict_ico;
	public static final int NON_DIR_ICON = R.drawable.file_ico;
	
	/**
	 * Dialogs
	 */
	public static final int INFORMATION_DIALOG = 0;
	public static final int RENAME_DIALOG = 1;
	public static final int NEW_FOLDER_DIALOG = 2;
	public static final int ALERT_DIALOG = 3;
	public static final int HISTORY_DIALOG = 4;
	public static final int SEARCH_DIALOG = 5;
	public static final int PROG_DIALOG = 6;
	
	/**
	 * Handler messages
	 */
	public static final int DELETE_SUCCESS = 0;
	public static final int DELETE_FAILURE = 1;
	public static final int PASTE_SUCCESS = 2;
	public static final int PASTE_FAILURE = 3;
	public static final int LOAD_SUCCESS = 4;
	public static final int LOAD_FAILURE = 5;
	
	/**
	 * History parameters
	 */
	public static final int MAX_HIST_SIZE = 20;
	
	/**
	 * Shared Properties
	 */
	public static final String SHARE_ENTRY = "ENTRY";
	public static final String SHARE_FILE = "FILE_INFO";
	public static final String SHARE_MESSAGE = "MESSAGE";
	
	/**
	 * View parameters
	 */
	public static final String PARENT = "..";
	public static final int FRONT = 0;
	
	public static int sortBy;
	public boolean atRoot;
	public boolean pasteable;
	public boolean cutMode;
	public static Entry curr;
	public static Entry root;
	public static Entry sdCard;
	public Entry pasteEntry;
	public ListView fileView;
	public static String searchTerm;
	public List<Entry> entries = new ArrayList<Entry>();
	public static List<Entry> history = new ArrayList<Entry>();
	public static List<Entry> search = new ArrayList<Entry>();
	
	private Handler handle = new Handler() {
		
		public void handleMessage(Message msg) {
			switch(msg.what) {
				case DELETE_SUCCESS:
					updateView(curr);
					Toast.makeText(Director.this, "File(s) deleted", Toast.LENGTH_SHORT).show();
					break;
				case DELETE_FAILURE:
					showAlertDialog("Failed to delete file(s)", R.drawable.alert);
					break;
				case PASTE_SUCCESS:
					pasteable = false;
					cutMode = false;
					updateView(curr);
					Toast.makeText(fileView.getContext(), "File(s) pasted", Toast.LENGTH_SHORT).show();
					break;
				case PASTE_FAILURE:
					showAlertDialog("Failed to paste file(s)", R.drawable.alert);
					break;
				case LOAD_SUCCESS:
					EntryArrayAdapter adapter = new EntryArrayAdapter(getApplicationContext(), R.layout.entry, entries);
			        fileView.setAdapter(adapter);
			        setTitle(curr.getPath());
					break;
				case LOAD_FAILURE:
					showAlertDialog("Failed to load file(s)", R.drawable.alert);
					break;
			}
		}
	};
	
	/**
	 * Creates a new folder within a given entry
	 * @param name The name of the new folder
	 * @param entry The entry to place it in
	 * @return True on success
	 */
	public boolean createNewEntry(String name, Entry entry) {
		File newFile = new File(entry.getPath() + "/" + name);
		newFile.mkdir();
		return true;
	}
	
	/**
	 * Deletes an entry
	 * @param entry The entry to be deleted
	 * @return True on success
	 */
	public boolean deleteEntry(final Entry entry) {
		if(entry.isDirectory()) {
			ArrayList<Entry> children = entry.getSubFiles();
			for(Entry child: children)
				deleteEntry(child);
		}
		entry.getFile().delete();
		return true;
	}
	
	/**
	 * Retrieves various information about a given entry and places it in a shared object
	 * @param entry The entry to retrieve information from
	 */
	public void getEntryInformation(Entry entry) {
		int files = 0, directories = 0;
    	ArrayList<Entry> subFiles;
		SharedPreferences.Editor fileInfo = this.getSharedPreferences(SHARE_FILE, 0).edit();
		fileInfo.putBoolean("directory", entry.isDirectory());
		fileInfo.putInt("icon", entry.getIcon());
		fileInfo.putString("title", entry.getName());
		fileInfo.putString("path", entry.getPath());
		long size = 0;
		if(entry.isDirectory())
			size = Entry.getNestedSize(entry);
		else
			size = entry.getSize();
		fileInfo.putString("size", new DecimalFormat("#0.00").format(size / 1000.0) + " KB (" + size + " bytes)");
    	fileInfo.putString("modified", entry.getDate().toGMTString());
    	fileInfo.putString("hidden", entry.getFile().isHidden() ? "Yes" : "No");
    	fileInfo.putString("permissions", entry.getPermissions());
    	if(entry.isDirectory() && entry.getAccessable() == ACCESS) {
    		subFiles = entry.getSubFiles();
    		for(Entry e: subFiles)
    			if(e.isDirectory())
    				directories++;
    			else
    				files++;
    		fileInfo.putString("contains", files + " files, " + directories + " sub-directories");
    	}
    	fileInfo.commit();
	}
	
	/**
	 * Returns whether a given entry exists in the history
	 * @param entry The entry to check for
	 * @return True if the entry exists in the history
	 */
	public boolean historyContains(Entry entry) {
		for(Entry e: history)
			if(e.getFile().getAbsolutePath().equals(entry.getFile().getAbsolutePath()))
				return true;
		return false;
	}
	
	/**
	 * Moves a given entry to a destination
	 * @param src The entry to move
	 * @param dest The destination
	 * @return True on success
	 * @throws IOException
	 */
	private boolean moveEntryTo(Entry src, Entry dest) throws IOException {
		FileChannel input;
		FileChannel output;
		Entry target = new Entry(new File(dest.getPath() + "/" + src.getName()));
		if(src.isDirectory()) {
			if(!target.getFile().exists())
				target.getFile().mkdir();
			ArrayList<Entry> children = src.getSubFiles();
			for(Entry child: children)
				moveEntryTo(child, target);	
		} else {
			try {
				input = src.getFileInChannel();
				output = dest.getFileOutChannel(src.getName());
			} catch (FileNotFoundException e) {
				return false;
			}
			try {
				input.transferTo(0, input.size(), output);
			} finally {
				if(input != null)
					input.close();
				if(output != null)
					output.close();
			}
		}
		return true;
	}
	
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		switch(requestCode) {
			case RENAME_DIALOG:
				if(resultCode == Activity.RESULT_OK) {
					String rename = data.getStringExtra("renameResults");
					SharedPreferences pathName = this.getSharedPreferences(SHARE_ENTRY, 0);
					String path = pathName.getString("path", "");
					if(rename.equals(new String())) {
						showAlertDialog("Filename cannot be empty", R.drawable.alert);
						return;
					}
					if(renameEntry(rename, new Entry(new File(path))))
						Toast.makeText(this, "Renamed to " + rename, Toast.LENGTH_SHORT).show();
					else
						showAlertDialog(new File(path).getName() + " could not be renamed", R.drawable.alert);
					updateView(curr);
				}
				break;
			case NEW_FOLDER_DIALOG:
				if(resultCode == Activity.RESULT_OK) {
					String newFolder = data.getStringExtra("newFolderResults");
					SharedPreferences pathName = this.getSharedPreferences(SHARE_ENTRY, 0);
					String path = pathName.getString("path", "");
					if(newFolder.equals(new String())) {
						showAlertDialog("Folder name cannot be empty", R.drawable.alert);
						return;
					}
					createNewEntry(newFolder, new Entry(new File(path)));
					Toast.makeText(this, newFolder + " was created under " + new File(path).getName(), Toast.LENGTH_SHORT).show();
				}
				break;
			case HISTORY_DIALOG:
				if(resultCode == Activity.RESULT_OK) {
					String path = data.getStringExtra("historyResults");
					Entry entry = new Entry(new File(path));
					if(entry.isDirectory())
						updateView(entry);
					else {
						updateView(entry.getParent());
						updateView(entry);
					}
				}
				break;
			case SEARCH_DIALOG:
				if(resultCode == Activity.RESULT_OK) {
					String path = data.getStringExtra("searchResults");
					Entry entry = new Entry(new File(path));
					if(entry.isDirectory())
						updateView(entry);
					else {
						updateView(entry.getParent());
						updateView(entry);
					}
				}
				break;
		}
	}
	
	public void onBackPressed() {
		if(!atRoot)
    		updateView(curr.getParent());
    	else
    		Toast.makeText(fileView.getContext(), "At root", Toast.LENGTH_SHORT).show();
	}
	
	public boolean onContextItemSelected(MenuItem item) {
    	final AdapterView.AdapterContextMenuInfo itemInfo = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
    	final Entry entry = entries.get(itemInfo.position);
    	switch(item.getItemId()) {
    		case ENTER_POS:
    			updateView(entry);
    			return true;
			case NEW_FOLD_POS:
				showNewFolderDialog(entry);
				return true;
    		case INFO_POS:
    			getEntryInformation(entry);
    			showActivity(Information.class, INFORMATION_DIALOG);
    			return true;
    		case CUT_POS:
    			pasteEntry = entry;
				cutMode = true;
				pasteable = true;
				Toast.makeText(fileView.getContext(), entry.getName() + " will be cut", Toast.LENGTH_SHORT).show();
    			return true;
    		case COPY_POS:
    			pasteEntry = entry;
    			cutMode = false;
    			pasteable = true;
    			Toast.makeText(fileView.getContext(), entry.getName() + " will by copied", Toast.LENGTH_SHORT).show();
    			return true;
    		case PASTE_POS:
    			showDialog(PROG_DIALOG);
				new Thread(){
					public void run(){
						if(pasteEntry(entry))
							handle.sendEmptyMessage(PASTE_SUCCESS);
						else
							handle.sendEmptyMessage(PASTE_FAILURE);
						dismissDialog(Director.PROG_DIALOG);
					}
				}.start();
    			return true;
    		case RENAME_POS:
    			showRenameDialog(entry);
    			return true;
    		case DELETE_POS:
    			showDeleteDialog(entry);
    			return true;
    	}
    	return false;
    }
	
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        sortBy = SORT_BY_TYPE;
        pasteable = false;
        cutMode = false;
        searchTerm = new String();
        root = new Entry(Environment.getRootDirectory().getParentFile());
        sdCard = new Entry(Environment.getExternalStorageDirectory());
        fileView = (ListView) findViewById(R.id.file_view);
        registerForContextMenu(fileView);
        fileView.setOnItemClickListener(new ListView.OnItemClickListener() {
        	public void onItemClick(AdapterView<?> parent, View view, int pos, long id) {
				updateView(entries.get(pos));
			}
        });
        curr = (Entry) getLastNonConfigurationInstance();
        if(curr == null)
        	curr = root;
        updateView(curr);
    }
    
    public void onCreateContextMenu(ContextMenu menu, View view, ContextMenu.ContextMenuInfo info) {
    	if(view.getId() == R.id.file_view) {
    		AdapterView.AdapterContextMenuInfo contextMenu = (AdapterView.AdapterContextMenuInfo) info;
    		Entry entry = entries.get(contextMenu.position);
    		menu.setHeaderTitle(entry.getName());
    		menu.add(Menu.NONE, ENTER_POS, ENTER_POS, "Enter Folder");
    		menu.add(Menu.NONE, NEW_FOLD_POS, NEW_FOLD_POS, "Create New Subfolder");
    		menu.add(Menu.NONE, INFO_POS, INFO_POS, "Information");
    		menu.add(Menu.NONE, CUT_POS, CUT_POS, "Cut");
    		menu.add(Menu.NONE, COPY_POS, COPY_POS, "Copy");
    		menu.add(Menu.NONE, PASTE_POS, PASTE_POS, "Paste Into");
    		menu.add(Menu.NONE, RENAME_POS, RENAME_POS, "Rename");
    		menu.add(Menu.NONE, DELETE_POS, DELETE_POS, "Delete");
    		menu.getItem(ENTER_POS).setVisible(false);
    		menu.getItem(NEW_FOLD_POS).setVisible(false);
    		menu.getItem(CUT_POS).setVisible(false);
    		menu.getItem(COPY_POS).setVisible(false);
    		menu.getItem(PASTE_POS).setVisible(false);
    		menu.getItem(RENAME_POS).setVisible(false);
    		menu.getItem(DELETE_POS).setVisible(false);
    		if(entry.isDirectory() && entry.getAccessable() == ACCESS) {
    			menu.getItem(ENTER_POS).setVisible(true);
    			if(entry.isReadable())
	        		menu.getItem(COPY_POS).setVisible(true);
    			if(entry.isWrittable()) {
    				menu.getItem(NEW_FOLD_POS).setVisible(true);
    				menu.getItem(CUT_POS).setVisible(true);
	        		if(pasteable)
	        			menu.getItem(PASTE_POS).setVisible(true);
	        		menu.getItem(RENAME_POS).setVisible(true);
	        		menu.getItem(DELETE_POS).setVisible(true);
    			}
    		} else if(entry.isReadable() && entry.getAccessable() == NON_DIR) {
    			if(entry.isReadable())
	    			menu.getItem(COPY_POS).setVisible(true);
    			if(entry.isWrittable()) {
    				menu.getItem(CUT_POS).setVisible(true);
	    			menu.getItem(RENAME_POS).setVisible(true);
	    			menu.getItem(DELETE_POS).setVisible(true);
    			}
    		}
    	}
    }
    
    public Dialog onCreateDialog(int id) {
		if(id == PROG_DIALOG){
			ProgressDialog prog = new ProgressDialog(this);
			prog.setMessage("Working...");
			prog.setIndeterminate(true);
			prog.setCancelable(true);
			return prog;
		}
		return super.onCreateDialog(id);
	}
    
    public boolean onCreateOptionsMenu(Menu menu) {
	    MenuInflater inflater = getMenuInflater();
	    inflater.inflate(R.menu.menu, menu);
	    return true;
	}
    
    public boolean onOptionsItemSelected(MenuItem item) {
    	switch(item.getItemId()) {
	    	case R.id.sort:
	    		showSortDialog();
	    		return true;
	    	case R.id.hist:
	    		showActivity(History.class, HISTORY_DIALOG);
	    		return true;
	    	case R.id.search:
	    		showActivity(Search.class, SEARCH_DIALOG);
	    		return true;
    		case R.id.up:
    			if(!atRoot)
    	    		updateView(curr.getParent());
    	    	else
    	    		Toast.makeText(fileView.getContext(), "At root", Toast.LENGTH_SHORT).show();
	    		return true;
	    	case R.id.to_root:
	    		if(!atRoot)
	    			updateView(root);
	    		else
	    			Toast.makeText(fileView.getContext(), "At root", Toast.LENGTH_SHORT).show();
	    		return true;
	    	case R.id.to_sd:
	    		updateView(sdCard);
	    		return true;
    	}
    	return false;
    }
    
    public Object onRetainNonConfigurationInstance() {
        final Entry curr = Director.curr;
        return curr;
    }
    
    public boolean onSearchRequested() {
    	showActivity(Search.class, SEARCH_DIALOG);
    	return true;
    }
    
    /**
     * Pastes given entry into a folder
     * @param entry The folder to paste into
     * @return True on success
     */
    public boolean pasteEntry(Entry entry) {
		try {
			moveEntryTo(pasteEntry, entry);
			if(cutMode)
				deleteEntry(pasteEntry);
		} catch (IOException e) {
			showAlertDialog("IO Exception encountered while pasting files" + e.getMessage(), R.drawable.alert);
			return false;
		}
    	return true;
    }
    
    /**
     * Renames a given entry
     * @param name The new name for the entry
     * @param entry The entry to rename
     * @return True on success
     */
    public boolean renameEntry(String name, Entry entry) {
    	File oldFile = entry.getFile();
		File newFile = new File(oldFile.getParent() + "/" + name);
		return oldFile.renameTo(newFile);
    }
    
    /**
     * Runs an activity
     * @param cls The activity to run
     */
    public void showActivity(Class<?> cls) {
    	Intent intent = new Intent(this, cls);
    	startActivity(intent);
    }
    
    /**
     * Runs an activity and catches the request code
     * @param cls The activity to run
     * @param requestCode The request code
     */
    public void showActivity(Class<?> cls, int requestCode) {
		Intent intent = new Intent(this, cls);
		startActivityForResult(intent, requestCode);
	}
    
    /**
     * Shows an alert dialog
     * @param message The message to display
     * @param iconId The icon id to display
     */
    public void showAlertDialog(String message, int iconId) {
    	SharedPreferences.Editor alertEdit = getSharedPreferences(SHARE_MESSAGE, 0).edit();
    	alertEdit.putInt("icon", iconId);
    	alertEdit.putString("message", message);
    	alertEdit.commit();
    	showActivity(Alert.class, ALERT_DIALOG);
    }
    
    /**
     * Shows the delete confirmation dialog
     * @param entry The entry to be deleted
     */
    public void showDeleteDialog(final Entry entry) {
    	AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage("Are you sure you want to delete " + entry.getName() + "?");
		builder.setCancelable(false);
		builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
				dialog.dismiss();
				showDialog(PROG_DIALOG);
				new Thread(){
					public void run(){
						if(deleteEntry(entry))
							handle.sendEmptyMessage(DELETE_SUCCESS);
						else
							handle.sendEmptyMessage(DELETE_FAILURE);
						dismissDialog(Director.PROG_DIALOG);
					}
				}.start();
			}
		});
		builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
				dialog.dismiss();
			}
		});
		AlertDialog alert = builder.create();
		alert.show();
    }
    
    /**
     * Shows the new folder dialog
     * @param entry The folder to create new folder in
     */
    public void showNewFolderDialog(final Entry entry) {
    	SharedPreferences.Editor newFolderEdit = getSharedPreferences(SHARE_ENTRY, 0).edit();
    	newFolderEdit.putString("name", entry.getName());
    	newFolderEdit.putString("path", entry.getFile().getAbsolutePath());
    	newFolderEdit.commit();
    	showActivity(NewFolder.class, NEW_FOLDER_DIALOG);
    }
    
    /**
     * Shows the rename dialog
     * @param entry The entry to rename
     */
    public void showRenameDialog(final Entry entry) {
    	SharedPreferences.Editor renameEdit = getSharedPreferences(SHARE_ENTRY, 0).edit();
		renameEdit.putString("name", entry.getName());
		renameEdit.putString("path", entry.getFile().getAbsolutePath());
		renameEdit.commit();
    	showActivity(Rename.class, RENAME_DIALOG);
    }
    
    /**
     * Shows the sort dialog
     */
    public void showSortDialog() {
    	final CharSequence[] items = {"Type", "Name", "Size", "Date"};
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle("Sort Files By");
		builder.setSingleChoiceItems(items, sortBy, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int item) {
				sortBy = item;
				updateView(curr);
				dialog.dismiss();
			}
		});
		AlertDialog alert = builder.create();
		alert.show();
    }
    
    /**
     * Updates the current file view
     * @param dir The current entry
     * @return True on success
     */
    public boolean updateView(final Entry dir) {
    	if(dir == null || !dir.isDirectory()) {
    		int pos = -1;
    		for(int i = 0; i < entries.size(); i++)
    			if(entries.get(i).equals(dir)) {
    				pos = i;
    				break;
    			}
    		if(pos < 0 || pos >= entries.size()) {
    			Toast.makeText(fileView.getContext(), dir.getPath() + " was not found.", Toast.LENGTH_SHORT).show();
    			return false;
    		}
    		getEntryInformation(dir);
			showActivity(Information.class, INFORMATION_DIALOG);
    		return true;
    	}
    	if(dir.getAccessable() != ACCESS) {
    		Toast.makeText(fileView.getContext(), "Could not access " + dir.getPath(), Toast.LENGTH_SHORT).show();
    		return false;
    	}
    	//showDialog(PROG_DIALOG);
    	new Thread() {
			public void run() {
				entries = dir.getSubFiles();
		        if(!dir.equals(root)) {
			        entries.add(FRONT, dir.getParent());
			        entries.get(FRONT).setName(PARENT);
		        }
		        if(entries.size() == 0) {
		        	entries = curr.getSubFiles();
		        	Toast.makeText(fileView.getContext(), dir.getPath() + " is empty", Toast.LENGTH_SHORT).show();
		        	handle.sendEmptyMessage(LOAD_FAILURE);
		        }
		        curr = dir;
		        if(curr.equals(root))
		    		atRoot = true;
		    	else
		    		atRoot = false;
		        if(!historyContains(curr)) {
					if(history.size() >= MAX_HIST_SIZE)
						history.remove(0);
	            	history.add(curr);
				}
				handle.sendEmptyMessage(LOAD_SUCCESS);
				//dismissDialog(Director.PROG_DIALOG);
			}
    	}.start();
    	return true;
    }
}