package hrms.gui;

import hrms.dao.EmployeeDAO;
import hrms.dao.SalaryDAO;
import hrms.model.Employee;
import hrms.model.Salary;
import hrms.util.AuditLogger;
import hrms.util.CsvExporter;
import hrms.util.DialogHelper;
import hrms.util.SalaryCalculator;

import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.util.List;

public class SalaryPanel extends JPanel {

    private final SalaryDAO   dao    = new SalaryDAO();
    private final EmployeeDAO empDAO = new EmployeeDAO();
    private DefaultTableModel model;
    private JTable            table;
    private JTextField        searchEmpId;
    private JLabel            totalLabel;

    private static final String[] COLS = {
        "Salary ID","Emp ID","Employee","Department",
        "Basic (PKR)","Bonus","OT Pay","Tax","Deductions","Net Salary","Pay Month","Pay Date"
    };

    public SalaryPanel() {
        setLayout(new BorderLayout(0,0));
        setBackground(Theme.BG);
        searchEmpId = Theme.field(8);
        totalLabel  = new JLabel(" ");
        totalLabel.setFont(Theme.FONT_SMALL); totalLabel.setForeground(Theme.TEXT_MUTED);
        add(buildHeader(), BorderLayout.NORTH);

        model = new DefaultTableModel(COLS,0){
            @Override public boolean isCellEditable(int r,int c){ return false; }
        };
        table = new JTable(model);
        Theme.styleTable(table);
        table.getColumnModel().getColumn(9).setCellRenderer(netRenderer());
        table.getColumnModel().getColumn(0).setPreferredWidth(66);
        table.getColumnModel().getColumn(1).setPreferredWidth(55);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        JPanel center = new JPanel(new BorderLayout(0,8));
        center.setBackground(Theme.BG);
        center.setBorder(BorderFactory.createEmptyBorder(0,18,0,18));
        center.add(Theme.scrollPane(table), BorderLayout.CENTER);
        center.add(buildDeptStats(),        BorderLayout.SOUTH);
        add(center, BorderLayout.CENTER);
        add(buildActionBar(), BorderLayout.SOUTH);
        loadAll();
    }

    // ── Header ────────────────────────────────────────────────────────────────

    private JPanel buildHeader() {
        JPanel hdr = new JPanel(new BorderLayout()){
            @Override protected void paintComponent(Graphics g){
                Graphics2D g2=(Graphics2D)g.create();
                GradientPaint gp=new GradientPaint(0,0,new Color(20,83,45),getWidth(),getHeight(),Theme.BG);
                g2.setPaint(gp); g2.fillRect(0,0,getWidth(),getHeight()); g2.dispose();
            }
        };
        hdr.setOpaque(false);
        hdr.setBorder(BorderFactory.createEmptyBorder(16,20,14,20));
        JPanel left=new JPanel(new GridLayout(2,1,0,3)); left.setOpaque(false);
        JLabel title=new JLabel("💰  Salary Management");
        title.setFont(Theme.FONT_TITLE); title.setForeground(Theme.TEXT_MAIN);
        left.add(title); left.add(totalLabel);
        hdr.add(left,BorderLayout.WEST);

        JPanel right=new JPanel(new FlowLayout(FlowLayout.RIGHT,8,0)); right.setOpaque(false);
        JLabel el=new JLabel("Emp ID:"); el.setForeground(Theme.TEXT_MUTED); el.setFont(Theme.FONT_SMALL);
        JButton btnFilter=Theme.primaryBtn("Filter");
        JButton btnAll=Theme.ghostBtn("All");
        JButton btnAdd=Theme.accentBtn("＋ Add Record");
        JButton btnExp=Theme.ghostBtn("↓ CSV");
        right.add(el); right.add(searchEmpId); right.add(btnFilter); right.add(btnAll); right.add(btnAdd); right.add(btnExp);
        hdr.add(right,BorderLayout.EAST);

        btnFilter.addActionListener(e->{
            try { populate(dao.getByEmployee(Integer.parseInt(searchEmpId.getText().trim()))); }
            catch(NumberFormatException ex){ JOptionPane.showMessageDialog(this,"Enter a valid Emp ID."); }
        });
        searchEmpId.addActionListener(e->btnFilter.doClick());
        btnAll.addActionListener(e->loadAll());
        btnAdd.addActionListener(e->showAddDialog());
        btnExp.addActionListener(e->CsvExporter.export(this,model,"Salaries"));
        return hdr;
    }

