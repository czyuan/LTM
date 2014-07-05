package knowledge;

import nlp.WordPair;

/**
 * This class implements the must-link.
 */

public class MustLink {
	public WordPair wordpair = null;
	public double weight = 1.0;

	public MustLink(String wordstr1_2, String wordstr2_2) {
		wordpair = new WordPair(wordstr1_2, wordstr2_2);
	}

	@Override
	public String toString() {
		return wordpair.toString();
	}
}
