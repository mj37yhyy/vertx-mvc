package org.mermaid.vertxmvc;

import io.vertx.core.http.HttpMethod;
import io.vertx.reactivex.core.MultiMap;
import io.vertx.reactivex.core.http.HttpServerFileUpload;
import org.mermaid.vertxmvc.annotation.Controller;
import org.mermaid.vertxmvc.annotation.RequestBody;
import org.mermaid.vertxmvc.annotation.RequestMapping;
import org.mermaid.vertxmvc.annotation.ResponseBody;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@Controller
public class TestController {

	@RequestMapping("/test/:la")
	public @ResponseBody
	Map test(Map map) {
		System.out
				.println("--------------TestController--------------/test/:la");
		EventBusHelper.send(TestService.class, "test", map, re -> {
			System.out.println(re);
		});
		return map;
	}

	@RequestMapping("/test2")
	// @ResponseBody
	public Map test2(Map map) {
		System.out.println("--------------TestController--------------/test2");
		EventBusHelper.send(TestService.class, "test", map, re -> {
			System.out.println(re);
		});
		return map;
	}

	@RequestMapping("/jsonTest")
	// @ResponseBody
	public String jsonTest(
			@RequestBody
			HashMap map) {
		System.out
				.println("--------------TestController--------------/jsonTest="
						+ map);
		return "gthjkl;";
	}

	@RequestMapping(value = "/fileTest")
	// @ResponseBody
	public String fileTest(
			MultiMap formAttributes,
			HttpServerFileUpload upload) {
		Set<String> a = formAttributes.names();
		System.out
				.println("--------------TestController--------------/fileTest");
		System.out.println("a=" + a);
		System.out.println("upload=" + upload.filename());
		upload.streamToFileSystem("d:/1.txt");
		return "gthjkl;";
	}

	@RequestMapping(pathRegex = ".*foo")
	public @ResponseBody
	Map test3(Map map) {
		System.out
				.println("--------------TestController--------------test3");
		EventBusHelper.send(TestService.class, "test", map, re -> {
			System.out.println(re);
		});
		return map;
	}

	@RequestMapping(routeWithRegex = ".*foo")
	public @ResponseBody
	Map test4(Map map) {
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
	@ResponseBody
	public Map foo_bar(Map map) {
		System.out
				.println("--------------TestController--------------foo_bar");
		EventBusHelper.send(TestService.class, "test", map, re -> {
			System.out.println(re);
		});
		return map;
	}

//	@RequestMapping(value = "/ndy")
//	public Handler<RoutingContext> myHandler() {
//
//		SockJSHandlerOptions options = new SockJSHandlerOptions()
//				.setHeartbeatInterval(2000);
//
//		Handler sockJSHandler = SockJSHandler.create(VertxMvc.getVertx(),
//				options);
//		return sockJSHandler;
//		// return routingContext -> {
//		////
//		//// HttpServerResponse response = routingContext.response();
//		//// response.putHeader("content-type", "text/plain;charset=utf-8");
//		//// response.end("你大爷\n");
//		//// };
//	}

	@RequestMapping(value = "/ndy2", params = "a==2 && b==3")
	public @ResponseBody
	Map ndy2(Map map) {
		return map;
	}
}
