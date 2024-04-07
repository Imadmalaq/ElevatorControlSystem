import javax.swing.*;
import java.awt.*;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;

public class Interface {
	 JFrame frame = new JFrame("Interface");
	 JLabel label = new JLabel();
	 String currentText = "";

	 private static Interface instance;

	 private Interface() {
		  // Create the frame
		  frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		  frame.setSize(800, 1000);
		  frame.setLayout(new GridLayout());



		  label.setVerticalAlignment(JLabel.TOP);
		  JScrollPane scrollPane = new JScrollPane(label);
		  scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);

		  // Add the label to the frame
		  frame.getContentPane().add(scrollPane);

		  // Make the frame visible
		  frame.setVisible(true);
		  frame.revalidate();
		  frame.repaint();
	 }

	 public static Interface getInstance(){
		  if (instance == null){
				instance = new Interface();
		  }
		  return instance;
	 }

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


		  frame.revalidate();
		  frame.repaint();
	 }

}
