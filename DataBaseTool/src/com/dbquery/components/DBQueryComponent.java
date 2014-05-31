package com.dbquery.components;

import static java.awt.event.InputEvent.CTRL_DOWN_MASK;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FileDialog;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Scanner;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.ButtonGroup;
import javax.swing.DefaultCellEditor;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JProgressBar;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.JTree;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumn;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultHighlighter;
import javax.swing.text.Highlighter;
import javax.swing.text.JTextComponent;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import jsyntaxpane.DefaultSyntaxKit;

import org.codehaus.jackson.map.ObjectMapper;
import org.oxbow.swingbits.table.filter.TableRowFilterSupport;

import com.dbquery.domain.Column;
import com.dbquery.domain.DataBase;
import com.dbquery.domain.IndexWrapper;
import com.dbquery.domain.Schema;
import com.dbquery.domain.Table;
import com.dbquery.model.ResultModel;
import com.dbquery.util.DBQueryUtil;

/**
 * @author Jayram Rout
 * 
 */
public class DBQueryComponent extends JFrame {
	private static final boolean DEBUG_LOGS = false;

	private boolean consoleOutputRequired = false;
	private final JTabbedPane jTabbedPaneResult = new JTabbedPane();
	public static int index = 0;
	private String initialMessage = "This Tool Support : SELECT UPDATE DELETE DESCRIBE : Operations\n"
			+ "Choose the environment(default is Integ) , disable/enable QueryName Popup(This is used to give a name to the query you execute)\n"
			+ "Opens a saved file to load in the editor\n"
			+ "Tips:\n"
			+ "\tSelect the SQL and press Alt+R or F5 or the GREEN button on the top left to execute the Query.\n"
			+ "\tTo Save the table in excel , Right click and choose Export To Excel\n"
			+ "\tYou can add the table in the CUSTOM schema by adding an entry in the config.properties . Add table name entry for custom_tables separated by comma.\n"
			+ "\tTriple Click on a cell to see the value in a popup Editor\n";
	private String queryText = "";
	private String queryName = "";
	final JEditorPane codeEditor;
	private static String hostname;
	private String environment = Environment.Integ.getValue();
	private boolean inputRequired = false;
	private String filePath;
	private String mainTitle;
	static Properties prop = null;
	private JTree tree;
	private DefaultMutableTreeNode nhSchema = null;
	static {
		FileWriter fw = null;
		InputStream inputStream = null;
		boolean openingFirstTime = false;
		FileInputStream fis = null;
		String userDir = System.getProperty("user.dir");
		String filePathOne = userDir + "/config.properties";
		String filePathTwo = userDir + "/src/config.properties";
		String currentFilePathInUse = "";
		try {
			prop = new Properties();

			File fileOne = new File(filePathOne);
			File fileTwo = new File(filePathTwo);

			if (fileOne.exists()) {
				fis = new FileInputStream(fileOne);
				currentFilePathInUse = fileOne.getAbsolutePath();
				prop.load(fis);
			} else if (fileTwo.exists()) {
				fis = new FileInputStream(fileTwo);
				currentFilePathInUse = fileTwo.getAbsolutePath();
				prop.load(fis);
			} else {
				openingFirstTime = true;
				inputStream = DBQueryComponent.class.getClassLoader().getResourceAsStream("config.properties");
				fw = new FileWriter(fileOne);
				int c = inputStream.read();
				while (c != -1) {
					fw.write(c);
					c = inputStream.read();
				}
				JOptionPane.showMessageDialog(null, new String[]{"Edit config.properties file under the current location and then run the application again"},
						"Result", JOptionPane.PLAIN_MESSAGE);
			}
			if (!DBQueryUtil.isUserNamePasswordCorrect(prop)) {
				DBQueryUtil.showUserNamePwdOption(prop);
				prop.store(new FileOutputStream(currentFilePathInUse), "jdbc:db2://<IPADDRESS>:<PORT>/DSNA:currentSchema=<SchemaName>;");
			}

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if (inputStream != null)
					inputStream.close();
				if (fw != null)
					fw.close();
			} catch (IOException ioe) {
			}
		}
		if (openingFirstTime)
			System.exit(0);

