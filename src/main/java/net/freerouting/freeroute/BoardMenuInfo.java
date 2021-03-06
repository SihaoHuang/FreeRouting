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
 * BoardLibraryMenu.java
 *
 * Created on 6. Maerz 2005, 05:37
 */
package net.freerouting.freeroute;

import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import static java.util.Map.entry;

/**
 *
 * @author Alfons Wirtz
 */
@SuppressWarnings("serial")
final class BoardMenuInfo extends BoardMenu {

    /**
     * Returns a new info menu for the board frame.
     */
    static BoardMenuInfo get_instance(BoardFrame p_board_frame) {
        BoardMenuInfo info_menu = new BoardMenuInfo(p_board_frame);
        java.util.ResourceBundle resources = java.util.ResourceBundle.getBundle(
                BoardMenuInfo.class.getPackageName() + ".resources.BoardMenuInfo",
                Locale.getDefault());

        info_menu.setText(resources.getString("info"));

        Map<SavableSubwindowKey, String> menu_items = Map.ofEntries(
                entry(SavableSubwindowKey.PACKAGES, "library_packages"),
                entry(SavableSubwindowKey.PADSTACKS, "library_padstacks"),
                entry(SavableSubwindowKey.COMPONENTS, "board_components"),
                entry(SavableSubwindowKey.INCOMPLETES, "incompletes"),
                entry(SavableSubwindowKey.LENGHT_VIOLATIONS, "length_violations"),
                entry(SavableSubwindowKey.CLEARANCE_VIOLATIONS, "clearance_violations"),
                entry(SavableSubwindowKey.UNCONNECTED_ROUTE, "unconnected_route"),
                entry(SavableSubwindowKey.ROUTE_STUBS, "route_stubs"));

        for (Entry<SavableSubwindowKey, String> entry : menu_items.entrySet()) {
            javax.swing.JMenuItem menu_item = new javax.swing.JMenuItem();
            menu_item.setText(resources.getString(entry.getValue()));
            menu_item.addActionListener((java.awt.event.ActionEvent evt) -> {
                info_menu.board_frame.savable_subwindows.get(entry.getKey()).setVisible(true);
            });
            info_menu.add(menu_item);
        }

        return info_menu;
    }

    /**
     * Creates a new instance of BoardLibraryMenu
     */
    private BoardMenuInfo(BoardFrame p_board_frame) {
        super(p_board_frame);
    }
}
