package org.mermaid.vertxmvc;

import org.mermaid.vertxmvc.annotation.Controller;
import org.mermaid.vertxmvc.annotation.RequestMapping;

import java.util.Map;

@Controller
public class TestController {

	@RequestMapping("/test/:la")
	public Map test(Map map) {
		System.out.println("--------------TestController--------------/test/:la");
		EventBusHelper.send(TestService.class, "test", map, re -> {
			System.out.println(re);
		});
		return map;
	}

	@RequestMapping("/test2")
	public Map test2(Map map) {
		System.out.println("--------------TestController--------------/test2");
		EventBusHelper.send(TestService.class, "test", map, re -> {
			System.out.println(re);
		});
		return map;
	}


	@RequestMapping(".*test3")
	public Map test3(Map map) {
		System.out.println("--------------TestController--------------.*/test3");
		EventBusHelper.send(TestService.class, "test", map, re -> {
			System.out.println(re);
		});
		return map;
	}
}
