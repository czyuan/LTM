package knowledge;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.PriorityQueue;

import fim.Apriori;
import fim.Transactions;
import nlp.Topic;
import nlp.Topics;
import nlp.Vocabulary;
import topicclustering.Cluster;
import topicclustering.ClusterObject;
import topicclustering.Clusters;
import utility.DistributionDivergenceUtil;
import utility.ItemWithValue;
import model.ModelParameters;
import model.TopicModel;

/**
 * This class implements the knowledge generation algorithm as in Chen and Liu,
 * ICML 2014.
 * 
 * There are two steps:
 * 
 * 1. Clustering existing topics by matching them to the topics from the current
 * domain.
 * 
 * 2. Apply frequent itemset mining in each (topic) cluster.
 * 
 * @author Zhiyuan (Brett) Chen
 * @email czyuanacm@gmail.com
 */
public class KnowledgeAsMustClustersGenerator {
	private final int MAXIMUM_ITEMSET_SIZE = 2;

	private ModelParameters param = null;

	public KnowledgeAsMustClustersGenerator(ModelParameters param2) {
		param = param2;
	}

	public HashMap<Integer, MustLinks> generateKnowledgeAsMustLinks(
			ArrayList<PriorityQueue<Integer>> topWordIDList,
			ArrayList<TopicModel> topicModelList_LastIteration,
			double[][] topicWordDistribution, Vocabulary vocab) {
		// Get all existing topics.
		Topics topicsFromLastIteration = new Topics();
		for (TopicModel topicModel : topicModelList_LastIteration) {
			topicsFromLastIteration
					.addTopics(topicModel
							.getTopics(param.numberOfTopWordsUsedForKnowledgeEstimation));
		}

		// 1. Clustering existing topics by matching them to the
		// topics from the current domain.
		Clusters topicClusters = null;
		topicClusters = clusterTopicsByMatchingExistingTopicsToCurrentTopics(
				topWordIDList, topicsFromLastIteration, topicWordDistribution,
				vocab, param.topicMatchingKLDivergenceThreshold);

		// 2. Apply frequent itemset mining in each (topic) cluster.
		HashMap<Integer, MustLinks> topicToMustlinksMap = new HashMap<Integer, MustLinks>();
		for (int i = 0; i < topicClusters.clusterList.size(); ++i) {
			Cluster topicCluster = topicClusters.clusterList.get(i);
			// Construct transactions.
			Transactions transactions = new Transactions(topicCluster);
			Apriori aprioir = new Apriori(transactions, param.minimumSupport,
					param.minimumSupportPercentage);
			ArrayList<ArrayList<String>> freqItemSetList = aprioir
					.runToSizeK(MAXIMUM_ITEMSET_SIZE);

			MustLinks mustlinks = new MustLinks();
			if (!freqItemSetList.isEmpty()) {
				// Add into the knowledge mustlinks.
				for (ArrayList<String> freqItemSet : freqItemSetList) {
					assert (freqItemSet.size() == 2) : "The size of frequent item set should be 2!";
					String wordstr1 = freqItemSet.get(0);
					String wordstr2 = freqItemSet.get(1);
					MustLink mustlink = new MustLink(wordstr1, wordstr2);
					mustlinks.addMustLink(mustlink);
				}
			}
			topicToMustlinksMap.put(i, mustlinks);
		}
		return topicToMustlinksMap;
	}

