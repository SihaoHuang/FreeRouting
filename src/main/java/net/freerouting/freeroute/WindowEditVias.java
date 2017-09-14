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
 * ViaTablePanel.java
 *
 * Created on 4. April 2005, 07:05
 */
package net.freerouting.freeroute;

import java.util.Locale;
import net.freerouting.freeroute.rules.BoardRules;
import net.freerouting.freeroute.rules.ItemClass;
import net.freerouting.freeroute.rules.ViaInfo;
import net.freerouting.freeroute.rules.ViaInfos;

/**
 * Edit window for the table of available vias.
 *
 * @author Alfons Wirtz
 */
@SuppressWarnings("serial")
public final class WindowEditVias extends BoardSavableSubWindow {

    private static final int TEXTFIELD_HEIGHT = 16;
    private static final int TEXTFIELD_WIDTH = 100;

    private final BoardFrame board_frame;

    private final javax.swing.JPanel main_panel;

    private javax.swing.JScrollPane scroll_pane;
    private javax.swing.JTable table;
    private ViaTableModel table_model;

    private final javax.swing.JComboBox<String> cl_class_combo_box;
    private final javax.swing.JComboBox<String> padstack_combo_box;

    private final java.util.ResourceBundle resources;

    /**
     * Creates a new instance of ViaTablePanel
     */
    private WindowEditVias(BoardFrame p_board_frame) {
        this.resources = java.util.ResourceBundle.getBundle("net.freerouting.freeroute.resources.WindowEditVias", Locale.getDefault());
        this.setTitle(resources.getString("title"));

        this.board_frame = p_board_frame;

        this.main_panel = new javax.swing.JPanel();
        this.main_panel.setLayout(new java.awt.BorderLayout());

        this.cl_class_combo_box = new javax.swing.JComboBox<>();
        this.padstack_combo_box = new javax.swing.JComboBox<>();
        add_combobox_items();

        add_table();

        javax.swing.JPanel via_info_button_panel = new javax.swing.JPanel();
        via_info_button_panel.setLayout(new java.awt.FlowLayout());
        this.main_panel.add(via_info_button_panel, java.awt.BorderLayout.SOUTH);
        final javax.swing.JButton add_via_button = new javax.swing.JButton(resources.getString("add"));
        add_via_button.setToolTipText(resources.getString("add_tooltip"));
        add_via_button.addActionListener(new AddViaListener());
        via_info_button_panel.add(add_via_button);
        final javax.swing.JButton remove_via_button = new javax.swing.JButton(resources.getString("remove"));
        remove_via_button.setToolTipText(resources.getString("remove_tooltip"));
        remove_via_button.addActionListener(new RemoveViaListener());
        via_info_button_panel.add(remove_via_button);

        this.add(main_panel);
        this.pack();
    }

    static WindowEditVias getInstance(BoardFrame p_board_frame) {
        WindowEditVias window = new WindowEditVias(p_board_frame);
        p_board_frame.set_context_sensitive_help(window, "WindowVia_EditVia");
        return window;
    }

    /**
     * Recalculates all values displayed in the parent window
     */
    @Override
    public void refresh() {
        this.padstack_combo_box.removeAllItems();
        this.cl_class_combo_box.removeAllItems();
        this.add_combobox_items();
        this.table_model.set_values();
    }

    private void add_table() {
        this.table_model = new ViaTableModel();
        this.table = new javax.swing.JTable(this.table_model);
        this.scroll_pane = new javax.swing.JScrollPane(this.table);
        int table_height = TEXTFIELD_HEIGHT * this.table_model.getRowCount();
        int table_width = TEXTFIELD_WIDTH * this.table_model.getColumnCount();
        this.table.setPreferredScrollableViewportSize(new java.awt.Dimension(table_width, table_height));
        this.table.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        this.main_panel.add(scroll_pane, java.awt.BorderLayout.CENTER);

        this.table.getColumnModel().getColumn(ColumnName.CLEARANCE_CLASS.ordinal()).setCellEditor(new javax.swing.DefaultCellEditor(cl_class_combo_box));

        this.table.getColumnModel().getColumn(ColumnName.PADSTACK.ordinal()).setCellEditor(new javax.swing.DefaultCellEditor(padstack_combo_box));
    }

