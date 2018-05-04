package jhelpviewer;

import java.awt.BorderLayout;
import java.awt.event.*;
import java.util.*;

import javax.swing.*;

interface DocIndexNameListener
{
	void navaigate(String name);
	void navaigateUrl(String name);
}

class DocIndexListModel extends AbstractListModel
{
	public DocIndexListModel(HtmlJavaDocIOManager manager)
	{
		this.manager = manager;
		setFilterString("");
	}
	@Override
	public Object getElementAt(int index)
	{
		return innerList.get(index);
	}

	@Override
	public int getSize()
	{
		return innerList.size();
	}
	
	protected int updateListByFilterString()
	{
		HtmlJavaDocIndex docIndex = getDocIndex();
		fireIntervalRemoved(this, 0, innerList.size());
		innerList = docIndex.getNamesList(filterString);
		fireIntervalAdded(this, 0, innerList.size());
		
		lastFilterIndex = 0;
		if(!filterString.isEmpty())
		{
			for (int i = 0; i < innerList.size(); i++)
			{
				if(innerList.get(i).startsWith(filterString))
				{
					lastFilterIndex = i;
					break;
				}
			}
		}
		return lastFilterIndex;
	}
	/**
	 * @param filterString the filterString to set
	 * @return index of string (in model with currently aplied filter),that starts with filter string.
	 * Or 0, if no such string.
	 * So it's possible to select such string
	 */
	public int setFilterString(String filterString)
	{
		if(this.filterString.equals(filterString))
			return lastFilterIndex;
		this.filterString = filterString;
		return updateListByFilterString();
	}
	/**
	 * @return the filterString
	 */
	public String getFilterString()
	{
		return filterString;
	}
	
	private String filterString = new String("will be reset at ctor with different value"); 
	List<String> innerList = new ArrayList<String>(); 
	private static final long serialVersionUID = 1L;
	private HtmlJavaDocIndex getDocIndex()
	{
		return manager.getHtmlJavaDocIndex();
	}
	private int lastFilterIndex = -1;
	public void update()
	{
		updateListByFilterString();
	}
	private HtmlJavaDocIOManager manager;
}

