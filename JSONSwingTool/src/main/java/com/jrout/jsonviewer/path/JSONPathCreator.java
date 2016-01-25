package com.jrout.jsonviewer.path;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;

import com.jrout.jsonviewer.JSONViewer;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * @author Jayram Rout
 *
 */
public class JSONPathCreator {

	public static Set getJSONKeys(String jsonContent) {
		JSONObject object = null;
		JSONArray array = null;
		JSONViewer.treeSet = new TreeSet();
		try {
			object = new JSONObject(jsonContent);
		} catch (JSONException e) {
			try {
				array = new JSONArray(jsonContent);
			} catch (JSONException je) {
			}
		}
		if (object != null) {
			getMapFromJSONObject("$", object);

		} else {
			getListFromJSONArray("$", array);
		}
		return null;
	}
	/**
	 * 
	 * @param object
	 * @return
	 * @throws JSONException
	 */
	public static HashMap<String, Object> getMapFromJSONObject(String jsonPathKey, JSONObject object)
			throws JSONException {
		HashMap<String, Object> retVal = new HashMap<String, Object>();
		Iterator<String> keys = object.keys();
		while (keys.hasNext()) {
			String key = keys.next();

			Object val = object.get(key);
			if (val instanceof JSONArray) {
				getListFromJSONArray(jsonPathKey + "." + key, (JSONArray) val);
			} else if (val instanceof JSONObject) {
				getMapFromJSONObject(jsonPathKey + "." + key, (JSONObject) val);
			} else {
				JSONViewer.treeSet.add(jsonPathKey + "." + key);
			}
		}
		return retVal;
	}
	/**
	 * 
	 * @param array
	 * @return
	 * @throws JSONException
	 */
	public static ArrayList<Object> getListFromJSONArray(String jsonPathKey, JSONArray array) throws JSONException {
		if(array == null ) return null;

		ArrayList<Object> retVal = new ArrayList<Object>();
		int size = array.length();

		for (int i = 0; i < size; i++) {
			Object val = array.get(i);
			if (val instanceof JSONObject) {
				getMapFromJSONObject(jsonPathKey + "[" + i + "]", (JSONObject) val);
			} else if (val instanceof JSONArray) {
				getListFromJSONArray(jsonPathKey + "[" + i + "]", (JSONArray) val);
			} else {
				JSONViewer.treeSet.add(jsonPathKey);
			}
		}
		return retVal;
	}
}
