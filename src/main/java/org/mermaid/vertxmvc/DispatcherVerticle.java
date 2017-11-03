package org.mermaid.vertxmvc;

import io.vertx.rxjava.core.AbstractVerticle;
import io.vertx.rxjava.core.MultiMap;
import io.vertx.rxjava.core.buffer.Buffer;
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
import rx.Subscription;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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

		MultiMap mparams = request.params();
		Map<String, String> params = ((io.vertx.core.MultiMap) mparams
				.getDelegate()).entries().stream()
						.collect(Collectors.toMap(x -> x.getKey(),
								x -> x.getValue()));

		if (Container.controllerMapingMap.containsKey(request.path())) {// 精确匹配（后期再做其它）
			Map<Method, Object> maping = Container.controllerMapingMap
					.get(request.path());
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
					// 调用Controller方法
					Object re = method.invoke(object,
							args.toArray(new Object[0]));
					if (re != null) {// 返回值序列化成json
						response.putHeader("content-type",
								"text/plain;charset=utf-8");
						response.end(binder.toJson(re));
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			});
		} else {
			response.putHeader("content-type", "text/plain;charset=utf-8");
			response.end("404");
		}

		logger.debug("结束");
	}

	/**
	 * 初始化Bus
	 */
	private void initBus() {
		Container.observableMap.forEach((key, value) -> {
			MessageConsumer<Object> consumer = Container.eventBus
					.<Object> consumer(key);
			Observable<Message<Object>> observable = consumer.toObservable();
			Subscription sub = observable.subscribe(msg -> {
				value.forEach((method, object) -> {
					try {
						Buffer buff = Buffer.buffer();
						msg.reply(binder
								.toJson(method.invoke(object, binder.fromJson(
										(String) msg.body(), HashMap.class))));
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
