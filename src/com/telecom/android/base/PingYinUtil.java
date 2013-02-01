package com.telecom.android.base;

import java.util.HashMap;

import net.sourceforge.pinyin4j.PinyinHelper;
import net.sourceforge.pinyin4j.format.HanyuPinyinCaseType;
import net.sourceforge.pinyin4j.format.HanyuPinyinOutputFormat;
import net.sourceforge.pinyin4j.format.HanyuPinyinToneType;
import net.sourceforge.pinyin4j.format.HanyuPinyinVCharType;

/**
 * 璁＄畻姹夊瓧鎷奸煶镄勫伐鍏风被. 
 * @author lsq
 * 
 */
public class PingYinUtil {
	private static String TAG = "PingYinUtil";

	public static void main(String[] args) {
		System.out.println(123);
	}

	private static HanyuPinyinOutputFormat format = new HanyuPinyinOutputFormat();
	static {
		format.setCaseType(HanyuPinyinCaseType.UPPERCASE);
		format.setToneType(HanyuPinyinToneType.WITHOUT_TONE);
		format.setVCharType(HanyuPinyinVCharType.WITH_V);

	}
	private static HashMap<String, String> m = new HashMap<String, String>();

	/**
	 * 寰楀埌涓€涓眽瀛楃殑涓変釜閲嶈瀛楃涓诧细鍏ㄩ儴镄勬眽瀛楁嫾阔筹紝姹夊瓧棣栧瓧姣嶏紝鍚勪釜姹夊瓧棣栧瓧姣?
	 * @param inputString
	 * @return
	 */
	public static String[] getPy(String inputString) {
		StringBuilder output = new StringBuilder();
		String firstName = "";
		StringBuilder allLetters = new StringBuilder();
		try {
			if (inputString == null || "".equals(inputString))
				return new String[] { "", "", "" };
			inputString = inputString.replaceAll("^[\\u4e00-\\u9fa5]+", "")
					.trim();
			char[] input = inputString.trim().toCharArray();
			for (int i = 0; i < input.length; i++) {
				String _t = null;
				if (java.lang.Character.toString(input[i]).matches(
						"[\\u4E00-\\u9FA5]+")) {
					String[] temp = PinyinHelper.toHanyuPinyinStringArray(
							input[i], format);
					_t = temp[0];
				} else
					_t = java.lang.Character.toString(input[i]);
				output.append(_t);
				if (i == 0) {
					firstName = output.substring(0, 1);
				}
				allLetters.append(_t.substring(0, 1));
			}

		} catch (Exception e) {
			return new String[] { "", "", "" };
		}
		return new String[] { output.toString(), firstName,
				allLetters.toString() };

	}

}
