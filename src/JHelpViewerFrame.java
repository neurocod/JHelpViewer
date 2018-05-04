/**
 * 
 */
package jhelpviewer;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.HeadlessException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JSplitPane;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;

/**
 * @author neurocod
 * 
 */
	
public class JHelpViewerFrame extends JFrame
{
	/**
	 * @param title
	 * @throws HeadlessException
	 */
	public JHelpViewerFrame(final HtmlJavaDocIOManager docIOManager, String title) throws HeadlessException
	{
		super(title);
		this.docIOManager = docIOManager;
		this.docIndex = docIOManager.getHtmlJavaDocIndex();
		
		//must be before creation of index - it will wait until index loaded
		docIOManager.addLoadDocIndexListener(myDocIndexListener);
		docIOManager.loadKeywordsIndexAndContinueInAnoterThread();
		
		paneMain = new JHelpTabs(docIOManager);
		indexPanel = new JHelpIndexPanel(docIOManager);
		
		setSize(100, 100);
		setPreferredSize(new Dimension(450, 130));
		setExtendedState(JHelpViewerFrame.MAXIMIZED_BOTH);
		
		createMenu();
		createToolbar();
		paneMain.safeNavigate("file:///F:/Java/docs/api/overview-summary.html");
		//api\index-files\index-27.html
		//paneMain.safeNavigate("file:///F:/Java/docs/api/index.html");

		JSplitPane mainSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, true);
		mainSplit.setOneTouchExpandable(true);
		
		//mainSplit.setLeftComponent(smallSplit);
		
		indexPanel.addDocIndexNameListener(new DocIndexNameListener()
		{
			@Override
			public void navaigate(String name)
			{
				paneMain.safeNavigate(HtmlJavaDocIndex.docIndexStartWhenInterop+name, "");
			}

			@Override
			public void navaigateUrl(String url)
			{
				assert(false);
			}
		});
		mainSplit.setLeftComponent(indexPanel);
		mainSplit.setRightComponent(paneMain);
		mainSplit.setDividerLocation(0.5);
		
