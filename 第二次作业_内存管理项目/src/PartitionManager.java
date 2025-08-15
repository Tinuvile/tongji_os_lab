import java.util.*;

/**
 * 分区管理器，实现动态分区分配算法
 */
public class PartitionManager {
    private List<MemoryBlock> memoryBlocks;
    private final int totalMemory = 640; // 总内存640K
    
    public PartitionManager() {
        memoryBlocks = new ArrayList<>();
        // 初始化时整个内存都是空闲的
        memoryBlocks.add(new MemoryBlock(0, totalMemory, false));
    }
    
    /**
     * 重置内存状态
     */
    public void reset() {
        memoryBlocks.clear();
        memoryBlocks.add(new MemoryBlock(0, totalMemory, false));
    }
    
    /**
     * 首次适应算法分配内存
     */
    public boolean allocateFirstFit(String processName, int size) {
        for (int i = 0; i < memoryBlocks.size(); i++) {
            MemoryBlock block = memoryBlocks.get(i);
            if (!block.isAllocated() && block.getSize() >= size) {
                // 找到合适的空闲块
                if (block.getSize() == size) {
                    // 大小正好相等，直接分配
                    block.setAllocated(true);
                    block.setProcessName(processName);
                } else {
                    // 需要分割块
                    MemoryBlock allocatedBlock = new MemoryBlock(
                        block.getStartAddress(), size, true, processName);
                    MemoryBlock remainingBlock = new MemoryBlock(
                        block.getStartAddress() + size, 
                        block.getSize() - size, false);
                    
                    memoryBlocks.set(i, allocatedBlock);
                    memoryBlocks.add(i + 1, remainingBlock);
                }
                return true;
            }
        }
        return false; // 没有找到合适的空闲块
    }
    
    /**
     * 最佳适应算法分配内存
     */
    public boolean allocateBestFit(String processName, int size) {
        int bestIndex = -1;
        int bestSize = Integer.MAX_VALUE;
        
        // 找到最小的足够大的空闲块
        for (int i = 0; i < memoryBlocks.size(); i++) {
            MemoryBlock block = memoryBlocks.get(i);
            if (!block.isAllocated() && block.getSize() >= size && block.getSize() < bestSize) {
                bestIndex = i;
                bestSize = block.getSize();
            }
        }
        
        if (bestIndex == -1) {
            return false; // 没有找到合适的空闲块
        }
        
        MemoryBlock bestBlock = memoryBlocks.get(bestIndex);
        if (bestBlock.getSize() == size) {
            // 大小正好相等，直接分配
            bestBlock.setAllocated(true);
            bestBlock.setProcessName(processName);
        } else {
            // 需要分割块
            MemoryBlock allocatedBlock = new MemoryBlock(
                bestBlock.getStartAddress(), size, true, processName);
            MemoryBlock remainingBlock = new MemoryBlock(
                bestBlock.getStartAddress() + size, 
                bestBlock.getSize() - size, false);
            
            memoryBlocks.set(bestIndex, allocatedBlock);
            memoryBlocks.add(bestIndex + 1, remainingBlock);
        }
        return true;
    }
    
    /**
     * 释放内存
     */
    public boolean deallocate(String processName) {
        for (int i = 0; i < memoryBlocks.size(); i++) {
            MemoryBlock block = memoryBlocks.get(i);
            if (block.isAllocated() && processName.equals(block.getProcessName())) {
                block.setAllocated(false);
                block.setProcessName(null);
                
                // 合并相邻的空闲块
                mergeAdjacentBlocks();
                return true;
            }
        }
        return false;
    }
    
    /**
     * 合并相邻的空闲块
     */
    private void mergeAdjacentBlocks() {
        for (int i = 0; i < memoryBlocks.size() - 1; i++) {
            MemoryBlock current = memoryBlocks.get(i);
            MemoryBlock next = memoryBlocks.get(i + 1);
            
            if (!current.isAllocated() && !next.isAllocated()) {
                // 合并两个相邻的空闲块
                current.setSize(current.getSize() + next.getSize());
                memoryBlocks.remove(i + 1);
                i--; // 重新检查当前位置
            }
        }
    }
    
    /**
     * 获取当前内存块列表
     */
    public List<MemoryBlock> getMemoryBlocks() {
        return new ArrayList<>(memoryBlocks);
    }
    
    /**
     * 获取空闲块列表
     */
    public List<MemoryBlock> getFreeBlocks() {
        List<MemoryBlock> freeBlocks = new ArrayList<>();
        for (MemoryBlock block : memoryBlocks) {
            if (!block.isAllocated()) {
                freeBlocks.add(block);
            }
        }
        return freeBlocks;
    }
    
    /**
     * 获取已分配块列表
     */
    public List<MemoryBlock> getAllocatedBlocks() {
        List<MemoryBlock> allocatedBlocks = new ArrayList<>();
        for (MemoryBlock block : memoryBlocks) {
            if (block.isAllocated()) {
                allocatedBlocks.add(block);
            }
        }
        return allocatedBlocks;
    }
    
    /**
     * 计算内存碎片
     */
    public double getFragmentation() {
        int totalFreeSize = 0;
        int largestFreeSize = 0;
        
        for (MemoryBlock block : memoryBlocks) {
            if (!block.isAllocated()) {
                totalFreeSize += block.getSize();
                largestFreeSize = Math.max(largestFreeSize, block.getSize());
            }
        }
        
        if (totalFreeSize == 0) {
            return 0.0;
        }
        
        return 1.0 - (double)largestFreeSize / totalFreeSize;
    }
} 