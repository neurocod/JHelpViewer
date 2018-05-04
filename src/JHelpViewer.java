package jhelpviewer;

public class JHelpViewer
{
	public static void main(String[] args)
	{
		final HtmlJavaDocIOManager manager = new HtmlJavaDocIOManager();
		
		JHelpViewerFrame mainFrame = new JHelpViewerFrame(manager, "Java Help Viewer");
		mainFrame.setDefaultCloseOperation(JHelpViewerFrame.EXIT_ON_CLOSE);		
		mainFrame.setVisible(true);
	}
}
