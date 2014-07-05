package topicclustering;

import java.util.ArrayList;
import java.util.Iterator;

import utility.FileReaderAndWriter;

/**
 * In topic clustering, each cluster has a list of cluster-objects (topics).
 */
public class Cluster implements Iterable<ClusterObject> {
	public int clusterId = -1;
	public ArrayList<ClusterObject> clusterObjectList = null;
	public ClusterObject centroid = null;

	public Cluster() {
		clusterObjectList = new ArrayList<ClusterObject>();
	}

	public Cluster(ArrayList<ClusterObject> cluserObjectList2) {
		clusterObjectList = cluserObjectList2;
	}

	public void addClusterOjbect(ClusterObject co) {
		clusterObjectList.add(co);
	}

	/**
	 * Remove all the objects while remaining the centroid.
	 */
	public void reset() {
		clusterObjectList = new ArrayList<ClusterObject>();
	}

	public void printToFile(String filePath) {
		FileReaderAndWriter.writeFile(filePath, toString());
	}

	@Override
	public String toString() {
		StringBuilder sbCluster = new StringBuilder();
		sbCluster.append("<Cluster id=" + clusterId + ">\r\n");
		for (ClusterObject co : clusterObjectList) {
			sbCluster.append(co.toString());
			sbCluster.append("\r\n");
		}
		sbCluster.append("</Cluster>");
		sbCluster.append(System.getProperty("line.separator"));
		return sbCluster.toString();
	}

	@Override
	public Iterator<ClusterObject> iterator() {
		return clusterObjectList.iterator();
	}
}
