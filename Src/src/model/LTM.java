package model;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.PriorityQueue;

import knowledge.KnowledgeAsMustClustersGenerator;
import knowledge.MustLink;
import knowledge.MustLinks;
import nlp.Corpus;
import nlp.Vocabulary;
import utility.ArrayAllocationAndInitialization;
import utility.FileReaderAndWriter;
import utility.InverseTransformSampler;

/**
 * This implements LTM (Lifelong Topic Model) proposed in (Chen and Liu, ICML
 * 2014).
 * 
 * @author Zhiyuan (Brett) Chen
 * @email czyuanacm@gmail.com
 */
public class LTM extends TopicModel {
	/******************* Hyperparameters *********************/
	// The hyperparameter for the document-topic distribution.
	// alpha is in the variable param in TopicModel.
	private double tAlpha = 0;
	// The hyperparameter for the topic-word distribution.
	// beta is in the variable param in TopicModel.
	private double vBeta = 0;

	/******************* Posterior distributions *********************/
	private double[][] theta = null; // Document-topic distribution, size D * T.
	private double[][] thetasum = null; // Cumulative document-topic
										// distribution, size
										// D * T.
	private double[][] phi = null; // Topic-word distribution, size T * V.
	private double[][] phisum = null; // Cumulative topic-word distribution,
										// size T * V.
	// Number of times to add the sum arrays, such as thetasum and phisum.
	public int numstats = 0;

	/******************* Temp variables while sampling *********************/
	// z is defined in the superclass TopicModel.
	// private int[][] z = null; // Topic assignments for each word.
	// ndt[d][t]: the counts of document d having topic t.
	private double[][] ndt = null;
	// ndsum[d]: the counts of document d having any topic.
	private double[] ndsum = null;
	// ntw[t][w]: the counts of word w appearing under topic t.
	private double[][] ntw = null;
	// ntsum[t]: the counts of any word appearing under topic t.
	private double[] ntsum = null;

	/******************* Knowledge *********************/
	// The must-links for each topic.
	HashMap<Integer, MustLinks> topicToMustlinksMap = null;
	// The topic model lists that the knowledge is extracted from.
	ArrayList<TopicModel> topicModelListForKnowledgeExtraction = null;
	// urn_Topic_W1_W2_Value.get(t).get(w1).get(w2): the urn values of pair of
	// words (w1, w2) under a topic t.
	// Note that the urn matrix does not contain values for same pair of words
	// (w, w).
	private HashMap<Integer, HashMap<Integer, HashMap<Integer, Double>>> urn_Topic_W1_W2_Value = null;

	/**
	 * Create a new topic model with all variables initialized. The z[][] is
	 * randomly assigned.
	 */
	public LTM(Corpus corpus2, ModelParameters param2) {
		super(corpus2, param2);

		/******************* Hyperparameters *********************/
		tAlpha = param.T * param.alpha;
		vBeta = param.V * param.beta;

		// Allocate memory for temporary variables and initialize their
		// values.
		allocateMemoryForTempVariables();

		// Initialize the first status of Markov chain using topic
		// assignments from the last iteration topic model result.
		TopicModel topicmodel_currentDomain = findCurrentDomainTopicModel(param.topicModelList_LastIteration);
		initializeFirstMarkovChainUsingExistingZ(topicmodel_currentDomain.z);

		// Get the topic model lists that the knowledge is extracted
		// from.
		if (param.includeCurrentDomainAsKnowledgeExtraction) {
			topicModelListForKnowledgeExtraction = param.topicModelList_LastIteration;
		} else {
			// Knowledge is extracted from the domain other than the
			// current domain.
			topicModelListForKnowledgeExtraction = new ArrayList<TopicModel>();
			for (TopicModel topicModel : param.topicModelList_LastIteration) {
				if (!topicModel.corpus.domain.equals(param.domain)) {
					topicModelListForKnowledgeExtraction.add(topicModel);
				}
			}
			--param.minimumSupport;
		}
	}