		this.add(mainSplit);
	}

	private void createToolbar()
	{
		JButton btnBack = new JButton(paneMain.getNavigateBackAction());
		toolBar.add(btnBack);
		
		JButton btnFwd = new JButton(paneMain.getNavigateForwardAction());
		toolBar.add(btnFwd);
		
		JButton btnNewTab = new JButton(paneMain.getCreateNewTabAction());
		toolBar.add(btnNewTab);
		
		JButton btnCloseTab = new JButton(paneMain.getCloseTabAction());
		toolBar.add(btnCloseTab);

		JButton btnTest= new JButton("Some Test for debug");
		btnTest.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				paneMain.safeNavigate("some bad url", "bla bla");
			}
			
		});
		toolBar.add(btnTest);
		
		JButton btnMsg = new JButton("show debug info");
		btnMsg.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				paneMain.showDebugInfo();
			}
			
		});
		toolBar.add(btnMsg);

		//home
		//synch
		
        add(toolBar, BorderLayout.PAGE_START);
	}
	
	private void createMenu()
	{
		JMenuBar menuBar = new JMenuBar();
		
		JMenu menuFile = new JMenu("File");
		menuBar.add(menuFile);
		menuFile.setMnemonic('F');
		
			JMenuItem itemReparseDocumentation = menuFile.add(getAddDocumentationAction());
			itemReparseDocumentation.setMnemonic('R');
			itemReparseDocumentation.setAccelerator(KeyStroke.getKeyStroke( KeyEvent.VK_R, ActionEvent.CTRL_MASK) );
			
//			JMenuItem itemExit = menuFile.add(ex);
//			itemExit.setMnemonic('B');
//			itemExit.setAccelerator(KeyStroke.getKeyStroke( KeyEvent.VK_LEFT, ActionEvent.ALT_MASK) );
		
		JMenu menuDocument = new JMenu("Document");
		menuBar.add(menuDocument);
		menuDocument.setMnemonic('D');
		
			JMenuItem itemBack = menuDocument.add(paneMain.getNavigateBackAction());
			itemBack.setMnemonic('B');
			itemBack.setAccelerator(KeyStroke.getKeyStroke( KeyEvent.VK_LEFT, ActionEvent.ALT_MASK) );
			
			JMenuItem itemForward = menuDocument.add(paneMain.getNavigateForwardAction());
			itemForward.setMnemonic('F');
			itemForward.setAccelerator(KeyStroke.getKeyStroke( KeyEvent.VK_RIGHT, ActionEvent.ALT_MASK) );
			
			JMenuItem itemNewTab = menuDocument.add(paneMain.getCreateNewTabAction());
			itemNewTab.setMnemonic('N');
			itemNewTab.setAccelerator(KeyStroke.getKeyStroke( KeyEvent.VK_T, ActionEvent.CTRL_MASK) );
			
			JMenuItem itemCloseTab = menuDocument.add(paneMain.getCloseTabAction());
			itemCloseTab.setMnemonic('C');
			itemCloseTab.setAccelerator(KeyStroke.getKeyStroke( KeyEvent.VK_W, ActionEvent.CTRL_MASK) );
		
		JMenu menuEdit = new JMenu("Edit");
		menuBar.add(menuEdit);
		menuEdit.setMnemonic('E');
		
			JMenuItem itemFind = menuEdit.add(paneMain.getFindAction());
			itemFind.setMnemonic('F');
			itemFind.setAccelerator(KeyStroke.getKeyStroke( KeyEvent.VK_F, ActionEvent.CTRL_MASK) );
			
			JMenuItem itemFindNext = menuEdit.add(paneMain.getFindNextAction());
			itemFindNext.setMnemonic('N');
			itemFindNext.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F3, 0) );
			itemFindNext.setDisplayedMnemonicIndex(5);
			
			JMenuItem itemFindPrevious = menuEdit.add(paneMain.getFindPreviousAction());
			itemFindPrevious.setMnemonic('P');
			itemFindPrevious.setAccelerator(KeyStroke.getKeyStroke( KeyEvent.VK_F3, ActionEvent.SHIFT_MASK) );
			
			JMenuItem itemSearchGoogle = menuEdit.add(indexPanel.getSearchSunByGoogleAction());
			itemSearchGoogle.setMnemonic('G');
			itemSearchGoogle.setAccelerator(KeyStroke.getKeyStroke( KeyEvent.VK_G, ActionEvent.CTRL_MASK) );

		setJMenuBar(menuBar);
	}
	
	public void addDocumentation()
	{
		JParseDialog dlg = new JParseDialog(this, docIOManager);
		dlg.setVisible(true);
	}
	
	@SuppressWarnings("serial")
	public Action getAddDocumentationAction()
	{
		return new AbstractAction("Add documentation")
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				addDocumentation();
			}
		};
	}

	private JToolBar toolBar = new JToolBar("main toolbar");
	private static final long serialVersionUID = 8270959676771898291L;

	private JHelpTabs paneMain;
	private HtmlJavaDocIndex docIndex;
	private JHelpIndexPanel indexPanel;
	private HtmlJavaDocIOManager docIOManager;
	private LoadDocIndexListener myDocIndexListener = new LoadDocIndexListenerAdapter()
	{
		@Override
		public void loadIndexError(String description)
		{
			log("loadIndexError: ");
			log(description+'\n');
			EventQueue.invokeLater(new Runnable()
			{
				@Override
				public void run()
				{
					addDocumentation();
				}
			});
		}
		
		@Override
		public void loadIndexSuccess()
		{
			log("loadIndexSuccess\n");
			docIOManager.saveIfNotEmpty();
		}
		
		@Override
		public void loadNotify(String description)
		{
			log("loadNotify: ");
			log(description+'\n');
		}

		@Override
		public void indexChanged()
		{
			EventQueue.invokeLater(new Runnable()
			{
				@Override
				public void run()
				{
					if(null!=indexPanel)
						indexPanel.updateIndex();
				}
			});
		}
	};
}

//all classes: "file:///F:/Java/docs/api/allclasses-frame.html"
//format:
//<A HREF="java/security/cert/CertificateFactorySpi.html" title="class in java.security.cert" target="classFrame">CertificateFactorySpi</A>

//all packets: "file:///F:/Java/docs/api/overview-frame.html"
//format:
//<A HREF="java/rmi/activation/package-frame.html" target="packageFrame">java.rmi.activation</A>