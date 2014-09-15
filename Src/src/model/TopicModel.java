package model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.PriorityQueue;
import java.util.Random;

import nlp.Corpus;
import nlp.Topics;
import utility.ExceptionUtility;
import utility.ItemWithValue;

/**
 * The superclass of all topic models. It contains all the basic settings that
 * are used by all topic models. It also contains the basic methods that are
 * used by all topic models.
 */
public abstract class TopicModel {
	public ModelParameters param = null;

	public Corpus corpus = null; // The corpus of a domain.
	public int[][] docs = null; // The word ids in each document of the corpus.
	public String[][] docsStr = null; // The word strs in each document of the
										// corpus.

	// Topic assignments for each word in Gibbs sampler.
	// We put z in the superclass as every topic model is supposed to have it.
	protected int[][] z = null;

	// Random number generator.
	protected Random randomGenerator = null;

	protected TopicModel(Corpus corpus2, ModelParameters param2) {
		corpus = corpus2;
		docs = corpus.docs;
		docsStr = corpus.docsStr;
		param = param2;
		randomGenerator = new Random(param.randomSeed);
	}

	// Run topic model.
	public abstract void run();

	// Get topic word distribution.
	public abstract double[][] getTopicWordDistribution();
	
	// Get document topic distribution.
	public abstract double[][] getDocumentTopicDistrbution();
	

	public static TopicModel selectModel(Corpus corpus2, ModelParameters param2) {
		String modelName = param2.modelName;
		/******************* Unsupervised Topic Model *********************/
		if (modelName.equals("LDA")) {
			return new LDA(corpus2, param2);
		}

		/******************* Knowledge-based Topic Model *********************/
		else if (modelName.equals("LTM")) {
			return new LTM(corpus2, param2);
		} else {
			ExceptionUtility
					.throwAndCatchException("The model name is not recognizable!");
		}
		return null;
	}

	/**
	 * Return the list of top words and their original probabilities.
	 */
	public ArrayList<ArrayList<ItemWithValue>> getTopWordStrsWithProbabilitiesUnderTopics(
			int twords) {
		double[][] topicWordDist = getTopicWordDistribution();
		assert (topicWordDist != null && topicWordDist[0] != null) : "Topic word distribution is null!";

		ArrayList<ArrayList<ItemWithValue>> topWordStrsUnderTopics = new ArrayList<ArrayList<ItemWithValue>>();
		int T = topicWordDist.length;
		int V = topicWordDist[0].length;
		// If twords is negative, then get all words.
		if (twords > V || twords < 0) {
			twords = V;
		}

		for (int t = 0; t < T; t++) {
			ArrayList<ItemWithValue> wordsProbsList = new ArrayList<ItemWithValue>();
			for (int w = 0; w < V; w++) {
				ItemWithValue wwp = new ItemWithValue(w, topicWordDist[t][w]);
				wordsProbsList.add(wwp);
			}
			Collections.sort(wordsProbsList);

			ArrayList<ItemWithValue> topwordsProbsList = new ArrayList<ItemWithValue>();
			for (int i = 0; i < twords; i++) {
				int wordid = (Integer) wordsProbsList.get(i).getIterm();
				String wordstr = corpus.vocab.getWordstrByWordid(wordid);
				double prob = wordsProbsList.get(i).getValue();
				ItemWithValue iwp = new ItemWithValue(wordstr, prob);
				topwordsProbsList.add(iwp);
			}
			topWordStrsUnderTopics.add(topwordsProbsList);
		}

		return topWordStrsUnderTopics;
	}

	public Topics getTopics(int twords) {
		return new Topics(
				this.getTopWordStrsWithProbabilitiesUnderTopics(twords));
	}

	/**
	 * Find the topic model of current domain.
	 */
	protected TopicModel findCurrentDomainTopicModel(
			ArrayList<TopicModel> topicModelList_LastIteration) {
		TopicModel topicmodel_currentDomain = null;
		for (TopicModel topicmodel : topicModelList_LastIteration) {
			if (topicmodel.corpus.domain.equals(corpus.domain)) {
				topicmodel_currentDomain = topicmodel;
				break;
			}
		}
		ExceptionUtility
				.assertAsException(topicmodel_currentDomain != null,
						"Cannot find the topic model of this domain in the last iteration!");

		return topicmodel_currentDomain;
	}

	/**
	 * Get the top words under each topic given the current status of the Markov
	 * chain.
	 */
	protected ArrayList<PriorityQueue<Integer>> getTopWordsUnderEachTopic(
			double[][] topicWordDistribution) {
		ArrayList<PriorityQueue<Integer>> topWordidList = new ArrayList<PriorityQueue<Integer>>();
		int top_words = param.numberOfTopWordsUsedForKnowledgeEstimation;

		for (int t = 0; t < param.T; ++t) {
			Comparator<Integer> comparator = new TopicalWordComparator(
					topicWordDistribution[t]);
			PriorityQueue<Integer> pqueue = new PriorityQueue<Integer>(
					top_words, comparator);

			for (int w = 0; w < param.V; ++w) {
				if (pqueue.size() < top_words) {
					pqueue.add(w);
				} else {
					if (topicWordDistribution[t][w] > topicWordDistribution[t][pqueue
							.peek()]) {
						pqueue.poll();
						pqueue.add(w);
					}
				}
			}

			topWordidList.add(pqueue);
		}
		return topWordidList;
	}

	/**
	 * Print out the knowledge. For non knowledge-based topic models, this
	 * function does nothing. For knowledge-based topic models, print out
	 * the knowledge according to their specific formats.
	 */
	public void printKnowledge(String filepath) {

	}
}
