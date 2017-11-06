package org.mermaid.vertxmvc;

import org.mermaid.vertxmvc.annotation.Observable;
import org.mermaid.vertxmvc.annotation.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class TestService {

	@Observable
	public Map test(HashMap msg) {
		System.out.println("------------TestService.test--------------");
		return msg;
	}

	@Observable
	public Map test2(HashMap msg) {
		System.out.println("------------TestService.test2--------------");
		return msg;
	}
}