		InputStream db2ErrorCodeInputStream = DBQueryComponent.class.getClassLoader().getResourceAsStream("db2ErrorCode.properties");
		try {
			prop.load(db2ErrorCodeInputStream);
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (db2ErrorCodeInputStream != null) {
					db2ErrorCodeInputStream.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		try {
			InetAddress addr;
			addr = InetAddress.getLocalHost();
			hostname = addr.getHostName();
			if (hostname != null && hostname.length() >= 1) {
				hostname = hostname.substring(5, hostname.length() - 1);
				hostname += " to ";
			}
		} catch (Exception exp) {
			exp.printStackTrace();
			hostname = "to ";
		}
	}

	public static void replace(String newstring, File in, File out) throws IOException {

		BufferedReader reader = new BufferedReader(new FileReader(in));
		PrintWriter writer = new PrintWriter(new FileWriter(out));
		String line = null;
		while ((line = reader.readLine()) != null)
			writer.println(line.replaceAll("MYDACID", newstring));

		// I'm aware of the potential for resource leaks here. Proper resource
		// handling has been omitted in the interest of brevity
		reader.close();
		writer.close();
	}
	private class ButtonActionListener implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			environment = e.getActionCommand();
		}
	}
	public DBQueryComponent() {
		super("Welcome " + hostname + "New Heights Query");
		mainTitle = getTitle();
		setLayout(new BorderLayout());

		JMenuBar menuBar = new JMenuBar();
		setJMenuBar(menuBar);

		JMenu fileMenu = new JMenu("File");
		fileMenu.setMnemonic(KeyEvent.VK_F);
		menuBar.add(fileMenu);

		JMenuItem openItem = new JMenuItem("Open");
		openItem.setMnemonic(KeyEvent.VK_O);
		openItem.setAccelerator(KeyStroke.getKeyStroke('O', CTRL_DOWN_MASK));

		JMenuItem saveItem = new JMenuItem("Save");
		saveItem.setMnemonic(KeyEvent.VK_S);
		saveItem.setAccelerator(KeyStroke.getKeyStroke('S', CTRL_DOWN_MASK));

		final JMenuItem saveAsItem = new JMenuItem("Save As");
		saveAsItem.setMnemonic(KeyEvent.VK_A);

		JMenuItem quitM = new JMenuItem("Quit", KeyEvent.VK_Q);

		fileMenu.add(openItem);
		fileMenu.add(saveItem);
		fileMenu.add(saveAsItem);
		fileMenu.add(quitM);

		JMenu optionMenu = new JMenu("Option");
		optionMenu.setMnemonic(KeyEvent.VK_O);
		menuBar.add(optionMenu);

		final JCheckBoxMenuItem cbMenuItem = new JCheckBoxMenuItem("Query Name Needed");
		cbMenuItem.setSelected(false);
		optionMenu.add(cbMenuItem);
		cbMenuItem.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				inputRequired = cbMenuItem.isSelected();
			}
		});

		optionMenu.addSeparator();
		final JCheckBoxMenuItem cbEnableOutputMenuItem = new JCheckBoxMenuItem("Output Log to Console");
		cbEnableOutputMenuItem.setSelected(false);
		optionMenu.add(cbEnableOutputMenuItem);
		cbEnableOutputMenuItem.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				consoleOutputRequired = cbEnableOutputMenuItem.isSelected();
			}
		});

		openItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				FileDialog fd = new FileDialog(DBQueryComponent.this, "Select File", FileDialog.LOAD);
				fd.show();
				if (fd.getFile() != null) {
					filePath = fd.getDirectory() + fd.getFile();
					setTitle(mainTitle + " " + fd.getFile());
					try {
						load(codeEditor, filePath);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		});

		saveItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					if (filePath == null) {

						FileDialog fd = new FileDialog(DBQueryComponent.this, "Save File", FileDialog.SAVE);
						fd.show();
						if (fd.getFile() != null) {
							filePath = fd.getDirectory() + fd.getFile();
							File f = new File(filePath);
							saveToFile(f);
							setTitle(mainTitle + " " + fd.getFile());
						}
					} else {
						save(codeEditor, filePath);
					}
				} catch (Exception e1) {
					e1.printStackTrace();
				}
			}
		});
		saveAsItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					FileDialog fd = new FileDialog(DBQueryComponent.this, "Save File As", FileDialog.SAVE);
					fd.show();
					if (fd.getFile() != null) {
						filePath = fd.getDirectory() + fd.getFile();
						File f = new File(filePath);
						saveToFile(f);
						setTitle(mainTitle + " " + fd.getFile());
					}
				} catch (Exception e1) {
					e1.printStackTrace();
				}
			}
		});

		quitM.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				System.exit(0);
			}
		});

		JButton submitButton = new JButton();
		try {
			Image img = ImageIO.read(getClass().getResource("/RunImage.png"));
			img = img.getScaledInstance(18, 18, java.awt.Image.SCALE_SMOOTH);

			submitButton.setIcon(new ImageIcon(img));
		} catch (IOException ex) {
		}

		submitButton.setMnemonic(KeyEvent.VK_R);

		DefaultSyntaxKit.initKit();
		codeEditor = new JEditorPane();

		codeEditor.addMouseListener(new MouseListener() {
			@Override
			public void mouseClicked(MouseEvent arg0) {
				if (DEBUG_LOGS)
					System.out.println("DBQueryComponent.DBQueryComponent().new MouseListener() {...}.mouseEntered()");
			}
			@Override
			public void mouseEntered(MouseEvent arg0) {
				if (DEBUG_LOGS)
					System.out.println("DBQueryComponent.DBQueryComponent().new MouseListener() {...}.mouseEntered()");
			}
			@Override
			public void mouseExited(MouseEvent arg0) {
				if (DEBUG_LOGS)
					System.out.println("DBQueryComponent.DBQueryComponent().new MouseListener() {...}.mouseExited()");
			}
			@Override
			public void mouseReleased(MouseEvent arg0) {
				if (DEBUG_LOGS)
					System.out.println("DBQueryComponent.DBQueryComponent().new MouseListener() {...}.mouseReleased()");
				hideSuggestion();
			}

			@Override
			public void mousePressed(MouseEvent arg0) {
				if (DEBUG_LOGS)
					System.out.println("DBQueryComponent.DBQueryComponent().new MouseListener() {...}.mousePressed()");
				if (!arg0.isShiftDown() && arg0.getClickCount() == 1) {
					removeAllHighlights();
				}

				if (arg0.getClickCount() == 3) {
					if (DEBUG_LOGS)
						System.out.println("DBQueryComponent.DBQueryComponent().new MouseListener() {...}.mousePressed() 3");
					removeAllHighlights();
				} else if (arg0.getClickCount() == 2) {
					highlighter(2);
				}

			}
		});
		codeEditor.addKeyListener(new KeyListener() {

			@Override
			public void keyTyped(KeyEvent e) {
				if (DEBUG_LOGS)
					System.out.println("Key Typed " + e.getKeyCode() + " " + e.getKeyChar());
				if (e.getKeyChar() == KeyEvent.VK_ENTER) {
					if (suggestion != null) {
						if (suggestion.insertSelection()) {
							e.consume();
							final int position = codeEditor.getCaretPosition();
							SwingUtilities.invokeLater(new Runnable() {
								@Override
								public void run() {
									try {
										codeEditor.getDocument().remove(position - 1, 1);
									} catch (BadLocationException e) {
										e.printStackTrace();
									}
								}
							});
						}
					}
				}
			}

			/**
			 * 46 is a dot character , 8 is backspace
			 */
			@Override
			public void keyReleased(KeyEvent e) {
				if (DEBUG_LOGS)
					System.out.println("Key Release " + e.getKeyCode() + " " + e.getKeyChar());
				if (e.getKeyCode() == KeyEvent.VK_DOWN && suggestion != null) {
					suggestion.moveDown();
				} else if (e.getKeyCode() == KeyEvent.VK_UP && suggestion != null) {
					suggestion.moveUp();
				} else if (Character.isLetterOrDigit(e.getKeyChar()) || 46 == e.getKeyCode() || 8 == e.getKeyCode()) {
					showTableSuggestionLater();
				} else if (!e.isControlDown() && Character.isWhitespace(e.getKeyChar())) {
					removeAllHighlights();
					hideSuggestion();
				}
			}

			/**
			 * 116 is F5 27 is Esc
			 */
			@Override
			public void keyPressed(KeyEvent e) {
				if (DEBUG_LOGS)
					System.out.println("Key Pressed " + e.getKeyCode() + " " + e.getKeyChar());
				if (e.getKeyCode() == 116) {
					performAction();
				} else if (e.getKeyCode() == 27) {
					hideSuggestion();
				}

			}
		});
		final JScrollPane editorScrollPane = new JScrollPane(codeEditor);
		editorScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
		editorScrollPane.setPreferredSize(new Dimension(1100, 500));

		Box box = Box.createVerticalBox();
		box.add(editorScrollPane);

		JPanel buttonsPanel = new JPanel(new GridLayout(0, 1));
		buttonsPanel.setLayout(new GridBagLayout());

		JRadioButton unitButton = new JRadioButton(Environment.Unit.getValue());
		JRadioButton integButton = new JRadioButton(Environment.Integ.getValue());
		integButton.setSelected(Boolean.TRUE);
		JRadioButton systemButton = new JRadioButton(Environment.System.getValue());
		JRadioButton otherButton = new JRadioButton(Environment.Other.getValue());

		unitButton.addActionListener(new ButtonActionListener());
		integButton.addActionListener(new ButtonActionListener());
		systemButton.addActionListener(new ButtonActionListener());
		otherButton.addActionListener(new ButtonActionListener());

		ButtonGroup group = new ButtonGroup();
		group.add(unitButton);
		group.add(integButton);
		group.add(systemButton);
		group.add(otherButton);

		buttonsPanel.add(unitButton);
		buttonsPanel.add(integButton);
		buttonsPanel.add(systemButton);
		buttonsPanel.add(otherButton);

		buttonsPanel.add(submitButton);

		JSplitPane schemaQueryEditorSplitPanel = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
		JSplitPane mainPanel = new JSplitPane(JSplitPane.VERTICAL_SPLIT);

		// Create the nodes.
		nhSchema = new DefaultMutableTreeNode("NH Schema");
		createNodes(nhSchema);

		// Create a tree that allows one selection at a time.
		tree = new JTree(nhSchema);
		tree.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent me) {
				if (DEBUG_LOGS)
					System.out.println("DBQueryComponent.DBQueryComponent().new MouseAdapter() {...}.mouseClicked()");
				if (me.getClickCount() == 2)
					doMouseClicked(me);
			}
		});

		tree.setEditable(true);
		tree.getSelectionModel().setSelectionMode(TreeSelectionModel.DISCONTIGUOUS_TREE_SELECTION);
		final JScrollPane schemaTreeScrollPane = new JScrollPane(tree);
		schemaTreeScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
		schemaTreeScrollPane.setPreferredSize(new Dimension(250, 500));

		JSplitPane buttonSchemaPanel = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
		buttonSchemaPanel.setEnabled(false);
		buttonSchemaPanel.setDividerSize(0);

		buttonSchemaPanel.add(buttonsPanel);
		buttonSchemaPanel.add(schemaTreeScrollPane);

		schemaQueryEditorSplitPanel.add(buttonSchemaPanel);
		schemaQueryEditorSplitPanel.add(box);

		mainPanel.add(schemaQueryEditorSplitPanel);
		mainPanel.add(jTabbedPaneResult);

		add(mainPanel);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		codeEditor.setContentType("text/sql");
		codeEditor.setText(initialMessage);

		submitButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (DEBUG_LOGS)
					System.out.println("DBQueryComponent.DBQueryComponent().new ActionListener() {...}.actionPerformed()");
				performAction();
			}
		});
	}

	/**
	 * 
	 */
	private void performAction() {
		setQueryText();
		final JDialog dlg = new JDialog(DBQueryComponent.this, "Progress Dialog", true);
		JProgressBar dpb = new JProgressBar(0, 500);
		dpb.setIndeterminate(true);
		dpb.setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
		dlg.add(BorderLayout.CENTER, dpb);
		dlg.add(BorderLayout.NORTH, new JLabel("Query in Progress..."));
		dlg.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		dlg.setSize(300, 75);
		dlg.setLocationRelativeTo(DBQueryComponent.this);

		final Thread t1 = new Thread(new Runnable() {
			public void run() {
				dlg.setVisible(true);
			}
		});
		t1.start();

		final Thread showResultThread = new Thread(new Runnable() {
			public void run() {
				try {
					showResult(dlg);
					dlg.setVisible(false);
				} catch (SQLException exp) {
					exp.printStackTrace();
					dlg.setVisible(false);
					JOptionPane.showMessageDialog(DBQueryComponent.this,
							new String[]{exp.getClass().getName() + ": ", exp.getMessage(), prop.getProperty(exp.getErrorCode() + "")}, "Exception",
							JOptionPane.ERROR_MESSAGE);
				} catch (Exception exp) {
					dlg.setVisible(false);
					JOptionPane.showMessageDialog(DBQueryComponent.this, new String[]{exp.getClass().getName() + ": ", exp.getMessage()}, "Exception",
							JOptionPane.ERROR_MESSAGE);
				}
			}
		});
		showResultThread.start();

		dlg.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent evt) {
				if (resultModel != null) {
					resultModel.closeConnections();
					resultModel.data = null;
					resultModel.columnNames = null;
					resultModel = null;
					showResultThread.stop();
				}
			}
		});

	}

	/**
	 * 
	 */
	private void setQueryText() {
		String selectedText = codeEditor.getSelectedText();
		if (selectedText != null && selectedText.length() > 6) {
			queryText = selectedText;
		} else {
			queryText = codeEditor.getText();
		}
		if (inputRequired) {
			queryName = JOptionPane.showInputDialog("Enter a Name for the Query");
		} else {
			queryName = "";
		}

	}

	ResultModel resultModel = null;

	private void showResult(final JDialog dlg) throws Exception, SQLException {
		try {
			// resultModel = new ResultModel();
			// resultModel.generateXML(environment, prop);
			resultModel = new ResultModel(queryText, environment, prop);
		} catch (SQLException exp) {
			exp.printStackTrace();
			throw exp;
		} catch (Exception ex) {
			ex.printStackTrace();
			throw ex;
		}

		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				if (resultModel.isDeleteUpdateOperation()) {
					final Object[][] data = resultModel.data;
					final String[] columnNames = resultModel.columnNames;
					dlg.setVisible(false);
					JOptionPane.showMessageDialog(DBQueryComponent.this, new String[]{(String) columnNames[0] + ": " + (String) data[0][0]}, "Result",
							JOptionPane.PLAIN_MESSAGE);
					return;
				} else {
					dlg.setVisible(false);
					// "Column Count :" + resultModel.columnNames.length
					JOptionPane.showMessageDialog(DBQueryComponent.this, new String[]{"Row Count : " + resultModel.data.length}, "Results Displayed...",
							JOptionPane.PLAIN_MESSAGE);
				}
			}
		});

		if (resultModel != null && resultModel.isDeleteUpdateOperation() || (resultModel.data != null && resultModel.data.length == 0)) {
			return;
		}

		final JTable table = new JTable(resultModel.data, resultModel.columnNames) {
			/*
			 * Have overridden this , bcoz if u edit a cell which is of Clob
			 * type , then the system hangs
			 * 
			 * @see javax.swing.JTable#isCellEditable(int, int)
			 */
			@Override
			public boolean isCellEditable(int row, int column) {
				return false;
			}
		};
		JTableHeader header = table.getTableHeader();
		ColumnHeaderToolTips tips = new ColumnHeaderToolTips();
		for (int c = 0; c < table.getColumnCount(); c++) {
			TableColumn col = table.getColumnModel().getColumn(c);

			tips.setToolTip(col, col.getHeaderValue().toString());
		}
		header.addMouseMotionListener(tips);

		final JPopupMenu popupMenu = new JPopupMenu();
		Image img = ImageIO.read(getClass().getResource("/ExportToExcel.png"));
		img = img.getScaledInstance(18, 18, java.awt.Image.SCALE_DEFAULT);
		JMenuItem deleteItem = new JMenuItem("Export To Excel", new ImageIcon(img));

		deleteItem.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					FileDialog fd = new FileDialog(DBQueryComponent.this, "Save File As", FileDialog.SAVE);
					fd.setFile("*.xls");
					fd.setVisible(true);
					if (fd.getFile() != null) {
						String fileName = fd.getFile();
						if (!(fileName.endsWith(".xls") || fileName.endsWith(".XLS"))) {
							fileName = fileName + ".xls";
						}
						System.out.println("DBQueryComponent.showResult().new MouseAdapter() {...}.mouseClicked()" + fd.getFile());
						filePath = fd.getDirectory() + fileName;
						File f = new File(filePath);
						DBQueryUtil.toExcelFile(table, f);
					}
				} catch (Exception e1) {
					e1.printStackTrace();
					JOptionPane.showMessageDialog(DBQueryComponent.this, new String[]{e1.getClass().getName() + ": ", e1.getMessage()},
							"Exception While Exporting to Excel ", JOptionPane.ERROR_MESSAGE);
				}
			}
		});
		popupMenu.add(deleteItem);
		table.setComponentPopupMenu(popupMenu);
		table.setCellSelectionEnabled(true);

		DefaultCellEditor onSingleClick = new DefaultCellEditor(new JTextField());
		onSingleClick.setClickCountToStart(2);

		// set the editor to the every column
		for (int i = 0; i < table.getColumnCount(); i++) {
			table.setDefaultEditor(table.getColumnClass(i), onSingleClick);
		}

		table.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
				if (e.getClickCount() == 3) {
					JTable target = (JTable) e.getSource();
					final int row = target.getSelectedRow();
					final int column = target.getSelectedColumn();

					final javax.swing.table.TableModel model = table.getModel();

					SwingUtilities.invokeLater(new Runnable() {
						@Override
						public void run() {
							JFrame popupFrame = new JFrame();
							popupFrame.setSize(300, 600);

							final JEditorPane cellEditor;
							DefaultSyntaxKit.initKit();
							cellEditor = new JEditorPane();

							TreeView tv = new TreeView();
							JTree treePane = tv.getTreeView(model.getValueAt(row, column) + "");
							final JScrollPane editorScrollPane = new JScrollPane(treePane);
							
							if(treePane == null ) {
								editorScrollPane.setViewportView(cellEditor);
								cellEditor.setContentType("text/xml");
								cellEditor.setText(model.getValueAt(row, column) + "");
							}
							
							editorScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
							editorScrollPane.setPreferredSize(new Dimension(1100, 500));

							popupFrame.add(editorScrollPane);
							popupFrame.show();
						}
					});
				}
			}
		});

		TableRowFilterSupport.forTable(table).apply();
		table.setAutoCreateRowSorter(true);
		JScrollPane scrollPane = new JScrollPane(table);
		table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		scrollPane.setPreferredSize(new Dimension(700, 150));
		if (resultModel == null)
			return;
		jTabbedPaneResult.addTab(DBQueryUtil.getShortName(environment) + " " + "Rows:" + resultModel.data.length
				+ ((queryName != null && !"".equals(queryName)) ? " for query : " + queryName : ""), scrollPane);
		UIManager.put("Table.alternateRowColor", Color.getHSBColor(0, 0, 87));

		table.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
				if (consoleOutputRequired) {
					printDebugData(table);
				}
			}
		});

		int tabIndex = index++;
		jTabbedPaneResult.setToolTipTextAt(tabIndex, getToolTip(queryText));
		initTabComponent(tabIndex);
		jTabbedPaneResult.setSelectedIndex(index - 1);
	}

	private String getToolTip(String queryText) {
		Scanner scanner = new Scanner(queryText);
		String s = null;
		StringBuilder sb = new StringBuilder("<html>");
		while (scanner.hasNextLine()) {
			s = scanner.nextLine();
			if (s != null && !(s.trim().startsWith("--")) && s.trim().length() > 0)
				sb = sb.append(s + "<br>");
		}
		return sb.toString();
	}

	public void showQueryPanel() {
		jTabbedPaneResult.setTabLayoutPolicy(JTabbedPane.WRAP_TAB_LAYOUT);
		pack();
		setLocationRelativeTo(null);
		setExtendedState(MAXIMIZED_BOTH);
		setVisible(true);
	}

	private void initTabComponent(int i) {
		jTabbedPaneResult.setTabComponentAt(i, new ButtonTabComponent(jTabbedPaneResult, queryText));
	}

	private void printDebugData(JTable table) {
		int numRows = table.getRowCount();
		int numCols = table.getColumnCount();
		javax.swing.table.TableModel model = table.getModel();

		System.out.println("Value of data: ");
		System.out.print("\n  ");
		for (int i = 0; i < numCols; i++) {
			System.out.print("\t" + model.getColumnName(i));
		}
		System.out.println();
		for (int i = 0; i < numRows; i++) {
			for (int j = 0; j < numCols; j++) {
				System.out.print("\t" + model.getValueAt(i, j));
			}
			System.out.println();
		}
		System.out.println("--------------------------");
	}

	public static void save(JTextComponent text, String inputFile) throws Exception {
		FileWriter writer = null;
		writer = new FileWriter(inputFile);
		text.write(writer);
		writer.close();
	}

	public static void load(JTextComponent text, String inputFile) throws Exception {
		FileReader inputReader = null;
		inputReader = new FileReader(inputFile);
		text.read(inputReader, inputFile);
		inputReader.close();
	}

	private void saveToFile(File fArg) {
		PrintWriter pw = null;

		try {
			pw = new PrintWriter(new BufferedWriter(new FileWriter(fArg)));
		} catch (IOException e) {
			popupError("Can't open file '" + fArg.getName() + "' for writing");
			return;
		}
		pw.print(codeEditor.getText());
		pw.close();
	}

	private void popupError(String s) {
		System.out.print("\07");
		System.out.flush();
		JOptionPane.showMessageDialog(this, s, "Error", JOptionPane.ERROR_MESSAGE);
		return;
	}

	/**
	 * 
	 * @param top
	 */
	private void createNodes(DefaultMutableTreeNode top) {
		DefaultMutableTreeNode schemaTree = null;
		DefaultMutableTreeNode column = null;
		// File file = null;
		// String fileName = "NHSchemas.json";
		InputStream inputStream = null;
		String customTables = prop.getProperty("custom_tables");
		if (customTables != null)
			customTables = customTables.trim();

		try {

			// Code for custom Schema
			DefaultMutableTreeNode customSchemaName = new DefaultMutableTreeNode("CUSTOM");
			top.add(customSchemaName);
			inputStream = DBQueryComponent.class.getClassLoader().getResourceAsStream("NHSchemas.json");
			ObjectMapper mapper = new ObjectMapper();
			DataBase dataBase = mapper.readValue(inputStream, DataBase.class);
			List<Schema> schemas = dataBase.getSchemas();
			for (Schema schema : schemas) {
				String schemaName = schema.getName();
				schemaTree = new DefaultMutableTreeNode(schemaName);
				List<Table> tables = schema.getTables();
				for (Table mytable : tables) {
					String tableName = mytable.getName();
					DefaultMutableTreeNode tableTree = new DefaultMutableTreeNode(tableName);
					List<Column> columns = mytable.getColumns();
					for (Column mycolumn : columns) {
						column = new DefaultMutableTreeNode(new Column(mycolumn.getName(), mycolumn.getDisplaySize(), mycolumn.getTypeName()));
						tableTree.add(column);
						schemaTree.add(tableTree);

						// This is only for Custom table
						if ("ANIC01TC".equals(schemaName) && customTables.contains(tableName)) {
							customSchemaName.add(tableTree);
						}
					}
				}
				top.add(schemaTree);
			}
		} catch (Exception exp) {
			exp.printStackTrace();
		}
	}

	protected void showTableSuggestionLater() {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				showTableSuggestion();
			}

		});
	}

	private SuggestionPanel suggestion;

	private void hideSuggestion() {
		if (DEBUG_LOGS)
			System.out.println("DBQueryComponent.hideSuggestion()");
		if (suggestion != null) {
			suggestion.setList(null);
			suggestion.hide();
		}
	}

	protected void showTableSuggestion() {
		hideSuggestion();
		final int position = codeEditor.getCaretPosition();
		Point location;
		try {
			location = codeEditor.modelToView(position).getLocation();
		} catch (BadLocationException e2) {
			e2.printStackTrace();
			return;
		}
		String text = codeEditor.getText();
		int start = Math.max(0, position - 1);
		while (start > 0) {
			if (!Character.isWhitespace(text.charAt(start))) {
				start--;
			} else {
				start++;
				break;
			}
		}
		if (start > position) {
			return;
		}

		if (DEBUG_LOGS)
			System.out.println("The text present in the editor :" + text);

		final String subWord = text.substring(start, position);
		if (subWord.length() < 2) {
			return;
		}

		suggestion = new SuggestionPanel(codeEditor, position, subWord, location);
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				codeEditor.requestFocusInWindow();
			}
		});
	}

	public class SuggestionPanel {
		private JList list;

		public JList getList() {
			return list;
		}

		public void setList(JList list) {
			this.list = list;
		}

		private JPopupMenu popupMenu;
		private String subWord;
		private final int insertionPosition;

		public SuggestionPanel(JEditorPane codeEditor, int position, String subWord, Point location) {
			this.insertionPosition = position;
			this.subWord = subWord;
			popupMenu = new JPopupMenu();
			popupMenu.removeAll();
			popupMenu.setOpaque(false);
			popupMenu.setBorder(null);
			list = createSuggestionList(subWord);
			if (list != null) {
				popupMenu.add(list, BorderLayout.CENTER);
				popupMenu.show(codeEditor, location.x, codeEditor.getBaseline(0, 0) + location.y);
			}
		}

		public void hide() {
			if (DEBUG_LOGS)
				System.out.println("Hide Me");

			popupMenu.setVisible(false);
			if (suggestion == this) {
				suggestion = null;
			}
		}

		private JList createSuggestionList(final String subWord) {
			Object[] data = null;
			int i = 0;
			if (DEBUG_LOGS)
				System.out.println("createSuggestionList : subWord" + subWord);
			data = new Object[10];
			Set popupSet = new HashSet();

			Enumeration schemaEnum = nhSchema.children();
			breakOut : if (schemaEnum != null) {
				while (schemaEnum.hasMoreElements()) {
					DefaultMutableTreeNode schemaNode = (DefaultMutableTreeNode) schemaEnum.nextElement();
					if (DEBUG_LOGS)
						System.out.println("Node Name " + schemaNode);
					Enumeration tableEnum = schemaNode.children();
					while (tableEnum.hasMoreElements()) {
						DefaultMutableTreeNode tableNode = (DefaultMutableTreeNode) tableEnum.nextElement();
						if (DEBUG_LOGS)
							System.out.println("Tablename " + tableNode);
						if (tableNode.toString().toLowerCase().startsWith(subWord.toLowerCase())) {
							if (popupSet.add(tableNode.toString()))
								i++;
						}
						if (i == 10)
							break breakOut;
					}
				}
			}

			// }

			if (i == 0)
				return null;

			JList list = new JList(popupSet.toArray());
			list.setBorder(BorderFactory.createLineBorder(Color.DARK_GRAY, 0));
			list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
			list.setSelectedIndex(0);
			list.addMouseListener(new MouseAdapter() {
				@Override
				public void mouseClicked(MouseEvent e) {
					if (e.getClickCount() == 2) {
						insertSelection();
						hideSuggestion();
					}
				}
			});
			return list;
		}

		public boolean insertSelection() {
			if (list != null && list.getSelectedValue() != null) {
				try {
					String selectedSuggestion = "";
					selectedSuggestion = ((String) list.getSelectedValue());

					codeEditor.getDocument().insertString(insertionPosition, selectedSuggestion.substring(subWord.length()), null);
					return true;
				} catch (BadLocationException e1) {
					e1.printStackTrace();
				}
				hideSuggestion();
			}
			return false;
		}

		public void moveUp() {
			if (list != null) {
				codeEditor.setCaretPosition(insertionPosition);
				int index = Math.min(list.getSelectedIndex() - 1, list.getModel().getSize() - 1);
				selectIndex(index);
			}
		}

		public void moveDown() {
			if (list != null) {
				codeEditor.setCaretPosition(insertionPosition);
				int index = Math.min(list.getSelectedIndex() + 1, list.getModel().getSize() - 1);
				selectIndex(index);
			}
		}

		private void selectIndex(int index) {
			final int position = codeEditor.getCaretPosition();
			list.setSelectedIndex(index);
			SwingUtilities.invokeLater(new Runnable() {
				@Override
				public void run() {
					codeEditor.setCaretPosition(position);
				};
			});
		}
	}

	private void doMouseClicked(MouseEvent me) {
		TreePath tp = tree.getPathForLocation(me.getX(), me.getY());
		final int position = codeEditor.getCaretPosition();

		if (tp != null) {
			try {
				String splitNames[] = tp.getLastPathComponent().toString().split(" ");

				String columnName = splitNames[0] + " ,";
				codeEditor.getDocument().insertString(position, columnName, null);
				codeEditor.setCaretPosition(position + columnName.length());
				codeEditor.getCaret().setVisible(true);
			} catch (BadLocationException e) {
				e.printStackTrace();
			}
		}
	}

	private Highlighter.HighlightPainter cyanPainter;
	private Highlighter.HighlightPainter yellowPainter;

	private void highlighter(int clickCount) {

		String selectedText = codeEditor.getSelectedText();

		if (DEBUG_LOGS)
			System.out.println(" Selected Text = <" + selectedText + ">" + " " + "\n".equals(selectedText));

		if (selectedText == null || " ".equals(selectedText) || "\n".equals(selectedText)) {
			return;
		}

		selectedText = selectedText.trim();

		final int position = codeEditor.getCaretPosition();

		int start = Math.max(0, position - 1);
		String text = codeEditor.getText();

		while (start > 0) {
			char c = text.charAt(start);

			if (DEBUG_LOGS)
				System.out.println(c + " -- " + "_".equals(c + ""));

			if (Character.isLetterOrDigit(c) || "_".equals(c + "") || ".".equals(c + "") || ",".equals(c + "")) {
				start--;
			} else {
				start++;
				break;
			}
		}
		if (DEBUG_LOGS)
			System.out.println("Start =" + start);
		int end = start;
		while (true) {
			if (DEBUG_LOGS)
				System.out.println("Position =" + position + " Text Length =" + text.length() + " end =" + end);
			if (end >= text.length()) {
				break;
			} else if (Character.isWhitespace(text.charAt(end))
					|| "\n".equals(text.charAt(end))
					|| !(Character.isLetterOrDigit(text.charAt(end)) || "_".equals(text.charAt(end) + "") || ".".equals(text.charAt(end) + "") || ","
							.equals(text.charAt(end) + ""))) {
				break;
			} else {
				end++;
			}
		}
		selectedText = text.substring(start, end);

		codeEditor.setCaretPosition(end);

		codeEditor.setSelectionStart(start);
		codeEditor.setSelectionEnd(end);

		cyanPainter = new DefaultHighlighter.DefaultHighlightPainter(Color.CYAN);
		yellowPainter = new DefaultHighlighter.DefaultHighlightPainter(Color.YELLOW);

		try {

			removeAllHighlights();

			if (clickCount == 2) {
				codeEditor.getHighlighter().addHighlight(start, end, cyanPainter);
			}

			List<IndexWrapper> indexWrappers = findIndexesForKeyword(selectedText);
			for (IndexWrapper wrapper : indexWrappers) {
				codeEditor.getHighlighter().addHighlight(wrapper.getStart(), wrapper.getEnd(), yellowPainter);
			}

		} catch (BadLocationException ble) {
		}
	}

	/**
	 * 
	 * @param keyword
	 * @return
	 */
	public List<IndexWrapper> findIndexesForKeyword(String keyword) {
		String regex = "\\b" + keyword + "\\b";
		Pattern pattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
		Matcher matcher = pattern.matcher(codeEditor.getText());
		List<IndexWrapper> wrappers = new ArrayList<IndexWrapper>();
		while (matcher.find()) {
			int end = matcher.end();
			int start = matcher.start();
			IndexWrapper wrapper = new IndexWrapper(start, end);
			wrappers.add(wrapper);
		}
		return wrappers;
	}

	private void removeAllHighlights() {
		codeEditor.getHighlighter().removeAllHighlights();
	}

}