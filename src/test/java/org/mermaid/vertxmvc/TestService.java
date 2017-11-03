package org.mermaid.vertxmvc;

import org.mermaid.vertxmvc.annotation.Observable;

import java.util.Map;

public class TestService {

	@Observable
	public Map test(Map msg) {
		System.out.println("------------TestService--------------");
		return msg;
	}

}
