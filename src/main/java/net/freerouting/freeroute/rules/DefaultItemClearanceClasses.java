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
 * DefaultItemClearancesClasses.java
 *
 * Created on 12. Juni 2005, 07:19
 *
 */
package net.freerouting.freeroute.rules;

import java.util.Arrays;

/**
 *
 * @author Alfons Wirtz
 */
@SuppressWarnings("serial")
public class DefaultItemClearanceClasses implements java.io.Serializable {

    private static final int LENGTH_ARRAY_CLASSES = ItemClass.values().length;
    private final int[] arr = new int[LENGTH_ARRAY_CLASSES];

    /**
     * Creates a new instance of DefaultItemClearancesClasses
     */
    public DefaultItemClearanceClasses() {
        Arrays.fill(arr, 1);
    }

    DefaultItemClearanceClasses(DefaultItemClearanceClasses p_classes) {
        System.arraycopy(p_classes.arr, 0, arr, 0, LENGTH_ARRAY_CLASSES);
    }

    /**
     * Returns the number of the default clearance class for the input item
     * class.
     */
    public int get(ItemClass p_item_class) {
        return this.arr[p_item_class.ordinal()];
    }

    /**
     * Sets the index of the default clearance class of the input item class in
     * the clearance matrix to p_index.
     */
    public void set(ItemClass p_item_class, int p_index) {
        this.arr[p_item_class.ordinal()] = p_index;
    }

    /**
     * Sets the indices of all default item clearance classes to p_index.
     */
    public void set_all(int p_index) {
        Arrays.fill(arr, p_index);
    }
}
