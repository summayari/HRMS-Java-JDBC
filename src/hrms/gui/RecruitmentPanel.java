package hrms.gui;

import hrms.dao.RecruitmentDAO;
import hrms.model.Candidate;
import hrms.util.AuditLogger;
import hrms.util.CsvExporter;
import hrms.util.DialogHelper;

import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.time.LocalDate;
import java.util.List;

public class RecruitmentPanel extends JPanel {

    private final RecruitmentDAO dao = new RecruitmentDAO();
    private DefaultTableModel model;
    private JTable table;
    private String currentFilter = "All";

    private static final String[] COLS = {
        "ID","Name","Email","Phone","Position","Department","Status","Applied","Interview","Notes"
    };

    public RecruitmentPanel() {
        setLayout(new BorderLayout(0,0));
        setBackground(Theme.BG);
        build();
    }

    private void build() {
        add(buildHeader(), BorderLayout.NORTH);

        model = new DefaultTableModel(COLS,0){
            @Override public boolean isCellEditable(int r,int c){ return false; }
        };
        table = new JTable(model);
        Theme.styleTable(table);
        table.getColumnModel().getColumn(6).setCellRenderer(statusRenderer());
        table.getColumnModel().getColumn(9).setPreferredWidth(180);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        JPanel center = new JPanel(new BorderLayout(0,0));
        center.setBackground(Theme.BG);
        center.setBorder(BorderFactory.createEmptyBorder(0,18,0,18));

        // Pipeline tabs
        center.add(buildPipelineTabs(), BorderLayout.NORTH);
        center.add(Theme.scrollPane(table), BorderLayout.CENTER);
        center.add(buildPipelineSummary(), BorderLayout.SOUTH);
        add(center, BorderLayout.CENTER);
        add(buildActionBar(), BorderLayout.SOUTH);
        loadAll();
    }

    private JPanel buildHeader() {
        JPanel hdr = new JPanel(new BorderLayout()){
            @Override protected void paintComponent(Graphics g){
                Graphics2D g2=(Graphics2D)g.create();
                GradientPaint gp=new GradientPaint(0,0,new Color(20,30,60),getWidth(),getHeight(),Theme.BG);
                g2.setPaint(gp); g2.fillRect(0,0,getWidth(),getHeight()); g2.dispose();
            }
        };
        hdr.setOpaque(false);
        hdr.setBorder(BorderFactory.createEmptyBorder(16,20,14,20));
        JPanel left=new JPanel(new GridLayout(2,1,0,3)); left.setOpaque(false);
        JLabel title=new JLabel("🎯  Recruitment Pipeline"); title.setFont(Theme.FONT_TITLE); title.setForeground(Theme.TEXT_MAIN);
        JLabel sub=new JLabel("Track candidates from application to hiring"); sub.setFont(Theme.FONT_SMALL); sub.setForeground(Theme.TEXT_MUTED);
        left.add(title); left.add(sub);
        hdr.add(left, BorderLayout.WEST);
        JPanel right=new JPanel(new FlowLayout(FlowLayout.RIGHT,8,0)); right.setOpaque(false);
        JButton btnAdd=Theme.accentBtn("＋ Add Candidate");
        JButton btnExp=Theme.ghostBtn("↓ CSV");
        right.add(btnAdd); right.add(btnExp);
        hdr.add(right, BorderLayout.EAST);
        btnAdd.addActionListener(e->showCandidateDialog(null));
        btnExp.addActionListener(e->CsvExporter.export(this,model,"Recruitment"));
        return hdr;
    }

    private JPanel buildPipelineTabs() {
        JPanel tabs=new JPanel(new FlowLayout(FlowLayout.LEFT,4,8));
        tabs.setBackground(Theme.BG);
        String[] filters={"All","Applied","Shortlisted","Interview","Hired","Rejected"};
        Color[]  colors ={Theme.TEXT_MUTED,Theme.INFO,Theme.WARNING,Theme.PRIMARY,Theme.SUCCESS,Theme.DANGER};
        ButtonGroup bg=new ButtonGroup();
        for (int i=0;i<filters.length;i++){
            final String f=filters[i]; final Color c=colors[i];
            JToggleButton btn=new JToggleButton(f){
                @Override protected void paintComponent(Graphics g){
                    Graphics2D g2=(Graphics2D)g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
                    if(isSelected()){ g2.setColor(new Color(c.getRed(),c.getGreen(),c.getBlue(),40)); g2.fillRoundRect(0,0,getWidth(),getHeight(),8,8); g2.setColor(c); g2.setStroke(new BasicStroke(1.5f)); g2.drawRoundRect(0,0,getWidth()-1,getHeight()-1,8,8); }
                    else if(getModel().isRollover()){ g2.setColor(new Color(255,255,255,8)); g2.fillRoundRect(0,0,getWidth(),getHeight(),8,8); }
                    g2.dispose(); super.paintComponent(g);
                }
            };
            btn.setFont(new Font("Segoe UI",Font.BOLD,11));
            btn.setForeground(i==0?c:Theme.TEXT_MUTED);
            btn.setOpaque(false); btn.setContentAreaFilled(false); btn.setBorderPainted(false); btn.setFocusPainted(false);
            btn.setBorder(BorderFactory.createEmptyBorder(5,12,5,12));
            btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            btn.setSelected(i==0);
            btn.addActionListener(ev->{ currentFilter=f; applyFilter(); });
            btn.getModel().addChangeListener(ev->btn.setForeground(btn.isSelected()?c:Theme.TEXT_MUTED));
            bg.add(btn); tabs.add(btn);
        }
        return tabs;
    }

