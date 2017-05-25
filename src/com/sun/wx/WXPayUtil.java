package com.sun.wx;

import java.io.FileInputStream;
import java.security.KeyStore;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLContexts;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import com.sun.utils.GsonUtil;
import com.sun.utils.Tools;
import com.sun.utils.XmlUtil;

public class WXPayUtil {
	// 统一下单
	private static final String CREATE_ORDER = "https://api.mch.weixin.qq.com/pay/unifiedorder";

	// 查询订单
	private static final String QUERY_ORDER = "https://api.mch.weixin.qq.com/pay/orderquery";

	// 关闭订单
	private static final String CLOSE_ORDER = "https://api.mch.weixin.qq.com/pay/closeorder";

	// 申请退款
	private static final String CREATE_REFUND = "https://api.mch.weixin.qq.com/secapi/pay/refund";

	// 查询退款
	private static final String QUERY_REFUND = "https://api.mch.weixin.qq.com/pay/refundquery";

	// 下载对账单
	private static final String DOWNLOAD_BILL = "https://api.mch.weixin.qq.com/pay/downloadbill";

	private static final String CHARSET = "UTF-8";

	private static final String SIGN_TYPE = "MD5";

	private static final String JSAPI = "JSAPI";

	private static final String APP = "APP";

	private static final String PACKAGE = "Sign=WXPay";

	private static final String DEVICE_INFO = "WEB";

	private static final String FEE_TYPE = "CNY";

	private static final String REFUND_SOURCE_UNSETTLED_FUNDS = "REFUND_SOURCE_UNSETTLED_FUNDS";

	private static final String ALL = "ALL";

	private static class SSLClient extends DefaultHttpClient {
		public SSLClient() throws Exception {
			SSLContext ctx = SSLContext.getInstance("TLS");
			X509TrustManager tm = new X509TrustManager() {
				@Override
				public void checkClientTrusted(X509Certificate[] chain,
						String authType) throws CertificateException {
				}

				@Override
				public void checkServerTrusted(X509Certificate[] chain,
						String authType) throws CertificateException {
				}

				@Override
				public X509Certificate[] getAcceptedIssuers() {
					return null;
				}
			};
			ctx.init(null, new TrustManager[] { tm }, null);
			SSLSocketFactory ssf = new SSLSocketFactory(ctx,
					SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
			ClientConnectionManager ccm = getConnectionManager();
			SchemeRegistry sr = ccm.getSchemeRegistry();
			sr.register(new Scheme("https", 443, ssf));
		}
	}