    private JPanel buildActionBar() {
        JPanel bar=new JPanel(new FlowLayout(FlowLayout.LEFT,10,10));
        bar.setBackground(Theme.BG_SECONDARY);
        bar.setBorder(BorderFactory.createMatteBorder(1,0,0,0,new Color(51,65,85)));
        JButton btnPayslip=Theme.primaryBtn("🧾 Payslip");
        JButton btnCalc   =Theme.accentBtn("🧮 Auto-Calc");
        JButton btnEdit   =Theme.accentBtn("✏ Edit");
        JButton btnDelete =Theme.dangerBtn("🗑 Delete");
        JButton btnRefresh=Theme.ghostBtn("↺ Refresh");
        bar.add(btnPayslip); bar.add(btnCalc); bar.add(btnEdit); bar.add(btnDelete); bar.add(btnRefresh);
        btnPayslip.addActionListener(e->showPayslip());
        btnCalc   .addActionListener(e->showAutoCalcDialog());
        btnEdit   .addActionListener(e->editSelected());
        btnDelete .addActionListener(e->deleteSelected());
        btnRefresh.addActionListener(e->loadAll());
        return bar;
    }

    // ── Renderers ─────────────────────────────────────────────────────────────

    private TableCellRenderer netRenderer(){
        return new DefaultTableCellRenderer(){
            @Override public Component getTableCellRendererComponent(JTable t,Object value,boolean sel,boolean focus,int row,int col){
                super.getTableCellRendererComponent(t,value,sel,focus,row,col);
                if(!sel){ setForeground(Theme.SUCCESS); setBackground(Theme.CARD_BG); }
                setFont(new Font("Segoe UI",Font.BOLD,12)); setOpaque(true);
                setBorder(BorderFactory.createEmptyBorder(0,10,0,10)); return this;
            }
        };
    }

    // ── Data ──────────────────────────────────────────────────────────────────

    private void loadAll(){ populate(dao.getAllSalaries()); }

    private void populate(List<Salary> list) {
        model.setRowCount(0);
        double grand=0;
        for (Salary s : list) {
            grand += s.getNetSalary();
            model.addRow(new Object[]{
                s.getSalaryId(), s.getEmpId(), s.getEmpName(), s.getDeptName(),
                fmt(s.getBasicSalary()), fmt(s.getBonus()),
                fmt(s.getOvertimePay()), fmt(s.getTaxAmount()),
                fmt(s.getDeductions()), fmt(s.getNetSalary()),
                s.getPayMonth(), s.getPayDate()
            });
        }
        totalLabel.setText(list.size()+" records  |  Total Payroll: PKR "+String.format("%,.0f",grand));
    }

    private String fmt(double v){ return String.format("PKR %,.0f",v); }

    // ── Dept stats ────────────────────────────────────────────────────────────

    private JPanel buildDeptStats() {
        JPanel w=new JPanel(new BorderLayout(0,6));
        w.setBackground(Theme.BG);
        w.setBorder(BorderFactory.createEmptyBorder(10,0,12,0));
        JLabel lbl=new JLabel("  Department Payroll Summary");
        lbl.setFont(Theme.FONT_HEADER); lbl.setForeground(Theme.TEXT_MAIN);
        w.add(lbl,BorderLayout.NORTH);
        String[] cols={"Department","Employees","Avg Net","Max","Min","Total Payroll"};
        DefaultTableModel sm=new DefaultTableModel(cols,0){
            @Override public boolean isCellEditable(int r,int c){ return false; }
        };
        JTable st=new JTable(sm); Theme.styleTable(st);
        st.setPreferredScrollableViewportSize(new Dimension(0,90));
        for(Object[] row:dao.getDeptStats()) sm.addRow(row);
        w.add(Theme.scrollPane(st),BorderLayout.CENTER);
        return w;
    }

