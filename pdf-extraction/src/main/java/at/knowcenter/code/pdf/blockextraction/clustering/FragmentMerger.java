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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import at.knowcenter.code.api.pdf.Block;
import at.knowcenter.code.api.pdf.Page;
import at.knowcenter.code.api.pdf.TextFragment;

/**
 * 
 * 
 * @author Roman Kern <rkern@tugraz.at>
 */
public abstract class FragmentMerger {
    protected final SortedSet<Cluster> clusters;
    protected double maxMergeHeight;
    protected final Page page;
    

    /**
     * Creates a new instance of this class.
     * @param fragments 
     * @param clusterComparator 
     */
    public FragmentMerger(Page page, Iterable<TextFragment> fragments, Comparator<Cluster> clusterComparator) {
        this.clusters = new TreeSet<Cluster>(clusterComparator);
        this.page = page;
        
        int id = 0;
        double maxFragmentHeight = 0;
        for (TextFragment fragment : fragments) {
            Cluster c = new Cluster(id++, null, null, Arrays.asList(fragment));
            if (c.maxHeight > maxFragmentHeight) { maxFragmentHeight = c.maxHeight; }
            clusters.add(c);
        }
        maxMergeHeight = getMaxMergeHeight(maxFragmentHeight);
    }
    
    /**
     * Creates a new instance of this class.
     * @param collection 
     * @param clusterComparator 
     */
    public FragmentMerger(Page page, Block collection, Comparator<Cluster> clusterComparator) {
        this.clusters = new TreeSet<Cluster>(clusterComparator);
        this.page = page;
        
        int id = 0;
        double maxFragmentHeight = 0;
        
        if (collection.hasSubBlocks()) {
            for (Block sc : collection.getSubBlocks()) {
                Cluster c = new Cluster(id++, sc, getResultCollectionOrder(), sc.getFragments());
                if (c.maxHeight > maxFragmentHeight) { maxFragmentHeight = c.maxHeight; }
                clusters.add(c);
            }
        } else {
            for (TextFragment fragment : collection.getFragments()) {
                Cluster c = new Cluster(id++, null, null, Arrays.asList(fragment));
                if (c.maxHeight > maxFragmentHeight) { maxFragmentHeight = c.maxHeight; }
                clusters.add(c);
            }
        }
        maxMergeHeight = getMaxMergeHeight(maxFragmentHeight);
    }

    /**
     * @param maxFragmentHeight
     * @return the max merge height
     */
    protected abstract double getMaxMergeHeight(double maxFragmentHeight);

