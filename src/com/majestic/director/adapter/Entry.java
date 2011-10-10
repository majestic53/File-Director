/**
 * 	File Director -- Entry.java
 *
 *      Author: David Jolly 
 *      		[jollyd@onid.oregonstate.edu]
 *
 */

package com.majestic.director.adapter;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;

import com.majestic.director.Director;

public class Entry implements Comparable<Entry> {

	/**
	 * Sort by date
	 */
	public class EntrySortByDate implements Comparator<Entry> {

		public int compare(Entry a, Entry b) {
			return a.getDate().compareTo(b.getDate());
		}
	}

	/**
	 * Sort by name
	 */
	public class EntrySortByName implements Comparator<Entry> {

		public int compare(Entry a, Entry b) {
			return a.getName().compareTo(b.getName());
		}
	}

	/**
	 * Sort by size
	 */
	public class EntrySortBySize implements Comparator<Entry> {

		public int compare(Entry a, Entry b) {
			return (int) (b.getSize() - a.getSize());
		}
	}

	/**
	 * Sort by type
	 */
	public class EntrySortByType implements Comparator<Entry> {

		public int compare(Entry a, Entry b) {
			if(a.isDirectory() == b.isDirectory())
				return a.getName().compareTo(b.getName());
			return (a.isDirectory() ? -1 : 1);
		}
	}
	
	/**
	 * Depth to recursively approximate folder size
	 */
	public static final int NESTED_FILE_DEPTH = 1;
	
	private File file;
	private int icon;
	private int accessable;
	private String name;
	
	/**
	 * Entry constructor
	 * @param file The file the entry represents
	 */
	public Entry(File file) {
		this.file = file;
		this.name = file.getName();
		accessable = Director.ACCESS;
		icon = Director.ACCESS_ICON;
    	if(!file.isDirectory()) {
    		accessable = Director.NON_DIR;
    		icon = Director.NON_DIR_ICON;
    	} else if(!file.canRead()) {
    		accessable = Director.RESTRICT;
    		icon = Director.RESTRICT_ICON;
    	}
	}
	
	/**
	 * Entry constructor
	 * @param file The file the entry represents
	 * @param access The current access permissions (see Director)
	 */
	public Entry(File file, int access) {
		this.file = file;
		this.name = file.getName();
		this.accessable = access;
		switch(access) {
			case Director.ACCESS:
				icon = Director.ACCESS_ICON;
				break;
			case Director.NON_DIR:
				icon = Director.NON_DIR_ICON;
				break;
			case Director.RESTRICT:
				icon = Director.RESTRICT_ICON;
				break;
			default:
				icon = Director.ACCESS_ICON;
		}
	}
	
	public int compareTo(Entry e) {
		return getName().compareTo(e.getName());
	}
	
	public boolean equals(Entry e) {
		if(getPath().equals(e.getPath()))
			return true;
		return false;
	}
	
	/**
	 * Retrieve the accessability of the entry
	 * @return The accessability of the entry
	 */
	public int getAccessable() {
		return accessable;
	}
	
	/**
	 * Retrieves the last modified date of the entry
	 * @return The last modified date of the entry
	 */
	public Date getDate() {
		return new Date(getFile().lastModified());
	}
	
	/**
	 * Retrieves the file that the entry represents
	 * @return The file that the entry represents
	 */
	public File getFile() {
		return file;
	}
	
	/**
	 * Retrieves the input channel for the entry
	 * @return The input channel for the entry
	 * @throws FileNotFoundException
	 */
	public FileChannel getFileInChannel() throws FileNotFoundException {
		return new FileInputStream(getFile()).getChannel();
	}
	
	/**
	 * Retrieves the output channel for the entry
	 * @param name The name of the output
	 * @return The output channel for the entry
	 * @throws FileNotFoundException
	 */
	public FileChannel getFileOutChannel(String name) throws FileNotFoundException {
		return new FileOutputStream(new File(getPath() + "/" + name)).getChannel();
	}
	
