import java.awt.*;
import javax.swing.*;
import javax.swing.table.*;

/**
 * This class is used to render a JProgressBar in our downloads JTable
 *
 * @since 2021.1.6
 */
class ProgressRenderer extends JProgressBar implements TableCellRenderer {

    /**
     * A constructor to create a new ProgressRenderer
     * @param min minimum value of progress bar
     * @param max maximum value of progress bar
     */
    public ProgressRenderer(int min, int max) {
        super(min, max);
    }

    @Override
    public Component getTableCellRendererComponent(JTable jTable, Object o, boolean b, boolean b1, int i, int i1) {
        int value = (int) ((Float) o).floatValue();
        setValue(value);
        return this;
    }
}
