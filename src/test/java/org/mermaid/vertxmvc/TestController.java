package org.mermaid.vertxmvc;

import io.vertx.core.http.HttpMethod;
import org.mermaid.vertxmvc.annotation.Controller;
import org.mermaid.vertxmvc.annotation.RequestMapping;

import java.util.Map;

@Controller
public class TestController {

	@RequestMapping("/test/:la")
	public Map test(Map map) {
		System.out
				.println("--------------TestController--------------/test/:la");
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

	@RequestMapping(pathRegex = ".*foo")
	public Map test3(Map map) {
		System.out
				.println("--------------TestController--------------test3");
		EventBusHelper.send(TestService.class, "test", map, re -> {
			System.out.println(re);
		});
		return map;
	}

	@RequestMapping(routeWithRegex = ".*foo")
	public Map test4(Map map) {
		System.out
				.println("--------------TestController--------------test4");
		EventBusHelper.send(TestService.class, "test", map, re -> {
			System.out.println(re);
		});
		return map;
	}

	@RequestMapping(
			routeWithRegex = ".*foo",
			pathRegex = "\\/([^\\/]+)\\/([^\\/]+)",
			method = {
					HttpMethod.GET, HttpMethod.POST
			})
	public Map foo_bar(Map map) {
		//Assert.requireNonEmpty(map.get("namespace"),"请填写名字空间名称");
		throw new RuntimeException("hkjl;");
//		System.out
//				.println("--------------TestController--------------foo_bar");
//		EventBusHelper.send(TestService.class, "test", map, re -> {
//			System.out.println(re);
//		});
//		return map;
	}
}
