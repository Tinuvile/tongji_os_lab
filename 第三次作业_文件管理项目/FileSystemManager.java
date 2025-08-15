import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.util.List;

/**
 * 文件系统管理器GUI主界面
 */
public class FileSystemManager extends JFrame {
    private VirtualFileSystem fileSystem;
    
    // GUI组件
    private JLabel statusLabel;
    private JLabel currentPathLabel;
    private JList<String> directoryList;
    private DefaultListModel<String> directoryListModel;
    private JTextArea logArea;
    private JTextArea fileContentArea;
    private JTextField commandField;
    
    // 按钮
    private JButton formatBtn, createDirBtn, removeDirBtn, changeDirBtn;
    private JButton createFileBtn, openFileBtn, closeFileBtn, readFileBtn, 
                   writeFileBtn, deleteFileBtn, showInfoBtn;

    public FileSystemManager() {
        fileSystem = new VirtualFileSystem();
        initializeGUI();
        updateDisplay();
    }

    private void initializeGUI() {
        setTitle("虚拟文件系统管理器");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // 创建主面板
        createTopPanel();
        createCenterPanel();
        createBottomPanel();

        // 设置窗口属性
        setSize(1000, 700);
        setLocationRelativeTo(null);
        setResizable(true);
    }

    /**
     * 创建顶部状态面板
     */
    private void createTopPanel() {
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBorder(new EmptyBorder(10, 10, 5, 10));

        // 状态标签
        statusLabel = new JLabel("状态: 未格式化");
        statusLabel.setFont(new Font("Microsoft YaHei", Font.BOLD, 14));
        statusLabel.setForeground(Color.RED);

        // 当前路径标签
        currentPathLabel = new JLabel("当前路径: ");
        currentPathLabel.setFont(new Font("Microsoft YaHei", Font.PLAIN, 12));

        topPanel.add(statusLabel, BorderLayout.NORTH);
        topPanel.add(currentPathLabel, BorderLayout.SOUTH);

        add(topPanel, BorderLayout.NORTH);
    }

    /**
     * 创建中央主要操作面板
     */
    private void createCenterPanel() {
        JPanel centerPanel = new JPanel(new BorderLayout());
        
        // 创建左侧操作面板
        JPanel leftPanel = createOperationPanel();
        
        // 创建右侧显示面板
        JPanel rightPanel = createDisplayPanel();
        
        // 使用分割面板
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, leftPanel, rightPanel);
        splitPane.setDividerLocation(300);
        splitPane.setResizeWeight(0.3);
        
