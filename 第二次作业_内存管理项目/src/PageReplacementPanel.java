import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

/**
 * 请求调页存储管理模拟面板
 */
public class PageReplacementPanel extends JPanel {
    private PageReplacementManager manager;
    private JTable pageTable;
    private DefaultTableModel pageTableModel;
    private JTable memoryTable;
    private DefaultTableModel memoryTableModel;
    private JTextArea logArea;
    private JComboBox<String> algorithmComboBox;
    private JLabel statisticsLabel;
    private JProgressBar simulationProgress;
    private List<Integer> currentSequence;
    
    public PageReplacementPanel() {
        manager = new PageReplacementManager();
        initializeComponents();
        updateDisplay();
    }
    
    private void initializeComponents() {
        setLayout(new BorderLayout());
        
        // 顶部控制面板
        JPanel controlPanel = createControlPanel();
        add(controlPanel, BorderLayout.NORTH);
        
        // 中间面板
        JPanel centerPanel = new JPanel(new BorderLayout());
        
        // 上方：页表和内存状态
        JPanel tablePanel = createTablePanel();
        centerPanel.add(tablePanel, BorderLayout.NORTH);
        
        // 下方：日志区域
        JPanel logPanel = createLogPanel();
        centerPanel.add(logPanel, BorderLayout.CENTER);
        
        add(centerPanel, BorderLayout.CENTER);
        
        // 底部状态面板
        JPanel statusPanel = createStatusPanel();
        add(statusPanel, BorderLayout.SOUTH);
    }
    
    private JPanel createControlPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        panel.setBorder(BorderFactory.createTitledBorder("模拟控制"));
        
        panel.add(new JLabel("置换算法:"));
        algorithmComboBox = new JComboBox<>(new String[]{"FIFO", "LRU"});
        panel.add(algorithmComboBox);
        
        JButton generateButton = new JButton("生成指令序列");
        generateButton.addActionListener(new GenerateSequenceAction());
        panel.add(generateButton);
        
        JButton simulateButton = new JButton("开始模拟");
        simulateButton.addActionListener(new SimulateAction());
        panel.add(simulateButton);
        
        JButton stepButton = new JButton("单步执行");
        stepButton.addActionListener(new StepAction());
        panel.add(stepButton);
        
        JButton resetButton = new JButton("重置");
        resetButton.addActionListener(e -> {
            manager.reset();
            updateDisplay();
            logArea.setText("系统已重置\n");
            simulationProgress.setValue(0);
        });
        panel.add(resetButton);
        
