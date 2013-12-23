package net.mikelab.webbase.validate;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.nio.file.DirectoryStream;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import com.google.gson.reflect.TypeToken;

import net.mikelab.webbase.link.HTMLTagWithAnchorParse;
import net.mikelab.webbase.link.worker.GenerateIndex;
import net.mikelab.webbase.struct.Page;
import net.mikelab.webbase.struct.Vertice;

public class ValidateMain {
	private static Map<String, String> index = new HashMap<>();
	private static String mode;
	
	private static String directoryOfLinkDownloaded;
	private static String directoryOfMetaDataFile;
	private static String directoryOfIndexFile;
	private static String directoryOfGraphFile;
		
	private static void writeMetaData(String directoryOfLinkDownloaded, String directoryOfMetaDataFile) {
		System.out.println("Creating meta data...");
		Path metaDataPath = FileSystems.getDefault().getPath(directoryOfMetaDataFile);
		try (DirectoryStream<Path> ds = Files.newDirectoryStream(FileSystems.getDefault().getPath(directoryOfLinkDownloaded))) {
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
	
	private static void createIndexFile(String directoryOfMetaDataFile, String directoryOfIndexFile) {
		System.out.println("Creating index file...");
		int count = 0;
		Path path = FileSystems.getDefault().getPath(directoryOfMetaDataFile);
		List<Future<Set<String>>> futureList = new LinkedList<>(); 
		try (DirectoryStream<Path> ds = Files.newDirectoryStream(path)) {
			ExecutorService pool = Executors.newFixedThreadPool(8);
			for (Path _page : ds) {
				count++;
				GenerateIndex c = new GenerateIndex(_page);
				Future<Set<String>> f = pool.submit(c);
				futureList.add(f);
			}
			System.out.println("  from number of meta data: " + count + " files");
			pool.shutdown();
			while (!pool.isTerminated());
			Set<String> uniqURL = new HashSet<String>();
			for (Future<Set<String>> f : futureList) {
				try {
					uniqURL.addAll(f.get());
				} catch (InterruptedException e) {
					e.printStackTrace();
				} catch (ExecutionException e) {
					e.printStackTrace();
				}
			}
			int id = 0;
			BufferedWriter indexWriter = Files.newBufferedWriter(FileSystems.getDefault().getPath(directoryOfIndexFile), StandardCharsets.UTF_8, StandardOpenOption.WRITE, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
			for (String URL : uniqURL) {
				indexWriter.write(id + " " + URL + "\n");
				id++;
			}
			indexWriter.close();
			for (Future<Set<String>> f : futureList) {
				try {
					f.get().clear();
				} catch (InterruptedException | ExecutionException e) {
					e.printStackTrace();
				}
			}
			uniqURL.clear();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private static void readIndexFile(String directoryOfIndexFile) {
		System.out.println("Reading index file...");
		try (BufferedReader br = Files.newBufferedReader(FileSystems.getDefault().getPath(directoryOfIndexFile), StandardCharsets.UTF_8)) {
			String temp = null;
			while ((temp = br.readLine()) != null) {
//				System.out.println(temp);
				String[] _temp = temp.split(" ", 2);
				String ID = new String(_temp[0].getBytes(StandardCharsets.UTF_8), StandardCharsets.UTF_8);
				String URL = new String(_temp[1].getBytes(StandardCharsets.UTF_8), StandardCharsets.UTF_8);
				index.put(URL, ID);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	
	private static void extractGraph(String directoryOfMetaDataFile, String directoryOfGraphFile) {
		System.out.println("Creating graph...");
		Path metaDataPath = FileSystems.getDefault().getPath(directoryOfMetaDataFile);
		Map<String, Vertice> mapID2Vertice = new HashMap<String, Vertice>();
		List<Vertice> ts = new LinkedList<Vertice>();
		Set<String> usedURL = new HashSet<String>();
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
					String pageURL = page.getSourceURL();
					String pageID = index.get(pageURL);
					if (pageID == null) {
						System.out.println(pageURL);
						System.out.println("cannot get index number from source URL");
						System.exit(1);
					}
					if (usedURL.contains(pageID) || !pageURL.startsWith("http")) {
						continue;
					}
					usedURL.add(pageID);
					Vertice t = new Vertice();
					t.setURL(pageURL);
					t.setSourceID(pageID);
					for (String destURL : page.getDestinationURL()) {
						if (index.containsKey(destURL)) {
							t.addOutLink(index.get(destURL));
						}
					}
					ts.add(t);
					mapID2Vertice.put(pageID, t);
				}	
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
	
	private static void readGraph(String  directoryOfGraphFile, String directoryOfCSVFile) {
		Type verticeType = new TypeToken<List<Vertice>>() {}.getType();
		try(BufferedWriter bw = Files.newBufferedWriter(FileSystems.getDefault().getPath(directoryOfCSVFile), StandardCharsets.UTF_8, StandardOpenOption.WRITE, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
			BufferedReader br = Files.newBufferedReader(FileSystems.getDefault().getPath(directoryOfGraphFile), StandardCharsets.UTF_8)) {
			List<Vertice> vs = Serializer.getStandardGson().fromJson(br, verticeType);
			for (Vertice v : vs) {
				StringBuilder line = new StringBuilder();
				line.append(v.getSourceID());
				for (String outlink : v.getOutLink()) {
					line.append(";");
					line.append(outlink);
				}
				line.append("\n");
				bw.write(line.toString());
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static boolean checkArguments(String[] args) {
		try {
			int i = 0;
			do {
				String option = args[i];
				switch (option) {
					case "--begin-at-mode": {
						mode = args[i+1];
					} break;
					default: {
						Path dir = FileSystems.getDefault().getPath(args[i+1]);
						if (!Files.isDirectory(dir)) {
							try {
								Files.createDirectory(dir);
							} catch (IOException e) {
								e.printStackTrace();
								return false;
							}
						}
						switch (option) {
							case "-l": {
								directoryOfLinkDownloaded = dir.toString();
							} break;
							case "-m": {
								directoryOfMetaDataFile = dir.toString();
							} break;
							case "-i": {
								directoryOfIndexFile = dir.resolve("index.txt").toString();
							} break;
							case "-g": {
								directoryOfGraphFile = dir.resolve("graph.json").toString();
							} break;
							default: {
								return false;
							}
						}
					}
				}
				i += 2;
			} while (i<args.length);
			return true;
		} catch (ArrayIndexOutOfBoundsException e) {
			e.printStackTrace();
			return false;
		}
	}
	
	public static void help() {
		System.out.println("  --begin-at-mode [ extract | index | graph | print-as-csv ]");
		System.out.println("  -l <Directory for reading data from WebBase(link)>");
		System.out.println("  -m <Directory for storing meta data>");
		System.out.println("  -i <Directory for storing index file>");
		System.out.println("  -g <Directory for storing graph>");
	}
	
	public static void main(String[] args) {
//		args = "--begin-at-mode extract -l /Users/pramote/Desktop/WebBase/dw/link -m /Users/pramote/Desktop/WebBase/dw/meta_link -i /Users/pramote/Desktop/WebBase/dw/index -g /Users/pramote/Desktop/WebBase/dw/graph".split(" ");
		if (args.length == 0 || checkArguments(args) == false) {
			help();
			System.exit(1);
		}
		if (mode.equals("extract")) {			
			writeMetaData(directoryOfLinkDownloaded, directoryOfMetaDataFile);
			createIndexFile(directoryOfMetaDataFile, directoryOfIndexFile);
			readIndexFile(directoryOfIndexFile);
			extractGraph(directoryOfMetaDataFile, directoryOfGraphFile);
		}
		else if (mode.equals("index")) {
			createIndexFile(directoryOfMetaDataFile, directoryOfIndexFile);
			readIndexFile(directoryOfIndexFile);
			extractGraph(directoryOfMetaDataFile, directoryOfGraphFile);
		}
		else if (mode.equals("graph")) {
			readIndexFile(directoryOfIndexFile);
			extractGraph(directoryOfMetaDataFile, directoryOfGraphFile);
		}
		else if (mode.equals("print-as-csv")) {
			readGraph(directoryOfGraphFile, "./graph.csv");
		}
	}
}