package org.mermaid.vertxmvc.classreading;

import java.io.InputStream;

public class Resource {

	String rootPath;
	String className;
	InputStream inputStream = null;

	public String getRootPath() {
		return rootPath;
	}

	public void setRootPath(String rootPath) {
		this.rootPath = rootPath;
	}

	public String getClassName() {
		return className;
	}

	public void setClassName(String className) {
		this.className = className;
	}

	public InputStream getInputStream() {
		return inputStream;
	}

	public void setInputStream(InputStream inputStream) {
		this.inputStream = inputStream;
	}

	@Override
	public String toString() {
		return "Resource [rootPath=" + rootPath + ", className=" + className
				+ "]";
	}

}
