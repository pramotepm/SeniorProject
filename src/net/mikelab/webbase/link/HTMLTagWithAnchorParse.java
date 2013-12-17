package net.mikelab.webbase.link;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.List;

import net.mikelab.webbase.struct.Page;
import net.mikelab.webbase.validate.Serializer;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import com.google.gson.reflect.TypeToken;

public class HTMLTagWithAnchorParse {
	private Path filePath = null;
	private List<Page> pages = new LinkedList<Page>(); 
	
	public HTMLTagWithAnchorParse(String filePath) {
		this.filePath = FileSystems.getDefault().getPath(filePath); 
	}
	
	public HTMLTagWithAnchorParse(Path filePath) {
		this.filePath = filePath;
	}
	
	public List<Page> getPages() {
		return this.pages;
	}
	
	public void writeToStdOut() {
		System.out.println(Serializer.getPrettyGson().toJson(pages));
	}
	
	public void writeToFile(String filePath) {
		Path p = Paths.get(filePath);
		writeToFile(p);
	}
	
	public static List<Page> deserialize(String json) {
		Type listType = new TypeToken<List<Page>>() {}.getType();
		return Serializer.getStandardGson().fromJson(json, listType);
	}
	
	public void writeToFile(Path filePath) {
		try (BufferedWriter w = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(new File(filePath.toString())), StandardCharsets.UTF_8))) {
			w.write(Serializer.getStandardGson().toJson(pages));
			w.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void extract() {
		String URL = null;
		String html = null;
		String line = null;
		System.out.println("Reading... [ " + this.filePath.toString() + " ]");
		try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(new File(this.filePath.toAbsolutePath().toString()))))) {
			line = br.readLine();
			while (line != null) {
				URL = null;
				while (!line.startsWith("URL: http") || line.endsWith("robots.txt")) {
					line = br.readLine();
					if (line == null)
						return;
				}
				URL = line.replaceFirst("URL: ", "");
				html = "";
				while ((line = br.readLine()) != null && !line.startsWith("URL: http")) {
					html += line;
				}	
				Page page = new Page(URL);
				Document doc = Jsoup.parse(html, URL);
				for (Element e : doc.select("a[href]")) {
					String absURLPath = new String(e.attr("abs:href").getBytes(StandardCharsets.UTF_8), StandardCharsets.UTF_8);
					if (absURLPath != null && absURLPath.startsWith("http")) {
						page.add(absURLPath);
					}
				}
				pages.add(page);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}

//String line = null;
//try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(new File(this.filePath.toAbsolutePath().toString()))))) {
//	while ((line = br.readLine()) != null) {
//		if (line.startsWith("URL: http") && !line.endsWith("robots.txt")) {
//			String URL = line.replaceFirst("URL: ", "");
//			Page page = new Page(URL);
//			String html = br.readLine();
//			Document doc = Jsoup.parse(html, URL);
//			for (Element e : doc.select("a[href]")) {
////				String anchorText = e.text();
////				String anchorText = new String(e.text().getBytes(StandardCharsets.UTF_8), StandardCharsets.UTF_8);
//				String absURLPath = new String(e.attr("abs:href").getBytes(StandardCharsets.UTF_8), StandardCharsets.UTF_8);
//				if (absURLPath != null && absURLPath.startsWith("http")) {
//					page.add(absURLPath);
//				}
//			}
//			pages.add(page);
//		}
//	}
//} catch (IOException e) {
//	e.printStackTrace();
//}
