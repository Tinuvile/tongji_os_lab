import java.util.*;

/**
 * 页面置换管理器，实现FIFO和LRU算法
 */
public class PageReplacementManager {
    private final int TOTAL_PAGES = 32;      // 总页面数
    private final int MEMORY_FRAMES = 4;     // 内存块数
    private final int INSTRUCTIONS_PER_PAGE = 10; // 每页指令数
    private final int TOTAL_INSTRUCTIONS = 320;   // 总指令数
    
    private Page[] pages;                    // 页表
    private int[] physicalMemory;           // 物理内存块（存储页面号，-1表示空闲）
    private int pageFaults;                 // 缺页次数
    private int currentTime;                // 当前时间
    private List<String> accessLog;         // 访问日志
    
    public PageReplacementManager() {
        reset();
    }
    
    /**
     * 重置系统状态
     */
    public void reset() {
        pages = new Page[TOTAL_PAGES];
        for (int i = 0; i < TOTAL_PAGES; i++) {
            pages[i] = new Page(i);
        }
        
        physicalMemory = new int[MEMORY_FRAMES];
        Arrays.fill(physicalMemory, -1);
        
        pageFaults = 0;
        currentTime = 0;
        accessLog = new ArrayList<>();
    }
    
    /**
     * 访问指定的指令地址
     */
    public boolean accessInstruction(int instructionAddress, String algorithm) {
        int pageNumber = instructionAddress / INSTRUCTIONS_PER_PAGE;
        Page page = pages[pageNumber];
        currentTime++;
        
        if (page.isInMemory()) {
            // 页面在内存中，命中
            page.setLastAccessTime(currentTime);
            int physicalAddress = page.getPhysicalFrame() * INSTRUCTIONS_PER_PAGE + 
                                (instructionAddress % INSTRUCTIONS_PER_PAGE);
            accessLog.add(String.format("指令%d -> 页面%d (命中) -> 物理地址%d", 
                instructionAddress, pageNumber, physicalAddress));
            return true;
        } else {
            // 页面不在内存中，缺页
            pageFaults++;
            
            // 寻找空闲块或选择置换页面
            int frameToUse = findAvailableFrame();
            if (frameToUse == -1) {
                // 没有空闲块，需要页面置换
                if ("FIFO".equals(algorithm)) {
                    frameToUse = selectPageFIFO();
                } else if ("LRU".equals(algorithm)) {
                    frameToUse = selectPageLRU();
                }
            }
            
            // 如果该块原来有页面，需要移出
            if (physicalMemory[frameToUse] != -1) {
                Page oldPage = pages[physicalMemory[frameToUse]];
                oldPage.setInMemory(false);
                oldPage.setPhysicalFrame(-1);
                accessLog.add(String.format("页面%d被置换出内存块%d", 
                    oldPage.getPageNumber(), frameToUse));
            }
            
            // 装入新页面
            page.setInMemory(true);
            page.setPhysicalFrame(frameToUse);
            page.setLastAccessTime(currentTime);
            page.setLoadTime(currentTime);
            physicalMemory[frameToUse] = pageNumber;
            
            int physicalAddress = frameToUse * INSTRUCTIONS_PER_PAGE + 
                                (instructionAddress % INSTRUCTIONS_PER_PAGE);
            accessLog.add(String.format("指令%d -> 页面%d (缺页) -> 装入块%d -> 物理地址%d", 
                instructionAddress, pageNumber, frameToUse, physicalAddress));
            return false;
        }
    }
    
    /**
     * 寻找空闲内存块
     */
    private int findAvailableFrame() {
        for (int i = 0; i < MEMORY_FRAMES; i++) {
            if (physicalMemory[i] == -1) {
                return i;
            }
        }
        return -1;
    }
    
