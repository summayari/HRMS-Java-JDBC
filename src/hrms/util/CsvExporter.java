package hrms.util;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/** Exports any JTable / DefaultTableModel to a CSV file. */
public class CsvExporter {

    public static void export(JComponent parent, DefaultTableModel model, String defaultName) {
        JFileChooser fc = new JFileChooser();
        fc.setSelectedFile(new File(defaultName + "_"
            + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmm")) + ".csv"));
        if (fc.showSaveDialog(parent) != JFileChooser.APPROVE_OPTION) return;

        File file = fc.getSelectedFile();
        if (!file.getName().endsWith(".csv"))
            file = new File(file.getAbsolutePath() + ".csv");

        try (PrintWriter pw = new PrintWriter(new FileWriter(file))) {
            // Header
            StringBuilder sb = new StringBuilder();
            for (int c = 0; c < model.getColumnCount(); c++) {
                if (c > 0) sb.append(",");
                sb.append(quote(model.getColumnName(c)));
            }
            pw.println(sb);

            // Rows
            for (int r = 0; r < model.getRowCount(); r++) {
                sb.setLength(0);
                for (int c = 0; c < model.getColumnCount(); c++) {
                    if (c > 0) sb.append(",");
                    Object val = model.getValueAt(r, c);
                    sb.append(quote(val == null ? "" : val.toString()));
                }
                pw.println(sb);
            }

            JOptionPane.showMessageDialog(parent,
                "Exported " + model.getRowCount() + " rows to:\n" + file.getAbsolutePath(),
                "Export Successful", JOptionPane.INFORMATION_MESSAGE);
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(parent,
                "Export failed: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private static String quote(String s) {
        if (s.contains(",") || s.contains("\"") || s.contains("\n"))
            return "\"" + s.replace("\"", "\"\"") + "\"";
        return s;
    }
}
