package com.jrout.jsonviewer;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;

import javax.swing.JComponent;
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
				String str = null;
				java.util.List files = (java.util.List) t.getTransferData(fileFlavor);
				for (int i = 0; i < files.size(); i++) {
					File file = (File) files.get(i);
					tpc.addTab(file.toString());
				}
			} else if (hasStringFlavor(t.getTransferDataFlavors())) {
				File tempFile = new File("temp.json");
				Writer writer = null;
				writer = new BufferedWriter(new FileWriter(tempFile));
				writer.write(t.getTransferData(stringFlavor).toString());
				writer.flush();
				writer.close();
				Helper.fileList.remove(tempFile.getAbsoluteFile().toString());
				tpc.addTab(tempFile.getAbsoluteFile().toString());

				// JOptionPane.showMessageDialog(null,
				// "Does not support Text Transfer", "Error",
				// JOptionPane.ERROR_MESSAGE);
				// return false;
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
