package com.sun.utils;

import java.net.URLEncoder;
import java.util.Map;


/**
 * 工具类<br>
 * 2015-05-07
 */
public final class Tools {

	private Tools() {
	}

	/**
	 * 获取object,如果null则返回defaultObject
	 */
	public static <T> T getObject(T object, T defaultObject) {
		if (null == object) {
			return defaultObject;
		}
		return object;
	}
	
	/**
	 * 通过Object类型获取String,如果null则返回暂无
	 */
	public static String getDesc(Object value) {
		return getDesc(value + "");
	}
	
	/**
	 * 通过String类型获取String,如果null则返回暂无
	 */
	public static String getDesc(String value) {
		return getDesc(value, StringUtil.DEFAULT_VALUE);
	}
	
	/**
	 * 通过Object类型获取String,如果为null则返回defaultShow
	 * @param value 要获取的Object类型
	 * @param defaultShow 如果value为null时返回defaultShow
	 */
	public static String getDesc(Object value, String defaultShow) {
		return getDesc(value + "", defaultShow);
	}
	
	/**
	 * 通过String类型获取String,如果为null则返回defaultShow
	 * @param value 要获取的String类型
	 * @param defaultShow 如果value为null时返回defaultShow
	 */
	public static String getDesc(String value, String defaultShow) {
		return getDesc(value, defaultShow, Integer.MAX_VALUE);
	}

	/**
	 * 通过String类型获取String,如果为null则返回defaultShow,不为null则返回max长度字符串
	 */
	public static String getDesc(String value, Integer max) {
		return getDesc(value, StringUtil.DEFAULT_VALUE, max);
	}

	public static String getDesc(String value, Integer max, Integer extra) {
		return getDesc(value, StringUtil.DEFAULT_VALUE, max, extra);
	}

	public static String getDesc(String value, String defaultShow, Integer max) {
		if ((value + "").equals("null")) {
			return defaultShow;
		} else {
			value = value.trim();
			if (value.length() > max) {
				value = value.substring(0, max) + "...";
			}
			return value.trim();
		}
	}

	public static String getDesc(String value, String defaultShow, Integer max,
			Integer extra) {
		if ((value + "").equals("null")) {
			return defaultShow;
		} else {
			value = value.trim();
			if (value.length() > (max)) {
				value = value.substring(0, max - extra) + "...";
			}

			return value.trim();
		}
	}

	public static Long getLong(String value, Long defaultLong) {
		Long v = getLong(value);
		if (v == null) {
			return defaultLong;
		}
		return v;
	}

	public static Long getLong(String value) {
		if (value == null) {
			return null;
		}
		try {
			return Long.parseLong(value.trim());
		} catch (Exception ex) {
			return null;
		}
	}

	public static Double getDouble(String value, Double defaultDouble) {
		Double v = getDouble(value);
		if (v == null) {
			return defaultDouble;
		}
		return v;
	}

	public static Double getDouble(String value) {
		if (value == null) {
			return null;
		}
		try {

			return Double.parseDouble(value.trim());
		} catch (Exception ex) {
			return null;
		}
	}

	public static Float getFloat(String value, Float defaultFloat) {
		Float v = getFloat(value);
		if (v == null) {
			return defaultFloat;
		}
		return v;
	}

	public static Float getFloat(String value) {
		if (value == null) {
			return null;
		}
		try {
			return Float.parseFloat(value.trim());
		} catch (Exception ex) {
			return null;
		}
	}
	
	public static Integer getInt(int value) {
		return getInt(value + "", -9999);
	}
	
	public static Integer getInt(int value, Integer defaultValue) {
		return getInt(value + "", defaultValue);
	}

	public static Integer getInt(String value, Integer defaultValue) {
		return getInt(value, null, null, defaultValue);
	}

	public static Integer getInt(String value, Integer min, Integer max) {
		return getInt(value, min, max, min);
	}

	public static Integer getInt(String value, Integer min, Integer max,
			Integer defaultValue) {
		if (max == null) {
			max = Integer.MAX_VALUE;
		}
		if (min == null) {
			min = Integer.MIN_VALUE;
		}
		Integer v = getInt(value);
		if (v == null) {
			return defaultValue;
		}

		if (v < min) {
			return min;
		}
		if (v > max) {
			return max;
		}
		return v;
	}

	public static Integer getInt(String value) {
		try {
			return Integer.parseInt(value.trim());
		} catch (Exception ex) {
			return null;
		}
	}

	public static Boolean getBool(String value) {
		if (value == null) {
			return null;
		}
		try {
			return Boolean.parseBoolean(value);
		} catch (Exception ex) {
			return null;
		}
	}

	public static String showPrice(Integer price) {
		return showPrice(price, 2);
	}

	public static String showPrice(Integer price, int digit) {
		if (price == null) {
			return null;
		}
		int multi = 1;
		for (int i = 0; i < digit; i++) {
			multi = 10 * multi;
		}
		String left = price / multi + ".";
		String right = price % multi + "";
		for (int i = 0; i < digit - right.length(); i++) {
			right = "0" + right;
		}
		return left + right;
	}

