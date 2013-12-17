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
	
	private String normalizeURL(String URL) {
		return URL.trim();
	}
	
	public Page(String sourceURL) {
		this.URL = normalizeURL(sourceURL);
		destination = new HashSet<>();
	}
	
	public void add(String destinationURL) {
		destination.add(normalizeURL(destinationURL));
	}
	
	public String getSourceURL() {
		return this.URL;
	}
		
	public Set<String> getDestinationURL() {
		return this.destination;
	}	
}