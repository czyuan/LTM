package topicclustering;

import java.util.ArrayList;
import java.util.Random;

import utility.InverseTransformSampler;

public class KMedoids extends ClusterModel {
	private int K = -1; // The number of clusters.
	private double[][] distanceMatrix = null;
	private int randomSeed = 0;

	public KMedoids(int K2, int randomSeed2) {
		K = K2;
		randomSeed = randomSeed2;
	}

	@Override
	public Clusters clusterTopics(ArrayList<ClusterObject> clusterObjectList) {
		distanceMatrix = getDistanceMatrix(clusterObjectList);
		Clusters clusters = assignInitialCentroidsKMeansPlusPlus(clusterObjectList);

		while (true) {
			clusters.reset();
			clusters = assignOjbectsToClusters(clusters, clusterObjectList);
			boolean isConverge = findNewCentroids(clusters);
			if (isConverge) {
				break;
			}
		}
		return clusters;
	}

	private double[][] getDistanceMatrix(
			ArrayList<ClusterObject> clusterObjectList) {
		int N = clusterObjectList.size();
		double[][] distanceMatrix = new double[N][N];
		for (int i = 0; i < N; ++i) {
			ClusterObject co1 = clusterObjectList.get(i);
			for (int j = i + 1; j < N; ++j) {
				ClusterObject co2 = clusterObjectList.get(j);
				distanceMatrix[co1.getCoID()][co2.getCoID()] = distanceMatrix[co2
						.getCoID()][co1.getCoID()] = co1.getDistance(co2);
			}
		}
		return distanceMatrix;
	}

	private double getDistance(ClusterObject co1, ClusterObject co2) {
		return distanceMatrix[co1.getCoID()][co2.getCoID()];
	}

	/**
	 * Apply K-Means++ to do a better initialization from the paper as below:
	 * 
	 * Arthur, David, and Sergei Vassilvitskii.
	 * "k-means++: The advantages of careful seeding." Proceedings of the
	 * eighteenth annual ACM-SIAM symposium on Discrete algorithms. Society for
	 * Industrial and Applied Mathematics, 2007.
	 */
	private Clusters assignInitialCentroidsKMeansPlusPlus(
			ArrayList<ClusterObject> clusterObjectList) {
		Clusters clusters = new Clusters();
		int N = clusterObjectList.size();

		Random randomGenerator = new Random(randomSeed);

		if (K > N) {
			K = N;
		}

		for (int k = 0; k < K; ++k) {
			double[] d = new double[N];
			for (int i = 0; i < N; ++i) {
				ClusterObject co_i = clusterObjectList.get(i);
				d[i] = getMinimumDistanceFromClusterCentroids(co_i, clusters);
				d[i] = d[i] * d[i];
			}
			int index = InverseTransformSampler.sample(d,
					randomGenerator.nextDouble());
			ClusterObject centroid = clusterObjectList.get(index);
			Cluster cluster = new Cluster();
			cluster.centroid = centroid;
			clusters.addClusterAndAssignClusterId(cluster);
		}
		return clusters;
	}

	private double getMinimumDistanceFromClusterCentroids(ClusterObject co,
			Clusters clusters) {
		if (clusters.clusterList.size() == 0) {
			return 1.0; // Uniform distribution.
		}
		double minimumD = Double.MAX_VALUE;
		for (Cluster cluster : clusters) {
			double distance = getDistance(co, cluster.centroid);
			minimumD = Math.min(minimumD, distance);
		}
		return minimumD;
	}

	private Clusters assignOjbectsToClusters(Clusters clusters,
			ArrayList<ClusterObject> clusterObjectList) {
		for (ClusterObject co : clusterObjectList) {
			double miniDistance = Double.MAX_VALUE;
			Cluster assignedCluster = null;
			for (Cluster cluster : clusters) {
				ClusterObject centroid = cluster.centroid;
				double distance = getDistance(co, centroid);
				if (distance < miniDistance) {
					miniDistance = distance;
					assignedCluster = cluster;
				}
			}
			assignedCluster.addClusterOjbect(co);
		}
		return clusters;
	}

	/**
	 * If we cannot find a new centroid, then the algorithm converges.
	 */
	private boolean findNewCentroids(Clusters clusters) {
		boolean isConverge = true;
		for (Cluster cluster : clusters) {
			ClusterObject newCentroid = findNewCentroid(cluster);
			if (!newCentroid.equals(cluster.centroid)) {
				isConverge = false;
			}
			cluster.centroid = newCentroid;
		}
		return isConverge;
	}

	/**
	 * O(n^2) algorithm to find the new centroid.
	 */
	private ClusterObject findNewCentroid(Cluster cluster) {
		ClusterObject centroid = null;
		double miniDistance = Double.MAX_VALUE;
		for (int i = 0; i < cluster.clusterObjectList.size(); ++i) {
			double sumDistance = 0.0;
			ClusterObject co_i = cluster.clusterObjectList.get(i);
			for (int j = 0; j < cluster.clusterObjectList.size(); ++j) {
				ClusterObject co_j = cluster.clusterObjectList.get(j);
				sumDistance += getDistance(co_i, co_j);
			}
			if (sumDistance < miniDistance) {
				miniDistance = sumDistance;
				centroid = co_i;
			}
		}
		return centroid;
	}
}