public class JHelpIndexPanel extends JPanel
{
	public void updateIndex()
	{
		indexModel.update();
	}
	@SuppressWarnings("serial")
	public Action getSearchSunByGoogleAction()
	{
		return new AbstractAction("Search sun.com")
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				searchSunByGoogle();
			}
		};
	}
	
	public void searchSunByGoogle()
	{
		System.out.println("sun");
		String text = editName.getText().trim();
		if(!text.isEmpty())
		{
			String url = "http://www.google.ru/search?num=50&complete=1&hl=ru&newwindow=1&safe=off&q=<SEARCHKEYWORDS>+site%3Asun.com&lr=&aq=f";
			url = url.replace("<SEARCHKEYWORDS>", text);
			editName.setText(url);
			editName.selectAll();
			editName.requestFocus();
			//fireDocIndexNameNavigateUrl(url);
		}
	}
	
	public JHelpIndexPanel(HtmlJavaDocIOManager docIOManager)
	{
		//this.docIndex = docIndex;
		BorderLayout layout = new BorderLayout(5, 5); 
		indexModel = new DocIndexListModel(docIOManager);
		listIndex = new JList(indexModel);
		listIndex.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		setLayout(layout);
		
		editName.addKeyListener(new KeyAdapter()
		{
			public boolean isListNavigationKeys(KeyEvent e)
			{
				int code = e.getKeyCode();
				return 	(KeyEvent.VK_UP == code || KeyEvent.VK_DOWN == code ||
				KeyEvent.VK_PAGE_DOWN == code || KeyEvent.VK_PAGE_UP == code);
			}
			@Override
			public void keyReleased(KeyEvent e)
			{
				//keys in edit are processed after releasing
				if(e.getKeyCode()==KeyEvent.VK_ENTER)
				{
					String text = (String)listIndex.getSelectedValue();
					if(text!=null)
					{
						text = text.trim();
						if(!text.isEmpty())
							fireDocIndexNameNavigate(text);
					}						
				}
				else if(!isListNavigationKeys(e))
				{
					editNameChanged();
				}
			}

			@Override
			public void keyPressed(KeyEvent e)
			{
				//list will change selection only in response to keyPressed, not keyReleased  
				if(isListNavigationKeys(e))
				{
					listIndex.dispatchEvent(e);
					String str = (String)listIndex.getSelectedValue();
					editName.setText(str);
				}
			}

			@Override
			public void keyTyped(KeyEvent e)
			{
				editNameChanged();
			}
		});
		JLabel label = new JLabel("Look at:");
		
		JPanel northPanel = new JPanel();
		northPanel.setLayout(new BoxLayout(northPanel, BoxLayout.Y_AXIS));
		northPanel.add(label);
		northPanel.add(editName);
		add(northPanel, BorderLayout.NORTH);
		
		
//		listIndex.setCellRenderer(new ListCellRenderer()
//		{
//			@Override
//			public Component getListCellRendererComponent(JList list,
//					Object value, int index, boolean isSelected,
//					boolean cellHasFocus)
//			{
//				//DefaultListCellRenderer
//				String str = (String)value;
//				textLabel.setText(str);
//				if(isSelected)
//				{
//					//textLabel.setUI(ui);
//				}
//				return textLabel;
//			}
//			protected JLabel textLabel = new JLabel()
//			{
//				private static final long serialVersionUID = 1L;
//				public void paint(Graphics g)
//				{
//					super.paint(g);
//					//LineMetrics metrics = 
//					//g.drawString(getText(), x, y)
//				}
//			};
//		});
		listIndex.addMouseListener(new MouseAdapter()
		{
			public void mouseClicked(MouseEvent e)
			{
				int index = listIndex.locationToIndex(e.getPoint());
				if(index==-1)
					return;
				String str = (String)indexModel.getElementAt(index);
				editName.setText(str);
				if (e.getClickCount() >= 2)
				{
					fireDocIndexNameNavigate(str);
				}
			}
		});
		
		add(new JScrollPane(listIndex), BorderLayout.CENTER);
	}
	protected void editNameChanged()
	{
		String text = editName.getText();
		if(!lastEditFilter.equals(text))
		{
			lastEditFilter = text;
			int index = indexModel.setFilterString(lastEditFilter);
			listIndex.ensureIndexIsVisible(index);
			listIndex.setSelectedIndex(index);
			//need ensure twice cause sometimes once is insufficient
			listIndex.ensureIndexIsVisible(index);
		}
	}
	public void addDocIndexNameListener(DocIndexNameListener listener)
	{
		listDocIndexNameListeners.add(listener);
	}

	public void removeDocIndexNameListener(DocIndexNameListener listener)
	{
		listDocIndexNameListeners.remove(listener);
	}
	protected void fireDocIndexNameNavigate(String name)
	{
		for(DocIndexNameListener l:listDocIndexNameListeners)
		{
			l.navaigate(name);
		}
	}
	protected void fireDocIndexNameNavigateUrl(String url)
	{
		for(DocIndexNameListener l:listDocIndexNameListeners)
		{
			l.navaigateUrl(url);
		}
	}
	private JTextField editName = new JTextField();
	private DocIndexListModel indexModel = null;
	private JList listIndex = null;
	private String lastEditFilter = new String();
	//private HtmlJavaDocIndex docIndex;
	private static final long serialVersionUID = 1L;
	private List<DocIndexNameListener> listDocIndexNameListeners = new LinkedList<DocIndexNameListener>();
}

//F:\Java\docs\java_ee_javadocs\api\index-all.html
//F:\Java\docs\api\index-files\
//F:\Java\docs\jsp\jstl\1.1\docs\tlddocs\index.html
//file:/F:/Java/docs/jsp/jstl/1.1/docs/tlddocs/index.html