package net.mikelab.webbase.link.worker;

import java.io.BufferedReader;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;

import net.mikelab.webbase.struct.Page;
import net.mikelab.webbase.validate.Serializer;
import net.mikelab.webbase.validate.ValidateMain;

import com.google.gson.reflect.TypeToken;

public class GenerateIndex implements Callable<Set<String>> {
	private Path _page;
	
	public GenerateIndex(Path _page) {
		this._page = _page;
	}
	
	@SuppressWarnings("unchecked")
	public Set<String> call() {
		Set<String> setOfURL = new HashSet<>();
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
				String URL = ValidateMain.purifyURLString(p.getSourceURL());
				if (URL.startsWith("http")) {
					setOfURL.add(URL);
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return setOfURL;
	}
}
