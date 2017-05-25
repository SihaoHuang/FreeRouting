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
 * BoardRulesMenu.java
 *
 * Created on 20. Februar 2005, 06:00
 */
package net.freerouting.freeroute;

import java.util.Locale;
import java.util.Map;
import static java.util.Map.entry;
import net.freerouting.freeroute.SavableSubwindows.SAVABLE_SUBWINDOW_KEY;

/**
 * Creates the rules menu of a board frame.
 *
 * @author Alfons Wirtz
 */
@SuppressWarnings("serial")
final class BoardMenuRules extends javax.swing.JMenu {

    private final BoardFrame board_frame;
    private final java.util.ResourceBundle resources;

    /**
     * Creates a new instance of BoardRulesMenu
     */
    private BoardMenuRules(BoardFrame p_board_frame) {
        board_frame = p_board_frame;
        resources = java.util.ResourceBundle.getBundle("net.freerouting.freeroute.resources.BoardMenuRules", Locale.getDefault());
    }

    /**
     * Returns a new windows menu for the board frame.
     */
    public static BoardMenuRules get_instance(BoardFrame p_board_frame) {
        BoardMenuRules rules_menu = new BoardMenuRules(p_board_frame);

        rules_menu.setText(rules_menu.resources.getString("rules"));

        Map<SAVABLE_SUBWINDOW_KEY, String> menu_items = Map.ofEntries(
                entry(SAVABLE_SUBWINDOW_KEY.CLEARANCE_MATRIX, "clearance_matrix"),
                entry(SAVABLE_SUBWINDOW_KEY.VIA, "vias"),
                entry(SAVABLE_SUBWINDOW_KEY.NET_INFO, "nets"),
                entry(SAVABLE_SUBWINDOW_KEY.EDIT_NET_RULES, "net_classes"));

        for (Map.Entry<SAVABLE_SUBWINDOW_KEY, String> entry : menu_items.entrySet()) {
            javax.swing.JMenuItem menu_item = new javax.swing.JMenuItem();
            menu_item.setText(rules_menu.resources.getString(entry.getValue()));
            menu_item.addActionListener((java.awt.event.ActionEvent evt) -> {
                rules_menu.board_frame.savable_subwindows.get(entry.getKey()).setVisible(true);
            });
            rules_menu.add(menu_item);
        }

        return rules_menu;
    }
}
