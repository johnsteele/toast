/*******************************************************************************
 * Copyright (c) 2009 The Eclipse Foundation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *    The Eclipse Foundation - initial API and implementation
 *******************************************************************************/
package org.eclipse.examples.expenses.application.rcp.customizers;

import java.text.DateFormat;
import java.util.Date;

import org.eclipse.examples.expenses.core.LineItem;
import org.eclipse.examples.expenses.views.ExpenseReportViewPrivilegedAccessor;
import org.eclipse.examples.expenses.views.IExpenseReportViewCustomizer;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.nebula.widgets.cdatetime.CDT;
import org.eclipse.nebula.widgets.cdatetime.CDateTimeCellEditor;

public class ExpenseReportViewCustomizer implements
		IExpenseReportViewCustomizer {

	private ExpenseReportViewPrivilegedAccessor accessor;

	public ExpenseReportViewCustomizer() {
		// TODO Auto-generated constructor stub
	}

	public void postCreateExpenseReportView(ExpenseReportViewPrivilegedAccessor accessor) {
		this.accessor = accessor;
		final TableViewer viewer = accessor.getLineItemViewer();
		customizeDateColumn(viewer);
		customizeCommentColumn(viewer);
	}

	private void customizeDateColumn(final TableViewer viewer) {
		TableViewerColumn dateColumn = new TableViewerColumn(viewer, accessor.getDateColumn());
		dateColumn.setLabelProvider(new ColumnLabelProvider() {
			final DateFormat dateFormat = DateFormat.getDateInstance(DateFormat.SHORT);
			@Override
			public String getText(Object element) {
				return dateFormat.format(((LineItem)element).getDate());
			}
		});
		dateColumn.setEditingSupport(new EditingSupport(viewer) {

			@Override
			protected boolean canEdit(Object element) {
				return true;
			}

			@Override
			protected CellEditor getCellEditor(Object element) {
				return new CDateTimeCellEditor(viewer.getTable(), CDT.DROP_DOWN);
			}

			@Override
			protected Object getValue(Object element) {
				return ((LineItem)element).getDate();
			}

			@Override
			protected void setValue(Object element, Object value) {
				((LineItem)element).setDate((Date)value);
			}
			
		});
	}

	void customizeCommentColumn(final TableViewer viewer) {
		TableViewerColumn commentColumn = new TableViewerColumn(viewer, accessor.getCommentColumn());
		commentColumn.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				return ((LineItem)element).getComment();
			}
		});
		
		commentColumn.setEditingSupport(new EditingSupport(viewer) {
			@Override
			protected boolean canEdit(Object element) {
				return true;
			}

			@Override
			protected Object getValue(Object element) {
				String comment  = ((LineItem)element).getComment();
				return comment == null ? "" : comment;
			}

			@Override
			protected void setValue(Object element, Object value) {
				((LineItem)element).setComment((String) value);
			}

			@Override
			protected CellEditor getCellEditor(Object element) {
				return new TextCellEditor(viewer.getTable());
			}			
		});
	}
}
