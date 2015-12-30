package org.mermaid.vertxmvc;

import io.vertx.rxjava.core.Vertx;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.mermaid.vertxmvc.annotation.Controller;
import org.mermaid.vertxmvc.annotation.RequestMapping;
import org.mermaid.vertxmvc.annotation.Service;
import org.mermaid.vertxmvc.classreading.ClassScanner;
import org.mermaid.vertxmvc.classreading.Metadata;
import org.mermaid.vertxmvc.classreading.MetadataReader;
import org.mermaid.vertxmvc.classreading.Resource;
import org.mermaid.vertxmvc.config.Config;
import org.mermaid.vertxmvc.utils.JsonBinder;

public class Container {

	/**
	 * 初始化
	 */
	public void initialization() {
		parseJSON();// 解析json
		Set<Resource> resourceSet = scanClasses();// 扫描类
		resourceSet.forEach(this::mapping);// 循环映射

	}

	/**
	 * 解析json
	 */
	private void parseJSON() {
		InputStream is = getClass().getClassLoader().getResourceAsStream(
				"vertx-mvc.json");
		try {
			String json = IOUtils.toString(is);
			Container.config = binder.fromJson(json, Config.class);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 扫描指定包下的类文件
	 * 
	 * @return
	 */
	private Set<Resource> scanClasses() {
		if (config.getScaner() != null
				&& config.getScaner().getBasePath() != null) {
			return ClassScanner.getResources(config.getScaner().getBasePath());
		}
		return null;
	}

	/**
	 * 初始化类
	 * 
	 * @param resource
	 */
	private void mapping(Resource resource) {
		try {
			Metadata metadata = metadataReader.getMetadataReader(resource);
			// 如果是 Controller
			if (metadata.getAnnotationMetadata().hasAnnotation(
					Controller.class.getName())) {
				// 映射路径和方法
				Class<?> controllerClass = loadClass(metadata);
				Method[] methods = controllerClass.getMethods();
				for (Method method : methods) {
					RequestMapping[] requestMappings = method
							.getAnnotationsByType(RequestMapping.class);
					if (requestMappings.length == 1) {// 如果方法包含RequestMapping注解映射之
						Object instance;
						// 生成对象
						if (!controllerObjectMap.containsKey(controllerClass
								.getName())) {
							instance = controllerClass.newInstance();
							controllerObjectMap.put(controllerClass.getName(),
									instance);
						} else {
							instance = controllerObjectMap.get(controllerClass
									.getName());
						}
						
						String[] paths = requestMappings[0].value();
						if (paths.length > 0) {// 如果有设定路径，则映射
							for (String path : paths) {
								controllerMapingMap.put(path, method);
							}
						} else {// 如果不填写路径，则以方法名做为路径
							controllerMapingMap.put("/" + method.getName(),
									method);
						}// else
					}// if
				}// for
			}// if
				// 如果是Service
			else if (metadata.getAnnotationMetadata().hasAnnotation(
					Service.class.getName())) {
				Class<?> clazz = loadClass(metadata);

			}
			logger.info(metadata.getClassMetadata().getClassName());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private Class<?> loadClass(Metadata metadata) throws ClassNotFoundException {
		Class<?> clazz = Thread.currentThread().getContextClassLoader()
				.loadClass(metadata.getClassMetadata().getClassName());
		return clazz;
	}

	/**
	 * 启动服务
	 */
	public void startServer() {
		if (Container.config != null) {
			Vertx vertx = Vertx.vertx();
			vertx.deployVerticle(DispatcherVerticle.class.getName());
		}
	}

	private Logger logger = LogManager.getLogger(getClass());
	private JsonBinder binder = JsonBinder.buildNonDefaultBinder();
	private MetadataReader metadataReader = new MetadataReader();
	static Config config = null;
	static final Map<String, Object> controllerObjectMap = new HashMap<String, Object>();
	static final Map<String, Method> controllerMapingMap = new HashMap<String, Method>();
}
