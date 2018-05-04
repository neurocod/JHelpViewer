package jhelpviewer;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.EventQueue;
import java.awt.event.*;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.*;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.text.*;
import javax.swing.text.DefaultHighlighter.DefaultHighlightPainter;
import javax.swing.text.html.*;
import javax.swing.text.html.HTMLEditorKit.LinkController;

@SuppressWarnings("serial")
class HelpTextPane extends JTextPane
{
	//implements incremental search
	protected class SearchPanel extends JPanel
	{
		public SearchPanel()
		{
			BoxLayout layout = new BoxLayout(this, BoxLayout.X_AXIS);
			setLayout(layout);
			add(btnHide);
			add(labelSearch);
			add(btnPrev);
			add(btnNext);
			add(inputField);
			add(labelInfo);
			btnHide.addActionListener(new ActionListener()
			{
				@Override
				public void actionPerformed(ActionEvent e)
				{
					setVisible(false);
				}
			});
			btnPrev.addActionListener(new ActionListener()
			{
				@Override
				public void actionPerformed(ActionEvent e)
				{
					searchPrev(inputField.getText());
				}
			});
			btnNext.addActionListener(new ActionListener()
			{
				@Override
				public void actionPerformed(ActionEvent e)
				{
					searchNext(inputField.getText());
				}
			});
			this.addKeyListener(new KeyAdapter()
			{
				@Override
				public void keyPressed(KeyEvent e)
				{
					if(e.getKeyCode()==KeyEvent.VK_ESCAPE)
						setVisible(false);
				}
			});
			inputField.addKeyListener(new KeyAdapter()
			{
				@Override
				public void keyPressed(KeyEvent e)
				{
					if(e.getKeyCode()==KeyEvent.VK_ESCAPE)
						setVisible(false);
				}

				@Override
				public void keyReleased(KeyEvent e)
				{
				}

				@Override
				public void keyTyped(KeyEvent e)
				{
					String text = inputField.getText();
					searchNext(text);	
				}				
			});
			
		}
		protected class SearchContext
		{
			public boolean sameAsLast;
			public int curPos;
			public int docLength;
			public Document doc;
			public String docText;
			public void setDocument(Document doc)
			{
				this.doc = doc;
				docLength = doc.getLength();
				try
				{
					docText = doc.getText(0, docLength);
				}
				catch (BadLocationException e)
				{
					assert(false);
				}
				docText = docText.toLowerCase();
			}
		}
		protected SearchContext initSearch(String text)
		{
			SearchContext search = new SearchContext();
			search.sameAsLast = lastSearchString.equals(text);
			lastSearchString = text;
			search.curPos = getCurrentPosition();
			search.setDocument(getDocument());
			
			return search;
		}
		
		protected void displaySearchMiss()
		{
			inputField.setBackground(Color.RED);
			setInfoMessage("Phrase not found");
		}
		
		protected void displaySearchSucces(int foundIndex, int textLength)
		{
			inputField.setBackground(Color.WHITE);
			setCaretPosition(foundIndex);
			setCurrentPosition(foundIndex);
			setInfoMessage("");
			try
			{
				highlighter.removeAllHighlights();
				highlighter.addHighlight(foundIndex, foundIndex+textLength, highlightPainter);
			}
			catch (BadLocationException e)
			{
				assert(false);
			}
		}
		
		protected void hideAllHighlights()
		{
			highlighter.removeAllHighlights();
		}
		
