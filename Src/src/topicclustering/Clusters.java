package topicclustering;

import java.util.ArrayList;
import java.util.Iterator;

import utility.FileReaderAndWriter;

/**
 * A list of clusters in topic clustering.
 */
public class Clusters implements Iterable<Cluster> {
	public ArrayList<Cluster> clusterList = null;

	public Clusters() {
		clusterList = new ArrayList<Cluster>();
	}

	public Clusters(ArrayList<Cluster> clusterList2) {
		clusterList = clusterList2;
	}

	public void addClusterAndAssignClusterId(Cluster cluster) {
		cluster.clusterId = clusterList.size();
		clusterList.add(cluster);
	}

	public void printToFile(String outputFilePath) {
		FileReaderAndWriter.writeFile(outputFilePath, this.toString());
	}

	public int size() {
		return clusterList.size();
	}

	/**
	 * Reset each cluster: remove all the objects while remaining the centroid.
	 */
	public void reset() {
		for (Cluster cluster : clusterList) {
			cluster.reset();
		}
	}

	@Override
	public String toString() {
		StringBuilder sbClusters = new StringBuilder();
		for (int i = 0; i < clusterList.size(); ++i) {
			sbClusters.append(clusterList.get(i).toString());
			sbClusters.append(System.getProperty("line.separator"));
		}
		return sbClusters.toString();
	}

	@Override
	public Iterator<Cluster> iterator() {
		return clusterList.iterator();
	}
}