    private JPanel buildPipelineSummary() {
        JPanel bar=new JPanel(new FlowLayout(FlowLayout.LEFT,16,8));
        bar.setBackground(Theme.BG_SECONDARY);
        bar.setBorder(BorderFactory.createMatteBorder(1,0,0,0,new Color(51,65,85)));
        int[] counts=dao.getPipelineCounts();
        String[] stages={"Applied","Shortlisted","Interview","Hired","Rejected"};
        Color[] colors={Theme.INFO,Theme.WARNING,Theme.PRIMARY,Theme.SUCCESS,Theme.DANGER};
        for(int i=0;i<stages.length;i++){
            JLabel l=new JLabel(stages[i]+": "+counts[i]);
            l.setFont(new Font("Segoe UI",Font.BOLD,11)); l.setForeground(colors[i]);
            bar.add(l);
        }
        return bar;
    }

    private JPanel buildActionBar(){
        JPanel bar=new JPanel(new FlowLayout(FlowLayout.LEFT,10,10));
        bar.setBackground(Theme.BG_SECONDARY);
        bar.setBorder(BorderFactory.createMatteBorder(1,0,0,0,new Color(51,65,85)));
        JButton btnShortlist=Theme.primaryBtn("📌 Shortlist");
        JButton btnInterview=Theme.accentBtn("📞 Schedule Interview");
        JButton btnHire     =Theme.accentBtn("✅ Mark Hired");
        JButton btnReject   =Theme.dangerBtn("✘ Reject");
        JButton btnRefresh  =Theme.ghostBtn("↺ Refresh");
        bar.add(btnShortlist); bar.add(btnInterview); bar.add(btnHire); bar.add(btnReject); bar.add(btnRefresh);
        btnShortlist.addActionListener(e->updateStatus("Shortlisted",null));
        btnInterview.addActionListener(e->scheduleInterview());
        btnHire     .addActionListener(e->updateStatus("Hired",null));
        btnReject   .addActionListener(e->updateStatus("Rejected",null));
        btnRefresh  .addActionListener(e->loadAll());
        return bar;
    }

    private TableCellRenderer statusRenderer(){
        return new DefaultTableCellRenderer(){
            @Override public Component getTableCellRendererComponent(JTable t,Object value,boolean sel,boolean focus,int row,int col){
                super.getTableCellRendererComponent(t,value,sel,focus,row,col);
                if(!sel) switch(value==null?"":value.toString()){
                    case "Hired"       ->{ setForeground(Theme.SUCCESS);  setBackground(new Color(22,101,52,70)); }
                    case "Rejected"    ->{ setForeground(Theme.DANGER);   setBackground(new Color(153,27,27,70)); }
                    case "Interview"   ->{ setForeground(Theme.PRIMARY);  setBackground(new Color(49,46,129,70)); }
                    case "Shortlisted" ->{ setForeground(Theme.WARNING);  setBackground(new Color(120,53,15,70)); }
                    default            ->{ setForeground(Theme.INFO);     setBackground(new Color(7,89,133,70)); }
                }
                setFont(new Font("Segoe UI",Font.BOLD,11)); setOpaque(true);
                setBorder(BorderFactory.createEmptyBorder(0,10,0,10)); return this;
            }
        };
    }

    private void loadAll(){
        model.setRowCount(0);
        for(Candidate c:dao.getAll()) addRow(c);
    }

    private void applyFilter(){
        model.setRowCount(0);
        List<Candidate> list="All".equals(currentFilter)?dao.getAll():dao.getByStatus(currentFilter);
        for(Candidate c:list) addRow(c);
    }

    private void addRow(Candidate c){
        model.addRow(new Object[]{c.getCandidateId(),c.getName(),c.getEmail(),c.getPhone(),
            c.getPosition(),c.getDepartment(),c.getStatus(),c.getAppliedDate(),
            c.getInterviewDate(),c.getNotes()});
    }