	/**
	 * Create a new topic model with all variables initialized. The z[][] is
	 * assigned to the value loaded from other models.
	 */
	public LTM(Corpus corpus2, ModelParameters param2, int[][] z2,
			double[][] twdist) {
		super(corpus2, param2);
		tAlpha = param.T * param.alpha;
		vBeta = param.V * param.beta;
		// Allocate memory for temporary variables and initialize their
		// values.
		allocateMemoryForTempVariables();
		// Assign z2 to z.
		z = z2; // Here we do not call initializeFirstMarkovChainUsingExistingZ
				// because we do not load the knowledge.
		// Assign Topic-Word distribution.
		phi = twdist;
	}

	// ------------------------------------------------------------------------
	// Memory Allocation and Initialization
	// ------------------------------------------------------------------------

	/**
	 * Allocate memory for temporary variables and initialize their values. Note
	 * that z[][] and y[][] are not created in this function, but in the
	 * function initializeFirstMarkovChainRandomly().
	 * 
	 * We Allocate gamma, eta, etasum, ntsw specifically to save the memory. For
	 * each must-link ml, we only allocate the size of words inside it.
	 */
	private void allocateMemoryForTempVariables() {
		/******************* Posterior distributions *********************/
		theta = ArrayAllocationAndInitialization.allocateAndInitialize(theta,
				param.D, param.T);
		phi = ArrayAllocationAndInitialization.allocateAndInitialize(phi,
				param.T, param.V);
		if (param.sampleLag > 0) {
			thetasum = ArrayAllocationAndInitialization.allocateAndInitialize(
					thetasum, param.D, param.T);
			phisum = ArrayAllocationAndInitialization.allocateAndInitialize(
					phisum, param.T, param.V);
		}

		/******************* Temp variables while sampling *********************/
		ndt = ArrayAllocationAndInitialization.allocateAndInitialize(ndt,
				param.D, param.T);
		ndsum = ArrayAllocationAndInitialization.allocateAndInitialize(ndsum,
				param.D);
		ntw = ArrayAllocationAndInitialization.allocateAndInitialize(ntw,
				param.T, param.V);
		ntsum = ArrayAllocationAndInitialization.allocateAndInitialize(ntsum,
				param.T);

		/******************* Knowledge *********************/
		urn_Topic_W1_W2_Value = new HashMap<Integer, HashMap<Integer, HashMap<Integer, Double>>>();
	}

	/**
	 * Initialized the first status of Markov chain using topic assignments from
	 * the LDA results.
	 */
	private void initializeFirstMarkovChainUsingExistingZ(int[][] z2) {
		z = new int[param.D][];
		for (int d = 0; d < param.D; ++d) {
			int N = docs[d].length;
			z[d] = new int[N];

			for (int n = 0; n < N; ++n) {
				int word = docs[d][n];
				int topic = z2[d][n];
				z[d][n] = topic;

				updateCount(d, topic, word, +1);
			}
		}
	}

	/**
	 * There are several main steps:
	 * 
	 * 1. Run a certain number of Gibbs Sampling sweeps.
	 * 
	 * 2. Compute the posterior distributions.
	 */
	@Override
	public void run() {
		// 1. Run a certain number of Gibbs Sampling sweeps.
		runGibbsSampling();
		// 2. Compute the posterior distributions.
		computePosteriorDistribution();
	}

	// ------------------------------------------------------------------------
	// Gibbs Sampler
	// ------------------------------------------------------------------------

