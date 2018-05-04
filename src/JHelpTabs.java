package jhelpviewer;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.*;
import java.net.URL;

import javax.swing.*;
import javax.swing.event.*;

/**
 * @author neurocod
 *
 */
class JHelpTabs extends JPanel
{
	private static final long serialVersionUID = 1L;

	JHelpTabs(HtmlJavaDocIOManager manager)
	{
		this.manager = manager;
		tabbedPane.setAllowCloseLastSoleTab(false);
		setLayout(new BorderLayout());
		createNewTab();
		tabbedPane.addChangeListener(new ChangeListener()
		{
			@Override
			public void stateChanged(ChangeEvent e)
			{
				HelpTextPane pane = getActiveHelpPane();
				String page = pane.getAdditionalPageName();
				urlPath.setText(page);
			}
			
		});
		urlPath.addKeyListener(new NavigateOnEnterKeyListener());
		add(urlPath, BorderLayout.NORTH);
		add(tabbedPane, BorderLayout.CENTER);
	}
	
	public HelpTextPane createNewTab()
	{
		String urlToNavigate=null;
		String desc = null;
		if(tabbedPane.getTabCount()>0)
		{
			int index = tabbedPane.getSelectedIndex();
			desc = tabbedPane.getTitleAt(index);
			HelpTextPane pane = getActiveHelpPane();
			URL url = pane.getPage();
			if(null==url)
				urlToNavigate = pane.getAdditionalPageName();
			else
				urlToNavigate = url.toString(); 
		}
		return createNewTab(urlToNavigate, desc);
	}
	
	public HelpTextPane createNewTab(String urlToNavigate, String desc)
	{
		final HelpTextPane pane = new HelpTextPane(manager, this);
		final Component framing = pane.getScrollFraming();
		//final Component framing = pane;
		tabbedPane.add(framing); 
		pane.addDescriptionChangeListener(new HelpTextPane.DescriptionChangeListener()
		{
			@Override
			public void descriptionChanged(String newDesc)
			{
				String desc = newDesc;
				if(null==desc || desc.isEmpty())
					desc = "Document";
				tabbedPane.setTitleAt(tabbedPane.indexOfComponent(framing), desc);
				if(pane==getActiveHelpPane())
				{
					if(pane.getPage()==null)
					{
						desc = pane.getAdditionalPageName();
					}
					else
					{
						desc = pane.getPage().toString();
					}
				}
				urlPath.setText(desc);
				updateUI();
			}
		});
		
		if(urlToNavigate!=null && desc!=null)
		{
			pane.safeNavigate(urlToNavigate.toString(), desc);
		}
		return pane;
	}
	
	public void closeCurrentTab()
	{
		if(tabbedPane.getTabCount()>1)
		{
			tabbedPane.remove(tabbedPane.getSelectedIndex());
		}
	}
	
	public void navigateBackward()
	{
		HelpTextPane curPane = getActiveHelpPane();
		curPane.navigateBackward();
		
		String page = curPane.getAdditionalPageName();
		urlPath.setText(page);
	}
	
	public void navigateForward()
	{
		HelpTextPane curPane = getActiveHelpPane();
		curPane.navigateForward();
		
		String page = curPane.getAdditionalPageName();
		urlPath.setText(page);
	}
	
	public void findNext()
	{
		HelpTextPane curPane = getActiveHelpPane();
		curPane.findNext();
	}
	
	public void findPrevious()
	{
		HelpTextPane curPane = getActiveHelpPane();
		curPane.findPrevious();
	}
	
	public void findCurrent()
	{
		HelpTextPane curPane = getActiveHelpPane();
		curPane.findCurrent();
	}
	
	protected HelpTextPane getActiveHelpPane()
	{
		if(tabbedPane.getTabCount()==0)
			createNewTab();
		Component c = tabbedPane.getSelectedComponent();
		return HelpTextPane.getFromFraming(c);
		//return (HelpTextPane)c;
	}
	
	public void safeNavigate(String url, String desccription)
	{
		urlPath.setText(url);
		getActiveHelpPane().safeNavigate(url, desccription);
	}
	
	public void safeNavigate(String url)
	{
		urlPath.setText(url);
		getActiveHelpPane().safeNavigate(url, "");
	}
	
	public void showDebugInfo()
	{
		getActiveHelpPane().showHistoryForDebug();
	}
	
	public void safeNavigate(URL url, String desccription)
	{
		safeNavigate(url.toString(), desccription);
	}
	
	protected class NavigateOnEnterKeyListener extends KeyAdapter
	{
		@Override
		public void keyPressed(KeyEvent e)
		{
			if(e.getKeyCode()==KeyEvent.VK_ENTER)
			{
				String url = urlPath.getText();
				if(!url.isEmpty())
				{
					int index0 = url.lastIndexOf('/');
					int index1 = url.lastIndexOf('\\');
					int index = Math.max(index0, index1);
					
					String desc = url;
					if(index>-1)
					{
						String sub = url.substring(index);
						if(sub.isEmpty())
						{
							desc = sub;
						}
					}

					safeNavigate(url, desc);
				}
			}
		}
	}
	
	@SuppressWarnings("serial")
	public Action getCloseTabAction()
	{
		Action act = new AbstractAction("Close tab")
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				closeCurrentTab();
			}
			
		};
		
		return act;
	}
	
	@SuppressWarnings("serial")
	public Action getCreateNewTabAction()
	{
		Action act = new AbstractAction("New tab")
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				createNewTab();
			}
			
		};
		
		return act;
	}
	
	@SuppressWarnings("serial")
	public Action getNavigateForwardAction()
	{
		Action act = new AbstractAction("> Forward")
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				navigateForward();
			}
			
		};
		
		return act;
	}
	
	@SuppressWarnings("serial")
	public Action getNavigateBackAction()
	{
		Action act = new AbstractAction("< Back ")
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				navigateBackward();
			}
			
		};
		
		return act;
	}
	
	@SuppressWarnings("serial")
	public Action getFindNextAction()
	{
		Action act = new AbstractAction("Find Next")
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				findNext();
			}
			
		};
		
		return act;
	}
	
	@SuppressWarnings("serial")
	public Action getFindPreviousAction()
	{
		Action act = new AbstractAction("Find Previous")
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				findPrevious();
			}
		};
		
		return act;
	}
	
	@SuppressWarnings("serial")
	public Action getFindAction()
	{
		Action act = new AbstractAction("Find in document")
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				findCurrent();
			}
		};
		
		return act;
	}
	
	protected JTabbedPaneWithCloseIcons tabbedPane = new JTabbedPaneWithCloseIcons(JTabbedPane.TOP, JTabbedPane.SCROLL_TAB_LAYOUT);
	protected JTextField urlPath = new JTextField();
	private HtmlJavaDocIOManager manager;
}