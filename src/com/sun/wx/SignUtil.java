package com.sun.wx;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import com.sun.utils.Tools;

public class SignUtil {

	// 签名
	static String getSign(Map<String, String> params, String partnerKey) {
		if (params == null || params.size() == 0) {
			return "";
		}
		List<String> keyList = new ArrayList<String>();
		for (Map.Entry<String, String> param : params.entrySet()) {
			String value = param.getValue();
			if (!"".equals(Tools.getDesc(value, ""))) { // 如果参数的值为空不参与签名
				String key = param.getKey();
				keyList.add(key);
			}
		}
		String[] keyArray = keyList.toArray(new String[keyList.size()]);
		Arrays.sort(keyArray, String.CASE_INSENSITIVE_ORDER); // 参数名ASCII码从小到大排序（字典序），参数名区分大小写
		// 使用URL键值对的格式（即key1=value1&key2=value2…）拼接成字符串
		StringBuffer stringBuffer = new StringBuffer();
		for (int i = 0; i < keyArray.length; ++i) {
			String key = keyArray[i];
			stringBuffer.append(key + "=");
			String value = Tools.getDesc(params.get(key), "");
			stringBuffer.append(value + "&");
		}
		// 最后拼接上key,key设置路径：微信商户平台(pay.weixin.qq.com)-->账户设置-->API安全-->密钥设置
		stringBuffer.append("key=" + partnerKey);
		// 进行MD5运算，再将得到的字符串所有字符转换为大写，得到sign值
		String signValue = Tools.getMD5(stringBuffer.toString()).toUpperCase();
		return signValue;
	}
	
	// 签名
	static String getSignObj(Map<String, Object> params, String partnerKey) {
		if (params == null || params.size() == 0) {
			return "";
		}
		List<String> keyList = new ArrayList<String>();
		for (Map.Entry<String, Object> param : params.entrySet()) {
			Object value = param.getValue();
			if (!"".equals(Tools.getDesc(value, ""))) { // 如果参数的值为空不参与签名
				String key = param.getKey();
				keyList.add(key);
			}
		}
		String[] keyArray = keyList.toArray(new String[keyList.size()]);
		Arrays.sort(keyArray, String.CASE_INSENSITIVE_ORDER); // 参数名ASCII码从小到大排序（字典序），参数名区分大小写
		// 使用URL键值对的格式（即key1=value1&key2=value2…）拼接成字符串
		StringBuffer stringBuffer = new StringBuffer();
		for (int i = 0; i < keyArray.length; ++i) {
			String key = keyArray[i];
			stringBuffer.append(key + "=");
			String value = Tools.getDesc(params.get(key), "");
			stringBuffer.append(value + "&");
		}
		// 最后拼接上key,key设置路径：微信商户平台(pay.weixin.qq.com)-->账户设置-->API安全-->密钥设置
		stringBuffer.append("key=" + partnerKey);
		// 进行MD5运算，再将得到的字符串所有字符转换为大写，得到sign值
		String signValue = Tools.getMD5(stringBuffer.toString()).toUpperCase();
		return signValue;
	}

	// 校验签名
	public static boolean checkSign(Map<String, Object> params, String partnerKey) {
		if (params == null || params.size() == 0) {
			return false;
		}
		String respSign = Tools.getDesc(params.remove("sign"), "");
		String sign = getSignObj(params, partnerKey);
		if (!respSign.equals(sign)) {
			return false;
		}
		return true;
	}

}