		protected void searchNext(String _text)
		{
			if(_text.isEmpty())
			{
				hideAllHighlights();
				return;
			}
			String text = _text.toLowerCase();
			SearchContext search = initSearch(text);
			if(!search.sameAsLast)
			{
				search.curPos = 0;
				setCurrentPosition(search.curPos);
			}
			
			int foundIndex = search.docText.indexOf(text, search.curPos);
			if(foundIndex>-1 && foundIndex==search.curPos && search.sameAsLast)
			{
				foundIndex = search.docText.indexOf(text, search.curPos+1);
			}
			
			if(foundIndex>-1)
			{
				displaySearchSucces(foundIndex, text.length());
			}
			else
			{
				if(getCurrentPosition()>0)
				{
					setCurrentPosition(0);
					searchNext(text);
					setInfoMessage("Passed end of document, continue from begining");
				}
				else
				{
					displaySearchMiss();
				}
			}
		}
		protected void searchPrev(String _text)
		{
			if(_text.isEmpty())
			{
				hideAllHighlights();
				return;
			}
			String text = _text.toLowerCase();
			SearchContext search = initSearch(text);
			if(!search.sameAsLast)
			{
				search.curPos = search.docLength;
				setCurrentPosition(search.curPos);
			}
			
			int foundIndex = search.docText.lastIndexOf(text, search.curPos);
			if(foundIndex>-1 && foundIndex==search.curPos && search.sameAsLast)
			{
				foundIndex = search.docText.lastIndexOf(text, search.curPos-1);
			}
			
			if(foundIndex>-1)
			{
				displaySearchSucces(foundIndex, text.length());
			}
			else
			{
				if(getCurrentPosition()<search.docLength)
				{
					setCurrentPosition(search.docLength);
					searchPrev(text);
					setInfoMessage("Passed start of document, continue from end");
				}
				else
				{
					displaySearchMiss();
				}
			}
		}
		/**
		 * @param currentPosition the currentPosition to set
		 */
		public void setCurrentPosition(int currentPosition)
		{
			this.currentPosition = currentPosition;
		}
		/**
		 * @return the currentPosition
		 */
		public int getCurrentPosition()
		{
			return currentPosition;
		}
		protected JLabel labelSearch = new JLabel("Search:");
		protected JTextField inputField = new JTextField();
		protected JButton btnHide = new JButton("x");
		protected JButton btnPrev = new JButton("< Previous");
		protected JButton btnNext = new JButton("Next >");
		protected String lastSearchString = new String();
		protected DefaultHighlightPainter highlightPainter = new DefaultHighlightPainter(getSelectionColor());
		DefaultHighlighter highlighter = (DefaultHighlighter)getHighlighter();
		protected JLabel labelInfo = new JLabel();
		private int currentPosition = 0;
		public void setInfoMessage(String text)
		{
			labelInfo.setText(text);
		}

		public void searchNext()
		{
			String text = inputField.getText();
			searchNext(text);
			if(text.isEmpty())
				inputField.requestFocus();
		}

		public void searchPrev()
		{
			String text = inputField.getText();
			searchPrev(text);
			if(text.isEmpty())
				inputField.requestFocus();
		}

		public void search()
		{
			inputField.requestFocus();
			String text = inputField.getText();
			searchNext(text);
		}
	}
	protected SearchPanel searchPanel = new SearchPanel();
	protected JHelpTabs parentTabs;
	public HelpTextPane(HtmlJavaDocIOManager manager, final JHelpTabs parentTabs)
	{
		this.parentTabs = parentTabs;
		//this.setLayout()
		this.manager = manager;
		setAutoscrolls(false);
		setEditable(false);
		scrPane.getViewport().add(this);
		topPanel.setLayout(new BorderLayout());
		topPanel.add(searchPanel, BorderLayout.NORTH);
		topPanel.add(scrPane, BorderLayout.CENTER);
		searchPanel.setVisible(false);
		
//		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
//		panel.add(this);
		addHyperlinkListener(defaultHyperlinkListener);
		addMouseListener(new LinkController()
		{
			@Override
			public void mouseClicked(MouseEvent e)
			{
				//middle button - click with wheel
				if(e.getButton()==MouseEvent.BUTTON2 && defaultHyperlinkListener.isLinkEntered())
				{
					HyperlinkEvent hev = defaultHyperlinkListener.getLastHyperlinkEvent();
					URL url = hev.getURL();
					parentTabs.createNewTab(url.toString(), HistoryItem.getDescriptionFromUrl(url.toString()));
				}
			}
		});
	}
	