    private void add_combobox_items() {
        net.freerouting.freeroute.board.RoutingBoard routing_board = board_frame.board_panel.board_handling.get_routing_board();
        for (int i = 0; i < routing_board.rules.clearance_matrix.get_class_count(); ++i) {
            cl_class_combo_box.addItem(routing_board.rules.clearance_matrix.get_name(i));
        }
        for (int i = 0; i < routing_board.library.via_padstack_count(); ++i) {
            padstack_combo_box.addItem(routing_board.library.get_via_padstack(i).name);
        }
    }

    /**
     * Adjusts the displayed window with the via table after the size of the
     * table has been changed.
     */
    private void adjust_table() {
        this.table_model = new ViaTableModel();
        this.table = new javax.swing.JTable(this.table_model);
        this.main_panel.remove(this.scroll_pane);
        this.add_table();
        this.pack();
        this.board_frame.refresh_windows();
    }

    private class AddViaListener implements java.awt.event.ActionListener {

        @Override
        public void actionPerformed(java.awt.event.ActionEvent p_evt) {
            net.freerouting.freeroute.board.RoutingBoard routing_board = board_frame.board_panel.board_handling.get_routing_board();
            ViaInfos via_infos = routing_board.rules.via_infos;
            Integer no = 1;
            String new_name = null;
            final String name_start = resources.getString("new_via");
            for (;;) {
                new_name = name_start + no.toString();
                if (!via_infos.name_exists(new_name)) {
                    break;
                }
                ++no;
            }
            net.freerouting.freeroute.rules.NetClass default_net_class = routing_board.rules.get_default_net_class();
            ViaInfo new_via = new ViaInfo(new_name, routing_board.library.get_via_padstack(0),
                    default_net_class.default_item_clearance_classes.get(ItemClass.VIA),
                    false, routing_board.rules);
            via_infos.add(new_via);
            adjust_table();
        }
    }

    private class RemoveViaListener implements java.awt.event.ActionListener {

        @Override
        public void actionPerformed(java.awt.event.ActionEvent p_evt) {
            if (table_model.getRowCount() <= 1) {
                board_frame.screen_messages.set_status_message(resources.getString("message_1"));
                return;
            }
            int selected_row = table.getSelectedRow();
            if (selected_row < 0) {
                return;
            }
            Object via_name = table_model.getValueAt(selected_row, ColumnName.NAME.ordinal());
            if (!(via_name instanceof String)) {
                return;
            }
            BoardRules board_rules = board_frame.board_panel.board_handling.get_routing_board().rules;
            ViaInfo via_info = board_rules.via_infos.get((String) via_name);
            // Check, if via_info is used in a via rule.
            for (net.freerouting.freeroute.rules.ViaRule curr_rule : board_rules.via_rules) {
                if (curr_rule.contains(via_info)) {
                    String message = resources.getString("message_2") + " " + curr_rule.name;
                    board_frame.screen_messages.set_status_message(message);
                    return;
                }
            }
            if (board_rules.via_infos.remove(via_info)) {
                adjust_table();
                String message = resources.getString("via") + "via " + via_info.get_name() + " "
                        + resources.getString("removed");
                board_frame.screen_messages.set_status_message(message);
            }
        }
    }

    @SuppressWarnings("serial")
    private class ViaTableModel extends javax.swing.table.AbstractTableModel {

        private Object[][] data = null;
        private String[] column_names = null;

