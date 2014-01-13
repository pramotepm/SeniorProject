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
import net.mikelab.webbase.utils.Serializer;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import com.google.gson.reflect.TypeToken;

public class HTMLLinkParser {
	private Path filePath = null;
	private List<Page> pages = new LinkedList<Page>(); 
	
	public HTMLLinkParser(String filePath) {
		this.filePath = FileSystems.getDefault().getPath(filePath); 
	}
	
	public HTMLLinkParser(Path filePath) {
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
	
	private List<Integer> findDelimeterLine() {
		List<Integer> startLine = new LinkedList<>();
		int line_number = 1;
		String line = null;
		try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(new File(this.filePath.toAbsolutePath().toString()))))) {
			while ((line = br.readLine()) != null) {
				if (line.trim().startsWith("URL: "))
					startLine.add(line_number);
				line_number++;
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return startLine;
	}
	
	public void extract() {
		String line = "";
		System.out.println("Reading... [ " + this.filePath.toString() + " ]");
		List<Integer> startAtLine = findDelimeterLine();
		int line_number = 0;
		int idx = 0;
		if (startAtLine.size() != 0) {
			try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(new File(this.filePath.toAbsolutePath().toString()))))) {
				while (line != null) {
					String URL = null;
					int startLine = startAtLine.get(idx);
					int endLine = -1;
					if (idx + 1 < startAtLine.size()) {
						endLine = startAtLine.get(idx + 1);
						idx++;
					}
					
					/*************** Find a source URL ***********/ 
					while (line_number != startLine) {
						line = br.readLine();
						line_number++;
					}
					URL = line.replaceFirst("URL: ", "");
					/**********************************************/
					
					/**************** Find a content *************/
					StringBuilder html = new StringBuilder();
					if (endLine != -1) {
						while (line_number != (endLine-1)) {
							html.append(br.readLine());
							line_number++;
						}
					}
					else {
						while ((line = br.readLine()) != null) {
							html.append(line);
						}
					}
					/*********************************************/
					
					if (!URL.endsWith("robots.txt") && URL.startsWith("http")) {
						Page page = new Page(URL);
						Document doc = Jsoup.parse(html.toString(), URL);
						for (Element e : doc.select("a[href]")) {
							String absURLPath = new String(e.attr("abs:href").getBytes(StandardCharsets.UTF_8), StandardCharsets.UTF_8);
							if (absURLPath != null && absURLPath.startsWith("http")) {
								page.add(absURLPath);
							}
						}
						pages.add(page);
					}
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}	
}