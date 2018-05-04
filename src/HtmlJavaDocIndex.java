package jhelpviewer;

import java.io.*;
import java.util.*;

public class HtmlJavaDocIndex implements Externalizable, IHtmlJavaDocModel
{
	@Override
	public void add(DocItem item)
	{
		if(!modoficationAllowed)
		{
			throw new IllegalStateException("Index filled, RebuildIndex called, modofication disabled");
		}
		keywordsMap.add(item);
	}
	
	@Override
	public boolean tryLoad(LoadDocIndexListener listener)
	{
		return false;
	}
	
	protected synchronized void loadIndexError(LoadDocIndexListener listener, String description)
	{
		setFailedState();
		listener.loadIndexError(description);
	}
	
	//@SuppressWarnings("unchecked")
	public synchronized void loadKeywordsIndexAndContinueInAnoterThread(
			final ObjectInput in, final LoadDocIndexListener listener)
	{
		modoficationAllowed = true;
		keywordsMap.clear();
		
		try
		{
			TimeStamp timeStamp = new TimeStamp();
			keywordsLists.readExternal(in);
			timeStamp.printDelay("loading index time: ");
		}
		catch (Exception e)
		{
			loadIndexError(listener, e.getMessage());
			return;
		}
		
		Thread thread = new Thread(new Runnable()
		{
			@Override
			public void run()
			{
				try
				{
					TimeStamp timeStamp = new TimeStamp();
					keywordsMap.readExternal(in);
					timeStamp.printDelay("loading map: ");
					
					if(keywordsLists.namesList.size()!=keywordsMap.mapNameToDoc.keySet().size())
					{
						loadIndexError(listener, "Wrong map keyset size");
					}
				}
				catch (Exception e)
				{
					loadIndexError(listener, e.toString());
				}
			}
		});
		thread.start();
	}

	@Override
	public List<DocItem> getDocItemsByName(String name)
	{
		return keywordsMap.getDocItemsByName(name);
	}

	@Override
	public List<String> getNamesList()
	{
		if(modoficationAllowed)
		{
			throw new IllegalStateException("RebuildIndex not called called, modofication are possible, index is not ready");
		}
		return keywordsLists.getNamesList();
	}

	@Override
	public List<String> getNamesList(String _subStrings)
	{
		return keywordsLists.getNamesList(_subStrings);
	}