        return panel;
    }
    
    private JPanel createTablePanel() {
        JPanel panel = new JPanel(new GridLayout(1, 2, 10, 0));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 0, 10));
        
        // 页表
        JPanel pageTablePanel = new JPanel(new BorderLayout());
        pageTablePanel.setBorder(BorderFactory.createTitledBorder("页表状态"));
        
        String[] pageColumns = {"页号", "在内存", "物理块号"};
        pageTableModel = new DefaultTableModel(pageColumns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        pageTable = new JTable(pageTableModel);
        pageTable.setRowHeight(20);
        JScrollPane pageScrollPane = new JScrollPane(pageTable);
        pageScrollPane.setPreferredSize(new Dimension(0, 200));
        pageTablePanel.add(pageScrollPane, BorderLayout.CENTER);
        
        // 物理内存状态
        JPanel memoryTablePanel = new JPanel(new BorderLayout());
        memoryTablePanel.setBorder(BorderFactory.createTitledBorder("物理内存状态"));
        
        String[] memoryColumns = {"内存块号", "页号", "装入时间", "最后访问时间"};
        memoryTableModel = new DefaultTableModel(memoryColumns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        memoryTable = new JTable(memoryTableModel);
        memoryTable.setRowHeight(20);
        JScrollPane memoryScrollPane = new JScrollPane(memoryTable);
        memoryScrollPane.setPreferredSize(new Dimension(0, 200));
        memoryTablePanel.add(memoryScrollPane, BorderLayout.CENTER);
        
        panel.add(pageTablePanel);
        panel.add(memoryTablePanel);
        
        return panel;
    }
    
    private JPanel createLogPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("执行日志"));
        
        logArea = new JTextArea();
        logArea.setEditable(false);
        logArea.setFont(new Font("微软雅黑", Font.PLAIN, 11));
        
        JScrollPane scrollPane = new JScrollPane(logArea);
        scrollPane.setPreferredSize(new Dimension(0, 200));
        panel.add(scrollPane, BorderLayout.CENTER);
        
        // 添加清空日志按钮
        JButton clearLogButton = new JButton("清空日志");
        clearLogButton.addActionListener(e -> logArea.setText(""));
        panel.add(clearLogButton, BorderLayout.SOUTH);
        
        return panel;
    }
    
    private JPanel createStatusPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("模拟状态"));
        
        // 统计信息
        statisticsLabel = new JLabel("等待开始模拟...");
        panel.add(statisticsLabel, BorderLayout.WEST);
        
        // 进度条
        simulationProgress = new JProgressBar(0, 320);
        simulationProgress.setStringPainted(true);
        simulationProgress.setString("0/320");
        panel.add(simulationProgress, BorderLayout.CENTER);
        
        return panel;
    }
    
    private void updateDisplay() {
        // 更新页表
        pageTableModel.setRowCount(0);
        Page[] pages = manager.getPages();
        
        for (Page page : pages) {
            Object[] row = {
                page.getPageNumber(),
                page.isInMemory() ? "是" : "否",
                page.isInMemory() ? page.getPhysicalFrame() : "-"
            };
            pageTableModel.addRow(row);
        }
        
        // 更新物理内存表
        memoryTableModel.setRowCount(0);
        int[] physicalMemory = manager.getPhysicalMemory();
        
        for (int i = 0; i < physicalMemory.length; i++) {
            Object[] row = new Object[4];
            row[0] = i;
            
            if (physicalMemory[i] == -1) {
                row[1] = "空闲";
                row[2] = "-";
                row[3] = "-";
            } else {
                Page page = pages[physicalMemory[i]];
                row[1] = physicalMemory[i];
                row[2] = page.getLoadTime();
                row[3] = page.getLastAccessTime();
            }
            memoryTableModel.addRow(row);
        }
        
        // 更新统计信息
        statisticsLabel.setText(String.format("缺页次数: %d, 缺页率: %.2f%%", 
            manager.getPageFaults(), manager.getPageFaultRate() * 100));
    }
    
    private class GenerateSequenceAction implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            currentSequence = manager.generateInstructionSequence();
            logArea.append("已生成新的指令访问序列\n");
            logArea.append(String.format("指令访问模式: 50%%顺序，25%%前地址随机，25%%后地址随机\n"));
            logArea.append("序列预览: ");
            
            // 显示前20个指令作为预览
            for (int i = 0; i < Math.min(20, currentSequence.size()); i++) {
                logArea.append(currentSequence.get(i) + " ");
            }
            if (currentSequence.size() > 20) {
                logArea.append("...");
            }
            logArea.append("\n\n");
            
            logArea.setCaretPosition(logArea.getDocument().getLength());
        }
    }
    
    private class SimulateAction implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            if (currentSequence == null) {
                currentSequence = manager.generateInstructionSequence();
                logArea.append("自动生成指令序列\n");
            }
            
            String algorithm = (String) algorithmComboBox.getSelectedItem();
            
            // 在后台线程中执行模拟，避免阻塞UI
            SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
                @Override
                protected Void doInBackground() throws Exception {
                    manager.simulateExecution(currentSequence, algorithm);
                    return null;
                }
                
                @Override
                protected void done() {
                    // 显示执行日志
                    List<String> logs = manager.getAccessLog();
                    for (String log : logs) {
                        logArea.append(log + "\n");
                    }
                    
                    updateDisplay();
                    simulationProgress.setValue(320);
                    simulationProgress.setString("320/320 (完成)");
                    logArea.setCaretPosition(logArea.getDocument().getLength());
                }
            };
            
            worker.execute();
        }
    }
    
    private class StepAction implements ActionListener {
        private int currentStep = 0;
        
        @Override
        public void actionPerformed(ActionEvent e) {
            if (currentSequence == null) {
                currentSequence = manager.generateInstructionSequence();
                logArea.append("自动生成指令序列\n");
                manager.reset();
                currentStep = 0;
            }
            
            if (currentStep < currentSequence.size()) {
                String algorithm = (String) algorithmComboBox.getSelectedItem();
                int instruction = currentSequence.get(currentStep);
                
                boolean hit = manager.accessInstruction(instruction, algorithm);
                
                // 显示这一步的日志
                List<String> logs = manager.getAccessLog();
                if (!logs.isEmpty()) {
                    String lastLog = logs.get(logs.size() - 1);
                    logArea.append(String.format("步骤%d: %s\n", currentStep + 1, lastLog));
                }
                
                updateDisplay();
                currentStep++;
                simulationProgress.setValue(currentStep);
                simulationProgress.setString(currentStep + "/320");
                
                if (currentStep >= currentSequence.size()) {
                    logArea.append(String.format("\n单步执行完成！缺页率: %.2f%%\n", 
                        manager.getPageFaultRate() * 100));
                    currentStep = 0; // 重置以便下次单步执行
                }
                
                logArea.setCaretPosition(logArea.getDocument().getLength());
            }
        }
    }
} 