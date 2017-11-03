package org.mermaid.vertxmvc;

import io.vertx.rxjava.core.AbstractVerticle;
import io.vertx.rxjava.core.MultiMap;
import io.vertx.rxjava.core.eventbus.Message;
import io.vertx.rxjava.core.eventbus.MessageConsumer;
import io.vertx.rxjava.core.http.HttpServer;
import io.vertx.rxjava.core.http.HttpServerRequest;
import io.vertx.rxjava.core.http.HttpServerResponse;
import io.vertx.rxjava.ext.web.Router;
import io.vertx.rxjava.ext.web.RoutingContext;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.mermaid.vertxmvc.utils.JsonBinder;
import rx.Observable;

import java.lang.reflect.Method;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class DispatcherVerticle extends AbstractVerticle {

	public void start() {
		Container.eventBus = vertx.eventBus();
		HttpServer server = vertx.createHttpServer();
		Router router = Router.router(vertx);
		router.route().handler(this::dispatcher);
		this.initBus();// 初始化所有bus
		server.requestHandler(router::accept).listen(
				Container.config.getServer().getPort());
	}

	/**
	 * 调用
	 *
	 * @param routingContext
	 */
	private void dispatcher(RoutingContext routingContext) {
		logger.debug("开始");
		HttpServerRequest request = routingContext.request();
		HttpServerResponse response = routingContext.response();
		response.putHeader("content-type", "text/plain;charset=utf-8");

		MultiMap mparams = request.params();
		Map<String, String> params = ((io.vertx.core.MultiMap) mparams
				.getDelegate()).entries().stream()
						.collect(Collectors.toMap(x -> x.getKey(),
								x -> x.getValue()));

		Set<String> set = Container.controllerMapingMap.keySet();
		for (String path : set) {
			// 正则匹配，只选先匹配到的
			if (Pattern.compile(path).matcher(request.path()).matches()) {
				Map<Method, Object> maping = Container.controllerMapingMap
						.get(path);
				maping.forEach((method, object) -> {
					try {
						List<Object> args = new ArrayList<Object>();
						for (Class<?> pc : method.getParameterTypes()) {
							if (pc.isInstance(request)) {// 如果是request，直接赋值
								args.add(request);
							} else if (pc.isInstance(response)) {// 如果是response，直接赋值
								args.add(response);
							} else if (pc.isInstance(params)) {// 如果是map，直接赋值
								args.add(params);
							} else {// 如果是javabean,进行转换
								Object pco = pc.newInstance();
								try {
									BeanUtils.populate(pco, params);
								} catch (IllegalAccessException e) {
									logger.error("无法赋值");
								}
								args.add(pco);
							}
						}
						Object re = null;
						try {
							// 调用Controller方法
							re = method.invoke(object,
									args.toArray(new Object[0]));
						} catch (Exception e) {// 如果发生错误
							logger.error(e.getMessage(), e);
							re = "{\"error\":\"" + e.getMessage() + "\"}";
						}
						if (re != null) {// 返回值序列化成json
							response.end(binder.toJson(re));
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
				});
				break;
			}
		}

		logger.debug("结束");
	}

	/**
	 * 初始化Bus
	 */
	private void initBus() {
		Container.observableMap.forEach((key, value) -> {
			MessageConsumer<Object> consumer = Container.eventBus
					.consumer(key);
			Observable<Message<Object>> observable = consumer.toObservable();
			observable.subscribe(msg -> {
				value.forEach((method, object) -> {
					try {
						if (method.getParameterTypes() == null
								|| method.getParameterTypes().length == 0) {// 直接用方法的入参类型进行转换
							msg.reply(binder
									.toJson(method.invoke(object,
											(Object) null)));
						} else if (method.getParameterTypes() != null
								&& method.getParameterTypes().length == 1) {// 直接用方法的入参类型进行转换，支持一个参数
							Class<?> pc = method.getParameterTypes()[0];
							if (pc.isInterface())
								logger.error("Observable:{}的入参不能是Interface",
										method.getDeclaringClass() + ":"
												+ method.getName());
							else
								msg.reply(binder
										.toJson(method.invoke(object,
												binder.fromJson(
														(String) msg.body(),
														pc))));
						} else {
							logger.error("Observable:{}的入参只支持0~1个",
									method.getDeclaringClass() + ":"
											+ method.getName());
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
				});
			});
		});
	}

	private JsonBinder binder = JsonBinder.buildNormalBinder();
	private Logger logger = LogManager.getLogger(getClass());
}
