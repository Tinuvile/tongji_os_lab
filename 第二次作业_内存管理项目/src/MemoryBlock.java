/**
 * 内存块类，用于表示内存中的分区
 */
public class MemoryBlock {
    private int startAddress;  // 起始地址
    private int size;          // 大小
    private boolean allocated; // 是否已分配
    private String processName; // 进程名（如果已分配）
    
    public MemoryBlock(int startAddress, int size, boolean allocated) {
        this.startAddress = startAddress;
        this.size = size;
        this.allocated = allocated;
        this.processName = null;
    }
    
    public MemoryBlock(int startAddress, int size, boolean allocated, String processName) {
        this.startAddress = startAddress;
        this.size = size;
        this.allocated = allocated;
        this.processName = processName;
    }
    
    // Getter和Setter方法
    public int getStartAddress() { return startAddress; }
    public void setStartAddress(int startAddress) { this.startAddress = startAddress; }
    
    public int getSize() { return size; }
    public void setSize(int size) { this.size = size; }
    
    public boolean isAllocated() { return allocated; }
    public void setAllocated(boolean allocated) { this.allocated = allocated; }
    
    public String getProcessName() { return processName; }
    public void setProcessName(String processName) { this.processName = processName; }
    
    public int getEndAddress() {
        return startAddress + size - 1;
    }
    
    @Override
    public String toString() {
        if (allocated) {
            return String.format("进程%s: %dK - %dK (%dK)", 
                processName, startAddress, getEndAddress(), size);
        } else {
            return String.format("空闲: %dK - %dK (%dK)", 
                startAddress, getEndAddress(), size);
        }
    }
} 