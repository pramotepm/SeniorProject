package net.mikelab.webbase.test;

import net.mikelab.webbase.content.HTMLContentParser;

public class Tester {
	public static void main(String[] args) {
		HTMLContentParser a = new HTMLContentParser("/Users/pramote/Desktop/WebBase/test/www_quintiles_com");
		a.extract();
	}
}
