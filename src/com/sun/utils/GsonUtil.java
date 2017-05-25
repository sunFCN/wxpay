package com.sun.utils;

import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

/**
 * JSON工具类，JSON转容器或对象。容器或对象转JSON
 * 
 * @author yangh
 */
public final class GsonUtil {

	/** 需要过滤的策略 **/
	private static final ExclusionStrategy strategy = new ExclusionStrategy() {
		@Override
		public boolean shouldSkipField(FieldAttributes fa) {
			return fa.getName().startsWith("others") || fa.getName().startsWith("errMap") || fa.getName().startsWith("imgString") || fa.getName().startsWith("jsonString");
		}

		@Override
		public boolean shouldSkipClass(Class<?> clazz) {
			return false;
		}
	};

	private static final Gson gson = new GsonBuilder().setExclusionStrategies(strategy).create();

	private GsonUtil() {
	}

	/**
	 * JSON转成TypeToken类型的对象或容器
	 * @param json JSON字符串
	 * @param token 要转到的类型
	 * @return 对象或容器
	 */
	public static <T> T fromJson(String json, TypeToken<T> token) {
		try {
			return gson.fromJson(json, token.getType());
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * JSON转成对象
	 * @param json JSON字符串
	 * @param c 对象类型
	 * @return 转换后的对象
	 */
	public static <T> T fromJson(String json, Class<T> c) {
		try {
			return gson.fromJson(json, c);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * 对象或容器转JSON
	 * @param src 要转换的容器或对象
	 * @return 转换后的JSON
	 */
	public static String toJson(Object obj) {
		String json = gson.toJson(obj);
		json = json.replaceAll(":null", ":\"\"");
		json = json.replaceAll("\"\\{", "{").replaceAll("\\}\"", "}");
		json = json.replaceAll("\"\\[", "[").replaceAll("\\]\"", "]");
		json = json.replaceAll("\\\\\"", "\\\"");
		return json;
	}
}