/**
 * Copyright (C) 2010
 * "Kompetenzzentrum fuer wissensbasierte Anwendungen Forschungs- und EntwicklungsgmbH"
 * (Know-Center), Graz, Austria, office@know-center.at.
 *
 * Licensees holding valid Know-Center Commercial licenses may use this file in
 * accordance with the Know-Center Commercial License Agreement provided with
 * the Software or, alternatively, in accordance with the terms contained in
 * a written agreement between Licensees and Know-Center.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package at.knowcenter.code.pdf.blockrelation.geometric.delaunay;

/*
 * Copyright (c) 2007 by L. Paul Chew.
 *
 * Permission is hereby granted, without written agreement and without
 * license or royalty fees, to use, copy, modify, and distribute this
 * software and its documentation for any purpose, subject to the following
 * conditions:
 *
 * The above copyright notice and this permission notice shall be included
 * in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS
 * OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER
 * DEALINGS IN THE SOFTWARE.
 */

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * A Triangle is an immutable Set of exactly three Pnts.
 *
 * All Set operations are available. Individual vertices can be accessed via
 * iterator() and also via triangle.get(index).
 *
 * Note that, even if two triangles have the same vertex set, they are
 * *different* triangles. Methods equals() and hashCode() are consistent with
 * this rule.
 *
 * @author Paul Chew
 *
 * Created December 2007. Replaced general simplices with geometric triangle.
 *
 */
class Triangle extends ArraySet<Pnt> {

    private int idNumber;                   // The id number
    private Pnt circumcenter = null;        // The triangle's circumcenter

    private static int idGenerator = 0;     // Used to create id numbers
    public static boolean moreInfo = false; // True iff more info in toString

    /**
     * @param vertices the vertices of the Triangle.
     * @throws IllegalArgumentException if there are not three distinct vertices
     */
    public Triangle (Pnt... vertices) {
        this(Arrays.asList(vertices));
    }

    /**
     * @param collection a Collection holding the Simplex vertices
     * @throws IllegalArgumentException if there are not three distinct vertices
     */
    public Triangle (Collection<? extends Pnt> collection) {
        super(collection);
        idNumber = idGenerator++;
        if (this.size() != 3)
            throw new IllegalArgumentException("Triangle must have 3 vertices");
    }

    @Override
    public String toString () {
        if (!moreInfo) return "Triangle" + idNumber;
        return "Triangle" + idNumber + super.toString();
    }

    /**
     * Get arbitrary vertex of this triangle, but not any of the bad vertices.
     * @param badVertices one or more bad vertices
     * @return a vertex of this triangle, but not one of the bad vertices
     * @throws NoSuchElementException if no vertex found
     */
    public Pnt getVertexButNot (Pnt... badVertices) {
        Collection<Pnt> bad = Arrays.asList(badVertices);
        for (Pnt v: this) if (!bad.contains(v)) return v;
        throw new NoSuchElementException("No vertex found");
    }

    /**
     * True iff triangles are neighbors. Two triangles are neighbors if they
     * share a facet.
     * @param triangle the other Triangle
     * @return true iff this Triangle is a neighbor of triangle
     */
    public boolean isNeighbor (Triangle triangle) {
        int count = 0;
        for (Pnt vertex: this)
            if (!triangle.contains(vertex)) count++;
        return count == 1;
    }

    /**
     * Report the facet opposite vertex.
     * @param vertex a vertex of this Triangle
     * @return the facet opposite vertex
     * @throws IllegalArgumentException if the vertex is not in triangle
     */
    public ArraySet<Pnt> facetOpposite (Pnt vertex) {
        ArraySet<Pnt> facet = new ArraySet<Pnt>(this);
        if (!facet.remove(vertex))
            throw new IllegalArgumentException("Vertex not in triangle");
        return facet;
    }

    /**
     * @return the triangle's circumcenter
     */
    public Pnt getCircumcenter () {
        if (circumcenter == null)
            circumcenter = Pnt.circumcenter(this.toArray(new Pnt[0]));
        return circumcenter;
    }

    /* The following two methods ensure that a Triangle is immutable */

    @Override
    public boolean add (Pnt vertex) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Iterator<Pnt> iterator () {
        return new Iterator<Pnt>() {
            private Iterator<Pnt> it = Triangle.super.iterator();
            public boolean hasNext() {return it.hasNext();}
            public Pnt next() {return it.next();}
            public void remove() {throw new UnsupportedOperationException();}
        };
    }

    /* The following two methods ensure that all triangles are different. */

    @Override
    public int hashCode () {
        return (int)(idNumber^(idNumber>>>32));
    }

    @Override
    public boolean equals (Object o) {
        return (this == o);
    }

}