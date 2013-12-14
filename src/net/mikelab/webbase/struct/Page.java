package net.mikelab.webbase.struct;

import java.util.HashSet;
import java.util.Set;

public class Page {
	/*
	 * input:
	 *   URL = Source URL
	 *   destination = <URL Destination>
	 */
	private String URL;
	private Set<String> destination;
	
	public Page(String sourceURL) {
		this.URL = sourceURL;
		destination = new HashSet<>();
	}
	
	public void add(String destinationURL) {
		destination.add(destinationURL);
	}
	
	public String getSourceURL() {
		return this.URL;
	}
		
	public Set<String> getDestinationURL() {
		return this.destination;
	}	
}