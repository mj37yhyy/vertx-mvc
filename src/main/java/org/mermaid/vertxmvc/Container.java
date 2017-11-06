package org.mermaid.vertxmvc;

import io.vertx.reactivex.core.Vertx;
import io.vertx.reactivex.core.eventbus.EventBus;
import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.mermaid.vertxmvc.classreading.ClassScanner;
import org.mermaid.vertxmvc.classreading.Metadata;
import org.mermaid.vertxmvc.classreading.MetadataReader;
import org.mermaid.vertxmvc.classreading.Resource;
import org.mermaid.vertxmvc.config.Config;
import org.mermaid.vertxmvc.utils.JsonBinder;
import org.yaml.snakeyaml.Yaml;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Set;

class Container {

	/**
	 * 初始化
	 */
	public void initialization() {
		try {
			parseYAML();// 解析yaml
		} catch (Exception e) {
			parseJSON();// 解析json
		}
		Set<Resource> resourceSet = scanClasses();
		if (resourceSet != null)
			resourceSet.forEach(resource -> {
				try {
					metadataSet.add(metadataReader.getMetadataReader(resource));
				} catch (IOException e) {
					logger.error(e.getMessage(), e);
				}
			});// 循环映射

	}

	/**
	 * 解析json
	 */
	private void parseJSON() {
		InputStream is = getClass().getClassLoader().getResourceAsStream(
				"vertx-mvc.json");
		try {
			Container.config = binder.fromJson(IOUtils.toString(is),
					Config.class);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 解析json
	 */
	private void parseYAML() {
		InputStream is = getClass().getClassLoader().getResourceAsStream(
				"vertx-mvc.yaml");
		Container.config = new Yaml().loadAs(is, Config.class);
	}

	/**
	 * 扫描指定包下的类文件
	 * 
	 * @return Set
	 */
	private Set<Resource> scanClasses() {
		if (config.getScaner() != null
				&& config.getScaner().getBasePath() != null) {
			return ClassScanner.getResources(config.getScaner().getBasePath());
		}
		return null;
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

	private JsonBinder binder = JsonBinder.buildNormalBinder(false);
	static Config config = null;
	static EventBus eventBus = null;
	static Set<Metadata> metadataSet = new HashSet<>();
	private MetadataReader metadataReader = new MetadataReader();
	private Logger logger = LogManager.getLogger(getClass());
}
