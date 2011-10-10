/**
 * 	File Director -- InfoEntry.java
 *
 *      Author: David Jolly 
 *      		[jollyd@onid.oregonstate.edu]
 *
 */

package com.majestic.director.adapter;

public class InfoEntry {

	private String title;
	private String attrib;
	
	/**
	 * InfoEntry constructor
	 * @param title The entry title
	 * @param attrib The entry attribute
	 */
	public InfoEntry(String title, String attrib) {
		this.title = title;
		this.attrib = attrib;
	}
	
	/**
	 * Retrieves the entry attribute
	 * @return The entry attribute
	 */
	public String getAttribute() {
		return attrib;
	}
	
	/**
	 * Retrieves the entry title
	 * @return The entry title
	 */
	public String getTitle() {
		return title;
	}
}