    // ── Payslip ───────────────────────────────────────────────────────────────

    private void showPayslip() {
        int row=table.getSelectedRow();
        if(row<0){ JOptionPane.showMessageDialog(this,"Select a salary record first."); return; }

        JDialog dlg=DialogHelper.create(this,"Payslip","",440,520);
        JPanel root=new JPanel(new BorderLayout());
        root.setBackground(Theme.BG_SECONDARY);

        // Header gradient
        JPanel hdr=new JPanel(new GridLayout(4,1,0,3)){
            @Override protected void paintComponent(Graphics g){
                Graphics2D g2=(Graphics2D)g.create();
                GradientPaint gp=new GradientPaint(0,0,Theme.PRIMARY_DARK,getWidth(),getHeight(),new Color(30,41,59));
                g2.setPaint(gp); g2.fillRect(0,0,getWidth(),getHeight()); g2.dispose();
            }
        };
        hdr.setBorder(BorderFactory.createEmptyBorder(18,22,18,22));
        addPayslipLabel(hdr,"HRMS Corporation",new Font("Segoe UI",Font.BOLD,18),Color.WHITE);
        addPayslipLabel(hdr,"Official Salary Slip",new Font("Segoe UI",Font.PLAIN,12),new Color(165,180,252));
        addPayslipLabel(hdr,"Employee: "+model.getValueAt(row,2)+"   |   Dept: "+model.getValueAt(row,3),Theme.FONT_SMALL,Theme.TEXT_MUTED);
        addPayslipLabel(hdr,"Month: "+model.getValueAt(row,10)+"   |   Date: "+model.getValueAt(row,11),Theme.FONT_SMALL,Theme.TEXT_MUTED);
        root.add(hdr,BorderLayout.NORTH);

        // Breakdown
        JPanel body=new JPanel(new GridBagLayout());
        body.setBackground(Theme.BG_SECONDARY);
        body.setBorder(BorderFactory.createEmptyBorder(20,28,10,28));

        Object[][] lines={
            {"Basic Salary",     model.getValueAt(row,4),  Theme.TEXT_MAIN},
            {"Bonus",            model.getValueAt(row,5),  Theme.ACCENT},
            {"Overtime Pay",     model.getValueAt(row,6),  Theme.INFO},
            {"Tax Deducted",     "− "+model.getValueAt(row,7), Theme.DANGER},
            {"Other Deductions", "− "+model.getValueAt(row,8), Theme.DANGER},
            {"─────────────────","──────────────────",""},
            {"NET SALARY",       model.getValueAt(row,9),  Theme.SUCCESS},
        };
        for(int i=0;i<lines.length;i++){
            JLabel k=new JLabel(lines[i][0].toString());
            k.setFont(i==6?new Font("Segoe UI",Font.BOLD,15):Theme.FONT_BODY);
            k.setForeground(i==5?new Color(51,65,85):Theme.TEXT_MUTED);
            body.add(k,DialogHelper.gbc(0,i,0));
            JLabel v=new JLabel(lines[i][1].toString());
            v.setFont(i==6?new Font("Segoe UI",Font.BOLD,18):Theme.FONT_BODY);
            v.setForeground(lines[i][2] instanceof Color?(Color)lines[i][2]:Theme.TEXT_MAIN);
            v.setHorizontalAlignment(SwingConstants.RIGHT);
            body.add(v,DialogHelper.gbc(1,i,1));
        }
        root.add(body,BorderLayout.CENTER);

        JLabel footer=new JLabel("  System-generated payslip. Not valid without HR stamp.",SwingConstants.CENTER);
        footer.setFont(Theme.FONT_SMALL); footer.setForeground(new Color(71,85,105));
        footer.setBorder(BorderFactory.createMatteBorder(1,0,0,0,new Color(51,65,85)));
        footer.setPreferredSize(new Dimension(0,32));
        JButton close=Theme.primaryBtn("Close");
        close.addActionListener(e->dlg.dispose());
        JPanel south=new JPanel(new BorderLayout()); south.setBackground(Theme.BG_SECONDARY);
        south.add(footer,BorderLayout.NORTH); south.add(DialogHelper.buttonRow(close),BorderLayout.SOUTH);
        root.add(south,BorderLayout.SOUTH);

        dlg.setContentPane(root); dlg.setVisible(true);
    }

