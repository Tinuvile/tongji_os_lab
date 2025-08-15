/**
 * 页面类，表示一个内存页面
 */
public class Page {
    private int pageNumber;        // 页面号
    private boolean inMemory;      // 是否在内存中
    private int physicalFrame;     // 物理块号（如果在内存中）
    private long lastAccessTime;   // 最后访问时间（用于LRU算法）
    private int loadTime;         // 装入时间（用于FIFO算法）
    
    public Page(int pageNumber) {
        this.pageNumber = pageNumber;
        this.inMemory = false;
        this.physicalFrame = -1;
        this.lastAccessTime = 0;
        this.loadTime = 0;
    }
    
    // Getter和Setter方法
    public int getPageNumber() { return pageNumber; }
    public void setPageNumber(int pageNumber) { this.pageNumber = pageNumber; }
    
    public boolean isInMemory() { return inMemory; }
    public void setInMemory(boolean inMemory) { this.inMemory = inMemory; }
    
    public int getPhysicalFrame() { return physicalFrame; }
    public void setPhysicalFrame(int physicalFrame) { this.physicalFrame = physicalFrame; }
    
    public long getLastAccessTime() { return lastAccessTime; }
    public void setLastAccessTime(long lastAccessTime) { this.lastAccessTime = lastAccessTime; }
    
    public int getLoadTime() { return loadTime; }
    public void setLoadTime(int loadTime) { this.loadTime = loadTime; }
    
    @Override
    public String toString() {
        if (inMemory) {
            return String.format("页面%d -> 块%d", pageNumber, physicalFrame);
        } else {
            return String.format("页面%d (未装入)", pageNumber);
        }
    }
} 