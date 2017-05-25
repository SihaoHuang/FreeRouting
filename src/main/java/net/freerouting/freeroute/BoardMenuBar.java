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
 * BoardMenuBar.java
 *
 * Created on 11. Februar 2005, 10:17
 */
package net.freerouting.freeroute;

/**
 * Creates the menu bar of a board frame together with its menu items.
 *
 * @author Alfons Wirtz
 */
@SuppressWarnings("serial")
final class BoardMenuBar extends javax.swing.JMenuBar {

    private final BoardMenuFile file_menu;

    void add_design_dependent_items() {
        this.file_menu.add_design_dependent_items();
    }

    private BoardMenuBar (BoardFrame p_board_frame, boolean p_session_file_option) {
        file_menu = BoardMenuFile.get_instance(p_board_frame, p_session_file_option);
    }

    /**
     * Creates a new BoardMenuBar together with its menus
     */
    public static BoardMenuBar get_instance(BoardFrame p_board_frame, boolean p_session_file_option) {
        BoardMenuBar menubar = new BoardMenuBar(p_board_frame, p_session_file_option);
        menubar.add(menubar.file_menu);

        javax.swing.JMenu display_menu = BoardMenuDisplay.get_instance(p_board_frame);
        menubar.add(display_menu);

        javax.swing.JMenu parameter_menu = BoardMenuParameter.get_instance(p_board_frame);
        menubar.add(parameter_menu);

        javax.swing.JMenu rules_menu = BoardMenuRules.get_instance(p_board_frame);
        menubar.add(rules_menu);

        javax.swing.JMenu info_menu = BoardMenuInfo.get_instance(p_board_frame);
        menubar.add(info_menu);

        javax.swing.JMenu other_menu = BoardMenuOther.get_instance(p_board_frame);
        menubar.add(other_menu);

        javax.swing.JMenu help_menu = new BoardMenuHelp(p_board_frame);
        menubar.add(help_menu);

        return menubar;
    }
}
