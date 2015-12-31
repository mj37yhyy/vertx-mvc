package org.mermaid.vertxmvc.test;

import java.util.Map;

import org.mermaid.vertxmvc.EvnetBusHelper;
import org.mermaid.vertxmvc.annotation.Controller;
import org.mermaid.vertxmvc.annotation.RequestMapping;

@Controller
public class TestController {

	@RequestMapping("/test")
	public Map test(Map map) {
		System.out.println("--------------TestController--------------");
		EvnetBusHelper.send(TestService.class, "test", map, re -> {
			System.out.println(re);
		});
		return map;
	}

}
