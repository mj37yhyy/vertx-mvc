package org.mermaid.vertxmvc;

import org.mermaid.vertxmvc.annotation.Controller;
import org.mermaid.vertxmvc.annotation.RequestMapping;

import java.util.Map;

@Controller
public class TestController {

	@RequestMapping("/test")
	public Map test(Map map) {
		System.out.println("--------------TestController--------------");
		EventBusHelper.send(TestService.class, "test", map, re -> {
			System.out.println(re);
		});
		return map;
	}

}
