package net.mikelab.webbase.content;
import java.util.concurrent.Callable;

import org.jdom2.Element;
import org.jdom2.IllegalDataException;
import org.jdom2.Verifier;

public class ElementCreator implements Callable<ElementCreator> {
	private String site = null;
	private String URL = null;
	private String status = null;
	private String date = null;
	private String modifiedDate = null;
	private String contentLength = null;
	private String content = null;
	private Element element;

	private String contentCleaner(String content) {
		StringBuilder sb = new StringBuilder(); 
		for (String s : content.split("\\s+")) {
			if (Verifier.checkCharacterData(s) == null) {
				sb.append(s);
				sb.append(" ");
			}
		}
		return sb.toString();
	}
	
	public String getSite() {
		return this.site;
	}
	
	public Element getElement() {
		return this.element;
	}
	
	public ElementCreator(String site, String URL, String status, String date, String modifiedDate, String contentLength, String content) {
		this.site = site;
		this.URL = URL;
		this.status = status;
		this.date = date;
		this.modifiedDate = modifiedDate;
		this.contentLength = contentLength;
		this.content = content;
	}
	
	@Override
	public ElementCreator call() throws Exception {
		element = new Element("page");
		try {
			element.addContent(new Element("url").setText(URL));
			element.addContent(new Element("status").setText(status));
			element.addContent(new Element("date").setText(date));
			element.addContent(new Element("modified-date").setText(modifiedDate));
			element.addContent(new Element("content-length").setText(contentLength));
			element.addContent(new Element("content").setText(contentCleaner(content)));
		} catch (IllegalDataException e) { }
		return this;
	}
}