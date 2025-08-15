import javax.swing.*;
import java.awt.*;

/**
 * 主界面面板，包含两个模拟功能的选择
 */
public class MainPanel extends JPanel {
    
    public MainPanel() {
        setLayout(new BorderLayout());
        
        // 标题
        JLabel titleLabel = new JLabel("内存管理模拟系统", JLabel.CENTER);
        titleLabel.setFont(new Font("微软雅黑", Font.BOLD, 24));
        titleLabel.setBorder(BorderFactory.createEmptyBorder(20, 0, 20, 0));
        add(titleLabel, BorderLayout.NORTH);
        
        // 选项卡面板
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        
        // 动态分区分配面板
        PartitionPanel partitionPanel = new PartitionPanel();
        tabbedPane.addTab("动态分区分配", partitionPanel);
        
        // 请求调页面板
        PageReplacementPanel pagePanel = new PageReplacementPanel();
        tabbedPane.addTab("请求调页存储", pagePanel);
        
        add(tabbedPane, BorderLayout.CENTER);
        
        // 底部信息
        JLabel infoLabel = new JLabel("选择上方选项卡进行不同的内存管理模拟", JLabel.CENTER);
        infoLabel.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        infoLabel.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
        add(infoLabel, BorderLayout.SOUTH);
    }
} 