package com.jsonviewer;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Scanner;
import java.util.zip.DataFormatException;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import javax.swing.JOptionPane;
import javax.swing.tree.DefaultMutableTreeNode;

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.output.ByteArrayOutputStream;
import org.apache.solr.common.util.Base64;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
/**
 * @author Jayram Rout
 *
 */
public class Helper {
	
	static List fileList = new ArrayList();
	
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
				DefaultMutableTreeNode keyNode = new DefaultMutableTreeNode(key.replace("\"", "")+" [ ]");
				getNodeFromList((ArrayList) value, keyNode);
				node.add(keyNode);
			} else if (value instanceof HashMap) {
				DefaultMutableTreeNode keyNode = new DefaultMutableTreeNode(key.replace("\"", ""));
				getNodeFromMap((HashMap) value, keyNode);
				node.add(keyNode);
			} else {
				DefaultMutableTreeNode keyValueNode = new DefaultMutableTreeNode(key.replace("\"", "")+" : " + ((value instanceof String) ? "\""+value.toString().replace("\"", "")+"\"" : value));
//				DefaultMutableTreeNode valueNode = new DefaultMutableTreeNode(value.toString().replace("\"", ""));
//				keyNode.add(valueNode);
				node.add(keyValueNode);
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
	 * It checks if a JSON File is Valid.
	 * As Of now I am not using it anywhere.Please remove this comment if its used in future.
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
			try {
                new JSONArray(contents);
                return true;
            } catch(JSONException je) {
            	JOptionPane.showMessageDialog(null, "The Source specified does not contain a valid JSON String", "Error", JOptionPane.ERROR_MESSAGE);
    			return false;
            }
		}
	}
	/**
	 * 
	 * @param tabbedPaneController
	 * @return
	 */
	public String getJSONString(String fileName){
		StringBuilder jsonBuilder = new StringBuilder();

		Scanner scanner = null;
		File file = null;
		FileReader in = null;
		BufferedReader br = null;
		try {
			if (fileName == JSONConstants.DEFAULT) {
				file = new File(fileName);
				br = new BufferedReader(new InputStreamReader(getClass()
						.getResourceAsStream("/StoreJSON.txt")));
			} else {
				file = new File(fileName);
				in = new FileReader(file);
				br = new BufferedReader(in);
			}
			try {
				String line;
				while ((line = br.readLine()) != null) {
					jsonBuilder.append(line);
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		} finally {
			try {
				if (scanner != null)
					scanner.close();

				if (in != null) {
					in.close();
				}
				if (br != null) {
					br.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return jsonBuilder.toString();
	} 
	/**
	 * 
	 * @param fileNames
	 * @param dir
	 * @param fileName
	 * @return
	 */
	public static List<String> getFileNames(List<String> fileNames, Path dir , String fileName){
	    try {
	        DirectoryStream<Path> stream = Files.newDirectoryStream(dir);
	        for (Path path : stream) {
	            if(path.toFile().isDirectory()){
	            	getFileNames(fileNames, path, fileName);
	            }else {
	            	if(path.getFileName().toString().contains(fileName)){
	            		fileNames.add(path.toAbsolutePath().toString());
//	            		System.out.println(path.getFileName());
	            	}
	            }
	        }
	        stream.close();
	    }catch(IOException e){
	        e.printStackTrace();
	    }
	    return fileNames;
	}
	
	/**
	 * 
	 * @param zippedBase64Str
	 * @return
	 * @throws IOException
	 * @throws DataFormatException
	 */
	public static String decompress(String filePath) throws IOException,
			DataFormatException {
		String result = null;
		;
		byte[] bytes = Base64.base64ToByteArray(readFileAsString(filePath));
		GZIPInputStream zi = null;
		try {
			zi = new GZIPInputStream(new ByteArrayInputStream(bytes));
			result = IOUtils.toString(zi);
		} finally {
			IOUtils.closeQuietly(zi);
		}
		System.out.println("decompressed " + result);

		return result;

	}
	/**
	 * 
	 * @param zippedBase64Str
	 * @return
	 * @throws IOException
	 * @throws DataFormatException
	 */
	public static String decompressContent(String content) throws IOException,
			DataFormatException {
		String result = null;
		content.length();
		System.out.println(content.trim().length());
		byte[] bytes = Base64.base64ToByteArray(content.trim());
		GZIPInputStream zi = null;
		try {
			zi = new GZIPInputStream(new ByteArrayInputStream(bytes));
			result = IOUtils.toString(zi);
		} finally {
			IOUtils.closeQuietly(zi);
		}
		//System.out.println("decompressed " + result);

		return result;

	}
	
	/**
	 * 
	 * @param srcTxt
	 * @return
	 * @throws IOException
	 */
	public static String compress(String srcTxt) throws IOException {
		ByteArrayOutputStream rstBao = new ByteArrayOutputStream();
		GZIPOutputStream zos = new GZIPOutputStream(rstBao);
		zos.write(srcTxt.getBytes());
		IOUtils.closeQuietly(zos);

		// byte[] bytes = ByteCompressor.compress(rstBao.toByteArray());
		byte[] bytes = rstBao.toByteArray();
		String comString = Base64.byteArrayToBase64(bytes, 0, bytes.length);
		System.out.println("Compressed" + comString);
		try {
			decompress(comString);
		} catch (DataFormatException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return comString;

	}	
	/**
	 * 
	 * @param filePath
	 * @return
	 * @throws java.io.IOException
	 */
	
	private static String readFileAsString(String filePath)
			throws java.io.IOException {
		StringBuffer fileData = new StringBuffer(1000);
		BufferedReader reader = new BufferedReader(new FileReader(filePath));
		char[] buf = new char[1024];
		int numRead = 0;
		while ((numRead = reader.read(buf)) != -1) {
			String readData = String.valueOf(buf, 0, numRead);
			fileData.append(readData);
			buf = new char[1024];
		}
		reader.close();
		return fileData.toString();
	}
}
