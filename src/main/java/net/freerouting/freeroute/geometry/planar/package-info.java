/*
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
/**
 * Contains general purpose 2-dimensional geometry classes.
 *
 * <h1>Some preliminary considerations on computational geometry:</h1>
 *
 * <p>
 * What is better in arithmetic calculations? Using floating-point or
 * fixed-point arithmetic? In floating-point arithmetic we can forget about
 * absolute exactness. There is for example no way to find out if three points
 * are exactly collinear in floating-point arithmetic. In fixed-point arithmetic
 * however there is an easy way to calculate for any line, if a point is on the
 * left, on the right, or exact on this line. That exactness is the main reason
 * to prefer fixed-point (integer) arithmetic to floating-point arithmetic in
 * computational geometry. </p>
 * <p>
 * With integer arithmetic we get no problems in calculations with orthogonal
 * lines, and some limited problems with 45-degree lines. With any-angle lines
 * however there is a problem. Between the start point and the end point of a
 * line segment there may not exist a single integer point exact on the line.
 * Also the intersection of two lines can in general not be represented exactly
 * by an integer point.</p>
 * <p>
 * Can we get exactness in calculations with shapes defined by integer points in
 * spite of this?</p>
 * <p>
 * Suppose we represent polygon shapes by its corner points. The intersection of
 * two polygon shapes is then represented by a subset of the corners of the two
 * polygons and the intersections of its boundary line segments. But these line
 * intersections can in general not be represented by integer points. If we
 * round them to integer points, the result will not be exact.</p>
 * <p>
 * But we can do better. We can represent shapes by its boundary lines instead
 * of its corner points. The intersection of two shapes is then described by a
 * subset of the boundary lines of the shapes. The only work to do is skipping
 * boundary lines that do not contribute to the result. But because we have not
 * calculated any new geometrical objects, the result of this calculation
 * remains exact.</p>
 * <p>
 * Similar problems exist with polygons for example. Therefore we will not
 * represent them by a sequence of corner points, but by a sequence of lines
 * with integer coordinates.</p>
 * <p>
 * Although our key concern will be on fixed-point (integer) arithmetic, we will
 * also use some floating-point and infinite precision rational number
 * arithmetic in our geometry system.</p>
 *
 * <h1>General Programming Principles used in the code:</h1>
 * <h2>Principle 1: Immutability</h2>
 * <p>
 * An object is called immutable, if its content will never change after it is
 * constructed. In this prototype all objects are created immutable, if there is
 * no good reason to do otherwise. In a language with a garbage collector there
 * never arises the need to copy an immutable object. Instead just another
 * reference to the object can be added. That is safe because nobody else is
 * allowed to change the object. The garbage collector recycles the object
 * automatically, when there is no more reference to it. Immutable objects also
 * have no synchronisation problems, in case they are accessed by several
 * threads simultaneously.</p>
 * <p>
 * Having objects in geometry immutable lets them look much more like they are
 * used in school or university mathematics. You do not expect in mathematics
 * for example, that the coordinates of a point will change later on.</p>
 *
 * <h2>Principle 2: Abstract Data Types</h2>
 * <p>
 * Object variables should be of abstract classes, if possible. Concrete classes
 * should only be used in the creation of objects, that is on the right side of
 * the ‘new’ keyword or in a so-called factory method. That makes it easy to
 * exchange and mix several implementations of an abstract class. The user has
 * no access to the actual implementation, but only to the abstract functions
 * every implementation must provide. There is one major problem. The virtual
 * function mechanism does not work in parameter position. This means that the
 * appropriate concrete class will not be found automatically, if an abstract
 * class is provided in parameter position. Adding for such functions a private
 * helper function for each concrete implementation of the abstract class solves
 * the problem, as long as the function has no more than one abstract parameter.
 * This solution still looks reasonable. Until now I could always avoid the need
 * for functions to have more than one abstract parameter. Functions with many
 * parameters may be bad designed anyway.</p>
 *
 * <h2>Description of the package:</h2>
 * <p>
 * This packages contains the implementation of 2-dimensional geometric objects
 * such as points, vectors, lines, shapes, and so on.</p>
 * <p>
 * The abstract class Point for example defines the behaviour, which must be
 * implemented by each concrete Point class. There are two concrete
 * implementations of the class Point at the moment, the class IntPoint, which
 * implements the Point as a tuple of integer values, and the class
 * RationalPoint. The class RationalPoint implements infinite precision integer
 * points in the projective plane. This is a generalisation of an implementation
 * of infinite precision rational points in the affine plane. With
 * RationalPoints for example the intersection of two lines can always be
 * calculated exactly. The usage of these two classes is mixed and
 * RationalPoints will always be converted to IntPoints if possible, to improve
 * the performance.</p>
 * <p>
 * Strongly related to the class Point is the class Vector. A Vector can be
 * added to a Point resulting in another Point. A Point can be subtracted from
 * another Point resulting in a Vector. Of course two vectors can be added
 * resulting in another vector. For every vector v there exists the negative
 * vector v.negate () so that v + v.negate () is the zero vector.</p>
 * <p>
 * Instead of angles we use Directions in our geometry system. The reason is
 * that for a Line (to be defined later) its Direction can be represented
 * exactly while its angle can be not. The best we can do with angles is to
 * approximate them by floating point numbers. But in this case there exists for
 * example no exact algorithm to determine if two angles are equal. A Direction
 * is defined as an equivalence class of Vectors. Two vectors define the same
 * Direction, if they point into the same direction.</p>
 * <p>
 * Directions are ordered in the following way. A Direction d1 is bigger than a
 * Direction d2, if it has a bigger angle with the positive X-axis. We can
 * compare the size of 2 Directions by calculating the determinant of their
 * defining vectors. This calculation is exact in integer arithmetic.</p>
 * <p>
 * An object of the class Line is defined as an infinite directed line. In
 * addition to an ordinary infinite line a directed line defines a positive and
 * a negative half-plane. The positive half-plane is to the left of the line,
 * and the negative half-plane to the right. Such a Line is represented by a
 * tuple of abstract points (a, b) on the line, which are assumed to be not
 * equal. The positive half-plane is the set of points p, so that a path from a
 * to b to p makes a left turn. Of course two different tuples of points can
 * define the same line.</p>
 * <p>
 * The class FloatPoint is used in arithmetic calculations, where execution
 * speed is more important than numerical accuracy. The class Line for example
 * contains the two methods: “intersection” and “intersection_approx”. Both of
 * them calculate the intersection of two lines. The function “intersection”
 * returns an object of the abstract class Point. The result is exact, but the
 * calculation may be slow, because it uses infinite precision arithmetic.</p>
 * <p>
 * The function “intersection_approx” uses double precision floating point
 * arithmetic to calculate an object of the class FloatPoint. This calculation
 * is quite fast, but the result of floating point arithmetic is not exact in
 * general.</p>
 * <p>
 * Because all calculations in the abstract class Point are expected to be
 * exact, FloatPoint is not derived from the abstract class Point, as IntPoint
 * and RationalPoint are.</p>
 * <p>
 * In the following section we describe the class hierarchy describing shapes in
 * the plane. The root of the hierarchy is the interface Area. It defines
 * functionality of of connected 2-dimensional sets of points in the plane,
 * which need not necessarily to be simply connected. (For non mathematicians
 * that means an Area may have holes.) Derived from the Interface Area is the
 * interface Shape, which defines functionality for simply connected areas
 * (areas without holes). Derived from the interface Shape are the Interface
 * ConvexShape and the abstract class PolylineShape. A shape is convex, if for
 * any 2 points a, b inside the shape the line segment from a to b is contained
 * completely in the shape. A PolylineShape is defined as a shape, whose border
 * consists of a sequence of straight lines. Derived from PolylineShape is the
 * class PolygonShape. A PolygonShape is a PolylineShape represented by a closed
 * polygon of corner points. From ConvexShape the class Circle and the abstract
 * class TileShape are derived. TileShape is also derived from PolylineShape,
 * There are currently 3 implementations of the class TileShape : IntBox,
 * IntOctagon and Simplex. IntBoxes are axis parallel rectangles with int
 * coordinates, IntOctagons are convex shapes, whose boundary line angles are
 * multiples of 45 degree,</p>
 * <p>
 * A Simplex is implemented as a finite set of lines. The shape of a Simplex is
 * defined as the intersection of all positive half-planes of its lines.
 * Simplexes are similar to convex polygons defined by a finite set of corner
 * points. The difference is that a Simplex may contain infinite parts. A
 * half-plane for example is a Simplex, but not a convex polygon. In an Integer
 * geometry system using Simplexes instead of convex polygons has many
 * advantages. Assume for example that we want to calculate the intersection of
 * two polygons. For this purpose we have to calculate the intersections of the
 * crossing edge lines. These intersections are in general no longer integer
 * points. This means, that the intersection of two convex polygons can not be
 * represented exactly by a convex polygon with integer coordinates. We could
 * use RationalPoints for the corners to achieve this goal, but then the
 * arithmetic calculations would get very slow. Calculating the intersection of
 * two Simplexes is quite different. We just take the sum of the defining lines
 * of the two Simplexes. The real work is skipping the lines, which do not
 * contribute to the result. So the intersection of two Simplexes calculated in
 * this way is exact, even if our lines have integer coordinates. A
 * RegularTileShape is a TileShape, where the directions of the boundary lines
 * may only be from a fixed finite set. IntBoxes and IntOctagons are
 * RegularTileShape´s, but Simplexes are not.</p>
 * <p>
 * In the class Polyline curves in the plane consisting of a list of straight
 * lines are described. No two consecutive lines in that list may be parallel.
 * Therefore a Polyline of n lines with integer coordinates is equivalent to a
 * polygon of n – 1 RationalPoints, where the k-th RationalPoint is the
 * intersection of the k-th and the k+1-th line for 0 &lt;= k &lt; n –1 (if we
 * disregard the possibility of integer overflow). We will use the class
 * Polyline especially to describe Traces on a Printed Circuit Board. That
 * enables us to implement a numerical stable any-angle pushing algorithm for
 * traces. The reason is similar as for Simplexes. We do not use intersection
 * points of lines to create the geometry of new trace objects in the pushing
 * algorithm. Instead we use lines, which already exist, for that purpose. That
 * concept is also advantageous when we want to smoothen traces by cutting of
 * corners. If this is done with polygons represented by corners, the
 * intersection of lines has to be calculated and used as primary data. If
 * integer points are used, these intersection points are not exact. Neighbour
 * lines change slightly and a small clearance violation may occur. If rational
 * points are used, the algorithm gets very slow. With Polylines these problems
 * do not exist, because intersection points of lines never become primary
 * data.</p>
 */
package net.freerouting.freeroute.geometry.planar;
