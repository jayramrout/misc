package com.jrout.jsonviewer;

import java.awt.BorderLayout;
import java.awt.Component;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTree;
import javax.swing.JViewport;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.jrout.jsonviewer.path.JSONPathCreator;
import org.codehaus.jackson.map.ObjectMapper;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.SyntaxConstants;
import org.fife.ui.rtextarea.RTextScrollPane;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * @author Jayram Rout
 *
 */
class TabbedPaneController {
	JPanel jPanelMain = null;
	JTabbedPane jTabbedPane;
	JPanel defaultTreePanel = null;
	FileTransferHandler fileTransferHandler;
	boolean noTabs = true;

	String fileSeparator;

	/**
	 * @param jTabbedPane
	 * @param jPanelMain
	 */
	public TabbedPaneController(final JTabbedPane jTabbedPane, JPanel jPanelMain) {
		this.jTabbedPane = jTabbedPane;
		this.jPanelMain = jPanelMain;
		fileTransferHandler = new FileTransferHandler(this);
		fileSeparator = System.getProperty("file.separator");
		if ("\\".equals(fileSeparator)) {
			fileSeparator = "\\\\";
		}
		jTabbedPane.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				int index = jTabbedPane.getSelectedIndex();
				if (index != -1) {
					JTabbedPane jSplitPane = (JTabbedPane) jTabbedPane.getComponentAt(index);
					RTextScrollPane textScrollPane = (RTextScrollPane) jSplitPane.getComponent(1);
					JViewport viewPort = (JViewport) textScrollPane.getComponent(0);
					RSyntaxTextArea textArea = (RSyntaxTextArea) viewPort.getComponent(0);
					JSONPathCreator.getJSONKeys(textArea.getText());

					String fileName = jTabbedPane.getToolTipTextAt(index);
					JFrame rootFrame = (JFrame) SwingUtilities.getRoot(jTabbedPane);
					if (rootFrame != null && fileName != null)
						rootFrame.setTitle(fileName);
				}
			}
		});
		init();
	}

	/**
	 * @param filename
	 * @return
	 */
	public void addTab(String filename) {
		if (noTabs) {
			jPanelMain.add(jTabbedPane, BorderLayout.CENTER);
			noTabs = false;
		}
		String[] str = filename.split(fileSeparator);
		makeJTreePane(str[str.length - 1], filename);
	}

	/**
	 * Remove all tabs and their components, then put the default file area
	 * back.
	 */
	public void clearAll() {
		if (noTabs == false) {
			jTabbedPane.removeAll();
			jPanelMain.remove(jTabbedPane);
			initializeTab = true;
			Helper.fileList.clear();
		}
		init();
	}

	boolean initializeTab = true;

	private void init() {
		noTabs = true;
		if (initializeTab) {
			initializeTab = false;
			createPanel("Store", JSONConstants.DEFAULT);
			jPanelMain.repaint();
		}
	}

	/**
	 * @param name
	 * @param fileName
	 */
	protected void makeJTreePane(String name, String fileName) {
		createPanel(name, fileName);
		initTabComponent();
	}

	/**
	 * 
	 * @param name
	 * @param filePath
	 *            This API first checks if the file is already opened. Of Opened
	 *            then it gives a popup Error saying File Already Present.
	 */
	private void createPanel(String name, String filePath) {
		if (Helper.fileList.contains(filePath)) {
			JOptionPane.showMessageDialog(null, "File Already Present", "Error", JOptionPane.ERROR_MESSAGE);
			return;
		} else {
			Helper.fileList.add(filePath);
		}

		InputStream is = null;
		try {
			if (name.equals("Store"))
				is = getClass().getResourceAsStream("/StoreJSON.txt");
			else
				is = new FileInputStream(new File(filePath));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		TreeView tv = new TreeView();
		JTree treePane = tv.getTreeView(is, null);

		if (treePane == null)
			return;

		treePane.setTransferHandler(fileTransferHandler);

		JScrollPane treeScrollPane = new JScrollPane(treePane);
		treeScrollPane.setBorder(BorderFactory.createEmptyBorder(1, 0, 0, 0));
		RSyntaxTextArea jsonTextArea = new RSyntaxTextArea(20, 60);
		jsonTextArea.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_JSON);
		jsonTextArea.setCodeFoldingEnabled(true);
		jsonTextArea.setEditable(true);
		// jsonTextArea.setTransferHandler(fileTransferHandler);

		RTextScrollPane jsonTextScrollPane = new RTextScrollPane(jsonTextArea);
		jsonTextScrollPane.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));

		ObjectMapper mapper = new ObjectMapper();
		try {
			if (name.equals("Store")) {
				jsonTextArea.read(new InputStreamReader(getClass().getResourceAsStream("/ReadMe.txt")), null);
			} else {

				Object json = null;
				String getJsonStringFromFile = new Helper().getJSONString(filePath);
				try{
					json = new JSONObject(getJsonStringFromFile);
				}catch(JSONException exp){
					json = new JSONArray(getJsonStringFromFile);
				}
				Object jsonObj = mapper.readValue(json.toString(), Object.class);
				// System.out.println(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(json));
				jsonTextArea.read(new StringReader(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(jsonObj)),
						null);
 			}
		} catch (IOException e) {
			e.printStackTrace();
		}

		// JTable table = new JTable();
		DynamicTable dTable = new DynamicTable();
		tv.setDynamicTable(dTable);
		JSplitPane treePropSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, treeScrollPane, new JScrollPane(
				new JTable(dTable)));
		treePropSplitPane.setDividerLocation(600);

		JTabbedPane treeJsonTab = new JTabbedPane();
		treeJsonTab.addTab("View", treePropSplitPane);
		treeJsonTab.addTab("Json", jsonTextScrollPane);

		jTabbedPane.addTab(name, null, (Component) treeJsonTab, filePath);
		jTabbedPane.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
		jTabbedPane.setSelectedComponent((Component) treeJsonTab);
		jPanelMain.add(jTabbedPane, BorderLayout.CENTER);
	}

	private void initTabComponent() {
		jTabbedPane.setTabComponentAt(jTabbedPane.getSelectedIndex(), new ButtonTabComponent(this));

	}
}