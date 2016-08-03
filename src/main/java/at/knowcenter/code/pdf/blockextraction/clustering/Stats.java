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

import gnu.trove.list.array.TDoubleArrayList;

/**
 * 
 * 
 * @author Roman Kern <rkern@know-center.at>
 */
public final class Stats {
    TDoubleArrayList values = new TDoubleArrayList();
    
    public void increment(double d) {
        values.add(d);
    }
    
    public int getN() {
        return values.size();
    }

    public double getMean() {
        double sum = 0;
        int n = values.size();
        for (int i = 0; i < n; i++) {
            sum += values.getQuick(i);
        }
        return sum / n;
    }
    
    public double getStandardDeviation() {
        return getStandardDeviation(getMean());
    }
    
    private double getStandardDeviation(double mean) {
        double sum = 0;
        int n = values.size();
        for (int i = 0; i < n; i++) {
            double d = values.getQuick(i) - mean;
            sum += d*d;
        }
        return Math.sqrt(sum / (n+1));
    }
    
    public double getMaxExpected(double scale) {
        double m = getMean();
        return m + scale*getStandardDeviation(m);
    }
}