    private void addPayslipLabel(JPanel p, String text, Font f, Color c){
        JLabel l=new JLabel(text); l.setFont(f); l.setForeground(c); p.add(l);
    }

    // ── Auto-Calc dialog ──────────────────────────────────────────────────────

    private void showAutoCalcDialog() {
        JDialog dlg=DialogHelper.create(this,"Auto Salary Calculator","",460,420);
        JPanel root=DialogHelper.rootPanel();
        root.add(DialogHelper.header("🧮","Auto Salary Calculator",
            "Net = Basic + Bonus + Overtime − Tax − Deductions",Theme.ACCENT),BorderLayout.NORTH);

        JPanel form=DialogHelper.formPanel();
        JTextField fEmpId =Theme.field(10);
        JTextField fBasic =Theme.field(10);
        JTextField fBonus =Theme.field(10); fBonus.setText("0");
        JTextField fOT    =Theme.field(10); fOT.setText("0");
        JTextField fTax   =Theme.field(10); fTax.setText("0");
        JTextField fDeduct=Theme.field(10); fDeduct.setText("0");
        JTextField fMonth =Theme.field(12);
        fMonth.setText(java.time.LocalDate.now().getMonth().toString().substring(0,1)
            +java.time.LocalDate.now().getMonth().toString().substring(1).toLowerCase()
            +"-"+java.time.LocalDate.now().getYear());

        JTextArea preview=new JTextArea(7,20);
        preview.setFont(new Font("Consolas",Font.PLAIN,11));
        preview.setBackground(new Color(15,23,42)); preview.setForeground(Theme.ACCENT);
        preview.setEditable(false); preview.setBorder(BorderFactory.createEmptyBorder(8,10,8,10));

        String[] lbls={"Employee ID *","Basic Salary *","Bonus","Overtime Hours","Tax %","Other Deductions","Pay Month *"};
        JTextField[] flds={fEmpId,fBasic,fBonus,fOT,fTax,fDeduct,fMonth};
        for(int i=0;i<lbls.length;i++){
            form.add(DialogHelper.label(lbls[i]),DialogHelper.gbc(0,i,0));
            form.add(flds[i],DialogHelper.gbc(1,i,1));
        }
        GridBagConstraints gc=DialogHelper.gbc(0,7,0); gc.gridwidth=2;
        form.add(new JScrollPane(preview),gc);

        // Live preview on any change
        java.awt.event.KeyAdapter liveUpdate=new java.awt.event.KeyAdapter(){
            public void keyReleased(java.awt.event.KeyEvent e){
                try{
                    double basic =Double.parseDouble(fBasic.getText().trim());
                    double bonus =Double.parseDouble(fBonus.getText().isEmpty()?"0":fBonus.getText().trim());
                    double ot    =Double.parseDouble(fOT.getText().isEmpty()?"0":fOT.getText().trim());
                    double tax   =Double.parseDouble(fTax.getText().isEmpty()?"0":fTax.getText().trim());
                    double deduct=Double.parseDouble(fDeduct.getText().isEmpty()?"0":fDeduct.getText().trim());
                    preview.setText(SalaryCalculator.breakdown(basic,bonus,ot,tax,deduct));
                } catch(Exception ignored){ preview.setText("Fill in values to see preview…"); }
            }
        };
        for(JTextField f:new JTextField[]{fBasic,fBonus,fOT,fTax,fDeduct}) f.addKeyListener(liveUpdate);

        root.add(form,BorderLayout.CENTER);

        JButton btnSave=Theme.accentBtn("Save Record");
        JButton btnCancel=Theme.ghostBtn("Cancel");
        root.add(DialogHelper.buttonRow(btnCancel,btnSave),BorderLayout.SOUTH);

        btnCancel.addActionListener(e->dlg.dispose());
        btnSave.addActionListener(e->{
            try{
                int    empId =Integer.parseInt(fEmpId.getText().trim());
                double basic =Double.parseDouble(fBasic.getText().trim());
                double bonus =Double.parseDouble(fBonus.getText().isEmpty()?"0":fBonus.getText().trim());
                double ot    =Double.parseDouble(fOT.getText().isEmpty()?"0":fOT.getText().trim());
                double tax   =Double.parseDouble(fTax.getText().isEmpty()?"0":fTax.getText().trim());
                double deduct=Double.parseDouble(fDeduct.getText().isEmpty()?"0":fDeduct.getText().trim());
                String month =fMonth.getText().trim();
                if(month.isEmpty()) throw new IllegalArgumentException("Pay Month required.");

                double otPay =SalaryCalculator.calcOvertimePay(basic,ot);
                double taxAmt=SalaryCalculator.calcTax(basic,bonus,tax);
                double net   =basic+bonus+otPay-taxAmt-deduct;

                Salary s=new Salary(empId,basic,bonus,deduct,month);
                s.setOvertimePay(otPay); s.setTaxAmount(taxAmt); s.setNetSalary(net);
                if(dao.addSalaryFull(s)){
                    AuditLogger.log("Salaries","Auto-Calc Add","Emp #"+empId+" "+month);
                    JOptionPane.showMessageDialog(dlg,"Salary saved. Net = PKR "+String.format("%,.0f",net));
                    dlg.dispose(); loadAll();
                } else JOptionPane.showMessageDialog(dlg,"Save failed.","Error",JOptionPane.ERROR_MESSAGE);
            } catch(NumberFormatException ex){ JOptionPane.showMessageDialog(dlg,"Enter valid numbers."); }
            catch(IllegalArgumentException ex){ JOptionPane.showMessageDialog(dlg,ex.getMessage()); }
        });

        dlg.setContentPane(root); dlg.setVisible(true);
    }

