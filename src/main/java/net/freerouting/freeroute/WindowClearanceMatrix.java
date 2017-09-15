/*
 *  Copyright (C) 2014  Alfons Wirtz  
 *   website www.freerouting.net
 *
 *   This program is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License at <http://www.gnu.org/licenses/> 
 *   for more details.
 *
 * ClearanceMatrixWindow.java
 *
 * Created on 20. Februar 2005, 06:09
 */
package net.freerouting.freeroute;

import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;
import static net.freerouting.freeroute.SignalLayerWithIndexBuilder.ALL_LAYER_INDEX;
import static net.freerouting.freeroute.SignalLayerWithIndexBuilder.INNER_LAYER_INDEX;
import net.freerouting.freeroute.datastructures.UndoableObjects;
import net.freerouting.freeroute.rules.ClearanceMatrix;

/**
 * Window for interactive editing of the clearance Matrix.
 *
 * @author Alfons Wirtz
 */
@SuppressWarnings("serial")
public final class WindowClearanceMatrix extends BoardSavableSubWindow {

    /**
     * Characters, which are not allowed in the name of a clearance class.
     */
    private static final String[] RESERVED_NAME_CHARS = {"(", ")", " ", "_"};
    private final BoardFrame board_frame;
    private final javax.swing.JPanel main_panel;
    private javax.swing.JPanel center_panel;
    private final ComboBoxLayer layer_combo_box;
    private javax.swing.JTable clearance_table;
    private ClearanceTableModel clearance_table_model;
    private final java.util.ResourceBundle resources;

