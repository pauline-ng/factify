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

package at.knowcenter.code.pdf.blockextraction.clustering;

/**
 * Pairs of clusters sortable in increasing distance.
 * 
 * @author Roman Kern <rkern@tugraz.at>
 */
final class ClusterPair implements Comparable<ClusterPair> {
    final Cluster a;
    final Cluster b;
    final double d;
    
    /**
     * Creates a new instance of this class.
     * @param a first cluster
     * @param b second cluster
     * @param d distance
     */
    public ClusterPair(Cluster a, Cluster b, double d) {
        this.a = a;
        this.b = b;
        this.d = d;
    }

    @Override
    public int compareTo(ClusterPair o) {
        int r = Double.compare(d, o.d);
        if (r == 0) {
            r  = (a.id+b.id) - (o.a.id+o.b.id);
            if (r == 0) {
                r = a.id - o.a.id;
            }
        }
        return r;
    }
}