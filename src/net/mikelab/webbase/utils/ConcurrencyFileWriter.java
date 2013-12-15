package net.mikelab.webbase.utils;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

public class ConcurrencyFileWriter {
	private static Object lock = new Object();
	private BufferedWriter bw = null;

	public ConcurrencyFileWriter(String filepath) {
		Path p = FileSystems.getDefault().getPath(filepath);
		try {
			bw = Files.newBufferedWriter(p, StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.WRITE);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void writeln(String text) {
		synchronized (lock) {
			try {
				System.out.println(text);
				bw.write(text + "\n");
			} catch (IOException e) {
				e.printStackTrace();
			}			
		}
	}

	public void close() {
		try {
			bw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}