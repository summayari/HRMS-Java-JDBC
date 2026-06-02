package hrms.gui;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

/** Expense Tracking panel for Accountant role. */
public class ExpenseTrackingPanel extends JPanel {
    public ExpenseTrackingPanel() {
        setLayout(new BorderLayout());
        setBackground(Theme.BG);
        add(Theme.pageHeader("🧾","Expense Tracking","Monitor and record company expenses",Theme.ACCOUNTANT_COLOR),BorderLayout.NORTH);

        JPanel center=new JPanel(new BorderLayout(0,14));
        center.setBackground(Theme.BG);
        center.setBorder(BorderFactory.createEmptyBorder(14,20,20,20));

        // Summary cards
        JPanel sumRow=new JPanel(new GridLayout(1,4,12,0)); sumRow.setBackground(Theme.BG);
        sumRow.add(Theme.statCard("Salaries",    "₨2.4M",  Theme.ACCOUNTANT_COLOR));
        sumRow.add(Theme.statCard("Bonuses",     "₨120K",  Theme.ACCENT));
        sumRow.add(Theme.statCard("Overtime",    "₨45K",   Theme.INFO));
        sumRow.add(Theme.statCard("Deductions",  "₨85K",   Theme.DANGER));

        // Expense table (placeholder)
        String[] cols={"#","Category","Description","Amount","Date","Recorded By","Status"};
        DefaultTableModel m=new DefaultTableModel(cols,0){public boolean isCellEditable(int r,int c){return false;}};
        m.addRow(new Object[]{"1","Payroll","March Salaries","₨2,400,000","Mar 2025","accountant","Processed"});
        m.addRow(new Object[]{"2","Bonuses","Q1 Performance Bonus","₨120,000","Mar 2025","accountant","Processed"});
        m.addRow(new Object[]{"3","Overtime","March Overtime Pay","₨45,000","Mar 2025","accountant","Processed"});
        m.addRow(new Object[]{"4","Utilities","Office Utilities","₨22,000","Mar 2025","admin","Pending"});

        JTable t=new JTable(m); Theme.styleTable(t);
        JPanel card=Theme.card("📋 Expense Ledger");

        JPanel btnRow=new JPanel(new FlowLayout(FlowLayout.LEFT,10,0)); btnRow.setBackground(Theme.CARD_BG);
        btnRow.add(Theme.accentBtn("+ Add Expense"));
        btnRow.add(Theme.infoBtn("📤 Export"));
        JPanel wrap=new JPanel(new BorderLayout(0,8)); wrap.setBackground(Theme.CARD_BG);
        wrap.add(btnRow,BorderLayout.NORTH); wrap.add(Theme.scrollPane(t),BorderLayout.CENTER);
        card.add(wrap,BorderLayout.CENTER);

        center.add(sumRow,BorderLayout.NORTH);
        center.add(card,BorderLayout.CENTER);
        add(center,BorderLayout.CENTER);
    }
}