    /**
     * FIFO算法选择置换页面
     */
    private int selectPageFIFO() {
        int oldestTime = Integer.MAX_VALUE;
        int frameToReplace = 0;
        
        for (int i = 0; i < MEMORY_FRAMES; i++) {
            int pageInFrame = physicalMemory[i];
            if (pageInFrame != -1) {
                Page page = pages[pageInFrame];
                if (page.getLoadTime() < oldestTime) {
                    oldestTime = page.getLoadTime();
                    frameToReplace = i;
                }
            }
        }
        return frameToReplace;
    }
    
    /**
     * LRU算法选择置换页面
     */
    private int selectPageLRU() {
        long oldestAccessTime = Long.MAX_VALUE;
        int frameToReplace = 0;
        
        for (int i = 0; i < MEMORY_FRAMES; i++) {
            int pageInFrame = physicalMemory[i];
            if (pageInFrame != -1) {
                Page page = pages[pageInFrame];
                if (page.getLastAccessTime() < oldestAccessTime) {
                    oldestAccessTime = page.getLastAccessTime();
                    frameToReplace = i;
                }
            }
        }
        return frameToReplace;
    }
    
    /**
     * 生成指令访问序列
     * 50%顺序执行，25%前地址均匀分布，25%后地址均匀分布
     */
    public List<Integer> generateInstructionSequence() {
        List<Integer> sequence = new ArrayList<>();
        Random random = new Random();
        
        int currentPC = 0; // 程序计数器
        
        for (int i = 0; i < TOTAL_INSTRUCTIONS; i++) {
            double prob = random.nextDouble();
            
            if (prob < 0.5) {
                // 50%概率顺序执行
                sequence.add(currentPC);
                currentPC = (currentPC + 1) % TOTAL_INSTRUCTIONS;
            } else if (prob < 0.75) {
                // 25%概率在前地址部分随机访问
                int addr = random.nextInt(currentPC == 0 ? 1 : currentPC);
                sequence.add(addr);
                currentPC = (addr + 1) % TOTAL_INSTRUCTIONS;
            } else {
                // 25%概率在后地址部分随机访问
                int addr = currentPC + 1 + random.nextInt(TOTAL_INSTRUCTIONS - currentPC - 1);
                if (addr >= TOTAL_INSTRUCTIONS) {
                    addr = random.nextInt(TOTAL_INSTRUCTIONS);
                }
                sequence.add(addr);
                currentPC = (addr + 1) % TOTAL_INSTRUCTIONS;
            }
        }
        
        return sequence;
    }
    
    /**
     * 模拟执行指令序列
     */
    public void simulateExecution(List<Integer> instructionSequence, String algorithm) {
        reset();
        accessLog.add("开始执行指令序列...");
        
        for (int instruction : instructionSequence) {
            accessInstruction(instruction, algorithm);
        }
        
        double pageFaultRate = (double) pageFaults / TOTAL_INSTRUCTIONS;
        accessLog.add(String.format("\n执行完成！总指令数: %d, 缺页次数: %d, 缺页率: %.2f%%", 
            TOTAL_INSTRUCTIONS, pageFaults, pageFaultRate * 100));
    }
    
    // Getter方法
    public Page[] getPages() { return pages; }
    public int[] getPhysicalMemory() { return physicalMemory; }
    public int getPageFaults() { return pageFaults; }
    public List<String> getAccessLog() { return accessLog; }
    public double getPageFaultRate() { 
        return (double) pageFaults / TOTAL_INSTRUCTIONS; 
    }
    
    /**
     * 获取当前内存状态字符串
     */
    public String getMemoryStatus() {
        StringBuilder sb = new StringBuilder();
        sb.append("物理内存状态:\n");
        for (int i = 0; i < MEMORY_FRAMES; i++) {
            if (physicalMemory[i] == -1) {
                sb.append(String.format("块%d: 空闲\n", i));
            } else {
                sb.append(String.format("块%d: 页面%d\n", i, physicalMemory[i]));
            }
        }
        return sb.toString();
    }
} 