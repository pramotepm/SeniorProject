import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringEscapeUtils;

public class Analyzer2 {
	public static void main(String[] args) {
		Queue<XMLWriter> XMLqueue = new LinkedList<XMLWriter>();
		Queue<Future<ElementCreator>> elementReturnList = new LinkedList<Future<ElementCreator>>(); 
		
		ExecutorService pool = Executors.newFixedThreadPool(32);
		
		String pageSeperator = "==P=>>>>=i===<<<<=T===>=A===<=!Junghoo!==>";;
		String URL = null;
		String status = null;
		String date = null;
		String modifiedDate = null;
		String contentLength = null;
		StringBuilder content = new StringBuilder();
		Pattern p = Pattern.compile("^.*: .*");
		XMLWriter xmlWriter = null;
		boolean isHeader = true;
		
		try {
			BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(new File(args[0])), "UTF-8"));
			String line = null;
			while ((line = br.readLine()) != null) {
				if (line.equals(pageSeperator)) {
					// page seperator at first line
					if (URL == null)
						continue;
					else {
						String site = URL.replaceFirst("http://", "").split("/")[0];
						if (xmlWriter == null) {
							xmlWriter = new XMLWriter(site, args[1]);
							XMLqueue.add(xmlWriter);
						}
						else if (!xmlWriter.getSite().equals(site)) {
							// xmlWriter.save();
							xmlWriter = new XMLWriter(site, args[1]);
							XMLqueue.add(xmlWriter);
						}
						else {
							String parsedContent = StringEscapeUtils.escapeXml(content.toString());
							if (contentLength == null)
								contentLength = String.valueOf(parsedContent.getBytes().length);
							// xmlWriter.addChild(URL, status, date, modifiedDate, contentLength, parsedContent);
							Callable<ElementCreator> c = new ElementCreator(site, URL, status, date, modifiedDate, contentLength, content.toString());
							Future<ElementCreator> elementReturned = pool.submit(c);
							elementReturnList.add(elementReturned);
						}
					}
					URL = null;
					status = null;
					date = null;
					modifiedDate = null;
					contentLength = null;					
					content = new StringBuilder();
					isHeader = true;
					continue;
				}
				else if (isHeader) {
					if (line.startsWith("URL:")) {
						URL = line.replaceFirst("URL:", "").trim();
						System.out.println(URL);
						if (URL.endsWith("robots.txt"))
							continue;
						// interrupt useless data (header of WebVac crawler)
						br.readLine();
						br.readLine();
						br.readLine();
						// and this is blank line
						br.readLine();

						status = br.readLine();
					}
					if (p.matcher(line).matches()) {
						if (line.startsWith("Last-Modified:"))
							modifiedDate = line.replaceFirst("Last-Modified:", "").trim();
						else if (line.startsWith("Content-Length:"))
							contentLength = line.replaceFirst("Content-Length:", "").trim();
						else if (line.startsWith("Date:"))
							date = line.replaceFirst("Date:", "").trim();
					}
					else
						isHeader = false;
				}
				else
					content.append(line);
			}
			br.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		while (!elementReturnList.isEmpty()) {
			try {
				ElementCreator ec = elementReturnList.poll().get();
				if (!XMLqueue.element().getSite().equals(ec.getSite())) {
					XMLqueue.element().save();
					XMLqueue.remove();
				}
				XMLqueue.element().addElement(ec.getElement());
			} catch (InterruptedException e) {
				e.printStackTrace();
			} catch (ExecutionException e) {
				e.printStackTrace();
			} 
		}
		// xmlWriter.save();
		pool.shutdown();
		// while (!pool.isTerminated());
	}
}