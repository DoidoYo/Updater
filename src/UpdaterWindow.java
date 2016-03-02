import java.awt.Dimension;
import java.awt.Font;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;

import com.dropbox.core.DbxEntry;
import com.dropbox.core.DbxException;

public class UpdaterWindow {

	public static JProgressBar progressBar;
	
	static Updater up;
	
	public static void createWindow(Updater updater,String latest_version, String current_string) {
		up = updater;
		
		JFrame frame = new JFrame("Current Version: " + current_string);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		//frame.setLocationRelativeTo(null);
		frame.setResizable(false);
		frame.setSize(400, 180);
		Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
		frame.setLocation(dim.width/2-frame.getSize().width/2, dim.height/2-frame.getSize().height/2);
		frame.setVisible(true);
		
		JPanel panel = new JPanel();
		JPanel panel2 = new JPanel();
		JPanel progressPanel = new JPanel();
		JPanel buttonPanel = new JPanel();
		
		panel2.setLayout(new BoxLayout(panel2, BoxLayout.X_AXIS));
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
		
		JLabel label1 = new JLabel("An Update Has Been Found!");
		label1.setFont(new Font(label1.getFont().getName(), Font.BOLD, 20));
		label1.setAlignmentX(JComponent.CENTER_ALIGNMENT);
		
		JLabel label2 = new JLabel("New Version: " + latest_version);
		panel2.add(label2);
		
		progressBar = new JProgressBar();
		progressBar.setPreferredSize(new Dimension(350, 30));
		progressPanel.setPreferredSize(new Dimension(350, 50));
		progressBar.setStringPainted(true);
		//progressBar.setString("0%");
		progressPanel.add(progressBar);
		
		JButton yes = new JButton("Update Now");
		JButton no = new JButton("Update Later");
		buttonPanel.add(yes);
		buttonPanel.add(Box.createGlue());
		buttonPanel.add(no);
		
		
		frame.add(panel);
		
		panel.add(Box.createGlue());
		panel.add(label1);
		panel.add(panel2);
		panel.add(Box.createGlue());
		panel.add(progressPanel);
		panel.add(buttonPanel);

		
    	System.out.println("NEW VERSION FOUND!");
    	
    	yes.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				System.out.println("Yes");
				
				//update this shit!
				
				yes.setEnabled(false);
				no.setEnabled(false);
				
				Thread work = new Thread(new Runnable() {
					
					@Override
					public void run() {
						//change config version
			        	try {
							updater.setVersion(latest_version);
						} catch (IOException e1) {
							e1.printStackTrace();
						}
						//download files 
			        	progressBar.setString("Couting Files...");
			        	deleteDirectory(new File("application"));
			        	//get total files
			        	countFilesInDropBoxDir("/" + latest_version);
			        	progressBar.setString("0%");
			        	//set progress bar
			        	progressBar.setMaximum(total_files);
			        	
			        	downloadFolder("/" + latest_version, "application");
			        	frame.dispose();
						
					}
				});
				
				work.start();
			}
		});
    	
    	no.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				//System.out.println("No");
				frame.dispose();
			}
		});
	}
	
	static int total_files = 0;
	static int progress_files = 0;
	
	public static void countFilesInDropBoxDir(String path) {
		int i = 0;
		try {
	        for (DbxEntry child : up.client.getMetadataWithChildren(path).children) {
	            if (child instanceof DbxEntry.Folder) {
	                // recurse
	            	countFilesInDropBoxDir(path + "/" + child.name);
	            } else if (child instanceof DbxEntry.File) {
	            	total_files++;
	            }
	        }
	    } catch (DbxException e) {
	        System.out.println(e.getMessage());
	    }
	}
	
	public static void downloadFolder(String path, String destination) {
		new File(destination).mkdirs();
	    try {
	        for (DbxEntry child : up.client.getMetadataWithChildren(path).children) {
	            if (child instanceof DbxEntry.Folder) {
	                // recurse
	                downloadFolder(path + "/" + child.name, destination + "/" + child.name);
	            } else if (child instanceof DbxEntry.File) {
	                // download an individual file
	                OutputStream outputStream = new FileOutputStream(
	                    destination + "/" + child.name);
	                try {
	                    DbxEntry.File downloadedFile = up.client.getFile(
	                        path + "/" + child.name, null, outputStream);
	                } finally {
	                    outputStream.close();
	                }
	                progress_files++;
	                progressBar.setValue(progress_files);
	                int progress = (int)(((float)progress_files/(float)total_files)*100);
	                progressBar.setString(progress + "%");
	            }
	        }
	    } catch (DbxException e) {
	        System.out.println(e.getMessage());
	    } catch (IOException e) {
	        System.out.println(e.getMessage());
	    }
	}
	
	public static boolean deleteDirectory(File directory) {
	    if(directory.exists()){
	        File[] files = directory.listFiles();
	        if(null!=files){
	            for(int i=0; i<files.length; i++) {
	                if(files[i].isDirectory()) {
	                    deleteDirectory(files[i]);
	                }
	                else {
	                    files[i].delete();
	                }
	            }
	        }
	    }
	    return(directory.delete());
	}
	
}
