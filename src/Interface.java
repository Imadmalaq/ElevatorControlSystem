import javax.swing.*;
import java.awt.*;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;

/**
 * A singleton class that creates a graphical user interface (GUI) for displaying text updates
 * with timestamps. The class utilizes a JFrame to display a scrollable list of messages.
 *
 * @version 1.0
 * @since 2024-04-10
 * @author Humam Khalil
 * @author Imad Mohamed
 * @author Michael Rochefort
 * @author Kieran Rourke
 * @author Kyle Taticek
 */
public class Interface {
	 JFrame frame = new JFrame("Interface");
	 JLabel label = new JLabel();
	 JScrollPane scrollPane;
	 String currentText = "";

	 private static Interface instance;

	/**
	 * Private constructor to prevent instantiation from outside this class.
	 * Initializes the GUI components including a JFrame, a JScrollPane, and a JLabel
	 * for displaying text.
	 */
	 private Interface() {
		  // Create the frame
		  frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		  frame.setSize(800, 1000);
		  frame.setLayout(new GridLayout());



		  label.setVerticalAlignment(JLabel.TOP);
		  scrollPane = new JScrollPane(label);
		  scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);

		  // Add the label to the frame
		  frame.getContentPane().add(scrollPane);

		  // Make the frame visible
		  frame.setVisible(true);
		  frame.revalidate();
		  frame.repaint();
	 }

	/**
	 * Provides access to the singleton instance of the Interface class.
	 * If the instance doesn't exist, it initializes a new instance.
	 *
	 * @return The singleton instance of the Interface class.
	 */
	 public static Interface getInstance(){
		  if (instance == null){
				instance = new Interface();
		  }
		  return instance;
	 }

	/**
	 * Adds new text to the interface along with a timestamp. The new text is appended
	 * to the existing text in the interface. It ensures that the latest message is visible
	 * by scrolling to the bottom of the JScrollPane.
	 *
	 * @param newText The new text to be added to the interface.
	 */
	 public void addText (String newText) {
		  // Create a label to display text
		  String time = LocalTime.now(
								ZoneId.of( "America/Montreal" )
					 )
					 .truncatedTo(
								ChronoUnit.SECONDS
					 )
					 .toString();
		  currentText += "<br>" + "Time:" + time + " " + newText;
		  label.setText("<html>" + currentText + "</html>");

		  // Adjust the viewport to scroll to the bottom
		  JScrollBar verticalScrollBar = scrollPane.getVerticalScrollBar();
		  verticalScrollBar.setValue(verticalScrollBar.getMaximum());


		  frame.revalidate();
		  frame.repaint();
	 }

}
