package org.mermaid.vertxmvc;

import org.mermaid.vertxmvc.utils.JsonBinder;

public class EventBusHelper {

	/**
	 * 发送消息到EventBus
	 * 
	 * @param className
	 *            要调用的类
	 * @param methodName
	 *            要调用的方法名
	 * @param msg
	 *            发送的消息
	 * @param returnHandler
	 *            消息返回回调
	 */
	public static <T> void send(String className, String methodName,
			Object msg, ReturnHandler<T> returnHandler) {
		JsonBinder binder = JsonBinder.buildNormalBinder(false);
		Container.eventBus.<T> send(className + ":" + methodName, binder.toJson(msg), ar -> {
			if (ar.succeeded()) {
				returnHandler.handler(ar.result().body());
			}
		});
	}

	/**
	 * 发送消息到EventBus
	 * 
	 * @param clazz
	 *            要调用的类
	 * @param methodName
	 *            要调用的方法名
	 * @param msg
	 *            发送的消息
	 * @param returnHandler
	 *            消息返回回调
	 */
	public static <T> void send(Class<?> clazz, String methodName, Object msg,
			ReturnHandler<T> returnHandler) {
		send(clazz.getName(), methodName, msg, returnHandler);
	}

	/**
	 * 发送消息到EventBus
	 * 
	 * @param instance
	 *            要调用的对象
	 * @param methodName
	 *            要调用的方法名
	 * @param msg
	 *            发送的消息
	 * @param returnHandler
	 *            消息返回回调
	 */
	public static <T> void send(Object instance, String methodName, Object msg,
			ReturnHandler<T> returnHandler) {
		send(instance.getClass().getName(), methodName, msg, returnHandler);
	}

	/**
	 * 返回接口
	 * 
	 * @author ji.miao
	 *
	 * @param <T>
	 */
	public interface ReturnHandler<T> {
		public void handler(T t);
	}
	
}
