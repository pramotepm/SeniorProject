package net.mikelab.webbase.main;

import net.mikelab.webbase.link.HTMLTagWithAnchorParse;

public class WebBaseMain {
	public static void main(String[] args) {
		HTMLTagWithAnchorParse a = new HTMLTagWithAnchorParse("/Users/pramote/Desktop/WebBase/dw/link/ideas_repec_org");
		a.extract();
	}
}
