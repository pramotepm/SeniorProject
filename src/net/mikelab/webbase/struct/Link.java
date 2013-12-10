package net.mikelab.webbase.struct;

import java.util.HashMap;
import java.util.Map;

public class Link {
	/*
	 * input:
	 *   URL = Source URL
	 *   destination = <URL Destination>, <Anchor Text>
	 */
	private String URL;
	private Map<String, String> destination;
	
	public Link(String URL) {
		this.URL = URL;
		destination = new HashMap<String, String>();
	}
	
	public void add(String destinationURL, String anchorText) {
		if (!destination.containsKey(destinationURL))
			destination.put(destinationURL, anchorText);
	}
	
	public String getURL() {
		return this.URL;
	}
	
	public Map<String, String> getDestinations() {
		return this.destination;
	}
}