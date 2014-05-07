package com.jsonviewer;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.ListIterator;

import javax.swing.JOptionPane;
import javax.swing.tree.DefaultMutableTreeNode;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * @author Jayram Rout
 *
 */
public class Helper {

	public static String getInputStreamContents(InputStream is) throws IOException {
		String retval = "";
		BufferedReader reader = new BufferedReader(new InputStreamReader(is));
		String line = "";
		StringBuilder builder = new StringBuilder();
		while ((line = reader.readLine()) != null) {
			builder.append(line);
			builder.append("\n");
		}
		retval = builder.toString();
		reader.close();
		return retval;
	}

	public static void getNodeFromMap(HashMap map, DefaultMutableTreeNode node) {
		Iterator<String> ite = map.keySet().iterator();
		while (ite.hasNext()) {
			String key = ite.next();
			Object value = map.get(key);
			if (value instanceof ArrayList) {
				DefaultMutableTreeNode keyNode = new DefaultMutableTreeNode(key.replace("\"", ""));
				getNodeFromList((ArrayList) value, keyNode);
				node.add(keyNode);
			} else if (value instanceof HashMap) {
				DefaultMutableTreeNode keyNode = new DefaultMutableTreeNode(key.replace("\"", ""));
				getNodeFromMap((HashMap) value, keyNode);
				node.add(keyNode);
			} else {
				DefaultMutableTreeNode keyNode = new DefaultMutableTreeNode(key.replace("\"", ""));
				DefaultMutableTreeNode valueNode = new DefaultMutableTreeNode(value.toString().replace("\"", ""));
				keyNode.add(valueNode);
				node.add(keyNode);
			}
		}
		sortchildren(node);
	}
	/**
	 * 
	 * @param list
	 * @param node
	 */
	public static void getNodeFromList(ArrayList list, DefaultMutableTreeNode node) {
		ListIterator ite = list.listIterator();
		int index = 0;
		DefaultMutableTreeNode arrayIndexNode;
		while (ite.hasNext()) {
			arrayIndexNode = new DefaultMutableTreeNode("[" + index++ + "]");
			Object value = ite.next();
			if (value instanceof ArrayList) {
				getNodeFromList((ArrayList) value, arrayIndexNode);
			} else if (value instanceof HashMap) {
				getNodeFromMap((HashMap) value, arrayIndexNode);
			} else {
				arrayIndexNode.add(new DefaultMutableTreeNode(value.toString().replace("\"", "")));
			}
			node.add(arrayIndexNode);
		}
	}
	/**
	 * 
	 * @param object
	 * @return
	 * @throws JSONException
	 */
	public static HashMap<String, Object> getMapFromJSONObject(JSONObject object) throws JSONException {
		HashMap<String, Object> retVal = new HashMap<String, Object>();
		Iterator<String> keys = object.keys();
		while (keys.hasNext()) {
			String key = keys.next();
			Object val = object.get(key);
			if (val instanceof JSONArray) {
				retVal.put(key, getListFromJSONArray((JSONArray) val));
			} else if (val instanceof JSONObject) {
				retVal.put(key, getMapFromJSONObject((JSONObject) val));
			} else {
				retVal.put(key, object.get(key));
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
	public static ArrayList<Object> getListFromJSONArray(JSONArray array) throws JSONException {
		ArrayList<Object> retVal = new ArrayList<Object>();
		int size = array.length();
		for (int i = 0; i < size; i++) {
			Object val = array.get(i);
			if (val instanceof JSONObject) {
				retVal.add(getMapFromJSONObject((JSONObject) val));
			} else if (val instanceof JSONArray) {
				retVal.add(getListFromJSONArray((JSONArray) val));
			} else {
				retVal.add(array.getString(i));
			}
		}
		return retVal;
	}

	/**
	 * 
	 * @param node
	 */
	private static void sortchildren(DefaultMutableTreeNode node) {
		ArrayList children = Collections.list(node.children());
		// for getting original location
		ArrayList<String> orgCnames = new ArrayList<String>();
		// new location
		ArrayList<String> cNames = new ArrayList<String>();
		// move the child to here so we can move them back
		DefaultMutableTreeNode temParent = new DefaultMutableTreeNode();
		for (Object child : children) {
			DefaultMutableTreeNode ch = (DefaultMutableTreeNode) child;
			temParent.insert(ch, 0);
			cNames.add(ch.toString().toUpperCase());
			orgCnames.add(ch.toString().toUpperCase());
		}
		Collections.sort(cNames);
		for (String name : cNames) {
			// find the original location to get from children arrayList
			int indx = orgCnames.indexOf(name);
			node.insert((DefaultMutableTreeNode) children.get(indx), node.getChildCount());
		}
	}
	/**
	 * 
	 * @param test
	 * @return
	 */
	public static boolean isJSONValid(File file) {
		String contents = null;
		try {
			FileInputStream is = new FileInputStream(file);
			contents = Helper.getInputStreamContents(is);
		} catch (IOException e) {
			e.printStackTrace();
		}finally{
		}
		try {
			new JSONObject(contents);
			return true;
		} catch (JSONException ex) {
			JOptionPane.showMessageDialog(null, "The Source specified does not contain a valid JSON String", "Error", JOptionPane.ERROR_MESSAGE);
			return false;
		}
	}
}
