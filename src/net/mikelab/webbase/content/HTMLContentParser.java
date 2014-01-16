package net.mikelab.webbase.content;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.DirectoryStream;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;

public class HTMLContentParser {
	private Path contentsPath;
	private Path changingHistoryFile;
	private final String pageSeperator = "==P=>>>>=i===<<<<=T===>=A===<=!Junghoo!==>";
	
	public HTMLContentParser(String contentDirectory, String changingHistotyFilePath) {
		this.contentsPath = FileSystems.getDefault().getPath(contentDirectory);
		this.changingHistoryFile = FileSystems.getDefault().getPath(changingHistotyFilePath);
	}
	
	public void extract() {
		try (DirectoryStream<Path> ds = Files.newDirectoryStream(FileSystems.getDefault().getPath(this.contentsPath.toAbsolutePath().toString()))) {
			for (Path contentPath : ds) {
				try(BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(new File(contentPath.toAbsolutePath().toString()))));
					BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(new File(this.changingHistoryFile.toAbsolutePath().toString())), StandardCharsets.UTF_8))) {
						String URL = null;
						String line = null;
						StringBuilder sb = new StringBuilder();
						while ((line = br.readLine()) != null) {
							if (line.equals(pageSeperator)) {
								if (URL != null && !URL.endsWith("robots.txt")) {
									int contentLength = sb.toString().replaceAll("\\s+", " ").getBytes().length;
									bw.write(URL + " " + contentLength + "\n");
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
		} catch (IOException e) {
			e.printStackTrace();
		}		
	}
}
