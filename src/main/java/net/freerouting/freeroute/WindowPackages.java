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
 * PackagesWindow.java
 *
 * Created on 7. Maerz 2005, 09:14
 */
package net.freerouting.freeroute;

import java.util.List;
import java.util.Locale;
import net.freerouting.freeroute.library.Package;
import net.freerouting.freeroute.library.Packages;

/**
 * Window displaying the library packagess.
 *
 * @author Alfons Wirtz
 */
@SuppressWarnings("serial")
public final class WindowPackages extends WindowObjectListWithFilter {

    /**
     * Creates a new instance of PackagesWindow
     */
    private WindowPackages(BoardFrame p_board_frame) {
        super(p_board_frame);
        java.util.ResourceBundle resources
                = java.util.ResourceBundle.getBundle("net.freerouting.freeroute.resources.Default", Locale.getDefault());
        this.setTitle(resources.getString("packages"));
    }

    static WindowPackages getInstance(BoardFrame p_board_frame) {
        WindowPackages window = new WindowPackages(p_board_frame);
        p_board_frame.set_context_sensitive_help(window, "WindowObjectList_LibraryPackages");
        return window;
    }

    /**
     * Fills the list with the library packages.
     */
    @Override
    void fill_list() {
        Packages packages = this.board_frame.board_panel.board_handling.get_routing_board().library.packages;
        Package[] sorted_arr = new Package[packages.count()];
        for (int i = 0; i < sorted_arr.length; ++i) {
            sorted_arr[i] = packages.get(i + 1);
        }
        java.util.Arrays.parallelSort(sorted_arr);
        for (int i = 0; i < sorted_arr.length; ++i) {
            this.add_to_list(sorted_arr[i]);
        }
        this.list.setVisibleRowCount(Math.min(packages.count(), DEFAULT_TABLE_SIZE));
    }

    @Override
    void select_instances() {
        List<?> selected_packages = list.getSelectedValuesList();
        if (selected_packages.isEmpty()) {
            return;
        }
        net.freerouting.freeroute.board.RoutingBoard routing_board = board_frame.board_panel.board_handling.get_routing_board();
        java.util.Set<net.freerouting.freeroute.board.Item> board_instances = new java.util.TreeSet<>();
        java.util.Collection<net.freerouting.freeroute.board.Item> board_items = routing_board.get_items();
        for (net.freerouting.freeroute.board.Item curr_item : board_items) {
            if (curr_item.get_component_no() > 0) {
                net.freerouting.freeroute.board.Component curr_component = routing_board.components.get(curr_item.get_component_no());
                if (selected_packages.contains(curr_component.get_package())) {
                    board_instances.add(curr_item);
                }
            }
        }
        board_frame.board_panel.board_handling.select_items(board_instances);
        board_frame.zoom_all();
        board_frame.board_panel.board_handling.zoom_selection();
    }
}