    /**
     * Merges all fragment according to the threshold
     * @return the result of the merge operation
     */
    public Block merge() {
        boolean isFinished = false;
        do {
            SortedSet<ClusterPair> candidates = new TreeSet<ClusterPair>();
            List<Cluster> clusterList = new ArrayList<Cluster>(clusters);
            
            if (useCycleState()) {
                for (int i = 0; i < clusterList.size(); i++) {
                    Cluster a = clusterList.get(i);
                    resetCycleStats(a);
                }
                for (int i = 0; i < clusterList.size(); i++) {
                    Cluster a = clusterList.get(i);
                    for (int j = i+1; j < clusterList.size(); j++) {
                        Cluster b = clusterList.get(j);
                        if (isMergeCandidate(a, b)) {
                            assert a.minId != b.minId : "Two clusters must never have the same min-id!";
                            if (a.minId <= b.minId)
                                updateCycleStats(a, b);
                            else
                                updateCycleStats(b, a);
                        } else if (!useExhaustiveSearch()) {
                            break;
                        }
                    }
                }
            }
            
            for (int i = 0; i < clusterList.size(); i++) {
                Cluster a = clusterList.get(i);
                for (int j = i+1; j < clusterList.size(); j++) {
                    Cluster b = clusterList.get(j);
                    
                    if (isMergeCandidate(a, b)) {
                        assert a.minId != b.minId : "Two clusters must never have the same min-id!";
                        
                        double distance = a.minId <= b.minId ? getDistance(a, b) : getDistance(b, a);
                        if (!Double.isNaN(distance)) {
                            candidates.add(new ClusterPair(a, b, distance));
                        }
                    } else if (!useExhaustiveSearch()) {
                        break;
                    }
                }
            }
            
            int pendingMergeCandidates = 0;
            Set<Cluster> mergedClusters = new HashSet<Cluster>();
            for (ClusterPair candidate : candidates) {
                if (!mergedClusters.contains(candidate.a) && !mergedClusters.contains(candidate.b)) {
                    clusters.remove(candidate.a);
                    clusters.remove(candidate.b);
                    
                    Cluster c;
                    if (candidate.a.id < candidate.b.id) {
                        c = new Cluster(candidate.a.id, candidate.a, candidate.b);
                    } else {
                        c = new Cluster(candidate.b.id, candidate.b, candidate.a);
                    }
                    clusters.add(c);
                    
                } else {
                    pendingMergeCandidates++;
                }
                // always mark both candidates as no longer available as the current pair is the highest ranked
                mergedClusters.add(candidate.a);
                mergedClusters.add(candidate.b);
            }
            isFinished = candidates.isEmpty() || pendingMergeCandidates == 0;
        } while (!isFinished);
        
        postProcessClusters();
        
        return getMergeResult();
    }

    /**
     * @return
     */
    protected boolean useCycleState() {
        return false;
    }

    /**
     * @param a
     */
    protected void resetCycleStats(Cluster a) {
    }

    /**
     * @param a
     * @param b
     */
    protected void updateCycleStats(Cluster a, Cluster b) {
    }

    /**
     * @return
     */
    protected boolean useExhaustiveSearch() {
        return false;
    }

    /**
     * Callback for derived classes to add logic to filter out spurious clusters.
     */
    protected void postProcessClusters() {
    }

    protected abstract double getDistance(Cluster a, Cluster b);

    /**
     * @param a
     * @param b
     * @return
     */
    protected boolean isMergeCandidate(Cluster a, Cluster b) {
        return a.isMergeCandidate(b, maxMergeHeight);
    }

    private Block getMergeResult() {
        SortedSet<Block> collections = new TreeSet<Block>(getResultCollectionOrder());
        
        for (Cluster c : clusters) {
            Block entry;
            if (c.collections != null) {
                SortedSet<Block> sorted = new TreeSet<Block>(c.collections.comparator());
                sorted.addAll(c.collections);
                entry = new Block(page, sorted);
            } else {
                Set<TextFragment> sorted = new TreeSet<TextFragment>(getResultFragmentOrder());
                sorted.addAll(c.fragments);
                entry = new Block(page, new ArrayList<TextFragment>(sorted));
            }
            
            collections.add(entry);
        }
        
        return new Block(page, collections);
    }

    /**
     * @return
     */
    protected abstract Comparator<TextFragment> getResultFragmentOrder();

    /**
     * @return
     */
    protected abstract Comparator<Block> getResultCollectionOrder();

    protected boolean contains(Cluster a, Cluster b, double threshold) {
        double xs1 = a.minX;
        double xe1 = a.maxX;
    
        double xs2 = b.minX;
        double xe2 = b.maxX;
        
        double w = Math.min(a.maxX - a.minId, b.maxX - b.minId);
    
        if (xe2 <= xs1 || xs2 >= xe1) {
            return false;
        } else if ((xs2 > xs1) && (xe2 > xe1)) {
            double overlap = xe1 - xs2;
            double overlapPercent = overlap / (w);
            return (overlapPercent > threshold);
        } else if ((xs2 < xs1) && (xe2 < xe1)) {
            double overlap = xe2 - xs1;
            double overlapPercent = overlap / (w);
            return (overlapPercent > threshold);
        }
        return true;
    }
}
