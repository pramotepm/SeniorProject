package net.mikelab.webbase.validate;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class Serializer {
	private static Gson g = new Gson();
	private static Gson g1 = new GsonBuilder().setPrettyPrinting().create();

	public static Gson getStandardGson() {
		return g;
	}
	
	public static Gson getPrettyGson() {
		return g1;
	}
}