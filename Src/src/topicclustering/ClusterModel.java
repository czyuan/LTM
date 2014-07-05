package topicclustering;

import java.util.ArrayList;

import utility.ExceptionUtility;

public abstract class ClusterModel {
	public static ClusterModel createClusterModel(String clusterSetting,
			int numberOfTopicClusters, int randomSeed) {
		if (clusterSetting.equals("KMedoids")) {
			return new KMedoids(numberOfTopicClusters, randomSeed);
		} else {
			ExceptionUtility.throwAndCatchException("Not recognizable model!");
		}
		return null;
	}

	public abstract Clusters clusterTopics(
			ArrayList<ClusterObject> clusterObjectList);
}