    /**
     * Creates a new instance of ClearanceMatrixWindow
     */
    private WindowClearanceMatrix(BoardFrame p_board_frame) {
        this.board_frame = p_board_frame;
        this.resources = java.util.ResourceBundle.getBundle("net.freerouting.freeroute.resources.WindowClearanceMatrix", Locale.getDefault());

        this.setTitle(resources.getString("title"));

        this.main_panel = new javax.swing.JPanel();
        main_panel.setBorder(javax.swing.BorderFactory.createEmptyBorder(20, 20, 20, 20));
        main_panel.setLayout(new java.awt.BorderLayout());

        // Add the layer combo box.
        final javax.swing.JPanel north_panel = new javax.swing.JPanel();
        north_panel.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 20, 0));
        javax.swing.JLabel layer_label = new javax.swing.JLabel(resources.getString("layer") + " ");
        layer_label.setToolTipText(resources.getString("layer_tooltip"));
        north_panel.add(layer_label);

        net.freerouting.freeroute.interactive.BoardHandling board_handling = board_frame.board_panel.board_handling;
        layer_combo_box = new ComboBoxLayer(board_handling.get_routing_board().layer_structure);
        north_panel.add(this.layer_combo_box);
        this.layer_combo_box.addActionListener(new ComboBoxListener());

        main_panel.add(north_panel, java.awt.BorderLayout.NORTH);

        // Add the clearance table.
        this.center_panel = add_clearance_table(p_board_frame);

        main_panel.add(center_panel, java.awt.BorderLayout.CENTER);

        // Add panel with buttons.
        javax.swing.JPanel south_panel = new javax.swing.JPanel();
        south_panel.setBorder(javax.swing.BorderFactory.createEmptyBorder(20, 20, 20, 20));
        south_panel.setLayout(new java.awt.BorderLayout());
        this.add(south_panel);

        final javax.swing.JButton add_class_button = new javax.swing.JButton(resources.getString("add_class"));
        add_class_button.setToolTipText(resources.getString("add_class_tooltip"));
        add_class_button.addActionListener(new AddClassListener());
        south_panel.add(add_class_button, java.awt.BorderLayout.WEST);

        final javax.swing.JButton prune_button = new javax.swing.JButton(resources.getString("prune"));
        prune_button.setToolTipText(resources.getString("prune_tooltip"));
        prune_button.addActionListener(new PruneListener());
        south_panel.add(prune_button, java.awt.BorderLayout.EAST);

        main_panel.add(south_panel, java.awt.BorderLayout.SOUTH);

        this.add(main_panel);
        this.pack();
    }

    static WindowClearanceMatrix getInstance(BoardFrame p_board_frame) {
        WindowClearanceMatrix window = new WindowClearanceMatrix(p_board_frame);
        p_board_frame.set_context_sensitive_help(window, "WindowClearanceMatrix");
        return window;
    }

    /**
     * Recalculates all displayed values
     */
    @Override
    public void refresh() {
        net.freerouting.freeroute.board.BasicBoard routing_board = this.board_frame.board_panel.board_handling.get_routing_board();
        if (this.clearance_table_model.getRowCount() != routing_board.rules.clearance_matrix.get_class_count()) {
            this.adjust_clearance_table();
        }
        this.clearance_table_model.set_values(this.layer_combo_box.get_selected_layer().index);
        this.repaint();
    }

    private javax.swing.JPanel add_clearance_table(BoardFrame p_board_frame) {
        this.clearance_table_model = new ClearanceTableModel(p_board_frame.board_panel.board_handling);
        this.clearance_table = new javax.swing.JTable(clearance_table_model);

        // Put the clearance table into a scroll pane.
        final int textfield_height = 16;
        final int textfield_width = Math.max(6 * max_name_length(), 100);
        int table_height = textfield_height * (this.clearance_table_model.getRowCount());
        int table_width = textfield_width * this.clearance_table_model.getColumnCount();
        this.clearance_table.setPreferredSize(new java.awt.Dimension(table_width, table_height));
        this.clearance_table.setAutoResizeMode(javax.swing.JTable.AUTO_RESIZE_OFF);
        // Put a panel around the table and the header before putting the table into the scroll pane,
        // because otherwise there seems to be a redisplay bug in horizontal scrolling.
        javax.swing.JPanel scroll_panel = new javax.swing.JPanel();
        scroll_panel.setLayout(new java.awt.BorderLayout());
        scroll_panel.add(this.clearance_table.getTableHeader(), java.awt.BorderLayout.NORTH);
        scroll_panel.add(this.clearance_table, java.awt.BorderLayout.CENTER);
        javax.swing.JScrollPane scroll_pane = new javax.swing.JScrollPane(scroll_panel,
                javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
                javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);

        final int scroll_bar_width = 20;
        final int scroll_pane_height = textfield_height * this.clearance_table_model.getRowCount() + scroll_bar_width;
        final int scroll_pane_width = Math.min(table_width + scroll_bar_width, 1_200);
        scroll_pane.setPreferredSize(new java.awt.Dimension(scroll_pane_width, scroll_pane_height));
        // Change the background color of the header and the first column of the table.
        java.awt.Color header_background_color = new java.awt.Color(220, 220, 255);
        javax.swing.table.JTableHeader table_header = this.clearance_table.getTableHeader();
        table_header.setBackground(header_background_color);

        javax.swing.table.TableColumn first_column = this.clearance_table.getColumnModel().getColumn(0);
        javax.swing.table.DefaultTableCellRenderer first_colunn_renderer = new javax.swing.table.DefaultTableCellRenderer();
        first_colunn_renderer.setBackground(header_background_color);
        first_column.setCellRenderer(first_colunn_renderer);

        final javax.swing.JPanel result = new javax.swing.JPanel();
        result.setLayout(new java.awt.BorderLayout());

        result.add(scroll_pane, java.awt.BorderLayout.CENTER);

        // add message for german localisation bug
        if (Locale.getDefault().getLanguage().equalsIgnoreCase("de")) {
            javax.swing.JLabel bug_label
                    = new javax.swing.JLabel("Wegen eines Java-System-Bugs muss das Dezimalkomma in dieser Tabelle als Punkt eingegeben werden!");
            result.add(bug_label, java.awt.BorderLayout.SOUTH);
        }
        return result;
    }

    /**
     * Adds a new class to the clearance matrix.
     */
    private void add_class() {
        String new_name = null;
        // Ask for the name of the new class.
        for (;;) {
            new_name = javax.swing.JOptionPane.showInputDialog(resources.getString("new_name"));
            if (new_name == null) {
                return;
            }
            new_name = new_name.trim();
            if (is_legal_class_name(new_name)) {
                break;
            }
        }
        final net.freerouting.freeroute.board.BasicBoard routing_board = this.board_frame.board_panel.board_handling.get_routing_board();
        final net.freerouting.freeroute.rules.ClearanceMatrix clearance_matrix = routing_board.rules.clearance_matrix;

        // Check, if the name exists already.
        boolean name_exists = false;
        for (int i = 0; i < clearance_matrix.get_class_count(); ++i) {
            if (new_name.equals(clearance_matrix.get_name(i))) {
                name_exists = true;
                break;
            }
        }
        if (name_exists) {
            return;
        }
        clearance_matrix.append_class(new_name);
        if (routing_board.get_test_level() == net.freerouting.freeroute.board.TestLevel.RELEASE_VERSION) {
            // clearance compensation is only used, if there are only the clearance classes default and null.
            routing_board.search_tree_manager.set_clearance_compensation_used(false);
        }
        adjust_clearance_table();
    }

    /**
     * Removes clearance classs, whose clearance values are all equal to a
     * previous class.
     */
    private void prune_clearance_matrix() {
        final net.freerouting.freeroute.board.BasicBoard routing_board = this.board_frame.board_panel.board_handling.get_routing_board();
        ClearanceMatrix clearance_matrix = routing_board.rules.clearance_matrix;
        for (int i = clearance_matrix.get_class_count() - 1; i >= 2; --i) {
            for (int j = clearance_matrix.get_class_count() - 1; j >= 0; --j) {
                if (i == j) {
                    continue;
                }
                if (clearance_matrix.is_equal(i, j)) {
                    String message = resources.getString("confirm_remove") + " " + clearance_matrix.get_name(i);
                    int selected_option
                            = javax.swing.JOptionPane.showConfirmDialog(this, message, null, javax.swing.JOptionPane.YES_NO_OPTION);
                    if (selected_option == javax.swing.JOptionPane.YES_OPTION) {
                        java.util.Collection<net.freerouting.freeroute.board.Item> board_items = routing_board.get_items();
                        routing_board.rules.change_clearance_class_no(i, j, board_items);
                        if (!routing_board.rules.remove_clearance_class(i, board_items)) {
                            Logger.getLogger(WindowClearanceMatrix.class.getName()).log(Level.SEVERE, "WindowClearanceMatrix.prune_clearance_matrix error removing clearance class");
                            return;
                        }
                        routing_board.search_tree_manager.clearance_class_removed(i);
                        adjust_clearance_table();
                    }
                    break;
                }
            }
        }
    }

    /**
     * Adjusts the displayed window with the clearance table after the size of
     * the clearance matrix has changed.
     */
    private void adjust_clearance_table() {
        this.clearance_table_model = new ClearanceTableModel(this.board_frame.board_panel.board_handling);
        this.clearance_table = new javax.swing.JTable(clearance_table_model);
        this.main_panel.remove(this.center_panel);
        this.center_panel = add_clearance_table(this.board_frame);
        this.main_panel.add(this.center_panel, java.awt.BorderLayout.CENTER);
        this.pack();
        this.board_frame.refresh_windows();
    }

    /**
     * Returns true, if p_string is a legal class name.
     */
    private boolean is_legal_class_name(String p_string) {
        if (p_string.isEmpty()) {
            return false;
        }
        for (int i = 0; i < RESERVED_NAME_CHARS.length; ++i) {
            if (p_string.contains(RESERVED_NAME_CHARS[i])) {
                return false;
            }
        }
        return true;
    }

    private int max_name_length() {
        int result = 1;
        net.freerouting.freeroute.rules.ClearanceMatrix clearance_matrix = board_frame.board_panel.board_handling.get_routing_board().rules.clearance_matrix;
        for (int i = 0; i < clearance_matrix.get_class_count(); ++i) {
            result = Math.max(result, clearance_matrix.get_name(i).length());
        }
        return result;
    }

    private class ComboBoxListener implements java.awt.event.ActionListener {

        @Override
        public void actionPerformed(java.awt.event.ActionEvent evt) {
            refresh();
        }
    }

    private class AddClassListener implements java.awt.event.ActionListener {

        @Override
        public void actionPerformed(java.awt.event.ActionEvent p_evt) {
            add_class();
        }
    }

    private class PruneListener implements java.awt.event.ActionListener {

        @Override
        public void actionPerformed(java.awt.event.ActionEvent p_evt) {
            prune_clearance_matrix();
        }
    }

    @SuppressWarnings("serial")
    private class ClearanceTableModel extends javax.swing.table.AbstractTableModel {

        private Object[][] data = null;
        private String[] column_names = null;

        ClearanceTableModel(net.freerouting.freeroute.interactive.BoardHandling p_board_handling) {
            net.freerouting.freeroute.rules.ClearanceMatrix clearance_matrix = p_board_handling.get_routing_board().rules.clearance_matrix;

            column_names = new String[clearance_matrix.get_class_count() + 1];
            column_names[0] = resources.getString("class");

            data = new Object[clearance_matrix.get_class_count()][];
            for (int i = 0; i < clearance_matrix.get_class_count(); ++i) {
                this.column_names[i + 1] = clearance_matrix.get_name(i);
                this.data[i] = new Object[clearance_matrix.get_class_count() + 1];
                this.data[i][0] = clearance_matrix.get_name(i);
            }
            this.set_values(0);
        }

        @Override
        public String getColumnName(int p_col) {
            return column_names[p_col];
        }

        @Override
        public int getRowCount() {
            return data.length;
        }

        @Override
        public int getColumnCount() {
            return column_names.length;
        }

        @Override
        public Object getValueAt(int p_row, int p_col) {
            return data[p_row][p_col];
        }

        @Override
        public void setValueAt(Object p_value, int p_row, int p_col) {
            Number number_value;
            if (p_value instanceof Number) {
                // does ot work because of a localisation Bug in Java
                number_value = (Number) p_value;
            } else {
                // Workaround because of a localisation Bug in Java
                // The numbers are always displayed in the English Format.
                if (!(p_value instanceof String)) {
                    return;
                }
                try {
                    number_value = Float.parseFloat((String) p_value);
                } catch (NumberFormatException e) {
                    return;
                }
            }
            int curr_row = p_row;
            int curr_column = p_col - 1;

            // check, if there are items on the board assigned to clearance class i or j.
            net.freerouting.freeroute.interactive.BoardHandling board_handling = board_frame.board_panel.board_handling;
            net.freerouting.freeroute.datastructures.UndoableObjects item_list = board_handling.get_routing_board().item_list;
            boolean items_already_assigned_row = false;
            boolean items_already_assigned_column = false;
            java.util.Iterator<UndoableObjects.UndoableObjectNode> it = item_list.start_read_object();
            for (;;) {
                net.freerouting.freeroute.board.Item curr_item = (net.freerouting.freeroute.board.Item) item_list.read_object(it);
                if (curr_item == null) {
                    break;
                }
                int curr_item_class_no = curr_item.clearance_class_no();
                if (curr_item_class_no == curr_row) {
                    items_already_assigned_row = true;
                }
                if (curr_item_class_no == curr_column) {
                    items_already_assigned_column = true;
                }

            }
            ClearanceMatrix clearance_matrix = board_handling.get_routing_board().rules.clearance_matrix;
            boolean items_already_assigned = items_already_assigned_row && items_already_assigned_column;
            if (items_already_assigned) {
                String message = resources.getString("already_assigned") + " ";
                if (curr_row == curr_column) {
                    message += resources.getString("the_class") + " " + clearance_matrix.get_name(curr_row);
                } else {
                    message += resources.getString("the_classes") + " " + clearance_matrix.get_name(curr_row)
                            + " " + resources.getString("and") + " " + clearance_matrix.get_name(curr_column);
                }
                message += resources.getString("change_anyway");
                int selected_option = javax.swing.JOptionPane.showConfirmDialog(
                        board_frame.savable_subwindows.get(SavableSubwindowKey.CLEARANCE_MATRIX),
                        message, null, javax.swing.JOptionPane.YES_NO_OPTION);
                if (selected_option != javax.swing.JOptionPane.YES_OPTION) {
                    return;
                }
            }

            this.data[p_row][p_col] = number_value;
            this.data[p_col - 1][p_row + 1] = number_value;
            fireTableCellUpdated(p_row, p_col);
            fireTableCellUpdated(p_col - 1, p_row + 1);

            int board_value = (int) Math.round(board_handling.coordinate_transform.user_to_board((number_value).doubleValue()));
            int layer_no = layer_combo_box.get_selected_layer().index;
            switch (layer_no) {
                case ALL_LAYER_INDEX:
                    // change the clearance on all layers
                    clearance_matrix.set_value(curr_row, curr_column, board_value);
                    clearance_matrix.set_value(curr_column, curr_row, board_value);
                    break;
                case INNER_LAYER_INDEX:
                    // change the clearance on all inner layers
                    clearance_matrix.set_inner_value(curr_row, curr_column, board_value);
                    clearance_matrix.set_inner_value(curr_column, curr_row, board_value);
                    break;
                default:
                    // change the clearance on layer with index layer_no
                    clearance_matrix.set_value(curr_row, curr_column, layer_no, board_value);
                    clearance_matrix.set_value(curr_column, curr_row, layer_no, board_value);
                    break;
            }
            if (items_already_assigned) {
                // force reinserting all item into the searck tree, because their tree shapes may have changed
                board_handling.get_routing_board().search_tree_manager.clearance_value_changed();
            }
        }

        @Override
        public boolean isCellEditable(int p_row, int p_col) {
            return p_row > 0 && p_col > 1;
        }

        @Override
        public Class<?> getColumnClass(int p_col) {
            if (p_col == 0) {
                return String.class;
            }
            return String.class;
            // Should be Number.class or Float.class. But that does not work because of a localisation bug in Java.
        }

        /**
         * Sets the values of this clearance table to the values of the
         * clearance matrix on the input layer.
         */
        private void set_values(int p_layer) {
            net.freerouting.freeroute.interactive.BoardHandling board_handling = board_frame.board_panel.board_handling;
            ClearanceMatrix clearance_matrix = board_handling.get_routing_board().rules.clearance_matrix;

            for (int i = 0; i < clearance_matrix.get_class_count(); ++i) {
                for (int j = 0; j < clearance_matrix.get_class_count(); ++j) {
                    switch (p_layer) {
                        case ALL_LAYER_INDEX:
                            // all layers
                            if (clearance_matrix.is_layer_dependent(i, j)) {
                                this.data[i][j + 1] = -1;
                            } else {
                                Float curr_table_value
                                        = (float) board_handling.coordinate_transform.board_to_user(clearance_matrix.value(i, j, 0));
                                this.data[i][j + 1] = curr_table_value;
                            }
                            break;
                        case INNER_LAYER_INDEX:
                            // all inner layers
                            if (clearance_matrix.is_inner_layer_dependent(i, j)) {
                                this.data[i][j + 1] = -1;
                            } else {
                                Float curr_table_value
                                        = (float) board_handling.coordinate_transform.board_to_user(clearance_matrix.value(i, j, 1));
                                this.data[i][j + 1] = curr_table_value;
                            }
                            break;
                        default:
                            Float curr_table_value
                                    = (float) board_handling.coordinate_transform.board_to_user(clearance_matrix.value(i, j, p_layer));
                            this.data[i][j + 1] = curr_table_value;
                            break;
                    }
                }
            }
        }

    }
}
