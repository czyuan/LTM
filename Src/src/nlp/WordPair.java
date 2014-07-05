package nlp;

/**
 * This class implements a pair of words (words can be identical) that can be treated as must-links,
 * cannot-links or any links that have related words.
 */
public class WordPair {
	public String wordstr1 = null;
	public String wordstr2 = null;
	public String setString = null;

	public WordPair() {
		setString = "";
	}

	public WordPair(String wordstr1_2, String wordstr2_2) {
		wordstr1 = wordstr1_2;
		wordstr2 = wordstr2_2;
		setString = wordstr1 + " " + wordstr2;
	}

	@Override
	public boolean equals(Object obj) {
		WordPair pair = (WordPair) obj;
		return this.setString.equals(pair.setString);
	}

	@Override
	public int hashCode() {
		return setString.hashCode();
	}

	@Override
	public String toString() {
		return setString;
	}
}
