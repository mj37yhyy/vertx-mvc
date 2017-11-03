package org.mermaid.vertxmvc;

import org.mermaid.vertxmvc.annotation.Observable;

import java.util.HashMap;
import java.util.Map;

public class TestService {

	@Observable
	public Map test(HashMap msg) {
		System.out.println("------------TestService--------------");
		return msg;
	}

}
