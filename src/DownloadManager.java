import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Observable;
import java.util.Observer;

/**
 * This class is the GUI for our download Manager, and manages general
 * aspects of the program
 *
 * @since 2021.1.6
 */
public class DownloadManager extends JFrame implements Observer {

    //buttons for downloading, pausing, resuming, removing and canceling downloads
    private JButton pauseButton, cancelButton, removeButton, resumeButton, downloadButton;
    //a text field to enter url addresses
    private JTextField urlEntryField;
    private DownloadsTableModel tableModel;
    private JTable downloadsTable;
    //selected download on the table
    private Download currentDownload;
    private boolean clearing;

    /**
     * A constructor to create a new Download Manager
     */
    public DownloadManager(){
        super();
        setTitle("Download Manager!");
        setSize(new Dimension(800, 300));
        setResizable(false);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        clearing = false;

        JPanel mainPanel = new JPanel(new BorderLayout());

        ActionHandler handler = new ActionHandler();

        pauseButton = new JButton("Pause");
        resumeButton = new JButton("Resume");
        removeButton = new JButton("Remove");
        cancelButton = new JButton("Cancel");
        downloadButton = new JButton("Download");

        pauseButton.addActionListener(handler);
        downloadButton.addActionListener(handler);
        removeButton.addActionListener(handler);
        resumeButton.addActionListener(handler);
        cancelButton.addActionListener(handler);

        urlEntryField = new JTextField(50);

        tableModel = new DownloadsTableModel();
        downloadsTable = new JTable(tableModel);
        downloadsTable.getSelectionModel().addListSelectionListener(e -> tableSelectionChanged());
        downloadsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        ProgressRenderer renderer = new ProgressRenderer(0, 100);
        renderer.setStringPainted(true);
        downloadsTable.setDefaultRenderer(JProgressBar.class, renderer);

        JPanel northPanel = new JPanel();
        northPanel.add(urlEntryField);
        northPanel.add(downloadButton);

        JScrollPane scrollPane = new JScrollPane(downloadsTable);
        TitledBorder border = BorderFactory.createTitledBorder("Downloads");
        scrollPane.setBorder(border);

        JPanel southPanel = new JPanel();
        southPanel.add(pauseButton);
        southPanel.add(removeButton);
        southPanel.add(cancelButton);
        southPanel.add(resumeButton);


        mainPanel.add(northPanel, BorderLayout.NORTH);
        mainPanel.add(scrollPane, BorderLayout.CENTER);
        mainPanel.add(southPanel, BorderLayout.SOUTH);

        setContentPane(mainPanel);
        setVisible(true);
    }

    /**
     * A method to update state of buttons due to
     * current state of download process
     */
    private void updateButtons() {
        if (currentDownload != null) {
            String state = currentDownload.getState();
            if(state.equals("Downloading")){
                pauseButton.setEnabled(true);
                resumeButton.setEnabled(false);
                cancelButton.setEnabled(true);
                removeButton.setEnabled(false);
            }
            else if(state.equals("Paused")){
                pauseButton.setEnabled(false);
                resumeButton.setEnabled(true);
                cancelButton.setEnabled(true);
                removeButton.setEnabled(false);
            }
            else if(state.equals("Error")){
                pauseButton.setEnabled(false);
                resumeButton.setEnabled(true);
                cancelButton.setEnabled(false);
                removeButton.setEnabled(true);
            }
            else if(state.equals("Complete") || state.equals("Cancelled")){
                pauseButton.setEnabled(false);
                resumeButton.setEnabled(false);
                cancelButton.setEnabled(false);
                removeButton.setEnabled(true);
            }

        } else {
            pauseButton.setEnabled(false);
            resumeButton.setEnabled(false);
            cancelButton.setEnabled(false);
            removeButton.setEnabled(false);
        }
    }

    /**
     * A method to perform suitable actions
     * when selction of a row in table changes
     */
    private void tableSelectionChanged() {
        if (currentDownload != null)
            currentDownload.deleteObserver(DownloadManager.this);
        if (!clearing) {
            currentDownload = tableModel.getDownload(downloadsTable.getSelectedRow());
            currentDownload.addObserver(DownloadManager.this);
            updateButtons();
        }
    }

    @Override
    public void update(Observable observable, Object o) {
        updateButtons();
    }

    /**
     * A method to build a ftp url
     * @param url url to be converted
     * @param username username to access to server
     * @param password password to access to server
     * @return ftp url
     * @throws MalformedURLException
     */
    public URL getFTPUrl(URL url, String username, String password) throws MalformedURLException {
        String ftpUrl = String.format("ftp://%s:%s@%s/%s;type=i", username, password, url.getHost(), url.getFile());
        return new URL(ftpUrl);
    }

    /**
     * This handles ActionEvents in Download Manager
     */
    private class ActionHandler implements ActionListener{

        @Override
        public void actionPerformed(ActionEvent actionEvent) {
            if(actionEvent.getSource() == downloadButton){
                String url = urlEntryField.getText();
                URL verifiedUrl = null;
                try {
                    verifiedUrl = new URL(url);
                    if(!(verifiedUrl.getProtocol().equals("http") || verifiedUrl.getProtocol().equals("https") ||
                            verifiedUrl.getProtocol().equals("ftp"))){
                        JOptionPane.showMessageDialog(null, "Invalid URL");
                        return;
                    }

                } catch (MalformedURLException e) {
                    e.printStackTrace();
                }
                if (verifiedUrl != null) {
                    if(verifiedUrl.getProtocol().equals("ftp")){
                        String username = JOptionPane.showInputDialog(null,"Enter Username");
                        String password = JOptionPane.showInputDialog(null, "Enter Username");
                        try {
                            tableModel.addDownload(new Download(getFTPUrl(verifiedUrl, username, password)));
                        } catch (MalformedURLException e) {
                            urlEntryField.setText("");
                            return;
                        }
                    }
                    else {
                        tableModel.addDownload(new Download(verifiedUrl));
                    }
                    urlEntryField.setText("");
                } else {
                    JOptionPane.showMessageDialog(null,
                            "Invalid Download URL", "Error",
                            JOptionPane.ERROR_MESSAGE);
                }
            }
            else if(actionEvent.getSource() == pauseButton){
                currentDownload.changeState("Paused");
            }
            else if(actionEvent.getSource() == removeButton){
                clearing = true;
                tableModel.removeDownload(downloadsTable.getSelectedRow());
                clearing = false;
                currentDownload = null;
            }
            else if(actionEvent.getSource() == cancelButton){
                currentDownload.changeState("Cancelled");
            }
            else if(actionEvent.getSource() == resumeButton){
                currentDownload.changeState("Resume");
            }
            updateButtons();
        }


    }






    /**
     * main method to run program
     */
    public static void main(String[] args) {
        DownloadManager manager = new DownloadManager();
        manager.setVisible(true);
    }
}