    // ── Edit / Delete ─────────────────────────────────────────────────────────

    private void editSelected(){
        int row=table.getSelectedRow();
        if(row<0){ JOptionPane.showMessageDialog(this,"Select a record first."); return; }
        int salId=(int)model.getValueAt(row,0);
        JDialog dlg=DialogHelper.create(this,"Edit Salary Record","",420,340);
        JPanel root=DialogHelper.rootPanel();
        root.add(DialogHelper.header("✏","Edit Salary Record","Employee: "+model.getValueAt(row,2),Theme.ACCENT),BorderLayout.NORTH);

        JPanel form=DialogHelper.formPanel();
        
        JTextField fBasic =Theme.field(14); fBasic.setText(model.getValueAt(row,4).toString().replace("PKR ","").replace(",",""));
        JTextField fBonus =Theme.field(14); fBonus.setText(model.getValueAt(row,5).toString().replace("PKR ","").replace(",",""));
        JTextField fDeduct=Theme.field(14); fDeduct.setText(model.getValueAt(row,8).toString().replace("PKR ","").replace(",",""));
        JTextField fMonth =Theme.field(14); fMonth.setText(model.getValueAt(row,10).toString());

        String[] lbls={"Basic Salary *","Bonus","Deductions","Pay Month *"};
        JTextField[] flds={fBasic,fBonus,fDeduct,fMonth};
        for(int i=0;i<4;i++){
            form.add(DialogHelper.label(lbls[i]),DialogHelper.gbc(0,i,0));
            form.add(flds[i],DialogHelper.gbc(1,i,1));
        }
        root.add(form,BorderLayout.CENTER);

        JButton btnSave=Theme.accentBtn("Save"); JButton btnCancel=Theme.ghostBtn("Cancel");
        root.add(DialogHelper.buttonRow(btnCancel,btnSave),BorderLayout.SOUTH);
        btnCancel.addActionListener(e->dlg.dispose());
        btnSave.addActionListener(e->{
            try{
                Salary s=new Salary(salId,0,Double.parseDouble(fBasic.getText().trim()),
                    Double.parseDouble(fBonus.getText().trim()),
                    Double.parseDouble(fDeduct.getText().trim()),fMonth.getText().trim());
                if(dao.updateSalary(s)){ AuditLogger.log("Salaries","Update","ID #"+salId); dlg.dispose(); loadAll(); }
                else JOptionPane.showMessageDialog(dlg,"Update failed.","Error",JOptionPane.ERROR_MESSAGE);
            } catch(NumberFormatException ex){ JOptionPane.showMessageDialog(dlg,"Enter valid numbers."); }
        });
        dlg.setContentPane(root); dlg.setVisible(true);
    }

