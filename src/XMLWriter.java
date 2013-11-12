import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;

import org.jdom2.Attribute;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.IllegalDataException;
import org.jdom2.Verifier;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;

public class XMLWriter {
	private static AtomicInteger fileCount = new AtomicInteger(0);
	private Document doc;
	private String site;
	private String outputDir;
	
	public XMLWriter(String site, String outputDir) {
		this.site = site;
		this.outputDir = outputDir.endsWith("/") ? outputDir : outputDir + "/";
		Element rootElem = new Element("site");
		rootElem.setAttribute(new Attribute("url", site));
		doc = new Document(rootElem);
	}
	
//	String URL = null;
//	String status = null;
//	String date = null;
//	String modifiedDate = null;
//	String contentLength = null;
	
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
	
	public void addChild(String URL, String status, String date, String modifiedDate, String contentLength, String content) {
		Element subElem = new Element("page");
		try {
			subElem.addContent(new Element("url").setText(URL));
			subElem.addContent(new Element("status").setText(status));
			subElem.addContent(new Element("date").setText(date));
			subElem.addContent(new Element("modified-date").setText(modifiedDate));
			subElem.addContent(new Element("content-length").setText(contentLength));
			subElem.addContent(new Element("content").setText(contentCleaner(content)));
			doc.getRootElement().addContent(subElem);
		} catch (IllegalDataException e) {
			doc = null;
		}
	}
	
	public void addElement(Element m) {
		doc.getRootElement().addContent(m);
	}
	
	public void save() {
		if (doc != null) {
			try {
				XMLOutputter out = new XMLOutputter(Format.getPrettyFormat());
				out.output(doc, new FileOutputStream(new File(outputDir + fileCount.getAndIncrement() + ".xml")));
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}