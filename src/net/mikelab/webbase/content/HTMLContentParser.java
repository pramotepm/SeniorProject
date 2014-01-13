package net.mikelab.webbase.content;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.FileSystems;
import java.nio.file.Path;

public class HTMLContentParser {
	private Path filePath;
	private final String pageSeperator = "==P=>>>>=i===<<<<=T===>=A===<=!Junghoo!==>";
	
	public HTMLContentParser(String filePath) {
		this.filePath = FileSystems.getDefault().getPath(filePath);
	}
	
	public void extract() {
		try(BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(new File(this.filePath.toAbsolutePath().toString()))))) {
			int c = 1;
			String URL = null;
			String line = null;
			StringBuilder sb = new StringBuilder();
			while ((line = br.readLine()) != null) {
				if (line.equals(pageSeperator)) {
					if (URL != null && !URL.endsWith("robots.txt")) {
						//System.out.println(URL + " " + contentLength);
						System.out.print(c++ + ": " + URL + " ");
						
						int contentLength = sb.toString().replaceAll("\\s+", " ").getBytes().length;
						System.out.println(contentLength);
					}
					sb = new StringBuilder();
					URL = null;
				}
				else {
					if (line.startsWith("URL: ")) {
						URL = line.replace("URL: ", "");					
						/***** Interrupt Unused Data *****/
						for (int i=0;i<5;i++)
							br.readLine();
						while (!br.readLine().equals(""));
						/********************************/
					}
					else {
						sb.append(line);
					}
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