	public void addHyperlinkListener(boolean replaceStandard, HyperlinkListener listener)
	{
		if(replaceStandard && defaultHyperlinkListener!=null && listener != defaultHyperlinkListener)
		{
			removeHyperlinkListener(defaultHyperlinkListener);
			defaultHyperlinkListener = null;
		}
		addHyperlinkListener(listener);
	}

	public boolean getScrollableTracksViewportWidth()
	{
		return wrapState;
	}
	
	public boolean getScrollableTracksViewportHeight()
	{
		return wrapState;
	}

	public boolean getLineWrap(boolean wrap)
	{
		return wrapState;
	}
	public void safeNavigate(String url, String description)
	{
		safeNavigate(url, description, true);
	}
	static protected boolean isLocalDirectoryForMe(String _path)
	{
		/*boolean b;
		b = isLocalDirectory("ya.ru");
		b = isLocalDirectory("/F:/Java/docs/api/index-files/../java/io/");
		b = isLocalDirectory("http://ya.ru");
		b = isLocalDirectory("F:\\projects");
		b = isLocalDirectory("F:\\projects\\VSS archives");
		b = isLocalDirectory("file:/F:/Java/docs/api/index-files/../java/io/");
		b = isLocalDirectory("file:/F:/Java/docs/api/index-files/../java/io/File.html#constructor_summary");*/
		String filePrefix = "file:";
		String path = _path;
		if(path.startsWith(filePrefix))
			path = path.substring(filePrefix.length());
		if(path.equals(listOfAllRootLocations))
			return true;
		File file = new File(path);
		return file.isDirectory();
	}
	
	protected void setHtmlContent(String html, String url, String description, boolean addToHistory)
	{
		setContentType("text/html");

		//clear current URL - necessary cause woudn't be possible to step into same url
		//again from hyperlink of this manual created text document
		Document doc = getDocument();
		doc.addDocumentListener(defaultDocListener);
		doc.putProperty(Document.StreamDescriptionProperty, null);

		if(addToHistory)
			addToHistoryAtCurrentPosition(url, description);
		additionalPageName = url;
		
		setText(html);
		fireDescriptionChanged(description);
	}
	
	static protected String getHtmlForDirectory(String path)
	{
		String filePrefix = "file:";
		String filePrefixToAdd = "file:/";
		if(path.startsWith(filePrefix))
			path = path.substring(filePrefix.length());
		
		StringBuilder builder = new StringBuilder();
		//make table: number, file name==href, file length()
		builder.append("<html><body>\n");
		if(path.equals(listOfAllRootLocations))
		{
			builder.append("List or root locations:<br/>");
			File[] roots = File.listRoots();
			for (int iFile = 0; iFile < roots.length; iFile++)
			{
				File file = roots[iFile];
				
				String text = String.format("<a href='%s'>%s</a><br/>",
						filePrefixToAdd+file.getAbsolutePath(), file.getAbsolutePath());
				builder.append(text);
			}
		}
		else
		{
			File dir = new File(path);
			assert(dir.isDirectory());
			File[] files = dir.listFiles();
			
			builder.append(String.format("Index of directory <b>%s</b><br/>\n", dir.getAbsoluteFile()));
			
			String parentPath = dir.getParent();
			if(null==parentPath)
			{
				builder.append(String.format("This is top-level location. View list of all <a href='%s'>root locations</a><br/>\n",
						listOfAllRootLocations));
			}
			else
			{
				String parentDir = String.format("<a href='file:/%s'>Parent Directory</a><br/>", parentPath);
				builder.append(parentDir);
			}
	
			builder.append("<hr/><table><tr>\n<td>name</td>");
			builder.append("<td>Type</td>");
			builder.append("<td>Length</td>");
			builder.append("</tr>");
			
			for (int iFile = 0; iFile < files.length; iFile++)
			{
				File file = files[iFile];
				
				String text = String.format("<tr><td><a href='%s'>%s</a></td><td>",
						filePrefix+"/"+file.getAbsolutePath(), file.getName()
						);
				builder.append(text);
				
				if(file.isDirectory())
					text = "Directory";
				else
					text = "";
				builder.append(text);
				
				text = String.format("</td><td>%s</td></tr>\n",
						file.isDirectory() ? "" : new Long(file.length()).toString());
				builder.append(text);
			}

			builder.append("</table>");
		}
		
		builder.append("</body></html>");
		return builder.toString();
	}
	
