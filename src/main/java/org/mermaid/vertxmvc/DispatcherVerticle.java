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
import io.vertx.reactivex.ext.web.handler.CookieHandler;
import io.vertx.reactivex.ext.web.handler.StaticHandler;
import ognl.DefaultTypeConverter;
import ognl.Ognl;
import ognl.OgnlException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.mermaid.vertxmvc.annotation.*;
import org.mermaid.vertxmvc.classreading.Metadata;
import org.mermaid.vertxmvc.converters.Converter;
import org.mermaid.vertxmvc.converters.WebDataBinder;
import org.mermaid.vertxmvc.utils.JsonBinder;
import org.mermaid.vertxmvc.utils.ognl.ExpressionEvaluator;
import org.mermaid.vertxmvc.utils.ognl.OgnlCache;

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
		VertxMvc.setVertx(this.vertx);
		VertxMvc.setEventBus(this.vertx.eventBus());
		Container.eventBus = this.vertx.eventBus();
	}

	private void initController() {
		this.router = Router.router(vertx);

		this.router.route().handler(CookieHandler.create());// cookie

		Container.metadataSet.forEach(this::initController);// 循环映射

		this.router.route("/*").handler(StaticHandler.create());// 静态资源映射

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
				RequestMapping requestMapping = controllerClass
						.getAnnotation(RequestMapping.class);

				// 配置根路径
				String contextPath = Container.config.getServer()
						.getContextPath();// 用户配置的上下文路径
				// 根path
				String[] basePaths = new String[] {
						"/"
				};
				if (requestMapping != null)
					basePaths = requestMapping.value();
				for (int i = 0; i < basePaths.length; i++) {
					basePaths[i] = contextPath + "" + basePaths[i];
				}
				String[] newPaths = basePaths;// 新path

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
					// 返回值注解
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
					RequestMapping methodRequestMapping = method
							.getAnnotation(RequestMapping.class);

					// 如果方法包含RequestMapping注解映射之
					if (methodRequestMapping != null) {// 如果方法注解存在path,将base
														// path与 method
														// path进行排列组合，形成新的path
						// 方法上的path
						String[] methodPaths = methodRequestMapping.value();
						// 新path数组的长度是base path长度乘以method path的长度
						int newPathsLength = basePaths.length
								* methodRequestMapping.value().length;
						newPaths = new String[newPathsLength];

						// 将base path与 method path进行排列组合，形成新的path
						for (String basePath : basePaths) {
							for (String methodPath : methodPaths) {
								newPaths[--newPathsLength] = (basePath
										+ methodPath).replaceAll("[/]+", "/");
							}
						}
						// 用方法注解替换类注解
						requestMapping = methodRequestMapping;
					}

					if (requestMapping != null) {
						// 路径路由正则表达式
						Route route;
						if (!requestMapping.pathRegex().equals("")
								|| !requestMapping.routeWithRegex()
										.equals("")) {

							logger.debug("pathRegex "
									+ requestMapping.pathRegex()
									+ ", routeWithRegex "
									+ requestMapping.routeWithRegex() + " in "
									+ metadata.getClassMetadata()
											.getClassName()
									+ "$" + method);

							if (!requestMapping.routeWithRegex()
									.equals("")) {
								route = this.router.routeWithRegex(
										requestMapping.routeWithRegex());
								if (!requestMapping.pathRegex().equals("")) {
									route = route.pathRegex(
											requestMapping.pathRegex());
								}
							} else {
								route = this.router.route()
										.pathRegex(requestMapping.pathRegex());
							}

							this.handleController(controllerInstance,
									method,
									requestMapping, parameterAnnotations,
									responseBody, route);
						}
						// 普通路径
						else {
							for (String path : newPaths) {
								logger.debug("path " + path + " in " + method);
								route = this.router.route(path);
								this.handleController(controllerInstance,
										method,
										requestMapping,
										parameterAnnotations,
										responseBody, route);
							} // for
						} // else
					}
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
		Object handle()
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
			result = responseHandler.handle();
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
			route.handler((Handler<RoutingContext>) method
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
						// request.headers() 转换成 Map
						/*
						 * Map<String, String> headers = request.headers()
						 * .getDelegate().entries().stream()
						 * .collect(Collectors.toMap(Map.Entry::getKey,
						 * Map.Entry::getValue));
						 */

						// 如果有参数且参数条件不匹配，进入下一个路由
						try {
							if (!"".equals(requestMapping.params())
									&& !expressionEvaluator.evaluateBoolean(
											requestMapping.params(), params)) {
								routingContext.next();
								// response.setStatusCode(404).end();
								return;
							}
						} catch (OgnlException e) {
							e.printStackTrace();
						}

						try {
							Map context = this.getOgnlContext();
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
									// 如果是RequestParam
									else if (annotation.annotationType()
											.isAssignableFrom(
													RequestParam.class)) {
										String paramName = ((RequestParam) annotation)
												.value();
										String formalParamValue = request
												.getParam(paramName);
										if (formalParamValue == null) {
											formalParamValue = ((RequestParam) annotation)
													.defaultValue();
										}
										args.add(this.getValue(
												parameterTypeClass,
												formalParamValue));
										isAdded = true;
										break;
									}
									// 如果是RequestHeader
									else if (annotation.annotationType()
											.isAssignableFrom(
													RequestHeader.class)) {
										String headerName = ((RequestHeader) annotation)
												.value();
										String formalHeaderValue = request
												.getHeader(headerName);
										if (formalHeaderValue == null) {
											formalHeaderValue = ((RequestHeader) annotation)
													.defaultValue();
										}
										args.add(this.getValue(
												parameterTypeClass,
												formalHeaderValue));
										isAdded = true;
										break;
									}
									// 如果是CookieValue
									else if (annotation.annotationType()
											.isAssignableFrom(
													CookieValue.class)) {
										String cookieName = ((CookieValue) annotation)
												.value();
										String formalCookieValue = routingContext
												.getCookie(cookieName)
												.getValue();
										if (formalCookieValue == null) {
											formalCookieValue = ((CookieValue) annotation)
													.defaultValue();
										}
										args.add(this.getValue(
												parameterTypeClass,
												formalCookieValue));
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
										// BeanUtils.populate(
										// parameterTypeInstance,
										// params);
										params.forEach((K, V) -> {
											try {
												OgnlCache.setValue(K, context,
														parameterTypeInstance,
														V);
											} catch (OgnlException e) {
												logger.debug("无法赋值", e);
											}
										});
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
	 * 生成OgnlContext。增加Converter
	 * 
	 * @return Map Context
	 */
	private Map getOgnlContext() {
		Map context = Ognl
				.createDefaultContext(
						this);
		Ognl.setTypeConverter(context,
				new DefaultTypeConverter() {

					// 重载父类方法
					public Object convertValue(
							Map context,
							Object value,
							Class toType) {
						Object result = null;
						Map<Class, Converter> customConverter = WebDataBinder
								.getCustomConverter();
						// 先循环自己的Converter
						for (Map.Entry<Class, Converter> entry : customConverter
								.entrySet()) {

							if (toType == entry
									.getKey()) {
								result = entry
										.getValue()
										.convert(
												(String) value);
								break;
							}
						}
						// 如果为空，则使用默认的
						if (result == null) {
							result = super.convertValue(
									context,
									value,
									toType);
						}
						return result;
					}
				});
		return context;
	}

	/**
	 * 获取转换后的值
	 * 
	 * @param parameterTypeClass
	 *            参数类型
	 * @param formalValue
	 *            形参的值
	 * @return 装换后的值
	 */
	private Object getValue(Class<?> parameterTypeClass, String formalValue) {
		Object value = formalValue;
		Converter converter = WebDataBinder
				.getCustomConverter()
				.get(parameterTypeClass);
		if (converter != null)
			value = converter
					.convert(formalValue);
		return value;
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
				Class<?> serviceClass = loadClass(metadata);
				for (Method method : serviceClass.getMethods()) {

					// Observable
					if (method.isAnnotationPresent(
							org.mermaid.vertxmvc.annotation.Observable.class)) {// 如果包含Observable
						Object serviceInstance = serviceClass
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
													serviceInstance,
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
														serviceInstance,
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
	private ExpressionEvaluator expressionEvaluator = new ExpressionEvaluator();
}
