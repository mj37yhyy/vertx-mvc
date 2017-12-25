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
	public static <T> void send(
			String className,
			String methodName,
			Object msg,
			ReturnHandler<T> returnHandler) {
		Container.eventBus.<T> send(className + ":" + methodName,
				binder.toJson(msg), replyHandler -> {
					if (replyHandler.succeeded()) {
						returnHandler.handle(replyHandler.result().body());
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
	public static <T> void send(
			Class<?> clazz,
			String methodName,
			Object msg,
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
	public static <T> void send(
			Object instance,
			String methodName,
			Object msg,
			ReturnHandler<T> returnHandler) {
		send(instance.getClass().getName(), methodName, msg, returnHandler);
	}

	/**
	 * 返回接口
	 * 
	 * @param <T>
	 */
	public interface ReturnHandler<T> {
		void handle(T t);
	}

	/**
	 * 注册到EventBus
	 * 
	 * @param name
	 *            注册名
	 * @param messageHandler
	 *            MessageHandler
	 * @param clazz
	 *            反序列化的类
	 */
	public static <T> void consumer(
			String name,
			MessageHandler<T> messageHandler,
			Class<T> clazz) {
		Container.eventBus.<T> consumer(name, message -> messageHandler
				.handle(binder.fromJson(message.body().toString(), clazz)));
	}

	/**
	 * 消息接口
	 */
	public interface MessageHandler<T> {
		void handle(T t);
	}

	private static JsonBinder binder = JsonBinder.buildNormalBinder();
}