	/**
	 * Run a certain number of Gibbs Sampling sweeps.
	 */
	private void runGibbsSampling() {
		for (int i = 0; i < param.nIterations; ++i) {
			for (int d = 0; d < param.D; ++d) {
				int N = docs[d].length;
				for (int n = 0; n < N; ++n) {
					// Sample from p(z_i|z_-i, w)
					sampleTopicAssignment(d, n);
				}
			}

			if (i >= param.nBurnin) {
				if (i == param.nBurnin
						|| i % param.knowledgeUpdatelag == 0) {
					// Extract the knowledge and update the urn matrix.
					// Compute the instance values of distributions.
					computeTopicWordDistribution(-1);
					ArrayList<PriorityQueue<Integer>> topWordIDList = getTopWordsUnderEachTopic(phi);

					topicToMustlinksMap = getKnowledgeFromTopicModelResults(
							topWordIDList,
							topicModelListForKnowledgeExtraction, phi,
							corpus.vocab);
					// The counting matrixes will be updated in this function.
					updateUrnMatrix(topicToMustlinksMap);
				}
			}

			if (i >= param.nBurnin && param.sampleLag > 0
					&& i % param.sampleLag == 0) {
				updatePosteriorDistribution();
			}
		}
	}

	/**
	 * Sample a topic assigned to the word in position n of document d.
	 */
	private void sampleTopicAssignment(int d, int n) {
		int old_topic = z[d][n];
		int word = docs[d][n];
		updateCount(d, old_topic, word, -1);

		double[] p = new double[param.T];
		for (int t = 0; t < param.T; ++t) {
			p[t] = (ndt[d][t] + param.alpha) / (ndsum[d] + tAlpha)
					* (ntw[t][word] + param.beta) / (ntsum[t] + vBeta);
		}
		int new_topic = InverseTransformSampler.sample(p,
				randomGenerator.nextDouble());

		z[d][n] = new_topic;
		updateCount(d, new_topic, word, +1);
	}

	/**
	 * Update the counts in the Gibbs sampler.
	 */
	private void updateCount(int d, int topic, int word, int flag) {
		ndt[d][topic] += flag;
		ndsum[d] += flag;

		// Note that the urn matrix does not contain values for same pair of
		// words (w, w).
		if (urn_Topic_W1_W2_Value.containsKey(topic)) {
			HashMap<Integer, HashMap<Integer, Double>> urn_W1_W2_Value = urn_Topic_W1_W2_Value
					.get(topic);
			if (urn_W1_W2_Value.containsKey(word)) {
				HashMap<Integer, Double> urn_W2_value = urn_W1_W2_Value
						.get(word);
				for (Map.Entry<Integer, Double> entry : urn_W2_value.entrySet()) {
					int w2 = entry.getKey();
					double count = flag * entry.getValue();
					ntw[topic][w2] += count;
					ntsum[topic] += count;
				}
			}
		}

		ntw[topic][word] += flag;
		ntsum[topic] += flag;
	}

	// ------------------------------------------------------------------------
	// Knowledge Extraction
	// ------------------------------------------------------------------------

	/**
	 * Extract knowledge from the topic model results from the last iteration.
	 */
	private HashMap<Integer, MustLinks> getKnowledgeFromTopicModelResults(
			ArrayList<PriorityQueue<Integer>> topWordIDList,
			ArrayList<TopicModel> topicModelList_LastIteration,
			double[][] topicWordDistribution, Vocabulary vocab) {
		KnowledgeAsMustClustersGenerator knowledgeGenarator = new KnowledgeAsMustClustersGenerator(
				param);
		return knowledgeGenarator.generateKnowledgeAsMustLinks(topWordIDList,
				topicModelList_LastIteration, topicWordDistribution, vocab);
	}

