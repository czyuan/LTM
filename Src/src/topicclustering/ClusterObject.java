package topicclustering;

import utility.DistributionDivergenceUtil;
import utility.ItemWithValue;
import nlp.Topic;
import nlp.Vocabulary;
import global.Constant;

/**
 * This class implements the object that is used for topic clustering.
 */
public class ClusterObject {
	public int coId = -1;
	// The word distribution over all words across the domains.
	private double[] distribution = null;
	public Topic topic = null;

	public ClusterObject(Topic topic2) {
		topic = topic2;
	}

	/**
	 * Convert the topic to cluster-object and normalize the probability.
	 */
	public ClusterObject(Topic topic2, Vocabulary globalVocab) {
		topic = topic2;

		distribution = new double[globalVocab.size()];
		for (int i = 0; i < distribution.length; ++i) {
			distribution[i] = Constant.SMOOTH_PROBABILITY;
		}
		for (ItemWithValue iwp : topic.topWordList) {
			String wordstr = iwp.getIterm().toString();
			int wordid = globalVocab.getWordidByWordstr(wordstr);
			distribution[wordid] = iwp.getValue();
		}
		// Normalize the distribution.
		double sum = 0;
		for (int i = 0; i < distribution.length; ++i) {
			sum += distribution[i];
		}
		for (int i = 0; i < distribution.length; ++i) {
			distribution[i] /= sum;
		}
	}

	public int getCoID() {
		return coId;
	}

	public double[] getDistribution() {
		return distribution;
	}

	public double getDistance(ClusterObject co) {
		return DistributionDivergenceUtil.getSymmetricKLDivergence(
				distribution, co.distribution);
	}

	@Override
	public String toString() {
		StringBuilder sbCO = new StringBuilder();
		for (ItemWithValue iwv : topic.topWordList) {
			sbCO.append(iwv.getIterm().toString());
			sbCO.append(' ');
		}
		return sbCO.toString().trim();
	}

	@Override
	public int hashCode() {
		return coId;
	}

	@Override
	public boolean equals(Object ob) {
		ClusterObject co = (ClusterObject) ob;
		return this.coId == co.coId;
	}
}
