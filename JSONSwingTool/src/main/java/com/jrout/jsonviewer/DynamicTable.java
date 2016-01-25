package com.jrout.jsonviewer;

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;
import javax.swing.table.*;
import java.util.List;

public class DynamicTable extends AbstractTableModel {
	private List data = new ArrayList();
	private Object[] columnNames = {"Property", "Value"};

	public Object getValueAt(int row, int col) {
		return ((String[]) data.get(row))[col];
	}
	public int getColumnCount() {
		return 2;
	}
	public int getRowCount() {
		return data.size();
	}
	public String getColumnName(int column) {
		return columnNames[column].toString();
	}

	public void addRow(String[] rowData) {
		int row = data.size();
		data.add(rowData);
		fireTableRowsInserted(row, row);
	}
	public void clear() {
		data.clear();
	}
}