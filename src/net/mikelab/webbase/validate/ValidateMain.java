package net.mikelab.webbase.validate;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import net.mikelab.webbase.link.HTMLTagWithAnchorParse;
import net.mikelab.webbase.link.worker.GenerateIndex;

public class ValidateMain {
		
	private static void writeMetaData(String directoryOfLinkDownload, String directoryOfMetaData) {
		Path metaDataPath = FileSystems.getDefault().getPath(directoryOfMetaData);
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
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private static void createIndexFile(String pathOfMetaLink) {
		AtomicInteger indexNumber = new AtomicInteger(0);
		Path path = FileSystems.getDefault().getPath(pathOfMetaLink);
		try (DirectoryStream<Path> ds = Files.newDirectoryStream(path)) {
			ExecutorService pool = Executors.newFixedThreadPool(8);
			for (Path _page : ds) {
				GenerateIndex c = new GenerateIndex(_page, indexNumber);
				pool.execute(c);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) {
//		writeMetaData("/Users/pramote/Desktop/WebBase/dw/link/", "/Users/pramote/Desktop/WebBase/dw/meta_link/");
		long s = System.currentTimeMillis();
		createIndexFile("/Users/pramote/Desktop/WebBase/dw/meta_link/");
		System.out.println((System.currentTimeMillis() - s) / 1000.0);
	}
}