	/**
	 * Note that the counting matrixes will be updated in this function.
	 */
	private void updateUrnMatrix(HashMap<Integer, MustLinks> topicToMustlinksMap) {
		HashMap<Integer, HashMap<Integer, HashMap<Integer, Double>>> newurn_Topic_W1_W2_Value = new HashMap<Integer, HashMap<Integer, HashMap<Integer, Double>>>();
		for (int t = 0; t < param.T; ++t) {
			newurn_Topic_W1_W2_Value.put(t,
					new HashMap<Integer, HashMap<Integer, Double>>());
			HashMap<Integer, HashMap<Integer, Double>> urn_W1_W2_Value = newurn_Topic_W1_W2_Value
					.get(t);
			if (!topicToMustlinksMap.containsKey(t)) {
				// There is no knowledge under this topic.
				continue;
			}
			MustLinks mustlinks = topicToMustlinksMap.get(t);
			for (MustLink mustlink : mustlinks) {
				String wordstr1 = mustlink.wordpair.wordstr1;
				String wordstr2 = mustlink.wordpair.wordstr2;
				if (!corpus.vocab.containsWordstr(wordstr1)
						|| !corpus.vocab.containsWordstr(wordstr2)) {
					// The knowledge word does not appear in this domain.
					continue;
				}
				int w1 = corpus.vocab.getWordidByWordstr(wordstr1);
				int w2 = corpus.vocab.getWordidByWordstr(wordstr2);

				// We only need the off diagonal elements in the urn matrix
				// since the pair words in the mustlinks are different.
				if (w1 != w2) {
					// Use PMI to update urn matrix.
					int coDocFrequency = corpus.getCoDocumentFrequency(
							wordstr1, wordstr2) + 1;
					int docFrequency1 = corpus.getDocumentFrequency(wordstr1) + 1;
					int docFrequency2 = corpus.getDocumentFrequency(wordstr2) + 1;

					double Pxy = 1.0 * coDocFrequency / param.D;
					double Px = 1.0 * docFrequency1 / param.D;
					double Py = 1.0 * docFrequency2 / param.D;
					double PMI = Math.log(Pxy / (Px * Py));
					double gpuScale = param.pmiScaleToGPU * PMI;
					if (gpuScale <= 0) {
						continue;
					}
					// System.out.println(w1 + " " + w2 + " " + gpuScale);

					if (!urn_W1_W2_Value.containsKey(w1)) {
						urn_W1_W2_Value.put(w1, new HashMap<Integer, Double>());
					}
					HashMap<Integer, Double> urn_W2_Value = urn_W1_W2_Value
							.get(w1);
					urn_W2_Value.put(w2, gpuScale);

					if (!urn_W1_W2_Value.containsKey(w2)) {
						urn_W1_W2_Value.put(w2, new HashMap<Integer, Double>());
					}
					urn_W2_Value = urn_W1_W2_Value.get(w2);
					urn_W2_Value.put(w1, gpuScale);
				}
			}
		}
		// Eliminate the effects of old urn matrix.
		for (int d = 0; d < param.D; ++d) {
			int N = corpus.docs[d].length;
			for (int n = 0; n < N; ++n) {
				int topic = z[d][n];
				int word = corpus.docs[d][n];
				updateCountWithUrn(topic, word, -1, urn_Topic_W1_W2_Value);
			}
		}
		// Add the effects of new urn matrix.
		for (int d = 0; d < param.D; ++d) {
			int N = corpus.docs[d].length;
			for (int n = 0; n < N; ++n) {
				int topic = z[d][n];
				int word = corpus.docs[d][n];
				updateCountWithUrn(topic, word, +1, newurn_Topic_W1_W2_Value);
			}
		}
		// Replace the old urn matrix with the new one.
		urn_Topic_W1_W2_Value = newurn_Topic_W1_W2_Value;
	}

	/**
	 * Adjust the urn model influence according to the new urn matrix.
	 */
	private void updateCountWithUrn(
			int topic,
			int word,
			int flag,
			HashMap<Integer, HashMap<Integer, HashMap<Integer, Double>>> tempurn_Topic_W1_W2_Value) {
		if (tempurn_Topic_W1_W2_Value.containsKey(topic)) {
			HashMap<Integer, HashMap<Integer, Double>> urn_W1_W2_Value = tempurn_Topic_W1_W2_Value
					.get(topic);
			if (urn_W1_W2_Value.containsKey(word)) {
				HashMap<Integer, Double> urn_W2_value = urn_W1_W2_Value
						.get(word);
				for (Map.Entry<Integer, Double> entry : urn_W2_value.entrySet()) {
					int w2 = entry.getKey();
					double value = entry.getValue();
					ntw[topic][w2] += flag * value;
					ntsum[topic] += flag * value;
				}
			}
		}
	}