    private void updateStatus(String status, String interviewDate){
        int row=table.getSelectedRow();
        if(row<0){ JOptionPane.showMessageDialog(this,"Select a candidate first."); return; }
        int id=(int)model.getValueAt(row,0);
        String name=(String)model.getValueAt(row,1);
        int confirm=JOptionPane.showConfirmDialog(this,"Mark "+name+" as "+status+"?","Confirm",JOptionPane.YES_NO_OPTION);
        if(confirm==JOptionPane.YES_OPTION){
            String notes=(String)model.getValueAt(row,9);
            if(dao.updateStatus(id,status,interviewDate,notes)){
                AuditLogger.log("Recruitment","Status Update",name+" → "+status);
                loadAll();
            }
        }
    }

    private void scheduleInterview(){
        int row=table.getSelectedRow();
        if(row<0){ JOptionPane.showMessageDialog(this,"Select a candidate first."); return; }
        String date=JOptionPane.showInputDialog(this,"Enter interview date (YYYY-MM-DD):","Interview Date",JOptionPane.QUESTION_MESSAGE);
        if(date!=null&&!date.isEmpty()){
            int id=(int)model.getValueAt(row,0);
            String notes=(String)model.getValueAt(row,9);
            dao.updateStatus(id,"Interview",date,notes);
            loadAll();
        }
    }

    private void showCandidateDialog(Candidate existing){
        boolean isEdit=existing!=null;
        JDialog dlg=DialogHelper.create(this,isEdit?"Edit Candidate":"Add Candidate","",500,480);
        JPanel root=DialogHelper.rootPanel();
        root.add(DialogHelper.header("🎯",isEdit?"Edit Candidate":"New Candidate Application","",Theme.ACCENT),BorderLayout.NORTH);

        JPanel form=DialogHelper.formPanel();
        JTextField fName=Theme.field(20); JTextField fEmail=Theme.field(20);
        JTextField fPhone=Theme.field(14); JTextField fPos=Theme.field(14);
        JTextField fDept=Theme.field(14); JTextField fDate=Theme.field(14);
        fDate.setText(LocalDate.now().toString());
        JComboBox<String> cbStatus=Theme.combo("Applied","Shortlisted","Interview","Hired","Rejected");
        JTextArea fNotes=new JTextArea(3,20);
        fNotes.setFont(Theme.FONT_BODY); fNotes.setBackground(Theme.INPUT_BG); fNotes.setForeground(Theme.TEXT_MAIN);
        fNotes.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(new Color(71,85,105)),BorderFactory.createEmptyBorder(6,8,6,8)));
        fNotes.setLineWrap(true);

        String[] lbls={"Full Name *","Email","Phone","Position Applied *","Department","Applied Date","Status","Notes"};
        Component[] ctrls={fName,fEmail,fPhone,fPos,fDept,fDate,cbStatus,new JScrollPane(fNotes)};
        for(int i=0;i<lbls.length;i++){
            form.add(DialogHelper.label(lbls[i]),DialogHelper.gbc(0,i,0));
            GridBagConstraints gc=DialogHelper.gbc(1,i,1);
            if(i==7) gc.ipady=30;
            form.add(ctrls[i],gc);
        }
        root.add(form,BorderLayout.CENTER);

        JButton btnSave=Theme.accentBtn(isEdit?"Save":"Add Candidate");
        JButton btnCancel=Theme.ghostBtn("Cancel");
        root.add(DialogHelper.buttonRow(btnCancel,btnSave),BorderLayout.SOUTH);
        btnCancel.addActionListener(e->dlg.dispose());
        btnSave.addActionListener(e->{
            if(fName.getText().trim().isEmpty()||fPos.getText().trim().isEmpty()){
                JOptionPane.showMessageDialog(dlg,"Name and Position are required."); return;
            }
            Candidate c=new Candidate();
            c.setName(fName.getText().trim()); c.setEmail(fEmail.getText().trim());
            c.setPhone(fPhone.getText().trim()); c.setPosition(fPos.getText().trim());
            c.setDepartment(fDept.getText().trim()); c.setAppliedDate(fDate.getText().trim());
            c.setStatus((String)cbStatus.getSelectedItem()); c.setNotes(fNotes.getText().trim());
            if(dao.add(c)){ AuditLogger.log("Recruitment","Add Candidate",c.getName()); dlg.dispose(); loadAll(); }
            else JOptionPane.showMessageDialog(dlg,"Save failed.","Error",JOptionPane.ERROR_MESSAGE);
        });
        dlg.setContentPane(root); dlg.setVisible(true);
    }
}