        centerPanel.add(splitPane, BorderLayout.CENTER);
        add(centerPanel, BorderLayout.CENTER);
    }

    /**
     * 创建操作按钮面板
     */
    private JPanel createOperationPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(new TitledBorder("文件系统操作"));

        // 创建按钮面板
        JPanel buttonPanel = new JPanel(new GridLayout(0, 1, 5, 5));
        buttonPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

        // 系统操作按钮
        formatBtn = createStyledButton("格式化", e -> formatFileSystem());
        
        // 目录操作按钮
        createDirBtn = createStyledButton("创建目录", e -> createDirectory());
        removeDirBtn = createStyledButton("删除目录", e -> removeDirectory());
        changeDirBtn = createStyledButton("切换目录", e -> changeDirectory());
        
        // 文件操作按钮
        createFileBtn = createStyledButton("创建文件", e -> createFile());
        openFileBtn = createStyledButton("打开文件", e -> openFile());
        closeFileBtn = createStyledButton("关闭文件", e -> closeFile());
        readFileBtn = createStyledButton("读取文件", e -> readFile());
        writeFileBtn = createStyledButton("写入文件", e -> writeFile());
        deleteFileBtn = createStyledButton("删除文件", e -> deleteFile());
        showInfoBtn = createStyledButton("显示信息", e -> showFileInfo());

        // 添加按钮到面板
        buttonPanel.add(formatBtn);
        buttonPanel.add(new JSeparator());
        buttonPanel.add(createDirBtn);
        buttonPanel.add(removeDirBtn);
        buttonPanel.add(changeDirBtn);
        buttonPanel.add(new JSeparator());
        buttonPanel.add(createFileBtn);
        buttonPanel.add(openFileBtn);
        buttonPanel.add(closeFileBtn);
        buttonPanel.add(readFileBtn);
        buttonPanel.add(writeFileBtn);
        buttonPanel.add(deleteFileBtn);
        buttonPanel.add(showInfoBtn);

        panel.add(buttonPanel, BorderLayout.CENTER);
        return panel;
    }

    /**
     * 创建显示面板
     */
    private JPanel createDisplayPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        
        // 创建选项卡面板
        JTabbedPane tabbedPane = new JTabbedPane();
        
        // 目录浏览选项卡
        JPanel dirPanel = new JPanel(new BorderLayout());
        dirPanel.setBorder(new TitledBorder("目录内容"));
        
        directoryListModel = new DefaultListModel<>();
        directoryList = new JList<>(directoryListModel);
        directoryList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        directoryList.setFont(new Font("Consolas", Font.PLAIN, 12));
        directoryList.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    String selected = directoryList.getSelectedValue();
                    if (selected != null && selected.endsWith("/")) {
                        String dirName = selected.substring(0, selected.length() - 1);
                        if (fileSystem.changeDirectory(dirName)) {
                            updateDisplay();
                            logMessage("已切换到目录: " + dirName);
                        }
                    }
                }
            }
        });
        
        JScrollPane dirScrollPane = new JScrollPane(directoryList);
        dirScrollPane.setPreferredSize(new Dimension(0, 200));
        dirPanel.add(dirScrollPane, BorderLayout.CENTER);
        
        // 文件内容选项卡
        JPanel contentPanel = new JPanel(new BorderLayout());
        contentPanel.setBorder(new TitledBorder("文件内容"));
        
        fileContentArea = new JTextArea();
        fileContentArea.setFont(new Font("Consolas", Font.PLAIN, 12));
        fileContentArea.setLineWrap(true);
        fileContentArea.setWrapStyleWord(true);
        
        JScrollPane contentScrollPane = new JScrollPane(fileContentArea);
        contentPanel.add(contentScrollPane, BorderLayout.CENTER);
        
        // 添加选项卡
        tabbedPane.addTab("目录浏览", dirPanel);
        tabbedPane.addTab("文件内容", contentPanel);
        
        panel.add(tabbedPane, BorderLayout.CENTER);
        return panel;
    }

    /**
     * 创建底部日志面板
     */
    private void createBottomPanel() {
        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.setBorder(new TitledBorder("操作日志"));
        bottomPanel.setPreferredSize(new Dimension(0, 150));

        logArea = new JTextArea();
        // 使用最佳的中文字体
        logArea.setFont(getBestChineseFont(11));
        logArea.setEditable(false);
        logArea.setBackground(Color.BLACK);
        logArea.setForeground(Color.GREEN);

        JScrollPane logScrollPane = new JScrollPane(logArea);
        bottomPanel.add(logScrollPane, BorderLayout.CENTER);

        add(bottomPanel, BorderLayout.SOUTH);
    }

    /**
     * 获取最适合显示中文的字体
     */
    private Font getBestChineseFont(int size) {
        // 优先使用的字体列表，按优先级排序
        String[] fontNames = {
            "Microsoft YaHei UI",    // 微软雅黑UI (更好的中文显示)
            "Microsoft YaHei",       // 微软雅黑
            "SimHei",               // 黑体
            "SimSun",               // 宋体  
            "NSimSun",              // 新宋体
            "Courier New",          // 等宽英文字体
            "Monospaced",           // Java默认等宽字体
            "Dialog"                // 系统默认字体
        };
        
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        String[] availableFonts = ge.getAvailableFontFamilyNames();
        
        // 查找第一个可用的字体
        for (String fontName : fontNames) {
            for (String availableFont : availableFonts) {
                if (availableFont.equals(fontName)) {
                    return new Font(fontName, Font.PLAIN, size);
                }
            }
        }
        
        // 如果没有找到合适的字体，返回默认字体
        return new Font("SansSerif", Font.PLAIN, size);
    }

    /**
     * 创建样式化按钮
     */
    private JButton createStyledButton(String text, ActionListener action) {
        JButton button = new JButton(text);
        button.setFont(new Font("Microsoft YaHei", Font.PLAIN, 12));
        button.setPreferredSize(new Dimension(120, 30));
        button.addActionListener(action);
        button.setFocusPainted(false);
        return button;
    }

    /**
     * 格式化文件系统
     */
    private void formatFileSystem() {
        if (fileSystem.format()) {
            logMessage("文件系统格式化成功");
            updateDisplay();
        } else {
            logMessage("文件系统格式化失败");
        }
    }

    /**
     * 创建目录
     */
    private void createDirectory() {
        if (!fileSystem.isFormatted()) {
            logMessage("错误: 文件系统未格式化");
            return;
        }
        
        String name = JOptionPane.showInputDialog(this, "请输入目录名称:", "创建目录", JOptionPane.PLAIN_MESSAGE);
        if (name != null && !name.trim().isEmpty()) {
            if (fileSystem.createDirectory(name)) {
                logMessage("成功创建目录: " + name);
                updateDisplay();
            } else {
                logMessage("创建目录失败: " + name);
            }
        }
    }

    /**
     * 删除目录
     */
    private void removeDirectory() {
        if (!fileSystem.isFormatted()) {
            logMessage("错误: 文件系统未格式化");
            return;
        }
        
        String name = JOptionPane.showInputDialog(this, "请输入要删除的目录名称:", "删除目录", JOptionPane.PLAIN_MESSAGE);
        if (name != null && !name.trim().isEmpty()) {
            if (fileSystem.removeDirectory(name)) {
                logMessage("成功删除目录: " + name);
                updateDisplay();
            } else {
                logMessage("删除目录失败: " + name + " (目录不存在或不为空)");
            }
        }
    }

    /**
     * 切换目录
     */
    private void changeDirectory() {
        if (!fileSystem.isFormatted()) {
            logMessage("错误: 文件系统未格式化");
            return;
        }
        
        String path = JOptionPane.showInputDialog(this, "请输入目录路径 (.. 返回上级, / 返回根目录):", "切换目录", JOptionPane.PLAIN_MESSAGE);
        if (path != null && !path.trim().isEmpty()) {
            if (fileSystem.changeDirectory(path)) {
                logMessage("成功切换到目录: " + path);
                updateDisplay();
            } else {
                logMessage("切换目录失败: " + path);
            }
        }
    }

    /**
     * 创建文件
     */
    private void createFile() {
        if (!fileSystem.isFormatted()) {
            logMessage("错误: 文件系统未格式化");
            return;
        }
        
        String name = JOptionPane.showInputDialog(this, "请输入文件名称:", "创建文件", JOptionPane.PLAIN_MESSAGE);
        if (name != null && !name.trim().isEmpty()) {
            if (fileSystem.createFile(name)) {
                logMessage("成功创建文件: " + name);
                updateDisplay();
            } else {
                logMessage("创建文件失败: " + name);
            }
        }
    }

    /**
     * 打开文件
     */
    private void openFile() {
        if (!fileSystem.isFormatted()) {
            logMessage("错误: 文件系统未格式化");
            return;
        }
        
        String name = JOptionPane.showInputDialog(this, "请输入要打开的文件名称:", "打开文件", JOptionPane.PLAIN_MESSAGE);
        if (name != null && !name.trim().isEmpty()) {
            if (fileSystem.openFile(name)) {
                logMessage("成功打开文件: " + name);
                logMessage("当前打开的文件: " + fileSystem.getOpenFiles());
            } else {
                logMessage("打开文件失败: " + name);
            }
        }
    }

    /**
     * 关闭文件
     */
    private void closeFile() {
        if (!fileSystem.isFormatted()) {
            logMessage("错误: 文件系统未格式化");
            return;
        }
        
        List<String> openFiles = fileSystem.getOpenFiles();
        if (openFiles.isEmpty()) {
            logMessage("没有打开的文件");
            return;
        }
        
        String name = (String) JOptionPane.showInputDialog(this, "请选择要关闭的文件:", "关闭文件", 
                JOptionPane.PLAIN_MESSAGE, null, openFiles.toArray(), openFiles.get(0));
        if (name != null) {
            if (fileSystem.closeFile(name)) {
                logMessage("成功关闭文件: " + name);
                fileContentArea.setText("");
            } else {
                logMessage("关闭文件失败: " + name);
            }
        }
    }

    /**
     * 读取文件
     */
    private void readFile() {
        if (!fileSystem.isFormatted()) {
            logMessage("错误: 文件系统未格式化");
            return;
        }
        
        List<String> openFiles = fileSystem.getOpenFiles();
        if (openFiles.isEmpty()) {
            logMessage("没有打开的文件，请先打开文件");
            return;
        }
        
        String name = (String) JOptionPane.showInputDialog(this, "请选择要读取的文件:", "读取文件", 
                JOptionPane.PLAIN_MESSAGE, null, openFiles.toArray(), openFiles.get(0));
        if (name != null) {
            String content = fileSystem.readFile(name);
            if (content != null) {
                fileContentArea.setText(content);
                logMessage("成功读取文件: " + name + " (" + content.length() + " 字符)");
            } else {
                logMessage("读取文件失败: " + name);
            }
        }
    }

    /**
     * 写入文件
     */
    private void writeFile() {
        if (!fileSystem.isFormatted()) {
            logMessage("错误: 文件系统未格式化");
            return;
        }
        
        List<String> openFiles = fileSystem.getOpenFiles();
        if (openFiles.isEmpty()) {
            logMessage("没有打开的文件，请先打开文件");
            return;
        }
        
        String name = (String) JOptionPane.showInputDialog(this, "请选择要写入的文件:", "写入文件", 
                JOptionPane.PLAIN_MESSAGE, null, openFiles.toArray(), openFiles.get(0));
        if (name != null) {
            String content = JOptionPane.showInputDialog(this, "请输入文件内容:", "写入文件", JOptionPane.PLAIN_MESSAGE);
            if (content != null) {
                if (fileSystem.writeFile(name, content)) {
                    logMessage("成功写入文件: " + name + " (" + content.length() + " 字符)");
                    updateDisplay();
                } else {
                    logMessage("写入文件失败: " + name);
                }
            }
        }
    }

    /**
     * 删除文件
     */
    private void deleteFile() {
        if (!fileSystem.isFormatted()) {
            logMessage("错误: 文件系统未格式化");
            return;
        }
        
        String name = JOptionPane.showInputDialog(this, "请输入要删除的文件名称:", "删除文件", JOptionPane.PLAIN_MESSAGE);
        if (name != null && !name.trim().isEmpty()) {
            int result = JOptionPane.showConfirmDialog(this, "确定要删除文件 '" + name + "' 吗?", "确认删除", JOptionPane.YES_NO_OPTION);
            if (result == JOptionPane.YES_OPTION) {
                if (fileSystem.deleteFile(name)) {
                    logMessage("成功删除文件: " + name);
                    updateDisplay();
                    fileContentArea.setText("");
                } else {
                    logMessage("删除文件失败: " + name);
                }
            }
        }
    }

    /**
     * 显示文件信息
     */
    private void showFileInfo() {
        if (!fileSystem.isFormatted()) {
            logMessage("错误: 文件系统未格式化");
            return;
        }
        
        String name = JOptionPane.showInputDialog(this, "请输入文件/目录名称:", "显示信息", JOptionPane.PLAIN_MESSAGE);
        if (name != null && !name.trim().isEmpty()) {
            String info = fileSystem.getFileInfo(name);
            if (info != null) {
                JOptionPane.showMessageDialog(this, info, "文件信息", JOptionPane.INFORMATION_MESSAGE);
                logMessage("显示文件信息: " + name);
            } else {
                logMessage("文件/目录不存在: " + name);
            }
        }
    }

    /**
     * 更新界面显示
     */
    private void updateDisplay() {
        if (fileSystem.isFormatted()) {
            statusLabel.setText("状态: 已格式化");
            statusLabel.setForeground(Color.GREEN);
            currentPathLabel.setText("当前路径: " + fileSystem.getCurrentPath());
            
            // 更新目录列表
            directoryListModel.clear();
            List<String> items = fileSystem.listDirectory();
            for (String item : items) {
                directoryListModel.addElement(item);
            }
            
            // 启用按钮
            enableButtons(true);
        } else {
            statusLabel.setText("状态: 未格式化");
            statusLabel.setForeground(Color.RED);
            currentPathLabel.setText("当前路径: ");
            directoryListModel.clear();
            
            // 禁用除格式化外的所有按钮
            enableButtons(false);
        }
    }

    /**
     * 启用/禁用按钮
     */
    private void enableButtons(boolean enabled) {
        createDirBtn.setEnabled(enabled);
        removeDirBtn.setEnabled(enabled);
        changeDirBtn.setEnabled(enabled);
        createFileBtn.setEnabled(enabled);
        openFileBtn.setEnabled(enabled);
        closeFileBtn.setEnabled(enabled);
        readFileBtn.setEnabled(enabled);
        writeFileBtn.setEnabled(enabled);
        deleteFileBtn.setEnabled(enabled);
        showInfoBtn.setEnabled(enabled);
    }

    /**
     * 记录日志消息
     */
    private void logMessage(String message) {
        try {
            String timestamp = java.time.LocalTime.now().toString().substring(0, 8);
            String logEntry = "[" + timestamp + "] " + message + "\n";
            logArea.append(logEntry);
            logArea.setCaretPosition(logArea.getDocument().getLength());
        } catch (Exception e) {
            // 如果出现字符编码问题，使用基本的ASCII字符记录
            String timestamp = java.time.LocalTime.now().toString().substring(0, 8);
            logArea.append("[" + timestamp + "] " + "Log message (encoding issue)\n");
            logArea.setCaretPosition(logArea.getDocument().getLength());
        }
    }

    /**
     * 主方法
     */
    public static void main(String[] args) {
        // 创建并显示GUI
        SwingUtilities.invokeLater(() -> {
            new FileSystemManager().setVisible(true);
        });
    }
} 