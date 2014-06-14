package com.jsonviewer;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.File;
import java.io.IOException;

import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.TransferHandler;

/**
 * @author Jayram Rout
 *
 */
class FileTransferHandler extends TransferHandler {
	private DataFlavor fileFlavor, stringFlavor;
	private TabbedPaneController tpc;
	protected String newline = "\n";

	FileTransferHandler(TabbedPaneController t) {
		tpc = t;
		fileFlavor = DataFlavor.javaFileListFlavor;
		stringFlavor = DataFlavor.stringFlavor;
	}

	public boolean importData(JComponent c, Transferable t) {
		if (!canImport(c, t.getTransferDataFlavors())) {
			return false;
		}
		try {
			if (hasFileFlavor(t.getTransferDataFlavors())) {
				//System.out.println(Helper.fileList);
				String str = null;
				java.util.List files = (java.util.List) t.getTransferData(fileFlavor);
				for (int i = 0; i < files.size(); i++) {
					File file = (File) files.get(i);
					if(Helper.fileList.contains(file.getAbsolutePath())){
						JOptionPane.showMessageDialog(null, "File Already Present", "Error", JOptionPane.ERROR_MESSAGE);
					}else if (Helper.isJSONValid(file)) {
						Helper.fileList.add(file.getAbsolutePath());
						tpc.addTab(file.toString());
					}
				}
			} else if (hasStringFlavor(t.getTransferDataFlavors())) {
				JOptionPane.showMessageDialog(null, "Does not support Text Transfer", "Error", JOptionPane.ERROR_MESSAGE);
				return false;
			}
		} catch (UnsupportedFlavorException ufe) {
			System.out.println("importData: unsupported data flavor");
		} catch (IOException ieo) {
			System.out.println("importData: I/O exception");
		}
		return false;
	}
	public int getSourceActions(JComponent c) {
		return COPY_OR_MOVE;
	}

	public boolean canImport(JComponent c, DataFlavor[] flavors) {
		if (hasFileFlavor(flavors)) {
			return true;
		}
		if (hasStringFlavor(flavors)) {
			return true;
		}
		return false;
	}

	private boolean hasFileFlavor(DataFlavor[] flavors) {
		for (int i = 0; i < flavors.length; i++) {
			if (fileFlavor.equals(flavors[i])) {
				return true;
			}
		}
		return false;
	}

	private boolean hasStringFlavor(DataFlavor[] flavors) {
		for (int i = 0; i < flavors.length; i++) {
			if (stringFlavor.equals(flavors[i])) {
				return true;
			}
		}
		return false;
	}
}
