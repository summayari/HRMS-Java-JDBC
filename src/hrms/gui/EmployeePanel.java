package hrms.gui;

import hrms.dao.DepartmentDAO;
import hrms.dao.EmployeeDAO;
import hrms.model.Department;
import hrms.model.Employee;
import hrms.util.AuditLogger;
import hrms.util.CsvExporter;
import hrms.util.DialogHelper;
import hrms.util.PhotoHelper;
import hrms.util.SalaryCalculator;

import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.util.List;

public class EmployeePanel extends JPanel {

    private final EmployeeDAO   empDAO  = new EmployeeDAO();
    private final DepartmentDAO deptDAO = new DepartmentDAO();

    private DefaultTableModel model;
    private JTable table;
    private JTextField        searchField;
    private JComboBox<String> deptFilter;
    private JComboBox<String> statusFilter;
    private JComboBox<String> roleFilter;
    private JLabel            countLabel;

    private static final String[] COLS = {
        "ID","Name","Department","Role Level","Position","Joined","Status"
    };

    public EmployeePanel() {
        setLayout(new BorderLayout(0,0));
        setBackground(Theme.BG);

        searchField  = Theme.field(16);
        deptFilter   = new JComboBox<>();
        statusFilter = Theme.combo("All Status","Active","Inactive","On Leave");
        roleFilter   = Theme.combo("All Roles","Employee","Team Lead","HR Manager","Accountant","Director","Admin");
        countLabel   = new JLabel("Loading...");
        countLabel.setFont(Theme.FONT_SMALL); countLabel.setForeground(Theme.TEXT_MUTED);

        add(buildHeader(),    BorderLayout.NORTH);
        add(buildFilterBar(), BorderLayout.CENTER);

        model = new DefaultTableModel(COLS,0){
            @Override public boolean isCellEditable(int r,int c){ return false; }
        };
        table = new JTable(model);
        Theme.styleTable(table);
        table.getColumnModel().getColumn(6).setCellRenderer(statusRenderer());
        table.getColumnModel().getColumn(3).setCellRenderer(roleLevelRenderer());
        table.getColumnModel().getColumn(0).setPreferredWidth(44);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        // Double-click → full profile
        table.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount()==2 && table.getSelectedRow()>=0) {
                    int id = (int) model.getValueAt(table.getSelectedRow(), 0);
                    Employee emp = empDAO.getById(id);
                    if (emp != null) new EmployeeProfileDialog(EmployeePanel.this, emp).setVisible(true);
                }
            }
        });

        JPanel center = new JPanel(new BorderLayout());
        center.setBackground(Theme.BG);
        center.setBorder(BorderFactory.createEmptyBorder(0,18,0,18));
        center.add(Theme.scrollPane(table), BorderLayout.CENTER);
        center.add(buildActionBar(), BorderLayout.SOUTH);

        // Re-layout all panels
        removeAll();
        setLayout(new BorderLayout());
        add(buildHeader(),    BorderLayout.NORTH);
        add(buildFilterBar(), BorderLayout.CENTER);

        // Swap center to south of filterbar — use wrapper
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setBackground(Theme.BG);
        wrapper.add(buildFilterBar(), BorderLayout.NORTH);
        wrapper.add(center,           BorderLayout.CENTER);
        add(wrapper, BorderLayout.CENTER);

        // Load depts into filter
        List<Department> depts = deptDAO.getAllDepartments();
        deptFilter = Theme.combo("All Departments");
        for (Department d : depts) deptFilter.addItem(d.getDeptName());
        deptFilter.addActionListener(e -> applyFilters());
        statusFilter.addActionListener(e -> applyFilters());
        roleFilter.addActionListener(e -> applyFilters());

        loadAll();
    }

    // ── Header ────────────────────────────────────────────────────────────────

    private JPanel buildHeader() {
        JPanel hdr = new JPanel(new BorderLayout()){
            @Override protected void paintComponent(Graphics g){
                Graphics2D g2=(Graphics2D)g.create();
                GradientPaint gp=new GradientPaint(0,0,new Color(30,27,75),getWidth(),getHeight(),Theme.BG);
                g2.setPaint(gp); g2.fillRect(0,0,getWidth(),getHeight()); g2.dispose();
            }
        };
        hdr.setOpaque(false);
        hdr.setBorder(BorderFactory.createEmptyBorder(16,20,14,20));
        JPanel left=new JPanel(new GridLayout(2,1,0,3)); left.setOpaque(false);
        JLabel title=new JLabel("👥  Employee Management");
        title.setFont(Theme.FONT_TITLE); title.setForeground(Theme.TEXT_MAIN);
        left.add(title); left.add(countLabel);
        hdr.add(left, BorderLayout.WEST);
        JPanel right=new JPanel(new FlowLayout(FlowLayout.RIGHT,8,0)); right.setOpaque(false);
        searchField.putClientProperty("JTextField.placeholderText","🔍 Search name…");
        JButton btnSearch=Theme.primaryBtn("Search");
        JButton btnAdd=Theme.accentBtn("＋ Add Employee");
        JButton btnExport=Theme.ghostBtn("↓ Export CSV");
        right.add(searchField); right.add(btnSearch); right.add(btnAdd); right.add(btnExport);
        hdr.add(right, BorderLayout.EAST);
        btnSearch.addActionListener(e->applyFilters());
        searchField.addActionListener(e->applyFilters());
        btnAdd.addActionListener(e->showDialog(null));
        btnExport.addActionListener(e->CsvExporter.export(this,model,"Employees"));
        return hdr;
    }

    private JPanel buildFilterBar() {
        JPanel bar=new JPanel(new FlowLayout(FlowLayout.LEFT,10,8));
        bar.setBackground(Theme.BG_SECONDARY);
        bar.setBorder(BorderFactory.createMatteBorder(1,0,1,0,new Color(51,65,85)));
        // Will be populated in constructor after depts loaded
        JLabel dl=label("Dept:"); JLabel sl=label("Status:"); JLabel rl=label("Role:");
        JButton clear=Theme.ghostBtn("Clear"); clear.setFont(Theme.FONT_SMALL);
        bar.add(dl); bar.add(deptFilter); bar.add(sl); bar.add(statusFilter); bar.add(rl); bar.add(roleFilter); bar.add(clear);
        clear.addActionListener(e->{ deptFilter.setSelectedIndex(0); statusFilter.setSelectedIndex(0); roleFilter.setSelectedIndex(0); searchField.setText(""); loadAll(); });
        return bar;
    }

    private JPanel buildActionBar() {
        JPanel bar=new JPanel(new FlowLayout(FlowLayout.LEFT,10,10));
        bar.setBackground(Theme.BG_SECONDARY);
        bar.setBorder(BorderFactory.createMatteBorder(1,0,0,0,new Color(51,65,85)));
        JButton btnView  =Theme.primaryBtn("👁 View Profile");
        JButton btnEdit  =Theme.accentBtn("✏ Edit");
        JButton btnPhoto =Theme.ghostBtn("📷 Photo");
        JButton btnDelete=Theme.dangerBtn("🗑 Delete");
        JButton btnRefresh=Theme.ghostBtn("↺ Refresh");
        JLabel hint=new JLabel("  ⬆ Double-click row to open full profile");
        hint.setFont(Theme.FONT_SMALL); hint.setForeground(new Color(99,102,241,160));
        bar.add(btnView); bar.add(btnEdit); bar.add(btnPhoto); bar.add(btnDelete); bar.add(btnRefresh); bar.add(hint);
        btnView.addActionListener(e->viewProfile());
        btnEdit.addActionListener(e->editSelected());
        btnPhoto.addActionListener(e->uploadPhoto());
        btnDelete.addActionListener(e->deleteSelected());
        btnRefresh.addActionListener(e->loadAll());
        return bar;
    }

    private JLabel label(String t){
        JLabel l=new JLabel(t); l.setForeground(Theme.TEXT_MUTED); l.setFont(Theme.FONT_SMALL); return l;
    }

    // ── Renderers ─────────────────────────────────────────────────────────────

    private TableCellRenderer statusRenderer(){
        return new DefaultTableCellRenderer(){
            @Override public Component getTableCellRendererComponent(JTable t,Object value,boolean sel,boolean focus,int row,int col){
                super.getTableCellRendererComponent(t,value,sel,focus,row,col);
                if(!sel) switch(value==null?"":value.toString()){
                    case "Active"   ->{ setForeground(Theme.SUCCESS); setBackground(new Color(22,101,52,80)); }
                    case "Inactive" ->{ setForeground(Theme.DANGER);  setBackground(new Color(153,27,27,80)); }
                    case "On Leave" ->{ setForeground(Theme.WARNING); setBackground(new Color(120,53,15,80)); }
                    default         ->{ setForeground(Theme.TEXT_MUTED); setBackground(Theme.CARD_BG); }
                }
                setFont(new Font("Segoe UI",Font.BOLD,11)); setOpaque(true);
                setBorder(BorderFactory.createEmptyBorder(0,10,0,10)); return this;
            }
        };
    }

    private TableCellRenderer roleLevelRenderer(){
        return new DefaultTableCellRenderer(){
            @Override public Component getTableCellRendererComponent(JTable t,Object value,boolean sel,boolean focus,int row,int col){
                super.getTableCellRendererComponent(t,value,sel,focus,row,col);
                if(!sel){
                    Color c = switch(value==null?"":value.toString()){
                        case "Director"   -> Theme.DANGER;
                        case "HR Manager" -> Theme.WARNING;
                        case "Team Lead"  -> Theme.PRIMARY;
                        case "Accountant" -> Theme.INFO;
                        case "Admin"      -> new Color(167,139,250);
                        default           -> Theme.TEXT_MUTED;
                    };
                    setForeground(c); setBackground(Theme.CARD_BG);
                }
                setFont(new Font("Segoe UI",Font.BOLD,10)); setOpaque(true);
                setBorder(BorderFactory.createEmptyBorder(0,10,0,10)); return this;
            }
        };
    }

    // ── Data ──────────────────────────────────────────────────────────────────

    private void loadAll(){ populate(empDAO.getAllEmployees()); }

    private void applyFilters(){
        String search=(searchField.getText().trim().toLowerCase());
        String dept=(String)deptFilter.getSelectedItem();
        String status=(String)statusFilter.getSelectedItem();
        String role=(String)roleFilter.getSelectedItem();
        List<Employee> all=search.isEmpty()?empDAO.getAllEmployees():empDAO.searchByName(search);
        model.setRowCount(0);
        for(Employee e:all){
            if(!"All Departments".equals(dept)&&!dept.equals(e.getDeptName())) continue;
            if(!"All Status".equals(status)&&!status.equals(e.getStatus())) continue;
            if(!"All Roles".equals(role)&&!role.equals(e.getRoleLevel())) continue;
            addRow(e);
        }
        countLabel.setText(model.getRowCount()+" employee(s) shown");
    }

    private void populate(List<Employee> list){
        model.setRowCount(0);
        for(Employee e:list) addRow(e);
        countLabel.setText(model.getRowCount()+" employee(s)");
    }

    private void addRow(Employee e){
        model.addRow(new Object[]{
            e.getEmpId(),e.getEmpName(),e.getDeptName(),
            e.getRoleLevel()!=null?e.getRoleLevel():"Employee",
            e.getPosition(),e.getJoiningDate(),e.getStatus()
        });
    }

    // ── View profile ──────────────────────────────────────────────────────────

    private void viewProfile(){
        int row=table.getSelectedRow();
        if(row<0){ JOptionPane.showMessageDialog(this,"Select an employee first."); return; }
        Employee emp=empDAO.getById((int)model.getValueAt(row,0));
        if(emp!=null) new EmployeeProfileDialog(this,emp).setVisible(true);
    }

    // ── Upload photo ──────────────────────────────────────────────────────────

    private void uploadPhoto(){
        int row=table.getSelectedRow();
        if(row<0){ JOptionPane.showMessageDialog(this,"Select an employee first."); return; }
        int id=(int)model.getValueAt(row,0);
        String src=PhotoHelper.pickPhoto(this);
        if(src==null) return;
        String saved=PhotoHelper.savePhoto(src,id);
        if(empDAO.updatePhotoPath(id,saved)){
            JOptionPane.showMessageDialog(this,"Profile photo updated!");
            AuditLogger.log("Employees","Photo Upload","Emp #"+id);
        } else {
            JOptionPane.showMessageDialog(this,"Failed to save photo path.","Error",JOptionPane.ERROR_MESSAGE);
        }
    }

    // ── Edit / Delete ─────────────────────────────────────────────────────────

    private void editSelected(){
        int row=table.getSelectedRow();
        if(row<0){ JOptionPane.showMessageDialog(this,"Select an employee first."); return; }
        showDialog(empDAO.getById((int)model.getValueAt(row,0)));
    }

    private void deleteSelected(){
        int row=table.getSelectedRow();
        if(row<0){ JOptionPane.showMessageDialog(this,"Select an employee first."); return; }
        int id=(int)model.getValueAt(row,0);
        String name=(String)model.getValueAt(row,1);
        int confirm=JOptionPane.showConfirmDialog(this,
            "Permanently delete "+name+"?\nThis removes all attendance, salary and leave records.",
            "Confirm Delete",JOptionPane.YES_NO_OPTION,JOptionPane.WARNING_MESSAGE);
        if(confirm==JOptionPane.YES_OPTION){
            if(empDAO.deleteEmployee(id)){
                AuditLogger.log("Employees","Delete","Deleted emp #"+id+" "+name);
                JOptionPane.showMessageDialog(this,"Employee deleted."); loadAll();
            } else JOptionPane.showMessageDialog(this,"Delete failed.","Error",JOptionPane.ERROR_MESSAGE);
        }
    }

    // ── Add/Edit Dialog ───────────────────────────────────────────────────────

    private void showDialog(Employee existing){
        boolean isEdit=existing!=null;
        JDialog dlg=DialogHelper.create(this,isEdit?"Edit Employee":"Add Employee","",580,620);
        JPanel root=DialogHelper.rootPanel();
        root.add(DialogHelper.header(isEdit?"✏":"＋",
            isEdit?"Edit Employee":"Add New Employee",
            isEdit?"Update employee details":"Fill in employee information",Theme.PRIMARY),BorderLayout.NORTH);

        List<Department> depts=deptDAO.getAllDepartments();
        String[] deptItems=depts.stream().map(d->d.getDeptId()+" – "+d.getDeptName()).toArray(String[]::new);

        JPanel form=new JPanel(new GridBagLayout());
        form.setBackground(Theme.BG_SECONDARY);
        form.setBorder(BorderFactory.createEmptyBorder(16,22,10,22));

        // Fields
        JTextField fName  =Theme.field(16); JTextField fJoin=Theme.field(10);
        JTextField fEmail =Theme.field(16); JTextField fPhone=Theme.field(12);
        JTextField fPos   =Theme.field(16); JTextField fCnic=Theme.field(16);
        JTextField fEmgCtc=Theme.field(16);
        JTextField fOT    =Theme.field(6); fOT.setText("0");
        JTextField fTax   =Theme.field(6); fTax.setText("0");
        JTextArea  fAddr  =new JTextArea(2,16);
        fAddr.setFont(Theme.FONT_BODY); fAddr.setBackground(Theme.INPUT_BG); fAddr.setForeground(Theme.TEXT_MAIN);
        fAddr.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(new Color(71,85,105)),BorderFactory.createEmptyBorder(6,8,6,8)));
        fAddr.setLineWrap(true);

        JComboBox<String> cbDept    =new JComboBox<>(deptItems); cbDept.setBackground(Theme.INPUT_BG); cbDept.setForeground(Theme.TEXT_MAIN); cbDept.setFont(Theme.FONT_BODY);
        JComboBox<String> cbStatus  =Theme.combo("Active","Inactive","On Leave");
        JComboBox<String> cbGender  =Theme.combo("Male","Female","Other");
        JComboBox<String> cbContract=Theme.combo("Permanent","Contract","Intern");
        JComboBox<String> cbRole    =Theme.combo("Employee","Team Lead","HR Manager","Accountant","Director","Admin");

        // Auto-calc net salary label
        JLabel netLbl=new JLabel("PKR 0");
        netLbl.setFont(new Font("Segoe UI",Font.BOLD,14)); netLbl.setForeground(Theme.ACCENT);

        java.awt.event.KeyAdapter netCalc=new java.awt.event.KeyAdapter(){
            public void keyReleased(java.awt.event.KeyEvent e){
                try{ double ot=Double.parseDouble(fOT.getText()); double tx=Double.parseDouble(fTax.getText());
                    // We don't have basic here, show formula hint
                    netLbl.setText("OT: "+String.format("%.1f",ot)+" hrs  |  Tax: "+String.format("%.1f",tx)+"%");
                } catch(Exception ignored){}
            }
        };
        fOT.addKeyListener(netCalc); fTax.addKeyListener(netCalc);

        if(isEdit){
            fName.setText(existing.getEmpName()); fJoin.setText(existing.getJoiningDate());
            fEmail.setText(existing.getEmail()); fPhone.setText(existing.getPhone());
            fPos.setText(existing.getPosition()); fCnic.setText(existing.getCnic());
            fEmgCtc.setText(existing.getEmergencyContact());
            fOT.setText(String.valueOf(existing.getOvertimeHours()));
            fTax.setText(String.valueOf(existing.getTaxPercent()));
            if(existing.getAddress()!=null) fAddr.setText(existing.getAddress());
            if(existing.getGender()!=null) cbGender.setSelectedItem(existing.getGender());
            if(existing.getStatus()!=null) cbStatus.setSelectedItem(existing.getStatus());
            if(existing.getContractType()!=null) cbContract.setSelectedItem(existing.getContractType());
            if(existing.getRoleLevel()!=null) cbRole.setSelectedItem(existing.getRoleLevel());
            for(int i=0;i<depts.size();i++) if(depts.get(i).getDeptId()==existing.getDeptId()){ cbDept.setSelectedIndex(i); break; }
        } else {
            fJoin.setText(java.time.LocalDate.now().toString());
        }

        // 2-column grid layout for compactness
        Object[][] rows={
            {"Full Name *",fName,          "Joining Date *",fJoin},
            {"Department *",cbDept,        "Position",fPos},
            {"Email",fEmail,               "Phone",fPhone},
            {"Gender",cbGender,            "Status",cbStatus},
            {"Contract Type",cbContract,   "Role Level",cbRole},
            {"CNIC",fCnic,                 "Emergency Contact",fEmgCtc},
            {"Overtime Hours",fOT,         "Tax %",fTax},
            {"Address",new JScrollPane(fAddr), "Salary Preview",netLbl},
        };
        for(int i=0;i<rows.length;i++){
            for(int j=0;j<2;j++){
                GridBagConstraints gc=DialogHelper.gbc(j*2,i,0);
                gc.insets=new Insets(4,4,4,4);
                form.add(DialogHelper.label((String)rows[i][j*2]),gc);
                gc=DialogHelper.gbc(j*2+1,i,0.5);
                gc.insets=new Insets(4,2,4,8);
                if(i==7&&j==0) gc.ipady=20;
                form.add((Component)rows[i][j*2+1],gc);
            }
        }

        JScrollPane formScroll=new JScrollPane(form);
        formScroll.setBorder(null); formScroll.getViewport().setBackground(Theme.BG_SECONDARY);
        root.add(formScroll,BorderLayout.CENTER);

        JButton btnSave=Theme.primaryBtn(isEdit?"Save Changes":"Add Employee");
        JButton btnCancel=Theme.ghostBtn("Cancel");
        root.add(DialogHelper.buttonRow(btnCancel,btnSave),BorderLayout.SOUTH);

        btnCancel.addActionListener(e->dlg.dispose());
        btnSave.addActionListener(e->{
            if(fName.getText().trim().isEmpty()||fJoin.getText().trim().isEmpty()){
                JOptionPane.showMessageDialog(dlg,"Name and Joining Date are required."); return;
            }
            int deptId=depts.get(cbDept.getSelectedIndex()).getDeptId();
            Employee emp=isEdit?existing:new Employee();
            emp.setEmpName(fName.getText().trim());
            emp.setDeptId(deptId);
            emp.setJoiningDate(fJoin.getText().trim());
            emp.setEmail(fEmail.getText().trim());
            emp.setPhone(fPhone.getText().trim());
            emp.setPosition(fPos.getText().trim());
            emp.setStatus((String)cbStatus.getSelectedItem());
            emp.setGender((String)cbGender.getSelectedItem());
            emp.setCnic(fCnic.getText().trim());
            emp.setAddress(fAddr.getText().trim());
            emp.setContractType((String)cbContract.getSelectedItem());
            emp.setRoleLevel((String)cbRole.getSelectedItem());
            emp.setEmergencyContact(fEmgCtc.getText().trim());
            try{ emp.setOvertimeHours(Double.parseDouble(fOT.getText().trim())); } catch(Exception ignored){}
            try{ emp.setTaxPercent(Double.parseDouble(fTax.getText().trim())); }   catch(Exception ignored){}
            if(isEdit) emp.setPhotoPath(existing.getPhotoPath());

            boolean ok=isEdit?empDAO.updateEmployee(emp):empDAO.addEmployee(emp);
            if(ok){
                AuditLogger.log("Employees",isEdit?"Update":"Add",emp.getEmpName());
                JOptionPane.showMessageDialog(dlg,isEdit?"Employee updated.":"Employee added.");
                dlg.dispose(); loadAll();
            } else JOptionPane.showMessageDialog(dlg,"Operation failed.","Error",JOptionPane.ERROR_MESSAGE);
        });

        dlg.setContentPane(root); dlg.setVisible(true);
    }
}
