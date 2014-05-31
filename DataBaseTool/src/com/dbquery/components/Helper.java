package com.dbquery.components;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.ListIterator;

import javax.swing.tree.DefaultMutableTreeNode;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

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
                DefaultMutableTreeNode keyNode = new DefaultMutableTreeNode(key.replace("\"", "")+" : "+ value.toString().replace("\"", "") );
                /*DefaultMutableTreeNode valueNode = new DefaultMutableTreeNode(value.toString().replace("\"", ""));
                keyNode.add(valueNode);*/
                node.add(keyNode);
            }
        }
    }

    public static void getNodeFromList(ArrayList list, DefaultMutableTreeNode node) {
        ListIterator ite = list.listIterator();
        int index = 0;
        DefaultMutableTreeNode arrayIndexNode;
        while (ite.hasNext()) {
        	arrayIndexNode = new DefaultMutableTreeNode("["+index+++"]");
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
}