        ViaTableModel() {
            column_names = new String[ColumnName.values().length];

            for (int i = 0; i < column_names.length; ++i) {
                column_names[i] = resources.getString((ColumnName.values()[i]).toString());
            }
            net.freerouting.freeroute.rules.BoardRules board_rules = board_frame.board_panel.board_handling.get_routing_board().rules;
            data = new Object[board_rules.via_infos.count()][];
            for (int i = 0; i < data.length; ++i) {
                this.data[i] = new Object[ColumnName.values().length];
            }
            set_values();
        }

        /**
         * Calculates the the valus in this table
         */
        private void set_values() {
            net.freerouting.freeroute.rules.BoardRules board_rules = board_frame.board_panel.board_handling.get_routing_board().rules;
            for (int i = 0; i < data.length; ++i) {
                ViaInfo curr_via = board_rules.via_infos.get(i);
                this.data[i][ColumnName.NAME.ordinal()] = curr_via.get_name();
                this.data[i][ColumnName.PADSTACK.ordinal()] = curr_via.get_padstack().name;
                this.data[i][ColumnName.CLEARANCE_CLASS.ordinal()] = board_rules.clearance_matrix.get_name(curr_via.get_clearance_class());
                this.data[i][ColumnName.ATTACH_SMD.ordinal()] = curr_via.attach_smd_allowed();
            }
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
            net.freerouting.freeroute.board.RoutingBoard routing_board = board_frame.board_panel.board_handling.get_routing_board();
            BoardRules board_rules = routing_board.rules;
            Object via_name = getValueAt(p_row, ColumnName.NAME.ordinal());
            if (!(via_name instanceof String)) {
                System.out.println("ViaVindow.setValueAt: String expected");
                return;
            }
            ViaInfo via_info = board_rules.via_infos.get((String) via_name);
            if (via_info == null) {
                System.out.println("ViaVindow.setValueAt: via_info not found");
                return;
            }

            if (p_col == ColumnName.NAME.ordinal()) {
                if (!(p_value instanceof String)) {
                    return;
                }
                String new_name = (String) p_value;
                if (board_rules.via_infos.name_exists(new_name)) {
                    return;
                }
                via_info.set_name(new_name);
                board_frame.savable_subwindows.get(SavableSubwindowKey.VIA).refresh();
            } else if (p_col == ColumnName.PADSTACK.ordinal()) {
                if (!(p_value instanceof String)) {
                    return;
                }
                String new_name = (String) p_value;
                net.freerouting.freeroute.library.Padstack new_padstack = routing_board.library.get_via_padstack(new_name);
                if (new_padstack == null) {
                    System.out.println("ViaVindow.setValueAt: via padstack not found");
                    return;
                }
                via_info.set_padstack(new_padstack);
            } else if (p_col == ColumnName.CLEARANCE_CLASS.ordinal()) {
                if (!(p_value instanceof String)) {
                    return;
                }
                String new_name = (String) p_value;
                int new_cl_class_index = board_rules.clearance_matrix.get_no(new_name);
                {
                    if (new_cl_class_index < 0) {
                        System.out.println("ViaVindow.setValueAt: clearance class not found");
                        return;
                    }
                }
                via_info.set_clearance_class(new_cl_class_index);
            } else if (p_col == ColumnName.ATTACH_SMD.ordinal()) {
                if (!(p_value instanceof Boolean)) {
                    System.out.println("ViaVindow.setValueAt: Boolean expected");
                    return;
                }
                Boolean attach_smd = (Boolean) p_value;
                via_info.set_attach_smd_allowed(attach_smd);
            }
            this.data[p_row][p_col] = p_value;
            fireTableCellUpdated(p_row, p_col);
        }

        @Override
        public boolean isCellEditable(int p_row, int p_col) {
            return true;
        }

        @Override
        public Class<?> getColumnClass(int p_col) {
            return getValueAt(0, p_col).getClass();
        }

    }

    private enum ColumnName {
        NAME, PADSTACK, CLEARANCE_CLASS, ATTACH_SMD
    }
}