	public void safeNavigate(String url, String _description, boolean addToHistory)
	{
		String description = _description;
		if(description.isEmpty())
			description = HistoryItem.getDescriptionFromUrl(url);
		
		if(url.startsWith(HtmlJavaDocIndex.docIndexStartWhenInterop))
		{
			String name = url.substring(HtmlJavaDocIndex.docIndexStartWhenInterop.length());
			List<DocItem> items = manager.getHtmlJavaDocIndex().getDocItemsByName(name);
			if(items.isEmpty())
				return;
			if(items.size()==1)
			{
				DocItem item = items.get(0);
				safeNavigate(item.getFileName(), item.getClassOrPackage(), addToHistory);
			}
			else
			{
				//form page
				StringBuilder builder = new StringBuilder();
				builder.append("<html><body>\n");
				int index=0;
				for(DocItem item: items)
				{
					String href = String.format("%d) %s <a href='%s'>%s.<b>%s</b></a><br>\n",
							++index, item.getType(), item.getFileName(), item.getClassOrPackage(), item.getNameWithSignature());
					builder.append(href);
				}
				builder.append("</body></html>");
				
				setHtmlContent(builder.toString(), url, description, addToHistory);
			}
		}
		else
		{
			try
			{				
				if(isLocalDirectoryForMe(url))
				{
					String html = getHtmlForDirectory(url);
					setHtmlContent(html, url, description, addToHistory);
				}
				else
				{
					//errors are saved also
					if(addToHistory)
						addToHistoryAtCurrentPosition(url, description);
					setPage(url);
				}
			}
			catch(IOException e1)
			{
				showNavigationExeptionMessage(url, e1);
			}
			Document doc = getDocument();
			doc.addDocumentListener(defaultDocListener);
			fireDescriptionChanged(description);
		}
		if(description!=null)
			setName(description);
	}
	
