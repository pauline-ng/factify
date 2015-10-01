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
package at.knowcenter.code.pdf.blockclassification.detection;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import at.knowcenter.clustering.Cluster;
import at.knowcenter.clustering.Clustering;
import at.knowcenter.clustering.ClusteringException;
import at.knowcenter.clustering.hierarchical.agglomerative.ClusterSimilarity;
import at.knowcenter.clustering.hierarchical.agglomerative.HacMetadata;
import at.knowcenter.clustering.hierarchical.agglomerative.InMemoryHac;
import at.knowcenter.code.api.pdf.Block;
import at.knowcenter.code.api.pdf.BlockLabel;
import at.knowcenter.code.api.pdf.BoundingBox;
import at.knowcenter.code.api.pdf.Document;
import at.knowcenter.code.api.pdf.ImageRegion;
import at.knowcenter.code.api.pdf.Page;
import at.knowcenter.code.api.pdf.Page.Image;
import at.knowcenter.code.api.pdf.Page.Line;
import at.knowcenter.code.api.pdf.ReadingOrder;
import at.knowcenter.code.pdf.blockclassification.BlockLabeling;
import at.knowcenter.code.pdf.blockclassification.detection.metadata.ArticleMetadataCollector;
import at.knowcenter.code.pdf.blockrelation.geometric.BlockNeighborhood;
import at.knowcenter.code.pdf.utils.HungarianAlgorithm;
import at.knowcenter.code.pdf.utils.PdfExtractionUtils;
import at.knowcenter.featureengineering.DefaultInstanceSet;
import at.knowcenter.featureengineering.Instance;
import at.knowcenter.featureengineering.InstanceSetException;
import at.knowcenter.featureengineering.functions.DistanceFunction;
import at.knowcenter.storage.utils.InMemoryStorage;

public class FigureDetector implements Detector {

	private static final int DISTANCE_THRESHOLD = 50;
	private static final int MAX_COUNT = 100;
	private static final DistanceFunction<BoundingBox> DISTANCE_FUNCTION = new DistanceFunction<BoundingBox>() {
		@Override
		public double calculate(BoundingBox a, BoundingBox b) {
			return BoundingBox.distance(a, b);
		}
	};
	private static final Comparator<Image> AREA_COMPARATOR = new Comparator<Image>() {
		@Override
		public int compare(Image o1, Image o2) {
			return Float.compare(o1.getBoundingBox().area(), o2.getBoundingBox().area());
		}
	};

