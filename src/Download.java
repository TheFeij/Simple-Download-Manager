import java.io.*;
import java.net.*;
import java.util.*;

/**
 * This class represents a download, it downloads a file from a url address
 *
 * @since 2021.1.6
 */
class Download extends Observable implements Runnable {

    // download URL
    private URL urlAddress;
    // size of download in bytes
    private long downloadSize;
    // number of bytes downloaded
    private long downloadedData;
    // current state of download process
    private String currentState;

    /**
     * A constructor to create a new Download Object
     * @param urlAddress url address to be downloaded
     */
    public Download(URL urlAddress) {
        this.downloadSize = -1;
        this.downloadedData = 0;
        this.urlAddress = urlAddress;
        this.currentState = "Downloading";
        download();
    }

    //Getters:
    public String getUrl() {
        return urlAddress.toString();
    }
    public long getSize() {
        return downloadSize;
    }
    public float getProgress() {
        return ((float) downloadedData / downloadSize) * 100;
    }
    public String getState() {
        return currentState;
    }
    ////////////////////////////////////////////////////////

    /**
     * A method to change state of the download
     * @param newState new state to be set
     */
    public void changeState(String newState){
        switch (newState) {
            case "Resume":
                currentState = "Downloading";
                notifyChanges();
                download();
                break;
            case "Paused":
                currentState = "Paused";
                break;
            case "Error":
                currentState = "Error";
                break;
            case "Cancelled":
                currentState = "Cancelled";
                break;
        }
        notifyChanges();
    }

    /**
     * A method to set preparations to start or resume a download
     */
    private void download() {
        Thread downloadThread = new Thread(this);
        downloadThread.start();
    }

    /**
     * A method to connect to the server and receive file
     */
    public void run() {

        RandomAccessFile downloadFile = null;
        InputStream downloadStream = null;
        String protocol = urlAddress.getProtocol();

        if(protocol.equals("ftp")){
            try {
                URLConnection connection = urlAddress.openConnection();
                connection.setRequestProperty("Range", "bytes=" + downloadedData + "-");
                downloadStream = connection.getInputStream();

                int contentLength = connection.getContentLength();
                if (contentLength <= 0) {
                    changeState("Error");
                }

                if (downloadSize == -1) {
                    downloadSize = contentLength;
                    notifyChanges();
                }

                String fileName = urlAddress.getFile().substring(urlAddress.getFile().lastIndexOf('/') + 1,
                        urlAddress.getFile().lastIndexOf(';'));
                downloadFile = new RandomAccessFile(fileName, "rw");
                downloadFile.seek(downloadedData);

            } catch (IOException e) {
                changeState("Error");
            }
        }
        else if(protocol.equals("http") || protocol.equals("https")){

            try {
                HttpURLConnection connection = (HttpURLConnection) urlAddress.openConnection();
                connection.setRequestProperty("Range", "bytes=" + downloadedData + "-");
                connection.connect();

                if (connection.getResponseCode() / 100 != 2) {
                    changeState("Error");
                }

                int contentLength = connection.getContentLength();
                if (contentLength <= 0) {
                    changeState("Error");;
                }

                if (downloadSize == -1) {
                    downloadSize = contentLength;
                    notifyChanges();
                }

                String fileName = urlAddress.getFile().substring(urlAddress.getFile().lastIndexOf('/') + 1);
                downloadFile = new RandomAccessFile(fileName, "rw");
                downloadFile.seek(downloadedData);

                downloadStream = connection.getInputStream();

            } catch (Exception e) {
                changeState("Error");
            }
        }

        try {
            while (currentState.equals("Downloading")) {
                byte[] buffer;
                if (downloadSize - downloadedData > 104857) {
                    buffer = new byte[104857];
                } else {
                    buffer = new byte[(int)(downloadSize - downloadedData)];
                }
                int read = downloadStream.read(buffer);
                if (read == -1 || read == 0)
                    break;
                downloadFile.write(buffer, 0, read);

                downloadedData += read;
                notifyChanges();
            }

            if (currentState.equals("Downloading")) {
                currentState = "Complete";
                notifyChanges();
            }
        } catch (IOException e) {
            changeState("Error");
        }finally {
            if (downloadFile != null) {
                try {
                    downloadFile.close();
                } catch (Exception ignored) {}
            }
            if (downloadStream != null) {
                try {
                    downloadStream.close();
                } catch (Exception ignored) {}
            }
        }

    }

    /**
     * A method to notify changes to observers
     */
    private void notifyChanges() {
        setChanged();
        notifyObservers();
    }
}