package com.bigrun.assortdemo.assortpinyin;

import net.sourceforge.pinyin4j.PinyinHelper;

public class AssortPinyinList {

	// 获得字符串的首字母 首字符 转汉语拼音
	public String getFirstChar(String value) {
		// 首字符
		char firstChar = value.charAt(0);
		// 首字母分类
		String first = null;
		// 是否是非汉字
		String[] print = PinyinHelper.toHanyuPinyinStringArray(firstChar);

		if (print == null) {
			// 如果是英文，将小写字母改成大写
			if ((firstChar >= 97 && firstChar <= 122)) {
				firstChar -= 32;
				first = String.valueOf((char) firstChar);
			} else if (firstChar >= 65 && firstChar <= 90) {
				first = String.valueOf((char) firstChar);
			} else {
				// 如果认为首字符为数字或者特殊字符
				first = "#";
			}
		} else {
			// 如果是中文 ，得到首字母并大写
			first = String.valueOf((char) (print[0].charAt(0) - 32));
		}

		if (first == null) {
			first = "?";
		}
		return first;
	}

}
