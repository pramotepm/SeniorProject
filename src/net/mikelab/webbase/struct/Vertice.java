package net.mikelab.webbase.struct;

import java.util.LinkedList;
import java.util.List;

public class Vertice {
	private String URL = null;
	private String sourceID = null;
	private int inDegree = 0;
	private int outDegree = 0;
	private List<String> outLink = null;
	private List<String> inLink = null;
	
	public Vertice() {
		outLink = new LinkedList<>();
		inLink = new LinkedList<>();
	}
	
 	public String getURL() {
		return URL;
	}
	public void setURL(String uRL) {
		URL = uRL;
	}
	public String getSourceID() {
		return sourceID;
	}
	public void setSourceID(String sourceID) {
		this.sourceID = sourceID;
	}
	public int getInDegree() {
		return inDegree;
	}
	public int getOutDegree() {
		return outDegree;
	}
	public List<String> getInLink() {
		return this.inLink;
	}
	public List<String> getOutLink() {
		return this.outLink;
	}
	public void addInLink(String id) {
		this.inLink.add(id);
		this.inDegree = this.inLink.size();
	}
	public void addOutLink(String id) {
		this.outLink.add(id);
		this.outDegree = this.outLink.size();
	}
}