	@Override
	public void detect(Document doc, List<Block> pageBlocks,
			BlockLabeling labeling, ReadingOrder readingOrder,
			BlockNeighborhood neighbourhood, ArticleMetadataCollector articleMetadata) {
		
		List<ImageRegion> images = new ArrayList<ImageRegion>();
		for (int pageId=0; pageId<pageBlocks.size(); pageId++) {
			Block pageBlock = pageBlocks.get(pageId);
			Page page = doc.getPages().get(pageId);
			List<Block> captionBlocks = new ArrayList<Block>();
			for (Block block : pageBlock.getSubBlocks()) {
				if (isImageCaptionBlock(block, labeling)) {
					captionBlocks.add(block);					
				}
			}
			//findImageRegionsOnPage(images, page, pageBlock, captionBlocks, labeling, neighbourhood);
//			System.out.println("Page "+page.getNumber());
			try {
				List<ImageRegion> imageRegions = clusterLinesAndImages(page, pageBlock);
				imageRegions = alignCaptionsToImages(imageRegions, captionBlocks);
				images.addAll(imageRegions);
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
		for (ImageRegion image : images) {
			System.out.println(image.captionBlock);
			System.out.println(image.captionBBox);
			System.out.println(image.imageBBox);
		}
		doc.setImages(images);
	}
	
	/**
	 * finds image regions based on clustering images and lines
	 * @param page
	 * @param pageBlock
	 * @return
	 * @throws InstanceSetException
	 * @throws ClusteringException
	 */
	private List<ImageRegion> clusterLinesAndImages(Page page, Block pageBlock) 
			throws InstanceSetException, ClusteringException {
		List<Image> images = page.getImages();
		List<Line> lines = page.getLines();
		if (lines.size() > MAX_COUNT) {
			lines = PdfExtractionUtils.getRandomSubList(lines, MAX_COUNT);
		}
		Map<String, Image> imageMap = new HashMap<String, Image>();
		Map<String, Line> lineMap = new HashMap<String, Line>();
		DefaultInstanceSet<BoundingBox> bboxSpace = createInstanceSet(page, images, lines, imageMap, lineMap);
//		System.out.println(bboxSpace.getInstanceCount());
		// cluster with hac/single linkage
		InMemoryHac<BoundingBox> hac = new InMemoryHac<BoundingBox>(1, DISTANCE_THRESHOLD, DISTANCE_FUNCTION, ClusterSimilarity.SINGLE);
		Clustering<HacMetadata> clustering = hac.cluster(bboxSpace);
		List<Cluster<HacMetadata>> clusters = clustering.getClusters();
		List<ImageRegion> imagesOnPage = new ArrayList<ImageRegion>();
		for (Cluster<HacMetadata> cluster : clusters) {
			ImageRegion imageRegion = getImageRegion(cluster, pageBlock, imageMap, lineMap);
			imagesOnPage.add(imageRegion);
		}
		return imagesOnPage;
	}

	/**
	 * creates the instance set for clustering
	 * @param images
	 * @param lines
	 * @param imageMap
	 * @param lineMap
	 * @return
	 * @throws InstanceSetException
	 */
	private DefaultInstanceSet<BoundingBox> createInstanceSet(Page page, List<Image> images, List<Line> lines, 
			Map<String, Image> imageMap, Map<String, Line> lineMap) throws InstanceSetException {
		DefaultInstanceSet<BoundingBox> bboxSpace = new DefaultInstanceSet<BoundingBox>(BoundingBox.class, new InMemoryStorage<String, BoundingBox>());
		BoundingBox bbox;
		int count = 1;
		String id;
		for (Image image : images) {
			bbox = image.getBoundingBox();
			if (bbox.getHeight() < 1 || bbox.getWidth() < 1) continue;
			if (bbox.getHeight() - page.getHeight() > -1 && bbox.getWidth() - page.getWidth() > -1) continue;
			id = "bbox-" + (count++);
			bboxSpace.addInstance(new Instance<BoundingBox>(id, bbox));
			imageMap.put(id, image);
		}
//		for (Line line : lines) {
//			bbox = new BoundingBox(line);
//			id = "bbox-" + (count++);
//			bboxSpace.addInstance(new Instance<BoundingBox>(id, bbox));
//			lineMap.put(id, line);
//		}
		return bboxSpace;
	}
	
	/**
	 * calculates the image region from a given cluster
	 * @param cluster
	 * @param pageBlock
	 * @param imageMap
	 * @param lineMap
	 * @return
	 */
	private ImageRegion getImageRegion(Cluster<?> cluster, Block pageBlock, Map<String, Image> imageMap, Map<String, Line> lineMap) {
		Set<String> ids = cluster.getAllInstanceIds();
		List<Image> images = new ArrayList<Image>();
		List<Line> lines = new ArrayList<Line>();
		for (String id : ids) {
			Image image = imageMap.get(id);
			Line line = lineMap.get(id);
			if (image != null) {
				images.add(image);
			}
			if (line != null) {
				lines.add(line);
			}
		}
		ImageRegion imageRegion = new ImageRegion(pageBlock, images, lines, new ArrayList<Block>());
		// include overlapping blocks (FIXME: inefficient; image region is created twice)
		List<Block> words = new ArrayList<Block>();
		for (Block block : pageBlock.getSubBlocks()) {
			if (block.getBoundingBox().intersects(imageRegion.imageBBox, 0)) {
				words.addAll(block.getWordBlocks());
			}
		}
		imageRegion = new ImageRegion(pageBlock, images, lines, words);
		return imageRegion;
	}
	
	/**
	 * aligns captions to images by computing the optimal matching in terms of distance between bounding boxes
	 * @param imagesOnPage
	 * @param captionBlocks
	 * @return
	 */
	private List<ImageRegion> alignCaptionsToImages(List<ImageRegion> imagesOnPage, List<Block> captionBlocks) {
		List<ImageRegion> result = new ArrayList<ImageRegion>();
		if (imagesOnPage.size() == 0 || captionBlocks.size() == 0) {
			return result;
		}
		double[][] distances = new double[imagesOnPage.size()][captionBlocks.size()];
		boolean transpose = imagesOnPage.size() > captionBlocks.size();
		for (int i = 0; i < imagesOnPage.size(); i++) {
			for (int j = 0; j < captionBlocks.size(); j++) {
				distances[i][j] = BoundingBox.distance(imagesOnPage.get(i).imageBBox, 
						captionBlocks.get(j).getBoundingBox());
			}
		}
		if (transpose) {
			distances = HungarianAlgorithm.transpose(distances);
		}
		int[][] matching = HungarianAlgorithm.hgAlgorithm(distances, "min");
		for (int[] match : matching) {
			Block captionBlock;
			ImageRegion imageRegion;
			if (transpose) {
				captionBlock = captionBlocks.get(match[0]);
				imageRegion = imagesOnPage.get(match[1]);
			} else {
				captionBlock = captionBlocks.get(match[1]);
				imageRegion = imagesOnPage.get(match[0]);
			}
			ImageRegion newImageRegion = new ImageRegion(captionBlock, imageRegion.images, imageRegion.lines,
					imageRegion.words);
			result.add(newImageRegion);
		}
		return result;
	}
	
	/**
	 * simple method that assigns the closest image (lines ignored)
	 * @param images
	 * @param page
	 * @param pageBlock
	 * @param captionBlock
	 * @param labeling
	 * @param neighborhood
	 */
	private void findImageRegion(List<ImageRegion> images, Page page, Block pageBlock, Block captionBlock, BlockLabeling labeling, BlockNeighborhood neighborhood) {
		Image closest = null;
		double closestDistance = Double.POSITIVE_INFINITY;
		for(Image image: page.getImages()) {
			boolean found = false;
			for(ImageRegion imageRegion: images) {
				if(imageRegion.images.contains(image)) {
					found = true;
					break;
				}
			}
			if(found) continue;
			
			double distance = BoundingBox.distance(image.getBoundingBox(), captionBlock.getBoundingBox());
			if(distance < closestDistance) {				
				if(!found) {
					closestDistance = distance;
					closest = image;
				}
			}
		}
		if(closest != null) {
			ImageRegion imageRegion = new ImageRegion(captionBlock, Arrays.asList(closest), new ArrayList<Line>(), new ArrayList<Block>());
			images.add(imageRegion);
		}
	}
	
	private boolean isImageCaptionBlock(Block block, BlockLabeling labeling) {
		if (labeling.hasLabel(block, BlockLabel.Caption)) {
			if (block.getText().substring(0,3).equalsIgnoreCase("fig")) {
				return true;
			}
		}
		return false;
	}
}