	/**
	 * Retrieves the entry icon
	 * @return the entry icon
	 */
	public int getIcon() {
		return icon;
	}
	
	/**
	 * Retrieves the entry name
	 * @return The entry name
	 */
	public String getName() {
		return name;
	}
	
	/**
	 * Retrieves a folder size recursively
	 * @param entry The entry to recurse from
	 * @return A folder size
	 */
	public static long getNestedSize(Entry entry) {
		return getNestedSizeHelper(entry, 0);
	}
	
	/**
	 * GetNestedSize helper function
	 * @param entry The entry to recurse from at that level
	 * @param level The depth of the current recursion
	 * @return A folder size
	 */
	private static long getNestedSizeHelper(Entry entry, int level) {
		long size = 0;
		if(level == NESTED_FILE_DEPTH)
			return size;
		if(entry.isDirectory() && entry.getAccessable() == Director.ACCESS) {
			ArrayList<Entry> subFiles = entry.getSubFiles();
    		for(Entry e: subFiles)
    			if(e.isDirectory())
    				size += getNestedSizeHelper(e, level + 1);
    			else
    				size += e.getSize();
		}
		return size;
	}
	
	/**
	 * Retrieves the parent entry of the entry
	 * @return The parent entry of the entry
	 */
	public Entry getParent() {
		return new Entry(file.getParentFile());
	}
	
	/**
	 * Retrieves the path of the entry
	 * @return The path of the entry
	 */
	public String getPath() {
		return file.getAbsolutePath();
	}
	
	/**
	 * Retrieves the permissions of the entry
	 * @return The permissions of the entry
	 */
	public String getPermissions() {
		String permissions = new String();
		if(isDirectory())
			permissions += "d";
		else
			permissions += "-";
		if(getFile().canRead())
			permissions += "r";
		else
			permissions += "-";
		if(getFile().canWrite())
			permissions += "w";
		else
			permissions += "-";
		return permissions;
	}
	
	/**
	 * Rerieves a list of sub-entries of an entry
	 * @return A list of sub-entries of an entry
	 */
	public ArrayList<Entry> getSubFiles() {
		ArrayList<Entry> subFiles = new ArrayList<Entry>();
		File[] files = file.listFiles();
		if(files == null)
			return subFiles;
		for(int i = 0; i < files.length; i++)
			subFiles.add(new Entry(files[i]));
		switch(Director.sortBy) {
			case Director.SORT_BY_TYPE:
				Collections.sort(subFiles, new EntrySortByType());
				break;
			case Director.SORT_BY_NAME:
				Collections.sort(subFiles, new EntrySortByName());
				break;
			case Director.SORT_BY_SIZE:
				Collections.sort(subFiles, new EntrySortBySize());
				break;
			case Director.SORT_BY_DATE:
				Collections.sort(subFiles, new EntrySortByDate());
				break;
		}
		return subFiles;
	}
	
	/**
	 * Retrieves the size of an entry
	 * @return The size of an entry
	 */
	public long getSize() {
		return file.length();
	}
	
	/**
	 * Retrieves the type of an entry
	 * @return The type of an entry
	 */
	public String getType() {
		String type = "File";
		if(isDirectory())
			type = "Directory";
		return type;
	}
	
	/**
	 * Determines if an entry is a director
	 * @return True if directory
	 */
	public boolean isDirectory() {
		return file.isDirectory();
	}
	
	/**
	 * Determines if an entry is a parent of a given entry
	 * @param parent The potential parent entry
	 * @return True if parent
	 */
	public boolean isParent(Entry parent) {
		return getParent().equals(parent);
	}
	
	/**
	 * Determines if an entry is readable
	 * @return True if readable
	 */
	public boolean isReadable() {
		return getFile().canRead();
	}
	
	/**
	 * Determines if an entry is writable
	 * @return True if writable
	 */
	public boolean isWrittable() {
		return getFile().canWrite();
	}
	
	/**
	 * Sets the name of the entry
	 * @param name The name of the entry
	 */
	public void setName(String name) {
		this.name = name;
	}
}
