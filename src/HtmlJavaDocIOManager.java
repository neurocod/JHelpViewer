package jhelpviewer;


import java.awt.EventQueue;
import java.io.*;
import java.util.*;

class BadFormatException extends IOException
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public BadFormatException()
	{
	}
	
	public BadFormatException(String desc)
	{
		super(desc);
	}
}

public class HtmlJavaDocIOManager
{
	static protected boolean debugScanOnlyOneFile = false;
	public List<String> getFilesToParse(String baseFile)
	{
		List<String> filenameList = new ArrayList<String>(); 
		File file = new File(baseFile);
		if(file.isDirectory())
		{
			for(int indexNum=1; indexNum<=27; indexNum++)
			{
				String filename = String.format("%sindex-%d.html", baseFile, indexNum);
				filenameList.add(filename);
			}
			filenameList.add("index-all.html");
		}
		else
			filenameList.add(baseFile);
		return filenameList;
	}
	
	public void addLoadDocIndexListener(LoadDocIndexListener listener)
	{
		loadDocIndexListenersList.add(listener);
	}

	public void removeLoadDocIndexListener(LoadDocIndexListener listener)
	{
		loadDocIndexListenersList.remove(listener);
	}

	public void fireDocIndexChanged()
	{
		for (LoadDocIndexListener listener : loadDocIndexListenersList)
		{
			listener.indexChanged();
		}
	}
	
	public void fireLoadDocIndexError(Exception e)
	{
		fireLoadDocIndexError(e.toString());
	}
	public void fireLoadDocIndexError(String description)
	{
		docIndex.setFailedState();
		System.out.println(description);
		for (LoadDocIndexListener listener : loadDocIndexListenersList)
		{
			listener.loadIndexError(description);
		}
	}
	
	public void fireLoadDocIndexSuccess()
	{
		for (LoadDocIndexListener listener : loadDocIndexListenersList)
		{
			listener.loadIndexSuccess();
		}
	}
	
	public void fireLoadDocIndexNotify(String description)
	{
		System.out.println(description);
		for (LoadDocIndexListener listener : loadDocIndexListenersList)
		{
			listener.loadNotify(description);
		}
	}
	
	public void loadKeywordsIndexAndContinueInAnoterThread()
	{
		try
		{
			final FileInputStream fis = new FileInputStream(indexFileName);
			final ObjectInputStream ois = new ObjectInputStream(fis);
			docIndex.loadKeywordsIndexAndContinueInAnoterThread(ois, new LoadDocIndexListenerAdapter()
			{
				protected void closeStreams()
				{
					try
					{
						ois.close();
						fis.close();
					}
					catch (IOException e)
					{
						fireLoadDocIndexError(e);
					}
				}
				@Override
				public void indexChanged()
				{
					log("indexChanged\n");
					EventQueue.invokeLater(new Runnable()
					{
						@Override
						public void run()
						{
							fireDocIndexChanged();
						}
					});
				}
				
				@Override
				public void loadIndexError(final String description)
				{
					log("loadIndexError: ");
					log(description+'\n');
					EventQueue.invokeLater(new Runnable()
					{
						@Override
						public void run()
						{
							fireLoadDocIndexError(description);
						}
					});
					closeStreams();
				}

				@Override
				public void loadIndexSuccess()
				{
					log("loadIndexSuccess"+'\n');
					EventQueue.invokeLater(new Runnable()
					{
						@Override
						public void run()
						{
							fireLoadDocIndexSuccess();
						}
					});
					closeStreams();
				}

				@Override
				public void loadNotify(final String description)
				{
					log("loadNotify: ");
					log(description+'\n');
					EventQueue.invokeLater(new Runnable()
					{
						@Override
						public void run()
						{
							fireLoadDocIndexNotify(description);	
						}
					});
				}
				
			});
		}
		catch (Exception e)
		{
			fireLoadDocIndexError(e);
		}
	}

	
	public void saveIfNotEmpty()
	{
		if(docIndex.getNamesList().size()>0)
    	{
			try
			{
	        	FileOutputStream fos = new FileOutputStream(indexFileName);
				ObjectOutputStream oos = new ObjectOutputStream(fos);
				try
				{
					docIndex.writeExternal(oos);
					if(errorStringBuilder.length()>0)
					{
						errorStringBuilder.append("Index build OK; count of words in index = ");
						errorStringBuilder.append(docIndex.getNamesList().size());
					}
				}
				finally
				{
					oos.close();
					fos.close();
				}
			}
			catch (Exception e)
			{
				errorStringBuilder.append("Error on saving index file.\n");
				addExceptionErrorInfo(e);
			}
    	}
    	else
    	{
    		errorStringBuilder.append("No help information found!\n");
    		errorStringBuilder.append(errorStringBuilder.toString()); 
    	}
	}
	
	protected void addExceptionErrorInfo(Exception e)
	{
		errorStringBuilder.append("Exception description: ");
		errorStringBuilder.append(e.toString());
		errorStringBuilder.append("\nStack trace:\n");
		errorStringBuilder.append(e.getStackTrace().toString());
		errorStringBuilder.append("\n");
	}
	
	public boolean finalizeIndexAndReplaceDocumentation(HtmlJavaDocIndex newDocIndex)
	{
		boolean needUpdate = newDocIndex.getCountOfDocItems()>0; 
		
		newDocIndex.finishedLoadingByParser();
		if(needUpdate)
		{
			docIndex = newDocIndex;
			fireDocIndexChanged();
			return true;
		}
		return false;
	}
	
	public void parseIndexFiles(String baseFile, LoadDocIndexListener listener, HtmlJavaDocIndex targetDocIndex)
	{
		HtmlJavaDocParser parser = new HtmlJavaDocParser(targetDocIndex);
		List<String> filenameList = getFilesToParse(baseFile);
		for(String filename: filenameList)
		{
			File file = new File(filename);
			listener.loadNotify("Parsing file "+file.getAbsolutePath()+"\n");
			if(file.exists())
			{
				parser.parseFile(file);
			}
			else
			{
				String message = String.format("The file '%s' does not exists, can not build index.\n",
						filename);
				listener.loadNotify(message);
			}
			
			if(debugScanOnlyOneFile)
			{
				break;
			}
		}
		listener.loadNotify(String.format("Parsing finished; count of doc items is %d\n", targetDocIndex.getCountOfDocItems()));
	}
	
	//listener is used to get info about parsing,
	//while index will be exchanged to newly parsed only at success 
	public boolean parseIndexFiles(String baseFile, LoadDocIndexListener listener)
	{
		HtmlJavaDocIndex newDocIndex = new HtmlJavaDocIndex();
		parseIndexFiles(baseFile, listener, newDocIndex);
		return finalizeIndexAndReplaceDocumentation(newDocIndex);
	}
	
	volatile private HtmlJavaDocIndex docIndex = new HtmlJavaDocIndex();
	volatile private static String indexFileName = "DocIndex.dat";
	volatile private StringBuilder errorStringBuilder = new StringBuilder();
	public HtmlJavaDocIndex getHtmlJavaDocIndex()
	{
		return docIndex;
	}
	private List<LoadDocIndexListener> loadDocIndexListenersList = new LinkedList<LoadDocIndexListener>();
}
