package org.mermaid.vertxmvc.test;

import java.util.Map;

import org.mermaid.vertxmvc.annotation.Observable;

public class TestService {

	@Observable
	public String test(Map msg) {
		System.out.println("------------TestService--------------");
		return "you say:" + msg;
	}

}
