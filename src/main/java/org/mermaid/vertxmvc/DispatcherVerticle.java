package org.mermaid.vertxmvc;

import io.reactivex.Observable;
import io.vertx.core.Handler;
import io.vertx.core.http.HttpMethod;
import io.vertx.reactivex.core.AbstractVerticle;
import io.vertx.reactivex.core.eventbus.Message;
import io.vertx.reactivex.core.eventbus.MessageConsumer;
import io.vertx.reactivex.core.http.HttpServerRequest;
import io.vertx.reactivex.core.http.HttpServerResponse;
import io.vertx.reactivex.ext.web.Route;
import io.vertx.reactivex.ext.web.Router;
import io.vertx.reactivex.ext.web.RoutingContext;
import io.vertx.reactivex.ext.web.handler.BodyHandler;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.mermaid.vertxmvc.annotation.*;
import org.mermaid.vertxmvc.classreading.Metadata;
import org.mermaid.vertxmvc.utils.JsonBinder;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class DispatcherVerticle extends AbstractVerticle {

	public void start() {
		System.out.println(
				" _     _   _____   _____    _____  __    __           ___  ___   _     _   _____  \n"
						+
						"| |   / / | ____| |  _  \\  |_   _| \\ \\  / /          /   |/   | | |   / / /  ___| \n"
						+
						"| |  / /  | |__   | |_| |    | |    \\ \\/ /          / /|   /| | | |  / /  | |     \n"
						+
						"| | / /   |  __|  |  _  /    | |     }  {          / / |__/ | | | | / /   | |     \n"
						+
						"| |/ /    | |___  | | \\ \\    | |    / /\\ \\        / /       | | | |/ /    | |___  \n"
						+
						"|___/     |_____| |_|  \\_\\   |_|   /_/  \\_\\      /_/        |_| |___/     \\_____| ");
		logger.info("vertx-mvc starting...");

		initEventBus();

		initController();

		initService();

		requestHandler();

		logger.info("vertx-mvc is started in {}!",
				Container.config.getServer().getPort());
	}

	private void initEventBus() {
		VertxMvc.setEventBus(this.vertx.eventBus());
		Container.eventBus = this.vertx.eventBus();
	}

	private void initController() {
		this.router = Router.router(vertx);
		Container.metadataSet.forEach(this::initController);// 循环映射
		// 兜底结束
		this.router.route()
				.handler(routingContext -> routingContext
						.response().setStatusCode(404)
						.end());
	}

	private void initService() {
		Container.metadataSet.forEach(this::initService);// 循环映射
	}

	private void requestHandler() {
		vertx.createHttpServer().requestHandler(this.router::accept)
				.listen(
						Container.config.getServer().getPort());
	}

	private Class<?> loadClass(Metadata metadata)
			throws ClassNotFoundException {
		return Thread.currentThread().getContextClassLoader()
				.loadClass(metadata.getClassMetadata().getClassName());
	}

	/**
	 * 初始化Controller
	 * 
	 * @param metadata
	 *            Metadata
	 */
	private void initController(Metadata metadata) {
		try {
			logger.debug("scan class : "
					+ metadata.getClassMetadata().getClassName());
			if (metadata.getAnnotationMetadata().hasAnnotation(
					Controller.class.getName())) {
				// 映射路径和方法
				Class<?> controllerClass = loadClass(metadata);
				Object controllerInstance = controllerClass.newInstance();
				Method[] methods = controllerClass.getMethods();

				for (Method method : methods) {

					// 入参注解
					Annotation[][] parameterAnnotations = method
							.getParameterAnnotations();
					for (Annotation[] parameterAnnotation : parameterAnnotations) {
						for (Annotation annotation : parameterAnnotation) {
							// RequestBody
							if (annotation.annotationType()
									.isAssignableFrom(RequestBody.class)) {
								this.router.route()
										.handler(BodyHandler.create());
							}
						}
					}

					ResponseBody responseBody = null;
					if (method.getReturnType()
							.isAssignableFrom(ResponseBody.class)
							|| method
									.getAnnotationsByType(
											ResponseBody.class).length > 0) {
						responseBody = method
								.getAnnotation(ResponseBody.class);
					}

					// 方法注解
					RequestMapping[] requestMappings = method
							.getAnnotationsByType(RequestMapping.class);
					if (requestMappings.length == 1) {// 如果方法包含RequestMapping注解映射之
						RequestMapping requestMapping = requestMappings[0];

						// 路径正则表达式
						if (!requestMapping.pathRegex().equals("")
								&& requestMapping.routeWithRegex()
										.equals("")) {

							Route route = this.router.route()
									.pathRegex(requestMapping.pathRegex());
							this.handleController(controllerInstance,
									method,
									requestMapping, parameterAnnotations,

									responseBody,
									route);
						}
						// 路由正则表达式
						else if (requestMapping.pathRegex().equals("")
								&& !requestMapping.routeWithRegex()
										.equals("")) {

							Route route = this.router.routeWithRegex(
									requestMapping.routeWithRegex());
							this.handleController(controllerInstance,
									method,
									requestMapping, parameterAnnotations,
									responseBody, route);
						}
						// 路径正则表达式 + 路由正则表达式
						else if (!requestMapping.pathRegex().equals("")
								&& !requestMapping.routeWithRegex()
										.equals("")) {

							Route route = this.router
									.routeWithRegex(
											requestMapping.routeWithRegex())
									.pathRegex(requestMapping.pathRegex());
							this.handleController(controllerInstance,
									method,
									requestMapping, parameterAnnotations,
									responseBody, route);
						}
						// 普通路径
						else {
							for (String path : requestMapping.value()) {
								Route route = this.router.route(path);
								this.handleController(controllerInstance,
										method,
										requestMapping,
										parameterAnnotations,
										responseBody, route);
							} // for
						} // else
					} // if
				} // for
			} // if
		} catch (Exception e) {
			logger.error("初始化错误，启动失败");
			logger.error(e.getMessage(), e);
			System.exit(0);
		}
	}

	/**
	 * 通用回复的回调接口
	 */
	interface ResponseHandler {
		Object handler()
				throws InvocationTargetException, IllegalAccessException;
	}

	/**
	 * 通用回复
	 */
	private void doResponse(
			HttpServerResponse response,
			ResponseBody responseBody,
			ResponseHandler responseHandler) {
		Object result;
		try {
			result = responseHandler.handler();
			if (result != null) {
				if (responseBody != null) {
					response.end(
							responseBody.converterType()
									.newInstance()
									.convert(result));
				} else
					response.end(result.toString());
			}
		} catch (InvocationTargetException e) {// 如果发生错误
			logger.error(e.getCause().getMessage(),
					e.getCause());
			result = "{\"error\":\""
					+ e.getCause().getMessage()
					+ "\"}";
			response.setStatusCode(500)
					.end(result.toString());
		} catch (Throwable e) {
			logger.error(e.getMessage(), e);
			result = "{\"error\":\""
					+ e.getMessage()
					+ "\"}";
			response.setStatusCode(500)
					.end(result.toString());
		}
	}

	/**
	 * 初始化控制器
	 * 
	 * @param controllerInstance
	 *            controller Instance Object
	 * @param method
	 *            Method
	 * @param requestMapping
	 *            RequestMapping
	 * @param parameterAnnotations
	 *            parameter Annotations type Annotation[][]
	 * @param responseBody
	 *            ResponseBody
	 * @param route
	 *            Route
	 */
	private void handleController(
			Object controllerInstance,
			Method method,
			RequestMapping requestMapping,
			Annotation[][] parameterAnnotations,
			ResponseBody responseBody,
			Route route)
			throws InvocationTargetException, IllegalAccessException {

		// http 方法
		if (requestMapping.method().length > 0) {
			for (HttpMethod httpMethod : requestMapping.method()) {
				route.method(httpMethod);
			}
		}

		// consumes
		if (requestMapping.consumes().length > 0) {
			for (String consume : requestMapping.consumes()) {
				route.consumes(consume);
			}
		}

		// produces
		if (requestMapping.produces().length > 0) {
			for (String produce : requestMapping.produces()) {
				route.produces(produce);
			}
		}

		// 返回值
		// 如果返回值是Handler且无参数
		if (method.getReturnType().isAssignableFrom(Handler.class)
				&& method.getParameterCount() == 0) {
			this.router.route()
					.handler((Handler<RoutingContext>) method
							.invoke(controllerInstance, null));
		} else {
			// handler
			route.handler(
					routingContext -> {
						HttpServerRequest request = routingContext.request();
						HttpServerResponse response = routingContext.response();

						response.putHeader("content-type",
								"text/plain;charset=utf-8");

						// routingContext.cookies();

						// String contentType =
						// request.getDelegate().getHeader("Content-Type");
						// System.out.println("contentType=" + contentType);
						// 如果是Multipart
						// if (contentType
						// .startsWith("multipart/form-data; boundary=")) {
						// if(requestMapping.isMultipart()){
						// request.setExpectMultipart(true);
						// request.uploadHandler(upload -> upload.endHandler(v
						// -> {
						// MultiMap formAttributes = request
						// .params();
						// System.out.println(
						// "formAttributes=" + formAttributes);
						// this.doResponse(response, responseBody,
						// () -> method.invoke(
						// controllerInstance,
						// formAttributes, upload));
						// }));
						// }
						// // 如果不是Multipart
						// else {
						// request.params() 转换成 Map
						Map<String, String> params = request
								.params().getDelegate().entries().stream()
								.collect(Collectors.toMap(Map.Entry::getKey,
										Map.Entry::getValue));

						try {
							List<Object> args = new ArrayList<>();
							for (int i = 0; i < method
									.getParameterTypes().length; i++) {
								Class<?> parameterTypeClass = method
										.getParameterTypes()[i];

								boolean isAdded = false;// 本参数是否已经加入
								Annotation[] annotations = parameterAnnotations[i];// 得到入参上注解
								for (Annotation annotation : annotations) {
									// 如果是RequestBody
									if (annotation.annotationType()
											.isAssignableFrom(
													RequestBody.class)) {
										// 调用转换器进行装换
										args.add(((RequestBody) annotation)
												.converterType().newInstance()
												.convert(
														routingContext
																.getBody(),
														parameterTypeClass));
										isAdded = true;
										break;
									}
								}
								if (!isAdded) {
									if (parameterTypeClass
											.isInstance(request)) {// 如果是request，直接赋值
										args.add(request);
									} else if (parameterTypeClass
											.isInstance(response)) {// 如果是response，直接赋值
										args.add(response);
									} else if (parameterTypeClass
											.isInstance(routingContext)) {// 如果是routingContext，直接赋值
										args.add(routingContext);
									} else if (parameterTypeClass
											.isInstance(params)) {// 如果是map，直接赋值
										args.add(params);
									} else {// 如果是javabean,进行转换
										Object parameterTypeInstance = parameterTypeClass
												.newInstance();
										try {
											BeanUtils.populate(
													parameterTypeInstance,
													params);
										} catch (IllegalAccessException e) {
											logger.error("无法赋值");
										}
										args.add(parameterTypeInstance);
									}
								}
							}
							this.doResponse(response, responseBody,
									() -> method.invoke(
											controllerInstance,
											args.toArray(new Object[0])));
						} catch (Exception e) {
							e.printStackTrace();
						}
						// }
					});
		}
	}

	/**
	 * 初始化Service
	 *
	 * @param metadata
	 *            Metadata
	 */
	private void initService(Metadata metadata) {
		try {
			if (metadata.getAnnotationMetadata().hasAnnotation(
					Service.class.getName())) {
				Class<?> observableClass = loadClass(metadata);
				for (Method method : observableClass.getMethods()) {

					// Observable
					if (method.isAnnotationPresent(
							org.mermaid.vertxmvc.annotation.Observable.class)) {// 如果包含Observable
						Object observableInstance = observableClass
								.newInstance();
						MessageConsumer<Object> consumer = Container.eventBus
								.consumer(metadata.getClassMetadata()
										.getClassName() + ":"
										+ method.getName());
						Observable<Message<Object>> observable = consumer
								.toObservable();
						observable.subscribe(msg -> {
							try {
								if (method.getParameterTypes() == null
										|| method
												.getParameterTypes().length == 0) {// 直接用方法的入参类型进行转换
									msg.reply(binder
											.toJson(method.invoke(
													observableInstance,
													(Object) null)));
								} else if (method.getParameterTypes() != null
										&& method
												.getParameterTypes().length == 1) {// 直接用方法的入参类型进行转换，支持一个参数
									Class<?> pc = method.getParameterTypes()[0];
									if (pc.isInterface())
										logger.error(
												"Observable:{}的入参不能是Interface",
												method.getDeclaringClass() + ":"
														+ method.getName());
									else
										msg.reply(binder
												.toJson(method.invoke(
														observableInstance,
														binder.fromJson(
																(String) msg
																		.body(),
																pc))));
								} else {
									logger.error("Observable:{}的入参只支持0~1个",
											method.getDeclaringClass() + ":"
													+ method.getName());
								}
							} catch (Exception e) {
								logger.error(e.getMessage(), e);
							} // catch
						});// subscribe
					} // if
				} // for
			} // if
		} catch (Exception e) {
			logger.error("初始化错误，启动失败");
			logger.error(e.getMessage(), e);
			System.exit(0);
		}
	}

	private JsonBinder binder = JsonBinder.buildNormalBinder();
	private Logger logger = LogManager.getLogger(getClass());
	private Router router = null;
}
