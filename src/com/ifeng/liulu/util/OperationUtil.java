package com.ifeng.liulu.util;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

public class OperationUtil {
	public static void printInfoMap(HashMap<String, Object> hashMap) {
		Set<String> keys = hashMap.keySet();
		Iterator<String> iterator = keys.iterator();
		while (iterator.hasNext()) {
			String key = iterator.next();
			Object value = hashMap.get(key);
			System.out.println(key + " : " + value);
		}
	}
}
