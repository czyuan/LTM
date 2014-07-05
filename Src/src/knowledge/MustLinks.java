package knowledge;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * This class implements knowledge in the forms of must-links.
 */
public class MustLinks implements Iterable<MustLink> {
	public ArrayList<MustLink> mustlinkList = null;
	public Map<MustLink, Integer> mustlinkToIndexMap = null;
	public Map<String, ArrayList<MustLink>> wordidToMustlinkListMap = null;

	public MustLinks() {
		mustlinkList = new ArrayList<MustLink>();
		mustlinkToIndexMap = new HashMap<MustLink, Integer>();
		wordidToMustlinkListMap = new HashMap<String, ArrayList<MustLink>>();
	}

	public void addMustLink(MustLink mustlink) {
		mustlinkToIndexMap.put(mustlink, mustlinkList.size());
		mustlinkList.add(mustlink);

		addWordIntoMap(mustlink.wordpair.wordstr1, mustlink);
		addWordIntoMap(mustlink.wordpair.wordstr2, mustlink);
	}

	/**
	 * Add the wordstr and its must-link into the map.
	 */
	private void addWordIntoMap(String wordstr, MustLink mustlink) {
		if (!wordidToMustlinkListMap.containsKey(wordstr)) {
			wordidToMustlinkListMap.put(wordstr, new ArrayList<MustLink>());
		}
		wordidToMustlinkListMap.get(wordstr).add(mustlink);
	}

	public MustLink getMustLink(int index) {
		assert (index < this.size() && index >= 0) : "Index is not correct!";
		return mustlinkList.get(index);
	}

	public int getMustLinkIndex(MustLink mustlink) {
		assert (!mustlinkToIndexMap.containsKey(mustlink)) : "This mustlink is not in the mustlinks!";
		return mustlinkToIndexMap.get(mustlink);
	}

	/**
	 * Get the list of must-links that contain this word.
	 * 
	 * @return
	 */
	public ArrayList<MustLink> getMustLinkListGivenWordstr(String wordstr) {
		if (!wordidToMustlinkListMap.containsKey(wordstr)) {
			return new ArrayList<MustLink>();
		} else {
			return wordidToMustlinkListMap.get(wordstr);
		}
	}

	public int size() {
		return mustlinkList.size();
	}

	@Override
	public String toString() {
		StringBuilder sbMustLinks = new StringBuilder();
		for (MustLink mustlink : mustlinkList) {
			sbMustLinks.append(mustlink.toString());
			sbMustLinks.append(System.getProperty("line.separator"));
		}
		return sbMustLinks.toString();
	}

	@Override
	public Iterator<MustLink> iterator() {
		return mustlinkList.iterator();
	}
}
