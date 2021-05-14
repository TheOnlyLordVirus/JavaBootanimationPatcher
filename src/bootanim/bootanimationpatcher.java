package bootanim;

import java.awt.EventQueue;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JButton;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

public class bootanimationpatcher 
{
	private static final bootanimationpatcher singletonWindowInstance = new bootanimationpatcher();
	private JFrame frame;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) 
	{
		// Entrypoint.
	}

	/**
	 * Create the application.
	 */
	public bootanimationpatcher() 
	{
		EventQueue.invokeLater(new Runnable() 
		{
			public void run() 
			{
				try 
				{
					singletonWindowInstance.frame.setVisible(true);
				} 
				
				catch (Exception e) 
				{
					e.printStackTrace();
				}
			}
		});
		
		initialize();
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() 
	{
		frame = new JFrame();
		frame.setBounds(100, 100, 450, 300);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		JButton btnNewButton = new JButton("Choose Video File");
		GroupLayout groupLayout = new GroupLayout(frame.getContentPane());
		groupLayout.setHorizontalGroup(
			groupLayout.createParallelGroup(Alignment.LEADING)
				.addGroup(groupLayout.createSequentialGroup()
					.addGap(153)
					.addComponent(btnNewButton, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
					.addGap(164))
		);
		groupLayout.setVerticalGroup(
			groupLayout.createParallelGroup(Alignment.LEADING)
				.addGroup(groupLayout.createSequentialGroup()
					.addGap(109)
					.addComponent(btnNewButton, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
					.addGap(129))
		);
		frame.getContentPane().setLayout(groupLayout);
		
		
		btnNewButton.addActionListener(new ActionListener() 
		{
			// On click.
			public void actionPerformed(ActionEvent e) 
			{
				editVideoBinary();
			}
		});
	}
	
	
	/**
	 * Chooses a file with a UI.
	 * @return Returns a file object of the users choice. If file isn't found for some reason an empty file object is created and returned.
	 */
	private File chooseFile()
	{
		JFileChooser chooser = new JFileChooser();

		int choice = chooser.showOpenDialog(chooser);

		if (choice == JFileChooser.APPROVE_OPTION)
		{
			return chooser.getSelectedFile();
		}
		
		else
		{
			return new File("");
		}
	}
	
	private void editVideoBinary()
	{
		// Files as byte arrays.
		byte[] templateBinary = new byte[0];
		byte[] nullBytes = new byte[16743];
		byte[] videoBinary;
		byte[] bootanimXEX;
		
		// Get template and video binary.
		try 
		{
			// Attempt to read the video file provided.
			videoBinary = Files.readAllBytes(chooseFile().toPath());

			templateBinary = Files.readAllBytes(Path.of("/bootanim/resources/template.bin"));
		} 
		
		// Show error.
		catch (IOException e) 
		{
			e.printStackTrace();
			JOptionPane.showMessageDialog(null, "Select a file!", "File read error!", JOptionPane.ERROR_MESSAGE);
			return;
		}
		
		// Write an array of null bytes that will be used to fill the remaining file space after the video file.
		for(int i = 0; i < 16743; i++)
		{
			nullBytes[i] = 0x00;
		}
		
		// Define our file size.
		int bootanimXEXsize = templateBinary.length + videoBinary.length + nullBytes.length;
		System.out.println("File size is:" + bootanimXEXsize + " bytes.");
		bootanimXEX = new byte[bootanimXEXsize];
		
			
		// Load the template bootanim.xex file to the ByteBuffer.
		ByteBuffer bb = ByteBuffer.allocate(bootanimXEXsize);
		bb.put(templateBinary);
		
		// Move to offset 0x2fc and write the length here as an Int.
		bb.position(0xf2c);
		bb.putInt(videoBinary.length);
		
		// Move to offset 0x237000 and write the video file.
		bb.position(0x237000);
		bb.put(videoBinary);
		
		// Note: Our current ByteBuffer position is directly after the videoBinarys data.
		// Fill the rest of the video file with null bytes.
		bb.put(nullBytes);
		
		// The Xbox 360 PPC Processor uses a Big Endian byte orientation, convert the bytes to this order.
		bb.order(ByteOrder.BIG_ENDIAN);
		
		// Store this raw PPC binary as a Byte Array.
		bootanimXEX = bb.array();
		
		
		// Write the file if we can. Other wise print an error.
		try (FileOutputStream fos = new FileOutputStream("bootanim.xex")) 
		{
			// Write raw bootanim.xex to the project root directory.
			fos.write(bootanimXEX);
			
			// This file must be encrypted and compressed before being written and compiled to a nand image.
		}
		
		// Error
		catch (IOException e)
		{
			e.printStackTrace();
			JOptionPane.showMessageDialog(null, "An unknown file write error has occured!", "File write error!", JOptionPane.ERROR_MESSAGE);
			return;
		}
		
		// Done!
		System.out.println("Done!");
	}
}
