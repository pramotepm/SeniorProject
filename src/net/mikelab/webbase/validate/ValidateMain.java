package net.mikelab.webbase.validate;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.DirectoryStream;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import net.mikelab.webbase.link.HTMLTagWithAnchorParse;
import net.mikelab.webbase.link.worker.GenerateIndex;
import net.mikelab.webbase.struct.Page;
import net.mikelab.webbase.struct.Vertice;
import net.mikelab.webbase.utils.ConcurrencyFileWriter;

public class ValidateMain {
	private static Map<String, String> index = new HashMap<>();
	
	private static void writeMetaData(String directoryOfLinkDownload, String directoryOfMetaDataFile) {
		Path metaDataPath = FileSystems.getDefault().getPath(directoryOfMetaDataFile);
		if (!Files.isDirectory(metaDataPath)) {
			try {
				Files.createDirectory(metaDataPath);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		try (DirectoryStream<Path> ds = Files.newDirectoryStream(FileSystems.getDefault().getPath(directoryOfLinkDownload))) {
			for (Path p : ds) {
				String fileName = "meta-link-" + p.getFileName().toString();
				HTMLTagWithAnchorParse parser = new HTMLTagWithAnchorParse(p.toAbsolutePath().toString());
				parser.extract();
				System.out.print(String.format("Writing... [%s] ", fileName));
				parser.writeToFile(metaDataPath.resolve(fileName));
				System.out.println("Done.");
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private static void createIndexFile(String pathOfMetaDataFile, String pathOfIndexFile) {
		ConcurrencyFileWriter cfw = new ConcurrencyFileWriter(pathOfIndexFile);
		AtomicInteger indexNumber = new AtomicInteger(0);
		Path path = FileSystems.getDefault().getPath(pathOfMetaDataFile);
		try (DirectoryStream<Path> ds = Files.newDirectoryStream(path)) {
			ExecutorService pool = Executors.newFixedThreadPool(8);
			for (Path _page : ds) {
				GenerateIndex c = new GenerateIndex(_page, indexNumber, cfw);
				pool.execute(c);
			}
			pool.shutdown();
			while (!pool.isTerminated());
			cfw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private static void readIndexFile(String pathOfIndexFile) {
		try (BufferedReader br = Files.newBufferedReader(FileSystems.getDefault().getPath(pathOfIndexFile), StandardCharsets.UTF_8)) {
			String temp = null;
			while ((temp = br.readLine()) != null) {
				String[] _temp = temp.split(" ");
				String ID = _temp[0];
				String URL = _temp[1];
				index.put(URL, ID);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	
	private static void extractGraph(String directoryOfMetaDataFile, String directoryOfGraphFile) {
		Path metaDataPath = FileSystems.getDefault().getPath(directoryOfMetaDataFile);
		Map<String, Vertice> mapID2Vertice = new HashMap<String, Vertice>();
		List<Vertice> ts = new LinkedList<Vertice>();
		try (DirectoryStream<Path> ds = Files.newDirectoryStream(metaDataPath)) {
			// Read JSON from meta file
			for (Path p : ds) {
				String fileContent = "";
				String temp = null;
				BufferedReader br = Files.newBufferedReader(p, StandardCharsets.UTF_8);
				while ((temp = br.readLine()) != null) {
					fileContent += temp;
				}
				br.close();

				// Parsing JSON to Page class and Mapping to numeric data
				List<Page> pages = HTMLTagWithAnchorParse.deserialize(fileContent);
				for (Page page : pages) {
					Vertice t = new Vertice();
					t.setURL(page.getSourceURL());
					t.setSourceID(index.get(page.getSourceURL()));
					for (String destURL : page.getDestinationURL()) {
						if (index.containsKey(destURL)) {
							t.addOutLink(index.get(destURL));
						}
					}
					ts.add(t);
					mapID2Vertice.put(index.get(page.getSourceURL()), t);
				}	
//				Files.write(FileSystems.getDefault().getPath(directoryOfGraphFile).resolve(fileName), Serializer.getStandardGson().toJson(incomplete_ts).getBytes(StandardCharsets.UTF_8), StandardOpenOption.CREATE, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING);
			}
			index.clear();
			
			// Extract in-link relationship
			for (Vertice v : ts) {
				String sourceID = v.getSourceID();
				for (String destID : v.getOutLink()) {
					mapID2Vertice.get(destID).addInLink(sourceID);
				}
			}
			BufferedWriter bw = Files.newBufferedWriter(FileSystems.getDefault().getPath(directoryOfGraphFile), StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING);
			bw.write(Serializer.getPrettyGson().toJson(ts));
			bw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}		
	}
	
	public static void main(String[] args) {
//		writeMetaData("/Users/pramote/Desktop/WebBase/dw/link/", "/Users/pramote/Desktop/WebBase/dw/meta_link/");
//		createIndexFile("/Users/pramote/Desktop/WebBase/dw/meta_link/", "/Users/pramote/Desktop/WebBase/dw/index/index.txt");
		readIndexFile("/Users/pramote/Desktop/WebBase/dw/index/index.txt");
		extractGraph("/Users/pramote/Desktop/WebBase/dw/meta_link/", "/Users/pramote/Desktop/WebBase/dw/graph/");
	}
}