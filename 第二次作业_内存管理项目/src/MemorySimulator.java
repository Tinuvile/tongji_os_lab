import javax.swing.*;
import java.awt.*;

/**
 * 内存管理模拟系统主程序
 */
public class MemorySimulator {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {
                // 如果设置系统外观失败，使用默认外观
                System.out.println("无法设置系统外观，使用默认外观");
            }
            
            JFrame frame = new JFrame("内存管理模拟系统");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(1200, 800);
            frame.setLocationRelativeTo(null);
            
            // 创建主面板
            MainPanel mainPanel = new MainPanel();
            frame.add(mainPanel);
            
            frame.setVisible(true);
        });
    }
} 