	/**
	 * We require a one-to-one matching between the existing topic and the
	 * current topic. A threshold (topicMatchingKLDivergenceThreshold) is used
	 * to filter those irrelevant topics.
	 */
	private Clusters clusterTopicsByMatchingExistingTopicsToCurrentTopics(
			ArrayList<PriorityQueue<Integer>> topWordIDList,
			Topics existingTopics, double[][] topicWordDistribution,
			Vocabulary vocab, double topicMatchingKLDivergenceThreshold) {
		ArrayList<Map<Integer, Double>> distributionListOfCurrentTopics = convertTopWordIdListToDistributions(
				topWordIDList, topicWordDistribution);

		Clusters clusters = new Clusters();
		for (int k = 0; k < distributionListOfCurrentTopics.size(); ++k) {
			clusters.addClusterAndAssignClusterId(new Cluster());
		}

		for (int i = 0; i < existingTopics.topicList.size(); ++i) {
			Topic existingTopic = existingTopics.topicList.get(i);
			Map<Integer, Double> distributionOfExistingTopic = getMapWordToProbabilityUnderMapExistingTopic(
					existingTopic, vocab);

			int minimumIndex = -1;
			double minimumValue = Double.MAX_VALUE;
			for (int k = 0; k < distributionListOfCurrentTopics.size(); ++k) {
				Map<Integer, Double> distributionListOfCurrentTopic = distributionListOfCurrentTopics
						.get(k);
				double value = DistributionDivergenceUtil
						.getSymmetricKLDivergence(
								distributionListOfCurrentTopic,
								distributionOfExistingTopic);
				if (value < minimumValue) {
					minimumValue = value;
					minimumIndex = k;
				}
			}

			if (minimumValue <= topicMatchingKLDivergenceThreshold) {
				ClusterObject co = new ClusterObject(existingTopic);
				clusters.clusterList.get(minimumIndex).addClusterOjbect(co);
			}
		}
		return clusters;
	}

	public ArrayList<Map<Integer, Double>> convertTopWordIdListToDistributions(
			ArrayList<PriorityQueue<Integer>> topWordIDList,
			double[][] topicWordDistribution) {
		ArrayList<Map<Integer, Double>> distributionList = new ArrayList<Map<Integer, Double>>();
		int K = topWordIDList.size();
		for (int k = 0; k < K; ++k) {
			PriorityQueue<Integer> pqueue = topWordIDList.get(k);
			Map<Integer, Double> distribution = getMapWordToProbabilityUnderTopic(
					k, pqueue, topicWordDistribution);
			distributionList.add(distribution);
		}
		return distributionList;
	}

	public Map<Integer, Double> getMapWordToProbabilityUnderTopic(int topic,
			PriorityQueue<Integer> pqueue, double[][] topicWordDistribution) {
		double[] dist = new double[pqueue.size()];
		int[] words = new int[pqueue.size()];
		int i = 0;
		for (int v : pqueue) {
			dist[i] = topicWordDistribution[topic][v];
			words[i++] = v;
		}
		// Normalize.
		double sum = 0;
		for (i = 0; i < dist.length; ++i) {
			sum += dist[i];
		}
		for (i = 0; i < dist.length; ++i) {
			dist[i] /= sum;
		}
		Map<Integer, Double> mpWordToProbabilityUnderTopics = new HashMap<Integer, Double>();
		for (i = 0; i < dist.length; ++i) {
			mpWordToProbabilityUnderTopics.put(words[i], dist[i]);
		}
		return mpWordToProbabilityUnderTopics;
	}

	public Map<Integer, Double> getMapWordToProbabilityUnderMapExistingTopic(
			Topic existingTopic, Vocabulary vocab) {
		Map<Integer, Double> mpWordToProbabilityUnderTopics = new HashMap<Integer, Double>();
		for (ItemWithValue iwv : existingTopic.topWordList) {
			String wordStr = iwv.getIterm().toString();
			double prob = iwv.getValue();
			if (vocab.containsWordstr(wordStr)) {
				int wordId = vocab.getWordidByWordstr(wordStr);
				mpWordToProbabilityUnderTopics.put(wordId, prob);
			}
		}
		// Normalize.
		double sum = 0;
		for (double prob : mpWordToProbabilityUnderTopics.values()) {
			sum += prob;
		}
		for (Map.Entry<Integer, Double> entry : mpWordToProbabilityUnderTopics
				.entrySet()) {
			double normalizedProb = entry.getValue() / sum;
			mpWordToProbabilityUnderTopics.put(entry.getKey(), normalizedProb);
		}
		return mpWordToProbabilityUnderTopics;
	}
}
