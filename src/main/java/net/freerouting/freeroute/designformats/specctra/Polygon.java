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
 * Polygon.java
 *
 * Created on 15. Mai 2004, 08:39
 */
package net.freerouting.freeroute.designformats.specctra;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import net.freerouting.freeroute.datastructures.IdentifierType;
import net.freerouting.freeroute.datastructures.IndentFileWriter;
import static net.freerouting.freeroute.designformats.specctra.ScopeKeyword.skip_scope;
import net.freerouting.freeroute.geometry.planar.IntPoint;
import net.freerouting.freeroute.geometry.planar.SimplexUtils;

/**
 * Describes a polygon in a Specctra dsn file.
 *
 * @author alfons
 */
class Polygon extends Shape {

    /**
     * Reads a closed polygon scope from a Specctra dsn file. If
     * p_layer_structure == null, only Layer.PCB and Layer.Signal are expected,
     * no induvidual layers.
     */
    static Shape read_polygon_scope(Scanner p_scanner, LayerStructure p_layer_structure) throws ReadScopeException {
        try {
            LayerInfo polygon_layer = read_layer_shape_scope(p_scanner, p_layer_structure);
            // overread the aperture width
            Object next_token = p_scanner.next_token();
            Collection<Object> coor_list = new LinkedList<>();

            // read the coordinates of the polygon
            for (;;) {
                next_token = p_scanner.next_token();
                if (next_token == null) {
                    throw new ReadScopeException("Polygon.read_polygon_scope: unexpected end of file");
                }
                if (next_token == Keyword.OPEN_BRACKET) {
                    // unknown scope
                    skip_scope(p_scanner);
                    next_token = p_scanner.next_token();
                }
                if (next_token == Keyword.CLOSED_BRACKET) {
                    break;
                }
                coor_list.add(next_token);
            }
            double[] coor_arr = new double[coor_list.size()];
            Iterator<Object> it = coor_list.iterator();
            for (int i = 0; i < coor_arr.length; ++i) {
                Object next_object = it.next();
                if (next_object instanceof Double) {
                    coor_arr[i] = (double) next_object;
                } else if (next_object instanceof Integer) {
                    coor_arr[i] = ((Number) next_object).doubleValue();
                } else {
                    throw new ReadScopeException("Polygon.read_polygon_scope: number expected");
                }
            }
            return new Polygon(polygon_layer, coor_arr);
        } catch (java.io.IOException e) {
            throw new ReadScopeException("Polygon.read_polygon_scope: IO error scanning file", e);
        }
    }

    final double[] coor;

    /**
     * Creates a new instance of Polygon p_coor is an array of dimension of
     * dimension 2 * point_count and contains x0, y0, x1, y1, ... If the polygon
     * is used as rectangle,
     */
    Polygon(LayerInfo p_layer, double[] p_coor) {
        super(p_layer);
        coor = (p_coor == null) ? null : p_coor.clone();
    }

    @Override
    net.freerouting.freeroute.geometry.planar.Shape transform_to_board(CoordinateTransform p_coordinate_transform) {
        IntPoint[] corner_arr = new IntPoint[coor.length / 2];
        double[] curr_point = new double[2];
        for (int i = 0; i < corner_arr.length; ++i) {
            curr_point[0] = coor[2 * i];
            curr_point[1] = coor[2 * i + 1];
            corner_arr[i] = p_coordinate_transform.dsn_to_board(curr_point).round();
        }
        return new net.freerouting.freeroute.geometry.planar.PolygonShape(corner_arr);
    }

    @Override
    net.freerouting.freeroute.geometry.planar.Shape transform_to_board_rel(CoordinateTransform p_coordinate_transform) {
        if (coor.length < 2) {
            return SimplexUtils.EMPTY;
        }
        IntPoint[] corner_arr = new IntPoint[coor.length / 2];
        for (int i = 0; i < corner_arr.length; ++i) {
            int curr_x = (int) Math.round(p_coordinate_transform.dsn_to_board(coor[2 * i]));
            int curr_y = (int) Math.round(p_coordinate_transform.dsn_to_board(coor[2 * i + 1]));
            corner_arr[i] = new IntPoint(curr_x, curr_y);
        }
        return new net.freerouting.freeroute.geometry.planar.PolygonShape(corner_arr);
    }

    @Override
    Rectangle bounding_box() {
        double[] bounds = new double[4];
        bounds[0] = Integer.MAX_VALUE;
        bounds[1] = Integer.MAX_VALUE;
        bounds[2] = Integer.MIN_VALUE;
        bounds[3] = Integer.MIN_VALUE;
        for (int i = 0; i < coor.length; ++i) {
            if (i % 2 == 0) {
                // x coordinate
                bounds[0] = Math.min(bounds[0], coor[i]);
                bounds[2] = Math.max(bounds[2], coor[i]);
            } else {
                // x coordinate
                bounds[1] = Math.min(bounds[1], coor[i]);
                bounds[3] = Math.max(bounds[3], coor[i]);
            }
        }
        return new Rectangle(layer, bounds);
    }

    /**
     * Writes this polygon as a scope to an output dsn-file.
     */
    @Override
    void write_scope(IndentFileWriter p_file, IdentifierType p_identifier_type) throws java.io.IOException {
        p_file.start_scope("polygon ");
        p_identifier_type.write(this.layer.name, p_file);
        p_file.write(" ");
        p_file.write(Integer.toString(0));
        int corner_count = coor.length / 2;
        for (int i = 0; i < corner_count; ++i) {
            p_file.new_line();
            p_file.write(Double.toString(coor[2 * i]));
            p_file.write(" ");
            p_file.write(Double.toString(coor[2 * i + 1]));
        }
        p_file.end_scope();
    }

    @Override
    void write_scope_int(IndentFileWriter p_file, IdentifierType p_identifier_type) throws java.io.IOException {
        p_file.start_scope("polygon ");
        p_identifier_type.write(this.layer.name, p_file);
        p_file.write(" ");
        p_file.write(Integer.toString(0));
        int corner_count = coor.length / 2;
        for (int i = 0; i < corner_count; ++i) {
            p_file.new_line();
            Integer curr_coor = (int) Math.round(coor[2 * i]);
            p_file.write(curr_coor.toString());
            p_file.write(" ");
            curr_coor = (int) Math.round(coor[2 * i + 1]);
            p_file.write(curr_coor.toString());
        }
        p_file.end_scope();
    }
}