	//slow (typicaly few seconds), use loading from another thread by parts (index-map) instead
	@Override
	public void readExternal(ObjectInput in) throws IOException,
			ClassNotFoundException
	{
		keywordsLists.setLoadingState();
		keywordsMap.setLoadingState();
		keywordsLists.readExternal(in);
		keywordsMap.readExternal(in);
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException
	{
		assert(getCountOfDocItems()>0);
		keywordsLists.writeExternal(out);
		keywordsMap.writeExternal(out);
	}

	class KeywordsLists extends SynchronizedReadiness implements Externalizable
	{
		@Override
		public synchronized void setFailedState()
		{
			super.setFailedState();
		}
		@SuppressWarnings("unchecked")
		@Override
		public void readExternal(ObjectInput in) throws IOException,
				ClassNotFoundException
		{
			setLoadingState();
			try
			{
				Object obj = in.readObject();
				System.out.println(obj.getClass());
				if (!(obj instanceof List))
				{
					throw new BadFormatException("Wrong keywords list index type - not a List");
				}
				if(!(obj instanceof RandomAccess))
					throw new BadFormatException("Wrong keywords list index type - not a RandomAccess");
				namesList = (List) obj;
				if(namesList.size()>0 && !(namesList.get(0) instanceof String))
					throw new BadFormatException();
			}
			catch(IOException e)
			{
				setFailedState();
				throw e;
			}
			catch(ClassNotFoundException e)
			{
				setFailedState();
				throw e;
			}
			
			rebuildLowercaseList();
			setReadyState();
		}

		@Override
		public synchronized void writeExternal(ObjectOutput out) throws IOException
		{
			if(!waitForReadyOrFailure()|| null==namesList || namesList.size()==0)
			{
				assert(false);
				return;
			}
			out.writeObject(namesList);
		}
		
		public synchronized List<String> getNamesList()
		{
			if(!waitForReadyOrFailure())
				return emptyList;
			return Collections.unmodifiableList(namesList);
		}

		public synchronized List<String> getNamesList(String _subStrings)
		{
			String subStrings = _subStrings.trim().toLowerCase();
			if(!waitForReadyOrFailure())
				return emptyList;
			if(subStrings.isEmpty())
				return getNamesList();
			
			List<String> subList = new ArrayList<String>();
			String []subStringsArr = subStrings.split(" ");
			List<String> namesList = getNamesList();
			for(int index=0; index<namesList.size(); index++)
			{
				String lowerName = keywordsLists.lowercaseNamesList.get(index);
				boolean match=true;
				for (int i = 0; i < subStringsArr.length; i++)
				{
					if(!lowerName.contains(subStringsArr[i]))
					{
						match = false;
						break;
					}	
				}
				if(match)
					subList.add(namesList.get(index));
			}
			return Collections.unmodifiableList(subList);	
		}
		
		synchronized void rebuildKeywordsListIndexByMap(KeywordsMap keywordsMap)
		{
			namesList = new ArrayList<String>(keywordsMap.mapNameToDoc.keySet());
			rebuildLowercaseList();
		}
		
		protected void rebuildLowercaseList()
		{
			lowercaseNamesList = new ArrayList<String>(namesList.size());
			for(String name: namesList)
			{
				lowercaseNamesList.add(name.toLowerCase()); 
			}
			lowercaseNamesList = Collections.unmodifiableList(lowercaseNamesList);
			modoficationAllowed = false;
		}

		private List<String> emptyList; 
		private List<String> namesList;
		private List<String> lowercaseNamesList;
		{
			emptyList = Collections.unmodifiableList(new ArrayList<String>());
			namesList = emptyList;
			lowercaseNamesList = emptyList;
		}
	}
	
	class KeywordsMap extends SynchronizedReadiness implements Externalizable
	{
		@Override
		public synchronized void setFailedState()
		{
			super.setFailedState();
			countOfDocItems = 0;	
			mapNameToDoc.clear();
		}
		
		@Override
		public void readExternal(ObjectInput in) throws IOException,
				ClassNotFoundException
		{
			setLoadingState();
			try
			{
				int itemsCount = in.readInt();
				for(int itemsRead = 0; itemsRead<itemsCount; itemsRead++)
				{
					DocItem item = new DocItem();
					item.readExternal(in);
					add(item);
				}
			}
			catch(IOException e)
			{
				setFailedState();
				throw e;
			}
			catch(ClassNotFoundException e)
			{
				setFailedState();
				throw e;
			}
			setReadyState();
		}

		@Override
		public void writeExternal(ObjectOutput out) throws IOException
		{
			out.writeInt(countOfDocItems);
			int itemsWrittenCheck = 0;
			
			for (ArrayList<DocItem> items: mapNameToDoc.values())
			{
				for (DocItem docItem : items)
				{
					docItem.writeExternal(out);
					itemsWrittenCheck++;
				}
			}
//			int uniqItems = mapNameToDoc.values().size();
//			int duplicated = countOfDocItems - uniqItems;
			assert(countOfDocItems==itemsWrittenCheck);
		}
		
		public void clear()
		{
			mapNameToDoc.clear();
			countOfDocItems = 0;
		}

		public synchronized void add(DocItem item)
		{
			ArrayList<DocItem> items = mapNameToDoc.get(item.getName());
			if(null==items)
			{
				items = new ArrayList<DocItem>();
				mapNameToDoc.put(item.getName(), items);
			}
			for (Iterator<DocItem> iterator = items.iterator(); iterator.hasNext();)
			{
				DocItem docItem = (DocItem) iterator.next();
				if(docItem.sameSignatureAndFile(item))
					return;
			}
			items.add(item);
			countOfDocItems++;
		}

		public synchronized List<DocItem> getDocItemsByName(String name)
		{
			if(!waitForReadyOrFailure())
				return new ArrayList<DocItem>();
			ArrayList<DocItem> items = mapNameToDoc.get(name);
			return items;
		}

		private volatile int countOfDocItems = 0;	
		private SortedMap<String, ArrayList<DocItem>> mapNameToDoc = new TreeMap<String, ArrayList<DocItem>>();//(String.CASE_INSENSITIVE_ORDER);
		public int getCountOfDocItems()
		{
			return countOfDocItems;
		}
	}
	public int getCountOfDocItems()
	{
		return keywordsMap.getCountOfDocItems();
	}
	protected final KeywordsLists keywordsLists = new KeywordsLists();
	protected final KeywordsMap keywordsMap = new KeywordsMap();

	private boolean modoficationAllowed = true;
	final public static String docIndexStartWhenInterop = new String("JHelpViewer:");
	public void finishedLoadingByParser()
	{
		keywordsLists.rebuildKeywordsListIndexByMap(keywordsMap);
		keywordsLists.setReadyState();
		keywordsMap.setReadyState();
	}

	public void setFailedState()
	{
		keywordsLists.setFailedState();
		keywordsMap.setFailedState();
	}
}


class SynchronizedReadiness
{
	public enum State
	{
		LOADING,
		READY,
		FAILED,
	}
	private volatile State state = State.LOADING;
	public synchronized void setLoadingState()
	{
		state = State.LOADING;
		notifyAll();
	}
	public synchronized void setReadyState()
	{
		state = State.READY;
		notifyAll();
	}
	public synchronized void setFailedState()
	{
		state = State.FAILED;
		notifyAll();
	}
	public synchronized boolean waitForReadyOrFailure()
	{
		while(State.LOADING==state)
		{
			try
			{
				wait();
			}catch (InterruptedException e)
			{
				setFailedState();
				break;
			}
		}
		return State.READY == state;
	}
}

class TimeStamp
{
	public TimeStamp()
	{
		reinit();
	}
	public void printDelay()
	{
		time1 = System.currentTimeMillis();
		System.out.print(new Long(time1 - time0));
		System.out.print(" ms\n");
	}
	public void printDelay(String description)
	{
		time1 = System.currentTimeMillis();
		System.out.print(description);
		System.out.print(new Long(time1 - time0));
		System.out.print(" ms\n");
	}
	public void reinit()
	{
		time0 = System.currentTimeMillis();
	}
	protected long time0, time1;
}