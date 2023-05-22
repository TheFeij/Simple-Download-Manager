import java.util.*;
import javax.swing.*;
import javax.swing.table.*;

/**
 * This class represents download'es table and
 * manages its data
 *
 * @since 2021.1.6
 */
class DownloadsTableModel extends AbstractTableModel implements Observer {

    // Titles of the table columns.
    private static final String[] tableColumns = {"URL", "Size", "Progress","Status"};
    // classes of the table columns.
    private static final Class[] columnTypes = {String.class, String.class, JProgressBar.class, String.class};
    // a list of all downloads.
    private ArrayList<Download> downloads= new ArrayList<>();



    //getters:
    public Download getDownload(int row) {
        return downloads.get(row);
    }
    public int getColumnCount() {
        return tableColumns.length;
    }
    public String getColumnName(int col) {
        return tableColumns[col];
    }
    public Class getColumnClass(int col) {
        return columnTypes[col];
    }
    public int getRowCount() {
        return downloads.size();
    }
    ////////////////////////////////////////////////////////////////////

    /**
     * A method to add a new download to the table
     * @param download download to be added
     */
    public void addDownload(Download download) {
        download.addObserver(this);
        downloads.add(download);
        fireTableRowsInserted(getRowCount(), getRowCount());
    }

    /**
     * A method to remove a download from the list
     * @param row row number of the download to be deleted
     */
    public void removeDownload(int row) {
        downloads.remove(row);
        fireTableRowsDeleted(row, row);
    }

    /**
     * A method to get value of a specific position in the table
     * @param row row of position
     * @param col column of the position
     * @return value at the specified position
     */
    public Object getValueAt(int row, int col) {

        Download download = downloads.get(row);
        switch (col) {
            case 0:
                return download.getUrl();
            case 1:
                if(download.getSize() == -1)
                    return "";
                else
                    return String.format("%.2fMB", (float)(download.getSize())/1048576);  //1048576B == 1MB
            case 2:
                return download.getProgress();
            case 3:
                return download.getState();
        }
        return "";
    }

    @Override
    public void update(Observable o, Object arg) {
        int index = downloads.indexOf(o);
        fireTableRowsUpdated(index, index);
    }
}