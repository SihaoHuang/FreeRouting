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
 * LocationAndVisibilitySavableWindow.java
 *
 * Created on 20. Dezember 2004, 09:03
 */
package net.freerouting.freeroute;

import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Subwindow of the board frame, whose location and visibility can be saved and
 * read from disc.
 *
 * @author Alfons Wirtz
 */
@SuppressWarnings("serial")
public abstract class BoardSavableSubWindow extends BoardSubWindow {

    /**
     * Reads the data of this frame from disc. Returns false, if the reading
     * failed.
     */
    public boolean read(java.io.ObjectInputStream p_object_stream) {
        try {
            SavedAttributes saved_attributes = (SavedAttributes) p_object_stream.readObject();
            this.setBounds(saved_attributes.bounds.getBounds());
            this.setVisible(saved_attributes.is_visible);
            return true;
        } catch (IOException | ClassNotFoundException e) {
            Logger.getLogger(BoardSavableSubWindow.class.getName()).log(Level.SEVERE, "SelectParameterWindow.read: read failed", e);
            return false;
        }
    }

    /**
     * Saves this frame to disk.
     */
    public void save(java.io.ObjectOutputStream p_object_stream) {
        SavedAttributes saved_attributes = new SavedAttributes(this.getBounds(), this.isVisible());

        try {
            p_object_stream.writeObject(saved_attributes);
        } catch (java.io.IOException e) {
            Logger.getLogger(BoardSavableSubWindow.class.getName()).log(Level.SEVERE, "BoardSubWindow.save: save failed", e);
        }
    }

    /**
     * Refreshs the displayed values in this window. To be overwritten in
     * derived classes.
     */
    public void refresh() {

    }

    /**
     * Type for attributes of this class, which are saved to an Objectstream.
     */
    @SuppressWarnings("serial")
    private static class SavedAttributes implements java.io.Serializable {

        public final Rectangle2D bounds;
        public final boolean is_visible;

        SavedAttributes(Rectangle2D p_bounds, boolean p_is_visible) {
            bounds = p_bounds;
            is_visible = p_is_visible;
        }
    }
}
