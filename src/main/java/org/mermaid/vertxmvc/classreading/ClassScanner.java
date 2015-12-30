package org.mermaid.vertxmvc.classreading;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.JarURLConnection;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.URLConnection;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class ClassScanner {
	/** URLClassLoader的addURL方法 */
	private static Method addURL = initAddMethod();

	/** 初始化方法 */
	private static final Method initAddMethod() {
		try {
			Method add = URLClassLoader.class.getDeclaredMethod("addURL",
					new Class[] { URL.class });
			add.setAccessible(true);
			return add;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	private static URLClassLoader system = (URLClassLoader) ClassLoader
			.getSystemClassLoader();

	/**
	 * 循环遍历目录，找出所有的JAR包
	 */
	private static final void loopFiles(File file, List<File> files) {
		if (file.isDirectory()) {
			File[] tmps = file.listFiles();
			for (File tmp : tmps) {
				loopFiles(tmp, files);
			}
		} else {
			if (file.getAbsolutePath().endsWith(".jar")
					|| file.getAbsolutePath().endsWith(".zip")) {
				files.add(file);
			}
		}
	}

	/**
	 * <pre>
	 * 加载JAR文件
	 * </pre>
	 * 
	 * @param file
	 */
	public static final void loadJarFile(File file) {
		try {
			addURL.invoke(system, new Object[] { file.toURI().toURL() });
			System.out.println("加载JAR包：" + file.getAbsolutePath());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * <pre>
	 * 从一个目录加载所有JAR文件
	 * </pre>
	 * 
	 * @param path
	 */
	public static final void loadJarPath(String path) {
		List<File> files = new ArrayList<File>();
		File lib = new File(path);
		loopFiles(lib, files);
		for (File file : files) {
			loadJarFile(file);
		}
	}

	/**
	 * 从包package中获取所有的Class
	 * 
	 * @param pack
	 * @return
	 */
	public static Set<Resource> getResources(String pack) {

		// 第一个class类的集合
		Set<Resource> resources = new LinkedHashSet<Resource>();
		// 是否循环迭代
		boolean recursive = true;
		// 获取包的名字 并进行替换
		String packageName = pack;
		String packageDirName = packageName.replace('.', '/');
		// 定义一个枚举的集合 并进行循环来处理这个目录下的things
		Enumeration<URL> dirs;
		try {
			dirs = Thread.currentThread().getContextClassLoader()
					.getResources(packageDirName);
			// 循环迭代下去
			while (dirs.hasMoreElements()) {
				// 获取下一个元素
				URL url = dirs.nextElement();
				// 得到协议的名称
				String protocol = url.getProtocol();
				// 如果是以文件的形式保存在服务器上
				if ("file".equals(protocol)) {
					System.err.println("file类型的扫描");
					// 获取包的物理路径
					String filePath = URLDecoder.decode(url.getFile(), "UTF-8");
					// 以文件的方式扫描整个包下的文件 并添加到集合中
					findAndAddClassesInPackageByFile(packageName, filePath,
							recursive, resources);
				} else if ("jar".equals(protocol)) {
					// 如果是jar包文件
					// 定义一个JarFile
					System.err.println("jar类型的扫描");
					JarFile jarFile;
					try {
						// 获取jar
						URLConnection con = (JarURLConnection) url
								.openConnection();
						JarURLConnection jarCon = (JarURLConnection) con;
						jarFile = jarCon.getJarFile();
						String jarFileUrl = jarCon.getJarFileURL()
								.toExternalForm();
						// 从此jar包 得到一个枚举类
						Enumeration<JarEntry> entries = jarFile.entries();
						// 同样的进行循环迭代
						while (entries.hasMoreElements()) {
							// 获取jar里的一个实体 可以是目录 和一些jar包里的其他文件 如META-INF等文件
							JarEntry entry = entries.nextElement();
							String name = entry.getName();
							// 如果是以/开头的
							if (name.charAt(0) == '/') {
								// 获取后面的字符串
								name = name.substring(1);
							}
							// 如果前半部分和定义的包名相同
							if (name.startsWith(packageDirName)) {
								int idx = name.lastIndexOf('/');
								// 如果以"/"结尾 是一个包
								if (idx != -1) {
									// 获取包名 把"/"替换成"."
									packageName = name.substring(0, idx)
											.replace('/', '.');
								}
								// 如果可以迭代下去 并且是一个包
								if ((idx != -1) || recursive) {
									// 如果是一个.class文件 而且不是目录
									if (name.endsWith(".class")
											&& !entry.isDirectory()) {
										// 去掉后面的".class" 获取真正的类名
										String className = name.substring(
												packageName.length() + 1,
												name.length() - 6);
										Resource resource = new Resource();
										resource.setClassName(packageName + '.'
												+ className);
										resource.setRootPath(jarFileUrl);
										resource.setInputStream(jarFile.getInputStream(entry));
										resources.add(resource);
									}
								}
							}
						}
					} catch (IOException e) {
						// log.error("在扫描用户定义视图时从jar包获取文件出错");
						e.printStackTrace();
					}
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

		return resources;
	}

	/**
	 * 以文件的形式来获取包下的所有Class
	 * 
	 * @param packageName
	 * @param packagePath
	 * @param recursive
	 * @param resources
	 * @throws FileNotFoundException 
	 */
	public static void findAndAddClassesInPackageByFile(String packageName,
			String packagePath, final boolean recursive, Set<Resource> resources) throws FileNotFoundException {
		// 获取此包的目录 建立一个File
		File dir = new File(packagePath);
		// 如果不存在或者 也不是目录就直接返回
		if (!dir.exists() || !dir.isDirectory()) {
			// log.warn("用户定义包名 " + packageName + " 下没有任何文件");
			return;
		}
		// 如果存在 就获取包下的所有文件 包括目录
		File[] dirfiles = dir.listFiles(new FileFilter() {
			// 自定义过滤规则 如果可以循环(包含子目录) 或则是以.class结尾的文件(编译好的java类文件)
			public boolean accept(File file) {
				return (recursive && file.isDirectory())
						|| (file.getName().endsWith(".class"));
			}
		});
		// 循环所有文件
		for (File file : dirfiles) {
			String fileName = file.getName();
			// 如果是目录 则继续扫描
			if (file.isDirectory()) {
				findAndAddClassesInPackageByFile(
						packageName.equals("") ? fileName : (packageName
								+ "." + fileName),
						file.getAbsolutePath(), recursive, resources);
			} else {
				// 如果是java类文件 去掉后面的.class 只留下类名
				String className = fileName.substring(0,
						file.getName().length() - 6);
				Resource resource = new Resource();
				resource.setClassName(packageName + '.' + className);
				resource.setRootPath(packagePath);
				resource.setInputStream(new FileInputStream(file));
				resources.add(resource);
			}
		}
	}
}
