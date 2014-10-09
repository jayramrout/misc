package com.jsonviewer;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JOptionPane;
import javax.swing.JTree;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeSelectionModel;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * @author Jayram Rout
 *
 */
public class TreeView extends javax.swing.JPanel {

	/** Creates new form TreeView */

	public TreeView() {
		init();
	}

	private void init() {
	}

	public JTree getTreeView(InputStream is, String contents) {
		try {
			if (contents == null) {
				contents = Helper.getInputStreamContents(is);
			}

			JSONObject object = null;
			JSONArray array = null;
			DefaultMutableTreeNode node = new DefaultMutableTreeNode(
					"ROOT");

			try {
				object = new JSONObject(contents);
				HashMap<String, Object> fm = Helper
						.getMapFromJSONObject(object);
				Helper.getNodeFromMap(fm, node);
			} catch (JSONException e) {
				try {
					array = new JSONArray(contents);
					ArrayList<Object> list = Helper.getListFromJSONArray(array);
					Helper.getNodeFromList(list, node);
				} catch (JSONException je) {
					try {
						JOptionPane
								.showMessageDialog(
										this,
										"The Source specified does not contain a valid JSON String",
										"Error", JOptionPane.ERROR_MESSAGE);
						return null;
						/*
						 * if(is == null){ JOptionPane.showMessageDialog(this,
						 * "The Source specified does not contain a valid JSON String"
						 * , "Error", JOptionPane.ERROR_MESSAGE); return null; }
						 */// getTreeView(null,
							// Helper.decompressContent(contents));
					} catch (JSONException jje) {
						JOptionPane
								.showMessageDialog(
										this,
										"The Source specified does not contain a valid JSON String",
										"Error", JOptionPane.ERROR_MESSAGE);
						return null;

					}
				}
			}
			jsonTree = new JTree(node);
			jsonTree.setShowsRootHandles(true);
		} catch (JSONException ex) {
			Logger.getLogger(TreeView.class.getName()).log(Level.SEVERE, null,
					ex);
		} catch (Exception ex) {
			Logger.getLogger(TreeView.class.getName()).log(Level.SEVERE, null,
					ex);
		}
		jsonTree.setEditable(Boolean.TRUE);
		jsonTree.getSelectionModel().setSelectionMode(
				TreeSelectionModel.SINGLE_TREE_SELECTION);
		jsonTree.addTreeSelectionListener(new TreeSelectionListener() {
			@Override
			public void valueChanged(TreeSelectionEvent e) {
				DefaultMutableTreeNode node = (DefaultMutableTreeNode) jsonTree
						.getLastSelectedPathComponent();

				if (node == null)
					return;

				Object nodeInfo = node.getUserObject();
				if (node.isLeaf()) {
					System.out.println(nodeInfo);
				} else {
					int childCount = jsonTree.getModel().getChildCount(node);
					for(int i = 0; i < childCount ; i++) {
						//System.out.println(" model.getClass() : "+jsonTree.getModel().getChild(node, i));
					}
				}
			}
		});
		return jsonTree;
	}

	private javax.swing.JTree jsonTree;
}
