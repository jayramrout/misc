import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import com.jsonviewer.JSONViewer;

/**
 * @author Jayram Rout
 *
 */
public class MainJSONViewer {

	public static void main(String[] args) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				UIManager.put("swing.boldMetal", Boolean.TRUE);
				try {
					UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
				} catch (Exception exp) {
					exp.printStackTrace();
				}
				new JSONViewer();
			}
		});
	}
}