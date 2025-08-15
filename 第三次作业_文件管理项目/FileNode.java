import java.util.*;

/**
 * 文件节点类 - 表示文件系统中的文件或目录
 */
public class FileNode {
    private String name;                    // 文件/目录名
    private boolean isDirectory;            // 是否为目录
    private String content;                 // 文件内容（仅对文件有效）
    private FileNode parent;                // 父目录
    private Map<String, FileNode> children; // 子文件/目录
    private Date createTime;                // 创建时间
    private Date modifyTime;                // 修改时间
    private long size;                      // 文件大小

    public FileNode(String name, boolean isDirectory, FileNode parent) {
        this.name = name;
        this.isDirectory = isDirectory;
        this.parent = parent;
        this.children = new HashMap<>();
        this.content = "";
        this.createTime = new Date();
        this.modifyTime = new Date();
        this.size = 0;
    }

    // Getter and Setter methods
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
        this.modifyTime = new Date();
    }

    public boolean isDirectory() {
        return isDirectory;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        if (!isDirectory) {
            this.content = content;
            this.size = content.length();
            this.modifyTime = new Date();
        }
    }

    public FileNode getParent() {
        return parent;
    }

    public void setParent(FileNode parent) {
        this.parent = parent;
    }

    public Map<String, FileNode> getChildren() {
        return children;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public Date getModifyTime() {
        return modifyTime;
    }

    public long getSize() {
        return size;
    }

    /**
     * 添加子节点
     */
    public boolean addChild(FileNode child) {
        if (isDirectory && !children.containsKey(child.getName())) {
            children.put(child.getName(), child);
            child.setParent(this);
            this.modifyTime = new Date();
            return true;
        }
        return false;
    }

    /**
     * 删除子节点
     */
    public boolean removeChild(String name) {
        if (isDirectory && children.containsKey(name)) {
            children.remove(name);
            this.modifyTime = new Date();
            return true;
        }
        return false;
    }

    /**
     * 获取子节点
     */
    public FileNode getChild(String name) {
        return children.get(name);
    }

    /**
     * 获取完整路径
     */
    public String getFullPath() {
        if (parent == null) {
            return name.equals("/") ? "/" : "/" + name;
        }
        String parentPath = parent.getFullPath();
        if (parentPath.equals("/")) {
            return "/" + name;
        }
        return parentPath + "/" + name;
    }

    @Override
    public String toString() {
        return name + (isDirectory ? "/" : "");
    }
} 