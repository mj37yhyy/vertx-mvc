package org.mermaid.vertxmvc;

import java.io.IOException;
import java.net.URL;
import java.util.Enumeration;


public class Test {
	

	@org.junit.Test
	public  void test() {
		Enumeration<URL> resourceUrls = null;
		try {
			resourceUrls = getClass().getClassLoader().getResources("org/mermaid/vertxmvc");
		} catch (IOException e) {
			e.printStackTrace();
		}
		while (resourceUrls.hasMoreElements()) {
			URL url = resourceUrls.nextElement();
			System.out.println(url);
		}

	}

}