    private void deleteSelected(){
        int row=table.getSelectedRow();
        if(row<0){ JOptionPane.showMessageDialog(this,"Select a record first."); return; }
        int id=(int)model.getValueAt(row,0);
        int confirm=JOptionPane.showConfirmDialog(this,
            "Delete salary record #"+id+" for "+model.getValueAt(row,2)+"?",
            "Confirm",JOptionPane.YES_NO_OPTION,JOptionPane.WARNING_MESSAGE);
        if(confirm==JOptionPane.YES_OPTION){
            if(dao.deleteSalary(id)){ AuditLogger.log("Salaries","Delete","ID #"+id); loadAll(); }
            else JOptionPane.showMessageDialog(this,"Delete failed.","Error",JOptionPane.ERROR_MESSAGE);
        }
    }

    // ── Simple add (manual) ───────────────────────────────────────────────────

    private void showAddDialog(){
        JDialog dlg=DialogHelper.create(this,"Add Salary Record","",420,360);
        JPanel root=DialogHelper.rootPanel();
        root.add(DialogHelper.header("＋","Add Salary Record","Manual entry — or use 🧮 Auto-Calc",Theme.ACCENT),BorderLayout.NORTH);
        JPanel form=DialogHelper.formPanel();
        JTextField fEmpId=Theme.field(10); JTextField fBasic=Theme.field(10);
        JTextField fBonus=Theme.field(10); fBonus.setText("0");
        JTextField fDeduct=Theme.field(10); fDeduct.setText("0");
        JTextField fMonth=Theme.field(12);
        fMonth.setText(java.time.LocalDate.now().getMonth().toString().substring(0,1)
            +java.time.LocalDate.now().getMonth().toString().substring(1).toLowerCase()
            +"-"+java.time.LocalDate.now().getYear());
        String[] lbls={"Employee ID *","Basic *","Bonus","Deductions","Pay Month *"};
        JTextField[] flds={fEmpId,fBasic,fBonus,fDeduct,fMonth};
        for(int i=0;i<5;i++){
            form.add(DialogHelper.label(lbls[i]),DialogHelper.gbc(0,i,0));
            form.add(flds[i],DialogHelper.gbc(1,i,1));
        }
        root.add(form,BorderLayout.CENTER);
        JButton btnSave=Theme.accentBtn("Add"); JButton btnCancel=Theme.ghostBtn("Cancel");
        root.add(DialogHelper.buttonRow(btnCancel,btnSave),BorderLayout.SOUTH);
        btnCancel.addActionListener(e->dlg.dispose());
        btnSave.addActionListener(e->{
            try{
                int empId=Integer.parseInt(fEmpId.getText().trim());
                double basic=Double.parseDouble(fBasic.getText().trim());
                double bonus=Double.parseDouble(fBonus.getText().trim());
                double deduct=Double.parseDouble(fDeduct.getText().trim());
                String month=fMonth.getText().trim();
                if(month.isEmpty()) throw new IllegalArgumentException("Month required.");
                if(dao.addSalary(new Salary(empId,basic,bonus,deduct,month))){
                    AuditLogger.log("Salaries","Add","Emp #"+empId+" "+month);
                    dlg.dispose(); loadAll();
                } else JOptionPane.showMessageDialog(dlg,"Insert failed.","Error",JOptionPane.ERROR_MESSAGE);
            } catch(NumberFormatException ex){ JOptionPane.showMessageDialog(dlg,"Enter valid numbers."); }
            catch(IllegalArgumentException ex){ JOptionPane.showMessageDialog(dlg,ex.getMessage()); }
        });
        dlg.setContentPane(root); dlg.setVisible(true);
    }
}
