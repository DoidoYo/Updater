import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.Locale;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;

import com.dropbox.core.DbxAppInfo;
import com.dropbox.core.DbxAuthFinish;
import com.dropbox.core.DbxClient;
import com.dropbox.core.DbxEntry;
import com.dropbox.core.DbxException;
import com.dropbox.core.DbxRequestConfig;
import com.dropbox.core.DbxWebAuthNoRedirect;

public class Updater {
	
	private static final String APP_KEY = "tcp2wrmvnh7krkl";  
	private static final String APP_SECRET = "026il9q5zlwvh31";  
	private static final String accessToken = "Kekqp91K7bAAAAAAAAAADOvvtOtAz5J4R0kfWky-YEJSnfzIxyJX9pEyDGkvIXw1";
	
	File configFile;
	DbxClient client;
	
	DbxEntry latest_version;
	
	JProgressBar progressBar;
	
	public static void main(String[] args) throws IOException, DbxException {
		new Updater();
	}
	
	public Updater() throws IOException, DbxException {
		
		DbxAppInfo appInfo = new DbxAppInfo(APP_KEY, APP_SECRET);

        DbxRequestConfig config = new DbxRequestConfig(
            "JavaTutorial/1.0", Locale.getDefault().toString());
        DbxWebAuthNoRedirect webAuth = new DbxWebAuthNoRedirect(config, appInfo);
        
        client = new DbxClient(config, accessToken);
        System.out.println("Linked account: " + client.getAccountInfo().displayName);
        
        DbxEntry.WithChildren listing = client.getMetadataWithChildren("/");
        System.out.println("Files in the root path:");
        
        latest_version = null;
        int latest_int = 0;
        
        for (DbxEntry child : listing.children) {
            System.out.println("	" + child.name + ": " + child.toString());
            String s = child.name.replace(".", "");
            
            int i = Integer.parseInt(s);
            
            if(i > latest_int) {
            	latest_version = child;
            	latest_int = i;
            }
        }
        
        //read current version
        configFile = new File("config");
        if(!configFile.exists()) {
    		configFile.createNewFile();
    		BufferedWriter writer = new BufferedWriter(new FileWriter(configFile));
    		writer.write("version: 0.0.0");
    		writer.flush();
    		writer.close();
    	}
        BufferedReader br = new BufferedReader(new FileReader("config"));
        
        int current_int = 0;
        String current_string = "";
        
        try {
            String line = br.readLine();
            
            String string = line.substring(line.indexOf(":")+1, line.length()); 
            string = string.replace(" ", "");
            current_string = string;
            string = string.replace(".", "");
            
            current_int = Integer.parseInt(string);
            
        } finally {
            br.close();
        }
        
        //setVersion("0.0.0");
        
        //see if new version is greater than current
        //if(latest_int > current_int) {
        if(latest_int > current_int) {
        	
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
    		
    		JLabel label2 = new JLabel("New Version: " + latest_version.name);
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
					
					//change config version
		        	try {
						setVersion(latest_version.name);
					} catch (IOException e1) {
						e1.printStackTrace();
					}
					//download files 
		        	progressBar.setString("Couting Files...");
		        	deleteDirectory(new File("application"));
		        	//get total files
		        	countFilesInDropBoxDir("/" + latest_version.name);
		        	progressBar.setString("0%");
		        	//set progress bar
		        	progressBar.setMaximum(total_files);
		        	
		        	downloadFolder("/" + latest_version.name, "application");
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
        
        //open normal program
	}
	
	public void setVersion(String v) throws IOException {
		//change config version
    	File tempConfigFile = new File("tempConfig");
    	
    	BufferedReader reader = new BufferedReader(new FileReader(configFile));
    	BufferedWriter writer = new BufferedWriter(new FileWriter(tempConfigFile));
    	
    	String currentLine;
    	
    	while((currentLine = reader.readLine()) != null) {
    	    //System.out.println(currentLine);
    	    String trimmedLine = currentLine.trim();
    	    if(trimmedLine.contains("version")) {
    	    	writer.write("version: " + v + System.getProperty("line.separator"));
    	    } else {
    	    	writer.write(currentLine + System.getProperty("line.separator"));
    	    }
    	}
    	
    	writer.flush();
    	writer.close(); 
    	reader.close(); 
    	configFile.delete();
    	tempConfigFile.renameTo(new File("config"));
	}
	
	int total_files = 0;
	int progress_files = 0;
	
	public void countFilesInDropBoxDir(String path) {
		int i = 0;
		try {
	        for (DbxEntry child : client.getMetadataWithChildren(path).children) {
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
	
	public void downloadFolder(String path, String destination) {
		new File(destination).mkdirs();
	    try {
	        for (DbxEntry child : client.getMetadataWithChildren(path).children) {
	            if (child instanceof DbxEntry.Folder) {
	                // recurse
	                downloadFolder(path + "/" + child.name, destination + "/" + child.name);
	            } else if (child instanceof DbxEntry.File) {
	                // download an individual file
	                OutputStream outputStream = new FileOutputStream(
	                    destination + "/" + child.name);
	                try {
	                    DbxEntry.File downloadedFile = client.getFile(
	                        path + "/" + child.name, null, outputStream);
	                } finally {
	                    outputStream.close();
	                }
	                progress_files++;
	                progressBar.setValue(progress_files);
	                progressBar.setString(((progress_files/total_files)*100) + "%");
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