	protected void fireDescriptionChanged(String description)
	{
		if (descriptionChangeListener != null)
		{
			descriptionChangeListener.descriptionChanged(description);
		}
	}
	protected void increaseFontSize(Document doc)
	{
		if (doc instanceof HTMLDocument)
		{
			//HTMLDocument hdoc = (HTMLDocument)doc;
//			StyleSheet sheet = hdoc.getStyleSheet();
//			sheet.setBaseFontSize(20);
			
			//enumerate styles
//			StyleSheet styles = hdoc.getStyleSheet();
//			Enumeration rules = styles.getStyleNames();
//			while (rules.hasMoreElements())
//			{
//				String name = (String)rules.nextElement();
//				Style rule = styles.getStyle(name);
//				System.out.println(rule.toString());
//			}
//			System.out.println("styles end\n\n\n");

			int yourSize = 15;
			javax.swing.text.SimpleAttributeSet attr = new javax.swing.text.SimpleAttributeSet();
			attr.addAttribute(javax.swing.text.StyleConstants.FontSize, new Integer(yourSize));
			try
			{
				int length = doc.getText(0,doc.getLength()).length();
//				if(length>0)
//				{
//					doc2.removeDocumentListener(caller);
//				}
				getStyledDocument().setCharacterAttributes(0, length, attr, false);
			} catch (BadLocationException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	public void safeNavigate(HistoryItem item, boolean addToHistory)
	{
		safeNavigate(item.getUrl(), item.getDescriptionFromUrl(), addToHistory);
	}
	public void safeNavigate(URL url, String description)
	{
		safeNavigate(url.toString(), description);
	}

	boolean wrapState = false;
	protected JScrollPane scrPane = new JScrollPane();
	public Component getScrollFraming()
	{
		//return scrPane;
		return topPanel;
	}
	public static HelpTextPane getFromFraming(Component c)
	{
		JPanel panel = (JPanel)c;
		Component[] components = panel.getComponents();
		for (Component comp: components)
		{
			if (comp instanceof JScrollPane)
			{
				HelpTextPane pane = (HelpTextPane)((JScrollPane) comp).getViewport().getView();
				return pane;
			}
		}
		return null;
		//c = ((JScrollPane)c).getViewport().getView();
		//c = ((JPanel)c).getComponent(0);
		//return (HelpTextPane)c;
	}
//	public HelpTextPane getScrollFraming()
//	{
//		return this;
//	}
//	protected JPanel panel = new JPanel();
//	public JPanel getScrollFraming()
//	{
//		return panel;
//	}
//	public static HelpTextPane getFromFraming(Component c)
//	{
//		c = ((JPanel)c).getComponent(0);
//		//c = ((JPanel)c).getComponent(0);
//		return (HelpTextPane)c;
//	}

	public void navigateBackward()
	{
		if(iterCurHistoryPos.hasPrevious())
		{
			iterCurHistoryPos.previous();
			if(iterCurHistoryPos.hasPrevious())
			{
				HistoryItem item = iterCurHistoryPos.previous();
				iterCurHistoryPos.next();
				safeNavigate(item, false);
			}
		}
	}
	
	public void navigateForward()
	{
		if(iterCurHistoryPos.hasNext())
		{
			HistoryItem item = iterCurHistoryPos.next();
			safeNavigate(item, false);
		}
	}

	protected void showNavigationExeptionMessage(String url, Exception e)
	{
		setContentType("text/html");
		String msg = String.format("<html><body>Error occured while navigating to page:<br><b>%s</b><br>Error message:<br><b>%s</b></body></html>",
				url, e.getLocalizedMessage());
		this.setText(msg);
	}
	
	protected class StatefullHyperlinkListener implements HyperlinkListener
	{
		@Override
		public void hyperlinkUpdate(HyperlinkEvent e)
		{
			HyperlinkEvent.EventType type = e.getEventType();
			if(HyperlinkEvent.EventType.ACTIVATED==type)
			{
				URL url = e.getURL();
				if(null==url)
					JOptionPane.showMessageDialog(HelpTextPane.this, "No URL exists for this hyperlink", "Error", JOptionPane.OK_OPTION);
				else
					safeNavigate(url, HistoryItem.getDescriptionFromUrl(url.getFile()));
			}
			else if(HyperlinkEvent.EventType.ENTERED==type)
			{
				lastHyperlinkEvent = e;
				linkEntered = true;
			}
			else if(HyperlinkEvent.EventType.EXITED==type)
			{
				lastHyperlinkEvent = null;
				linkEntered = false;
			}
		}
		public void setLinkEntered(boolean linkEntered)
		{
			this.linkEntered = linkEntered;
		}
		public boolean isLinkEntered()
		{
			return linkEntered;
		}
		private boolean linkEntered=false;
		private HyperlinkEvent lastHyperlinkEvent = null;
		public HyperlinkEvent getLastHyperlinkEvent()
		{
			return lastHyperlinkEvent;
		}
	}
	protected StatefullHyperlinkListener defaultHyperlinkListener = new StatefullHyperlinkListener(); 
	
	static class HistoryItem
	{
		public HistoryItem(String url, String desc)
		{
			this.url = url;
			this.desc = desc;
		}
		public String getUrl()
		{
			return url;
		}
		public String getDescription()
		{
			return desc;
		}
		public static String getDescriptionFromUrl(String url)
		{
			String desc = url;
			char []delimiters = {'/', '\\', '#'};
			int index = -1;
			for(char delim: delimiters)
			{
				index = url.lastIndexOf(delim);
				if(-1!=index)
				{
					break;
				}
			}
			if(-1!=index)
			{
				desc = url.substring(index+1);
			}
			if(desc.length()>5)
			{
				desc = desc.replace(".html", "");
				desc = desc.replace(".htm", "");
			}
			return desc;
		}
		public String getDescriptionFromUrl()
		{
			return getDescriptionFromUrl(url);
		}
		public String toString()
		{
			return String.format("HistoryItem: desc='%s' url='%s'", desc, url);
		}
		protected String url;
		protected String desc;
		//if error, needs to be removed
		//protected boolean errorPage = false;
	}
	public static abstract class DescriptionChangeListener
	{
		public abstract void descriptionChanged(String newDesc);//, HelpTextPane pane
	}
	public void addDescriptionChangeListener(DescriptionChangeListener listener)
	{
		assert(descriptionChangeListener==null);
		descriptionChangeListener = listener;
	}
	
	protected void addToHistoryAtCurrentPosition(String url, String desc)
	{
		//original behaviour - if is in the middle of history,
		//push new url in the middle, but no trim all further history
		iterCurHistoryPos.add(new HistoryItem(url, desc));
	}
	
	public void showHistoryForDebug()
	{
		StringBuilder msg = new StringBuilder();
		ListIterator<HistoryItem> iter = urlHistory.listIterator();
		while(iter.hasNext())
		{
			HistoryItem item = iter.next();
			msg.append(item.toString());
			msg.append("\r\n");
		}
		
//		ListIterator<String> iterCopy = iterCurHistoryPos.;
//		msg.append("\niterator info: ");
//		if(iterCurHistoryPos.hasPrevious())
//		{
//			msg.append("prev: ");
//			msg.append(iterCurHistoryPos.previous());
//		}
//		else
//		{
//			if(iterCurHistoryPos.hasNext())
//			{
//				msg.append("next: ");
//				msg.append(iterCurHistoryPos.next());
//			}			
//			else
//				msg.append("no elements");
//		}
		JOptionPane.showMessageDialog(this, msg);
	}
	
	public String getAdditionalPageName()
	{
		if(null==getPage())
		{
			return additionalPageName;
		}
		else
		{
			String page = getPage().toString();
			return page;
		}
	}
	
	private JPanel topPanel = new JPanel();
	private LinkedList<HistoryItem> urlHistory = new LinkedList<HistoryItem>();
	private ListIterator<HistoryItem> iterCurHistoryPos = urlHistory.listIterator();
	private DescriptionChangeListener descriptionChangeListener = null;
	private HtmlJavaDocIOManager manager;
	private String additionalPageName = new String();
	private DocumentListener defaultDocListener = new DocumentListener()
	{

		@Override
		public void changedUpdate(DocumentEvent e)
		{
			// TODO Auto-generated method stub
			
		}

		@Override
		public void insertUpdate(DocumentEvent e)
		{
			final Document doc = e.getDocument();
			//IncreaseFontEvent fe = new IncreaseFontEvent(e);
			EventQueue.invokeLater(new Runnable()
			{
				@Override
				public void run()
				{
					increaseFontSize(doc);
				}
			});
		}

		@Override
		public void removeUpdate(DocumentEvent e)
		{
			// TODO Auto-generated method stub
			
		}
	};
	static protected String listOfAllRootLocations = "/{A435956B-BA53-470e-B0DA-6058510A85AC}";
	public void findNext()
	{
		if(!searchPanel.isVisible())
			searchPanel.setVisible(true);
		searchPanel.searchNext();
	}

	public void findPrevious()
	{
		if(!searchPanel.isVisible())
			searchPanel.setVisible(true);
		searchPanel.searchPrev();
	}

	public void findCurrent()
	{
		if(!searchPanel.isVisible())
			searchPanel.setVisible(true);
		searchPanel.search();
	}
}