	public static String showDigiLong(Long price, int digit) {
		if (price == null) {
			return null;
		}
		int multi = 1;
		for (int i = 0; i < digit; i++) {
			multi = 10 * multi;
		}
		String left = price / multi + ".";
		String right = price % multi + "";
		for (int i = 0; i < digit - right.length(); i++) {
			right = "0" + right;
		}
		return left + right;
	}

	public static Integer getPrice(String sPrice) {
		return getPrice(sPrice, 2);
	}

	public static Integer getPrice(String sPrice, int digit) {
		if (sPrice == null) {
			return null;
		}
		if (sPrice.equals("")) {
			return null;
		}
		sPrice = sPrice.trim();
		Integer price = null;
		boolean isFu = false;
		if (sPrice.charAt(0) == '-') {
			isFu = true;
		}
		int pindex = sPrice.indexOf(".");
		int multi = 1;
		for (int i = 0; i < digit; i++) {
			multi = multi * 10;
		}
		if (pindex < 0) {
			price = Tools.getInt(sPrice);
			if (price != null) {
				price = price * multi;
			}
		}
		if (pindex > 0) {
			try {
				String big = sPrice.substring(0, pindex);
				String small = sPrice.substring(pindex + 1, sPrice.length());
				if (small.length() > digit) {
					small = small.substring(0, digit);
				}
				for (int i = 0; i < digit - small.length(); i++) {
					small = small + "0";
				}
				Integer b = Tools.getInt(big.trim());
				Integer s = Tools.getInt(small.trim());
				if ((b != null) && (s != null)) {
					price = b * multi + s;
				}
			} catch (Exception ex1) {
				ex1.printStackTrace();
			}
		}
		if (price != null) {
			if (isFu && (price > 0)) {
				price = 0 - price;
			}
		}
		return price;
	}

	public static Long getDigiLong(String sPrice, int digit) {
		Long price = null;
		boolean isFu = false;
		if (sPrice.charAt(0) == '-') {
			isFu = true;
		}
		int pindex = sPrice.indexOf(".");
		int multi = 1;
		for (int i = 0; i < digit; i++) {
			multi = multi * 10;
		}
		if (pindex < 0) {
			price = Tools.getLong(sPrice);
			if (price != null) {
				price = price * multi;
			}
		}
		if (pindex > 0) {
			try {
				String big = sPrice.substring(0, pindex);
				String small = sPrice.substring(pindex + 1, sPrice.length());
				if (small.length() > digit) {
					small = small.substring(0, digit);
				}
				for (int i = 0; i < digit - small.length(); i++) {
					small = small + "0";
				}
				Long b = Tools.getLong(big.trim());
				Integer s = Tools.getInt(small.trim());
				if ((b != null) && (s != null)) {
					price = b * multi + s;
				}
			} catch (Exception ex1) {
				ex1.printStackTrace();
			}
		}
		if (price != null) {
			if (isFu && (price > 0)) {
				price = 0 - price;
			}
		}
		return price;
	}

	public static String getMD5(String source) {
		return getMD5(source, null);
	}

	public static String getMD5(String source, String encode) {
		String s = null;
		char hexDigits[] = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
				'a', 'b', 'c', 'd', 'e', 'f' };
		try {
			java.security.MessageDigest md = java.security.MessageDigest
					.getInstance("MD5");
			if (encode == null) {
				md.update(source.getBytes());
			} else {
				md.update(source.getBytes(encode));
			}
			byte tmp[] = md.digest(); // MD5 的计算结果是一个 128 位的长整数，
			// 用字节表示就是 16 个字节
			char str[] = new char[16 * 2]; // 每个字节用 16 进制表示的话，使用两个字符，
			// 所以表示成 16 进制需要 32 个字符
			int k = 0; // 表示转换结果中对应的字符位置
			for (int i = 0; i < 16; i++) { // 从第一个字节开始，对 MD5 的每一个字节
											// 转换成 16 进制字符的转换
				byte byte0 = tmp[i]; // 取第 i 个字节
				str[k++] = hexDigits[byte0 >>> 4 & 0xf]; // 取字节中高 4 位的数字转换,
				// >>> 为逻辑右移，将符号位一起右移
				str[k++] = hexDigits[byte0 & 0xf]; // 取字节中低 4 位的数字转换
			}
			s = new String(str); // 换后的结果转换为字符串

		} catch (Exception e) {
			e.printStackTrace();
		}
		return s;
	}

	/**
	 * 生成url请求参数
	 */
	public static String genUrlParams(Map<String, String> map) {
		Object[] keys = map.keySet().toArray();
		String params = "";
		for (int i = 0; i < keys.length; i++) {
			try {
				if (map.get(keys[i]) != null) {
					String value = URLEncoder.encode(map.get(keys[i]) + "", "utf-8");
					params = params + "&" + keys[i] + "=" + value;
				}
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
		if (params.length() > 0) {
			params = params.substring(1, params.length());
		}
		return params;
	}
}