	private static String post(String url, String xml, String charset) {
		HttpClient httpClient = null;
		HttpPost httpPost = null;
		String result = null;
		try {
			httpClient = new SSLClient();
			httpPost = new HttpPost(url);
			if (xml != null) {
				httpPost.setEntity(new StringEntity(xml, charset));
			}
			HttpResponse response = httpClient.execute(httpPost);
			if (response != null) {
				HttpEntity resEntity = response.getEntity();
				if (resEntity != null)
					result = EntityUtils.toString(resEntity, charset);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}

	/**
	 * 时间戳:标准北京时间，时区为东八区，自1970年1月1日 0点0分0秒以来的秒数。注意：部分系统取到的值为毫秒级，需要转换成秒(10位数字)。
	 * 
	 * @return
	 */
	private static String getTimeStamp() {
		return String.valueOf(System.currentTimeMillis() / 1000);
	}

	/**
	 * 获取32位随机字符串
	 * 
	 * @return
	 */
	private static String getNonceStr() {
		return UUID.randomUUID().toString().replace("-", "");
	}

	/**
	 * 统一下单
	 * 
	 * @param appid
	 * @param mchId
	 * @param body
	 * @param attach
	 * @param outTradeNo
	 * @param totalFee
	 * @param spbillCreateIp
	 * @param timeStart
	 * @param timeExpire
	 * @param notifyUrl
	 * @param tradeType
	 * @param openid
	 * @param partnerKey
	 * @return
	 */
	public static String createOrder(String appid, String mchId, String body,
			String attach, String outTradeNo, String totalFee,
			String spbillCreateIp, String timeStart, String timeExpire,
			String notifyUrl, String tradeType, String openid, String partnerKey) {
		return createOrder(appid, mchId, DEVICE_INFO, SIGN_TYPE, body, null,
				attach, outTradeNo, FEE_TYPE, totalFee, spbillCreateIp,
				timeStart, timeExpire, null, notifyUrl, tradeType, null, null,
				openid, partnerKey);
	}

	/**
	 * 统一下单
	 * 
	 * @param appid
	 *            :应用ID
	 * @param mchId
	 *            :商户号
	 * @param deviceInfo
	 *            :设备号
	 * @param signType
	 *            :签名类型
	 * @param body
	 *            :商品描述
	 * @param detail
	 *            :商品详情
	 * @param attach
	 *            :附加数据
	 * @param outTradeNo
	 *            :商户订单号
	 * @param feeType
	 *            :货币类型
	 * @param totalFee
	 *            :总金额
	 * @param spbillCreateIp
	 *            :终端IP
	 * @param timeStart
	 *            :交易起始时间
	 * @param timeExpire
	 *            :交易结束时间
	 * @param goodsTag
	 *            :商品标记
	 * @param notifyUrl
	 *            :通知地址
	 * @param tradeType
	 *            :交易类型，JSAPI--公众号支付、APP--app支付
	 * @param productId
	 *            :商品ID，公众号支付参数
	 * @param limitPay
	 *            :指定支付方式
	 * @param openid
	 *            :用户标识，公众号支付必填参数
	 * @param partnerKey
	 *            :API密钥
	 * @return
	 */
	public static String createOrder(String appid, String mchId,
			String deviceInfo, String signType, String body, String detail,
			String attach, String outTradeNo, String feeType, String totalFee,
			String spbillCreateIp, String timeStart, String timeExpire,
			String goodsTag, String notifyUrl, String tradeType,
			String productId, String limitPay, String openid, String partnerKey) {
		Map<String, String> signMap = new HashMap<String, String>();
		signMap.put("appid", Tools.getDesc(appid, ""));
		signMap.put("mch_id", Tools.getDesc(mchId, ""));
		signMap.put("device_info", Tools.getDesc(deviceInfo, DEVICE_INFO));
		signMap.put("nonce_str", getNonceStr());
		signMap.put("sign_type", Tools.getDesc(signType, SIGN_TYPE));
		signMap.put("body", Tools.getDesc(body, ""));
		signMap.put("detail", Tools.getDesc(detail, ""));
		signMap.put("attach", Tools.getDesc(attach, ""));
		signMap.put("out_trade_no", Tools.getDesc(outTradeNo, ""));
		signMap.put("fee_type", Tools.getDesc(feeType, FEE_TYPE));
		signMap.put("total_fee", Tools.getDesc(totalFee, ""));
		signMap.put("spbill_create_ip", Tools.getDesc(spbillCreateIp, ""));
		signMap.put("time_start", Tools.getDesc(timeStart, ""));
		signMap.put("time_expire", Tools.getDesc(timeExpire, ""));
		signMap.put("goods_tag", Tools.getDesc(goodsTag, ""));
		signMap.put("notify_url", Tools.getDesc(notifyUrl, ""));
		signMap.put("trade_type", Tools.getDesc(tradeType, ""));
		signMap.put("product_id", Tools.getDesc(productId, ""));
		signMap.put("limit_pay", Tools.getDesc(limitPay, ""));
		signMap.put("openid", Tools.getDesc(openid, ""));
		signMap.put("sign", SignUtil.getSign(signMap, partnerKey));
		String template = "<xml><appid><![CDATA[%s]]></appid><mch_id><![CDATA[%s]]></mch_id><device_info><![CDATA[%s]]></device_info><nonce_str><![CDATA[%s]]></nonce_str><sign><![CDATA[%s]]></sign><sign_type><![CDATA[%s]]></sign_type><body><![CDATA[%s]]></body><detail><![CDATA[%s]]></detail><attach><![CDATA[%s]]></attach><out_trade_no><![CDATA[%s]]></out_trade_no><fee_type><![CDATA[%s]]></fee_type><total_fee><![CDATA[%s]]></total_fee><spbill_create_ip><![CDATA[%s]]></spbill_create_ip><time_start><![CDATA[%s]]></time_start><time_expire><![CDATA[%s]]></time_expire><goods_tag><![CDATA[%s]]></goods_tag><notify_url><![CDATA[%s]]></notify_url><trade_type><![CDATA[%s]]></trade_type><product_id><![CDATA[%s]]></product_id><limit_pay><![CDATA[%s]]></limit_pay><openid><![CDATA[%s]]></openid></xml>";
		template = String.format(template, signMap.get("appid"),
				signMap.get("mch_id"), signMap.get("device_info"),
				signMap.get("nonce_str"), signMap.get("sign"),
				signMap.get("sign_type"), signMap.get("body"),
				signMap.get("detail"), signMap.get("attach"),
				signMap.get("out_trade_no"), signMap.get("fee_type"),
				signMap.get("total_fee"), signMap.get("spbill_create_ip"),
				signMap.get("time_start"), signMap.get("time_expire"),
				signMap.get("goods_tag"), signMap.get("notify_url"),
				signMap.get("trade_type"), signMap.get("product_id"),
				signMap.get("limit_pay"), signMap.get("openid"));
		String result = post(CREATE_ORDER, template, CHARSET);
		Map<String, Object> params = XmlUtil.dom2Map(XmlUtil.parse(result));
		boolean flag = SignUtil.checkSign(params, partnerKey);
		if (!flag) {
			return null;
		}
		if ("SUCCESS".equals(Tools.getDesc(params.get("return_code"), "FAIL"))
				&& "SUCCESS".equals(Tools.getDesc(params.get("result_code"),
						"FAIL"))) {
			tradeType = Tools.getDesc(params.get("trade_type"), "");
			if (tradeType.isEmpty()) {
				return null;
			}
			String prepayId = Tools.getDesc(params.get("prepay_id"), "");
			Map<String, String> returnMap = new HashMap<String, String>();
			if (JSAPI.equals(tradeType)) { // 公众号支付
				returnMap.put("appId", appid);
				returnMap.put("timeStamp", getTimeStamp());
				returnMap.put("nonceStr", getNonceStr());
				returnMap.put("package", "prepay_id=" + prepayId);
				returnMap.put("signType", SIGN_TYPE);
				returnMap.put("paySign",
						SignUtil.getSign(returnMap, partnerKey));
				return GsonUtil.toJson(returnMap);
			} else if (APP.equals(tradeType)) { // app支付
				returnMap.put("appid", appid);
				returnMap.put("partnerid", mchId);
				returnMap.put("prepayid", prepayId);
				returnMap.put("package", PACKAGE);
				returnMap.put("noncestr", getNonceStr());
				returnMap.put("timestamp", getTimeStamp());
				returnMap.put("sign", SignUtil.getSign(returnMap, partnerKey));
				return GsonUtil.toJson(returnMap);
			}
			return null;
		}
		return null;
	}

	/**
	 * 查询订单
	 * 
	 * @param appid
	 * @param mchId
	 * @param outTradeNo
	 * @param partnerKey
	 * @return
	 */
	public static String queryOrder(String appid, String mchId,
			String outTradeNo, String partnerKey) {
		return queryOrder(appid, mchId, null, outTradeNo, null, partnerKey);
	}

	/**
	 * 查询订单
	 * 
	 * @param appid
	 *            :公众账号ID
	 * @param mchId
	 *            :商户号
	 * @param transactionId
	 *            :微信订单号
	 * @param outTradeNo
	 *            :商户订单号
	 * @param signType
	 *            :签名类型
	 * @param partnerKey
	 *            :API密钥
	 * @return
	 */
	public static String queryOrder(String appid, String mchId,
			String transactionId, String outTradeNo, String signType,
			String partnerKey) {
		Map<String, String> signMap = new HashMap<String, String>();
		signMap.put("appid", Tools.getDesc(appid, ""));
		signMap.put("mch_id", Tools.getDesc(mchId, ""));
		signMap.put("transaction_id", Tools.getDesc(transactionId, ""));
		signMap.put("out_trade_no", Tools.getDesc(outTradeNo, ""));
		signMap.put("nonce_str", getNonceStr());
		signMap.put("sign_type", Tools.getDesc(signType, SIGN_TYPE));
		signMap.put("sign", SignUtil.getSign(signMap, partnerKey));
		String template = "<xml><appid>%s</appid><mch_id>%s</mch_id><transaction_id>%s</transaction_id><out_trade_no>%s</out_trade_no><nonce_str>%s</nonce_str><sign>%s</sign><sign_type>%s</sign_type></xml>";
		template = String.format(template, signMap.get("appid"),
				signMap.get("mch_id"), signMap.get("transaction_id"),
				signMap.get("out_trade_no"), signMap.get("nonce_str"),
				signMap.get("sign"), signMap.get("sign_type"));
		String result = post(QUERY_ORDER, template, CHARSET);
		Map<String, Object> params = XmlUtil.dom2Map(XmlUtil.parse(result));
		boolean flag = SignUtil.checkSign(params, partnerKey);
		if (!flag) {
			return null;
		}
		return result;
	}

	/**
	 * 关闭订单
	 * 
	 * @param appid
	 * @param mchId
	 * @param outTradeNo
	 * @param partnerKey
	 * @return
	 */
	public static String closeOrder(String appid, String mchId,
			String outTradeNo, String partnerKey) {
		return closeOrder(appid, mchId, outTradeNo, null, partnerKey);
	}

	/**
	 * 关闭订单
	 * 
	 * @param appid
	 *            :公众账号ID
	 * @param mchId
	 *            :商户号
	 * @param outTradeNo
	 *            :商户订单号
	 * @param signType
	 *            :签名类型
	 * @param partnerKey
	 *            :API密钥
	 * @return
	 */
	public static String closeOrder(String appid, String mchId,
			String outTradeNo, String signType, String partnerKey) {
		Map<String, String> signMap = new HashMap<String, String>();
		signMap.put("appid", Tools.getDesc(appid, ""));
		signMap.put("mch_id", Tools.getDesc(mchId, ""));
		signMap.put("out_trade_no", Tools.getDesc(outTradeNo, ""));
		signMap.put("nonce_str", getNonceStr());
		signMap.put("sign_type", Tools.getDesc(signType, SIGN_TYPE));
		signMap.put("sign", SignUtil.getSign(signMap, partnerKey));
		String template = "<xml><appid>%s</appid><mch_id>%s</mch_id><out_trade_no>%s</out_trade_no><nonce_str>%s</nonce_str><sign>%s</sign><sign_type>%s</sign_type></xml>";
		template = String.format(template, signMap.get("appid"),
				signMap.get("mch_id"), signMap.get("out_trade_no"),
				signMap.get("nonce_str"), signMap.get("sign"),
				signMap.get("sign_type"));
		String result = post(CLOSE_ORDER, template, CHARSET);
		Map<String, Object> params = XmlUtil.dom2Map(XmlUtil.parse(result));
		boolean flag = SignUtil.checkSign(params, partnerKey);
		if (!flag) {
			return null;
		}
		return result;
	}

	/**
	 * 申请退款
	 * 
	 * @param appid
	 * @param mchId
	 * @param outTradeNo
	 * @param outRefundNo
	 * @param totalFee
	 * @param refundFee
	 * @param partnerKey
	 * @param certPath
	 * @return
	 * @throws Exception
	 */
	public static String createRefund(String appid, String mchId,
			String outTradeNo, String outRefundNo, String totalFee,
			String refundFee, String partnerKey, String certPath)
			throws Exception {
		return createRefund(appid, mchId, null, null, null, outTradeNo,
				outRefundNo, totalFee, refundFee, null, mchId, null,
				partnerKey, certPath);
	}

	/**
	 * 申请退款
	 * 
	 * @param appid
	 *            :公众账号ID
	 * @param mchId
	 *            :商户号
	 * @param deviceInfo
	 *            :设备号
	 * @param signType
	 *            :签名类型
	 * @param transactionId
	 *            :微信订单号
	 * @param outTradeNo
	 *            :商户订单号
	 * @param outRefundNo
	 *            :商户退款单号
	 * @param totalFee
	 *            :订单金额
	 * @param refundFee
	 *            :退款金额
	 * @param refundFeeType
	 *            :货币种类
	 * @param opUserId
	 *            :操作员
	 * @param refundAccount
	 *            :退款资金来源
	 * @param partnerKey
	 *            :API密钥
	 * @param certPath
	 *            :证书路径
	 * @return
	 * @throws Exception
	 */
	public static String createRefund(String appid, String mchId,
			String deviceInfo, String signType, String transactionId,
			String outTradeNo, String outRefundNo, String totalFee,
			String refundFee, String refundFeeType, String opUserId,
			String refundAccount, String partnerKey, String certPath)
			throws Exception {
		Map<String, String> signMap = new HashMap<String, String>();
		signMap.put("appid", Tools.getDesc(appid, ""));
		signMap.put("mch_id", Tools.getDesc(mchId, ""));
		signMap.put("device_info", Tools.getDesc(deviceInfo, DEVICE_INFO));
		signMap.put("nonce_str", getNonceStr());
		signMap.put("sign_type", Tools.getDesc(signType, SIGN_TYPE));
		signMap.put("transaction_id", Tools.getDesc(transactionId, ""));
		signMap.put("out_trade_no", Tools.getDesc(outTradeNo, ""));
		signMap.put("out_refund_no", Tools.getDesc(outRefundNo, ""));
		signMap.put("total_fee", Tools.getDesc(totalFee, ""));
		signMap.put("refund_fee", Tools.getDesc(refundFee, ""));
		signMap.put("refund_fee_type", Tools.getDesc(refundFeeType, FEE_TYPE));
		signMap.put("op_user_id", Tools.getDesc(opUserId, ""));
		signMap.put("refund_account",
				Tools.getDesc(refundAccount, REFUND_SOURCE_UNSETTLED_FUNDS));
		signMap.put("sign", SignUtil.getSign(signMap, partnerKey));
		String template = "<xml><appid>%s</appid><mch_id>%s</mch_id><device_info>%s</device_info><nonce_str>%s</nonce_str><sign>%s</sign><sign_type>%s</sign_type><transaction_id>%s</transaction_id><out_trade_no>%s</out_trade_no><out_refund_no>%s</out_refund_no><total_fee>%s</total_fee><refund_fee>%s</refund_fee><refund_fee_type>%s</refund_fee_type><op_user_id>%s</op_user_id><refund_account>%s</refund_account></xml>";
		template = String.format(template, signMap.get("appid"),
				signMap.get("mch_id"), signMap.get("device_info"),
				signMap.get("nonce_str"), signMap.get("sign"),
				signMap.get("sign_type"), signMap.get("transaction_id"),
				signMap.get("out_trade_no"), signMap.get("out_refund_no"),
				signMap.get("total_fee"), signMap.get("refund_fee"),
				signMap.get("refund_fee_type"), signMap.get("op_user_id"),
				signMap.get("refund_account"));
		FileInputStream instream = new FileInputStream(Tools.getDesc(certPath,
				""));
		mchId = signMap.get("mch_id"); // 商户号
		KeyStore keyStore = null;
		// Trust own CA and all self-signed certs
		SSLContext sslcontext = null;
		try {
			keyStore = KeyStore.getInstance("PKCS12");
			keyStore.load(instream, mchId.toCharArray());
			sslcontext = SSLContexts.custom()
					.loadKeyMaterial(keyStore, mchId.toCharArray()).build();
		} finally {
			instream.close();
		}
		// Allow TLSv1 protocol only
		SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(
				sslcontext, new String[] { "TLSv1" }, null,
				SSLConnectionSocketFactory.BROWSER_COMPATIBLE_HOSTNAME_VERIFIER);
		CloseableHttpClient httpclient = HttpClients.custom()
				.setSSLSocketFactory(sslsf).build();
		HttpPost httpPost = new HttpPost(CREATE_REFUND);
		httpPost.setHeader("Accept", "*/*");
		httpPost.setHeader("Accept-Charset", CHARSET);
		httpPost.setHeader("Connection", "Keep-Alive");
		httpPost.setHeader("Pragma", "no-cache");
		httpPost.setHeader("User-Agent",
				"Mozilla/4.0 (compatible; MSIE 8.0; Windows NT 6.0)");
		httpPost.setEntity(new StringEntity(template, CHARSET));
		try {
			CloseableHttpResponse response = httpclient.execute(httpPost);
			try {
				String result = EntityUtils.toString(response.getEntity(),
						CHARSET);
				EntityUtils.consume(response.getEntity());
				Map<String, Object> params = XmlUtil.dom2Map(XmlUtil.parse(result));
				boolean flag = SignUtil.checkSign(params, partnerKey);
				if (!flag) {
					return null;
				}
				return result;
			} finally {
				response.close();
			}
		} finally {
			httpclient.close();
			instream.close();
		}
	}

	/**
	 * 查询退款
	 * 
	 * @param appid
	 * @param mchId
	 * @param outTradeNo
	 * @param partnerKey
	 * @return
	 */
	public static String queryRefund(String appid, String mchId,
			String outTradeNo, String partnerKey) {
		return queryRefund(appid, mchId, null, null, null, outTradeNo, null,
				null, partnerKey);
	}

	/**
	 * 查询退款
	 * 
	 * @param appid
	 *            :公众账号ID
	 * @param mchId
	 *            :商户号
	 * @param deviceInfo
	 *            :设备号
	 * @param signType
	 *            :签名类型
	 * @param transactionId
	 *            :微信订单号
	 * @param outTradeNo
	 *            :商户订单号
	 * @param outRefundNo
	 *            :商户退款单号
	 * @param refundId
	 *            :微信退款单号
	 * @param partnerKey
	 *            :API密钥
	 * @return
	 */
	public static String queryRefund(String appid, String mchId,
			String deviceInfo, String signType, String transactionId,
			String outTradeNo, String outRefundNo, String refundId,
			String partnerKey) {
		Map<String, String> signMap = new HashMap<String, String>();
		signMap.put("appid", Tools.getDesc(appid, ""));
		signMap.put("mch_id", Tools.getDesc(mchId, ""));
		signMap.put("device_info", Tools.getDesc(deviceInfo, DEVICE_INFO));
		signMap.put("nonce_str", getNonceStr());
		signMap.put("sign_type", Tools.getDesc(signType, SIGN_TYPE));
		signMap.put("transaction_id", Tools.getDesc(transactionId, ""));
		signMap.put("out_trade_no", Tools.getDesc(outTradeNo, ""));
		signMap.put("out_refund_no", Tools.getDesc(outRefundNo, ""));
		signMap.put("refund_id", Tools.getDesc(refundId, ""));
		signMap.put("sign", SignUtil.getSign(signMap, partnerKey));
		String template = "<xml><appid>%s</appid><mch_id>%s</mch_id><device_info>%s</device_info><nonce_str>%s</nonce_str><sign>%s</sign><sign_type>%s</sign_type><transaction_id>%s</transaction_id><out_trade_no>%s</out_trade_no><out_refund_no>%s</out_refund_no><refund_id>%s</refund_id></xml>";
		template = String.format(template, signMap.get("appid"),
				signMap.get("mch_id"), signMap.get("device_info"),
				signMap.get("nonce_str"), signMap.get("sign"),
				signMap.get("sign_type"), signMap.get("transaction_id"),
				signMap.get("out_trade_no"), signMap.get("out_refund_no"),
				signMap.get("refund_id"));
		String result = post(QUERY_ORDER, template, CHARSET);
		Map<String, Object> params = XmlUtil.dom2Map(XmlUtil.parse(result));
		boolean flag = SignUtil.checkSign(params, partnerKey);
		if (!flag) {
			return null;
		}
		return result;
	}

	/**
	 * 下载账单
	 * 
	 * @param appid
	 * @param mchId
	 * @param billDate
	 * @param partnerKey
	 * @return
	 */
	public static String downloadBill(String appid, String mchId,
			String billDate, String partnerKey) {
		return downloadBill(appid, mchId, null, null, billDate, null, null,
				partnerKey);
	}

	/**
	 * 下载账单
	 * 
	 * @param appid
	 *            :公众账号ID
	 * @param mchId
	 *            :商户号
	 * @param deviceInfo
	 *            :设备号
	 * @param signType
	 *            :签名类型
	 * @param billDate
	 *            :对账单日期
	 * @param billType
	 *            :账单类型
	 * @param tarType
	 *            :压缩账单(暂只支持数据流形式)
	 * @param partnerKey
	 *            :API密钥
	 * @return
	 */
	public static String downloadBill(String appid, String mchId,
			String deviceInfo, String signType, String billDate,
			String billType, String tarType, String partnerKey) {
		Map<String, String> signMap = new HashMap<String, String>();
		signMap.put("appid", Tools.getDesc(appid, ""));
		signMap.put("mch_id", Tools.getDesc(mchId, ""));
		signMap.put("device_info", Tools.getDesc(deviceInfo, DEVICE_INFO));
		signMap.put("nonce_str", getNonceStr());
		signMap.put("sign_type", Tools.getDesc(signType, SIGN_TYPE));
		signMap.put("bill_date", Tools.getDesc(billDate, ""));
		signMap.put("bill_type", Tools.getDesc(billType, ALL));
		signMap.put("tar_type", Tools.getDesc(tarType, ""));
		signMap.put("sign", SignUtil.getSign(signMap, partnerKey));
		String template = "<xml><appid>%s</appid><mch_id>%s</mch_id><device_info>%s</device_info><nonce_str>%s</nonce_str><sign>%s</sign><sign_type>%s</sign_type><bill_date>%s</bill_date><bill_type>%s</bill_type><tar_type>%s</tar_type></xml>";
		template = String.format(template, signMap.get("appid"),
				signMap.get("mch_id"), signMap.get("device_info"),
				signMap.get("nonce_str"), signMap.get("sign"),
				signMap.get("sign_type"), signMap.get("bill_date"),
				signMap.get("bill_type"), signMap.get("tar_type"));
		String result = post(DOWNLOAD_BILL, template, CHARSET);
		return result;
	}
}
