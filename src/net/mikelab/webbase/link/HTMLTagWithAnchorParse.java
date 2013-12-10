package net.mikelab.webbase.link;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.Set;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

public class HTMLTagWithAnchorParse {
	private Set<String> destinationURL = new HashSet<>();
	private String filePath;
	private String lineSeperator = "==<<==>>==SEPERATOR==<<==>>==";
	
	public HTMLTagWithAnchorParse(String filePath) {
		// TODO Auto-generated constructor stub
		this.filePath = filePath; 
	}
	
	public void extractAllLink() {
		try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(new File(this.filePath))));) {
			String line = null;
			while((line = br.readLine()) != null) {
				if (line.startsWith("URL: ") && !line.endsWith("robots.txt")) {
					String URL = line.replaceFirst("URL: ", "");
					String html = br.readLine();
					Document doc = Jsoup.parseBodyFragment(html, URL);
					for (Element e : doc.select("a[href]")) {
						String absURLPath = e.attr("abs:href");
						if (absURLPath != null && absURLPath.startsWith("http")) {
							destinationURL.add(absURLPath);
						}
					}
					for (String s : destinationURL)
						System.out.println(s);
					System.out.println(lineSeperator);
				}
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}  
	}
}