	// ------------------------------------------------------------------------
	// Posterior Distribution Computation
	// ------------------------------------------------------------------------

	/**
	 * After burn in phase, update the posterior distributions every sample lag.
	 */
	private void updatePosteriorDistribution() {
		for (int d = 0; d < param.D; ++d) {
			for (int t = 0; t < param.T; ++t) {
				thetasum[d][t] += (ndt[d][t] + param.alpha)
						/ (ndsum[d] + tAlpha);
			}
		}

		for (int t = 0; t < param.T; ++t) {
			for (int w = 0; w < param.V; ++w) {
				phisum[t][w] += (ntw[t][w] + param.beta) / (ntsum[t] + vBeta);
			}
		}
		++numstats;
	}

	/**
	 * Compute the posterior distributions.
	 */
	private void computePosteriorDistribution() {
		computeDocumentTopicDistribution(param.sampleLag);
		computeTopicWordDistribution(param.sampleLag);
	}

	/**
	 * Document-topic distribution: theta[][].
	 */
	private void computeDocumentTopicDistribution(int slag) {
		if (slag > 0) {
			for (int d = 0; d < param.D; ++d) {
				for (int t = 0; t < param.T; ++t) {
					theta[d][t] = thetasum[d][t] / numstats;
				}
			}
		} else {
			for (int d = 0; d < param.D; ++d) {
				for (int t = 0; t < param.T; ++t) {
					theta[d][t] = (ndt[d][t] + param.alpha)
							/ (ndsum[d] + tAlpha);
				}
			}
		}
	}

	/**
	 * Topic-word distribution: phi[][].
	 */
	private void computeTopicWordDistribution(int slag) {
		if (slag > 0) {
			for (int t = 0; t < param.T; ++t) {
				for (int w = 0; w < param.V; ++w) {
					phi[t][w] = phisum[t][w] / numstats;
				}
			}
		} else {
			for (int t = 0; t < param.T; ++t) {
				for (int w = 0; w < param.V; ++w) {
					phi[t][w] = (ntw[t][w] + param.beta) / (ntsum[t] + vBeta);
				}
			}
		}
	}

	@Override
	public double[][] getTopicWordDistribution() {
		return phi;
	}

	@Override
	/**
	 * Print out the must-links for each topic.
	 */
	public void printKnowledge(String filepath) {
		StringBuilder sbKnowledge = new StringBuilder();
		for (int t = 0; t < param.T; ++t) {
			if (topicToMustlinksMap.containsKey(t)) {
				MustLinks mustlinks = topicToMustlinksMap.get(t);
				if (mustlinks.size() > 0) {
					sbKnowledge.append("<Topic=" + t + ">");
					sbKnowledge.append(System.getProperty("line.separator"));
					sbKnowledge.append(mustlinks);
					sbKnowledge.append("<\\Topic>");
					sbKnowledge.append(System.getProperty("line.separator"));
					sbKnowledge.append(System.getProperty("line.separator"));
				}
			}
		}
		FileReaderAndWriter.writeFile(filepath, sbKnowledge.toString());
	}
}

/**
 * Comparator to rank the words according to their proabilities.
 */
class TopicalWordComparator implements Comparator<Integer> {
	private double[] distribution = null;

	public TopicalWordComparator(double[] distribution2) {
		distribution = distribution2;
	}

	@Override
	public int compare(Integer w1, Integer w2) {
		if (distribution[w1] < distribution[w2]) {
			return -1;
		} else if (distribution[w1] > distribution[w2]) {
			return 1;
		}
		return 0;
	}
}