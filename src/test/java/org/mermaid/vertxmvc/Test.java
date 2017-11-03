package org.mermaid.vertxmvc;

import org.mermaid.vertxmvc.utils.JsonBinder;

import java.util.HashMap;


public class Test {
	

	@org.junit.Test
	public  void test() throws Exception {
		JsonBinder binder = JsonBinder.buildNormalBinder();

		System.out.println(binder.fromJson("{\"a\":\"2\",\"b\":\"3\"}", HashMap.class));

	}

}
