package net.mikelab.webbase.link;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.LinkedList;
import java.util.List;

import net.mikelab.webbase.struct.Link;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.stream.JsonWriter;

public class HTMLTagWithAnchorParse {
	private String filePath;
	private List<Link> LinkURLs = new LinkedList<Link>();
	
	public HTMLTagWithAnchorParse(String filePath) {
		this.filePath = filePath; 
	}
	
	public void writeToFile(String filePath) throws IOException {
		Gson g = new GsonBuilder().setPrettyPrinting().create();
		Path p = Paths.get(filePath);
		BufferedWriter w = Files.newBufferedWriter(p, StandardCharsets.UTF_8, StandardOpenOption.CREATE);
		JsonWriter jw = new JsonWriter(w);
		jw.setSerializeNulls(true);
		w.write(g.toJson(LinkURLs));
		w.close();
		jw.close();
	}
	
	public void extractAllLink() {
		try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(new File(this.filePath))));) {
			String line = null;
			while((line = br.readLine()) != null) {
				if (line.startsWith("URL: ") && !line.endsWith("robots.txt")) {
					String URL = line.replaceFirst("URL: ", "");
					Link LinkURL = new Link(URL);
					String html = br.readLine();
					Document doc = Jsoup.parseBodyFragment(html, URL);
					for (Element e : doc.select("a[href]")) {
						String anchorText = new String(e.text().getBytes(), "UTF-8");
						String absURLPath = e.attr("abs:href");
						if (absURLPath != null && absURLPath.startsWith("http")) {
							LinkURL.add(absURLPath, anchorText);
						}
					}
					LinkURLs.add(LinkURL);
				}
			}
			
		} catch (IOException e) {
			e.printStackTrace();
		}  
	}
}