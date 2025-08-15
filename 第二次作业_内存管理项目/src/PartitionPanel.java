import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

/**
 * 动态分区分配模拟面板
 */
public class PartitionPanel extends JPanel {
    private PartitionManager manager;
    private JTable memoryTable;
    private DefaultTableModel tableModel;
    private JTextArea logArea;
    private JTextField processNameField;
    private JTextField sizeField;
    private JComboBox<String> algorithmComboBox;
    private JTextField deallocateField;
    private JLabel fragmentationLabel;
    
    public PartitionPanel() {
        manager = new PartitionManager();
        initializeComponents();
        updateDisplay();
    }
    
    private void initializeComponents() {
        setLayout(new BorderLayout());
        
        // 顶部控制面板
        JPanel controlPanel = createControlPanel();
        add(controlPanel, BorderLayout.NORTH);
        
        // 中间分割面板
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        
        // 左侧：内存状态表格
        JPanel leftPanel = createMemoryTablePanel();
        splitPane.setLeftComponent(leftPanel);
        
        // 右侧：操作日志
        JPanel rightPanel = createLogPanel();
        splitPane.setRightComponent(rightPanel);
        
        splitPane.setDividerLocation(500);
        add(splitPane, BorderLayout.CENTER);
        
        // 底部状态面板
        JPanel statusPanel = createStatusPanel();
        add(statusPanel, BorderLayout.SOUTH);
    }
    
    private JPanel createControlPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createTitledBorder("操作控制"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        
        // 分配内存区域
        gbc.gridx = 0; gbc.gridy = 0;
        panel.add(new JLabel("进程名:"), gbc);
        
        gbc.gridx = 1;
        processNameField = new JTextField(8);
        panel.add(processNameField, gbc);
        
        gbc.gridx = 2;
        panel.add(new JLabel("大小(K):"), gbc);
        
        gbc.gridx = 3;
        sizeField = new JTextField(8);
        panel.add(sizeField, gbc);
        
        gbc.gridx = 4;
        panel.add(new JLabel("算法:"), gbc);
        
        gbc.gridx = 5;
        algorithmComboBox = new JComboBox<>(new String[]{"首次适应", "最佳适应"});
        panel.add(algorithmComboBox, gbc);
        
        gbc.gridx = 6;
        JButton allocateButton = new JButton("分配内存");
        allocateButton.addActionListener(new AllocateAction());
        panel.add(allocateButton, gbc);
        
        // 释放内存区域
        gbc.gridx = 0; gbc.gridy = 1;
        panel.add(new JLabel("释放进程:"), gbc);
        
        gbc.gridx = 1;
        deallocateField = new JTextField(8);
        panel.add(deallocateField, gbc);
        
        gbc.gridx = 2;
        JButton deallocateButton = new JButton("释放内存");
        deallocateButton.addActionListener(new DeallocateAction());
        panel.add(deallocateButton, gbc);
        
        gbc.gridx = 3;
        JButton resetButton = new JButton("重置");
        resetButton.addActionListener(e -> {
            manager.reset();
            updateDisplay();
            logArea.append("系统已重置\n");
            logArea.setCaretPosition(logArea.getDocument().getLength());
        });
        panel.add(resetButton, gbc);
        
        return panel;
    }
    
    private JPanel createMemoryTablePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("内存分区状态"));
        
        String[] columnNames = {"起始地址", "结束地址", "大小(K)", "状态", "进程名"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        memoryTable = new JTable(tableModel);
        memoryTable.getTableHeader().setReorderingAllowed(false);
        
        // 设置表格样式
        memoryTable.setRowHeight(25);
        memoryTable.setGridColor(Color.LIGHT_GRAY);
        
        JScrollPane scrollPane = new JScrollPane(memoryTable);
        panel.add(scrollPane, BorderLayout.CENTER);
        
        return panel;
    }
    
    private JPanel createLogPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("操作日志"));
        
        logArea = new JTextArea();
        logArea.setEditable(false);
        logArea.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        
        JScrollPane scrollPane = new JScrollPane(logArea);
        panel.add(scrollPane, BorderLayout.CENTER);
        
        // 添加清空日志按钮
        JButton clearLogButton = new JButton("清空日志");
        clearLogButton.addActionListener(e -> logArea.setText(""));
        panel.add(clearLogButton, BorderLayout.SOUTH);
        
        return panel;
    }
    
    private JPanel createStatusPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panel.setBorder(BorderFactory.createTitledBorder("系统状态"));
        
        fragmentationLabel = new JLabel("内存碎片率: 0.00%");
        panel.add(fragmentationLabel);
        
        return panel;
    }
    
    private void updateDisplay() {
        // 更新表格
        tableModel.setRowCount(0);
        List<MemoryBlock> blocks = manager.getMemoryBlocks();
        
        for (MemoryBlock block : blocks) {
            Object[] row = {
                block.getStartAddress() + "K",
                block.getEndAddress() + "K",
                block.getSize() + "K",
                block.isAllocated() ? "已分配" : "空闲",
                block.isAllocated() ? block.getProcessName() : "-"
            };
            tableModel.addRow(row);
        }
        
        // 更新碎片率
        double fragmentation = manager.getFragmentation();
        fragmentationLabel.setText(String.format("内存碎片率: %.2f%%", fragmentation * 100));
    }
    
    private class AllocateAction implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            try {
                String processName = processNameField.getText().trim();
                String sizeText = sizeField.getText().trim();
                
                if (processName.isEmpty() || sizeText.isEmpty()) {
                    JOptionPane.showMessageDialog(PartitionPanel.this, 
                        "请输入进程名和大小", "输入错误", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                
                int size = Integer.parseInt(sizeText);
                if (size <= 0) {
                    JOptionPane.showMessageDialog(PartitionPanel.this, 
                        "大小必须大于0", "输入错误", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                
                String algorithm = (String) algorithmComboBox.getSelectedItem();
                boolean success;
                
                if ("首次适应".equals(algorithm)) {
                    success = manager.allocateFirstFit(processName, size);
                } else {
                    success = manager.allocateBestFit(processName, size);
                }
                
                if (success) {
                    logArea.append(String.format("成功为进程%s分配%dK内存（%s算法）\n", 
                        processName, size, algorithm));
                    processNameField.setText("");
                    sizeField.setText("");
                } else {
                    logArea.append(String.format("为进程%s分配%dK内存失败：没有足够的空闲空间\n", 
                        processName, size));
                }
                
                updateDisplay();
                logArea.setCaretPosition(logArea.getDocument().getLength());
                
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(PartitionPanel.this, 
                    "大小必须是有效的数字", "输入错误", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    private class DeallocateAction implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            String processName = deallocateField.getText().trim();
            
            if (processName.isEmpty()) {
                JOptionPane.showMessageDialog(PartitionPanel.this, 
                    "请输入要释放的进程名", "输入错误", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            boolean success = manager.deallocate(processName);
            
            if (success) {
                logArea.append(String.format("成功释放进程%s的内存\n", processName));
                deallocateField.setText("");
            } else {
                logArea.append(String.format("释放进程%s的内存失败：找不到该进程\n", processName));
            }
            
            updateDisplay();
            logArea.setCaretPosition(logArea.getDocument().getLength());
        }
    }
} 