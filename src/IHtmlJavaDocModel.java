package jhelpviewer;

import java.util.List;

interface LoadDocIndexListener
{
	void log(String str);
	void loadIndexError(String description);
	void loadIndexSuccess();
	void loadNotify(String description);
	void indexChanged();
}

abstract class LoadDocIndexListenerAdapter implements LoadDocIndexListener
{
	@Override
	public void log(String str)
	{
		System.out.println(str);
	}
}

public interface IHtmlJavaDocModel
{
	public void add(DocItem item);
	public boolean tryLoad(LoadDocIndexListener listener);
	public List<DocItem> getDocItemsByName(String name);
	/**
	 * @return the unmodifiable List<String> of all names which index contains
	 */
	public List<String> getNamesList();
	/**
	 * @param subStrings filter for list to return. Empty filter string means no filter, return all elements to string.
	 * If not empty, returns any element, which contains all the sub-keywords specified.
	 * Comparasions are case insensitive.
	 * Sub-keywords are separated with space. I. e., "str to" will return "toString", "toPlainString" etc.  
	 * @return the unmodifiable List<String> of names which this index contains,
	 * where each name contains specified substrings
	 * Returned list implements RandomAccess
	 */
	public List<String> getNamesList(String _subStrings);
}
