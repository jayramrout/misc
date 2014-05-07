package com.jsonviewer;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JOptionPane;
import javax.swing.JTree;
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

    public JTree getTreeView(InputStream is) {
        try {
            String contents = Helper.getInputStreamContents(is);
            JSONObject object = null;
            JSONArray array = null;
            
            try {            
                object = new JSONObject(contents);
            } catch(JSONException e) {
                try {
                    array = new JSONArray(contents);
                } catch(JSONException je) {
                }
            } finally {
                if(object == null && array == null) {
                    JOptionPane.showMessageDialog(this, "The Source specified does not contain a valid JSON String", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
            DefaultMutableTreeNode node = new DefaultMutableTreeNode("JSONObject");
            if (array == null) {
                HashMap<String, Object> fm = Helper.getMapFromJSONObject(object);
                Helper.getNodeFromMap(fm, node);
                jsonTree = new JTree(node);
            } else {
                ArrayList<Object> list = Helper.getListFromJSONArray(array);
                Helper.getNodeFromList(list, node);
                jsonTree = new JTree(node);
            }
        } catch (JSONException ex) {
            Logger.getLogger(TreeView.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            Logger.getLogger(TreeView.class.getName()).log(Level.SEVERE, null, ex);
        }
        jsonTree.setEditable(Boolean.TRUE);
        jsonTree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
        return jsonTree;
    }
    
    private javax.swing.JTree jsonTree;
    private javax.swing.JScrollPane treeScrollPane;
}

