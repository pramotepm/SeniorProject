package net.mikelab.webbase.link.worker;

import java.io.BufferedReader;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import net.mikelab.webbase.struct.Page;
import net.mikelab.webbase.validate.Serializer;

import com.google.gson.reflect.TypeToken;

public class GenerateIndex implements Runnable {
	private AtomicInteger indexNumber;
	private Path _page;
	
	public GenerateIndex(Path _page, AtomicInteger indexNumber) {
		this._page = _page;
		this.indexNumber = indexNumber;
	}
	
	@SuppressWarnings("unchecked")
	public void run() {
		Type listType = new TypeToken<List<Page>>() {}.getType();
		String temp = null;
		String content = "";
		try(BufferedReader br = Files.newBufferedReader(_page, StandardCharsets.UTF_8)) {
			while((temp = br.readLine()) != null) {
				content += temp;
			}
			br.close();
			List<Page> pages = (List<Page>) Serializer.getStandardGson().fromJson(content, listType);
			for (Page p : pages) {
				String x = indexNumber.getAndIncrement() + " " + p.getSourceURL();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
