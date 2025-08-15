import java.util.*;

/**
 * 虚拟文件系统类 - 实现文件系统的核心功能
 */
public class VirtualFileSystem {
    private FileNode root;          // 根目录
    private FileNode currentDir;    // 当前目录
    private Set<FileNode> openFiles; // 当前打开的文件
    private boolean isFormatted;    // 是否已格式化

    public VirtualFileSystem() {
        this.openFiles = new HashSet<>();
        this.isFormatted = false;
    }

    /**
     * 格式化文件系统
     */
    public boolean format() {
        try {
            this.root = new FileNode("/", true, null);
            this.currentDir = root;
            this.openFiles.clear();
            this.isFormatted = true;
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 检查是否已格式化
     */
    public boolean isFormatted() {
        return isFormatted;
    }

    /**
     * 获取当前目录
     */
    public FileNode getCurrentDirectory() {
        return currentDir;
    }

    /**
     * 获取当前路径
     */
    public String getCurrentPath() {
        return currentDir != null ? currentDir.getFullPath() : "";
    }

    /**
     * 创建子目录
     */
    public boolean createDirectory(String name) {
        if (!isFormatted || name == null || name.trim().isEmpty()) {
            return false;
        }
        
        name = name.trim();
        
        // 检查名称是否合法
        if (name.contains("/") || name.equals(".") || name.equals("..")) {
            return false;
        }

        // 检查是否已存在
        if (currentDir.getChild(name) != null) {
            return false;
        }

        FileNode newDir = new FileNode(name, true, currentDir);
        return currentDir.addChild(newDir);
    }

    /**
     * 删除子目录
     */
    public boolean removeDirectory(String name) {
        if (!isFormatted || name == null || name.trim().isEmpty()) {
            return false;
        }

        FileNode target = currentDir.getChild(name.trim());
        if (target == null || !target.isDirectory()) {
            return false;
        }

        // 检查目录是否为空
        if (!target.getChildren().isEmpty()) {
            return false;
        }

        return currentDir.removeChild(name.trim());
    }

    /**
     * 更改当前目录
     */
    public boolean changeDirectory(String path) {
        if (!isFormatted || path == null) {
            return false;
        }

        path = path.trim();
        
        if (path.equals("..")) {
            // 返回上级目录
            if (currentDir.getParent() != null) {
                currentDir = currentDir.getParent();
                return true;
            }
            return false;
        } else if (path.equals(".")) {
            // 当前目录
            return true;
        } else if (path.equals("/")) {
            // 根目录
            currentDir = root;
            return true;
        } else {
            // 进入子目录
            FileNode target = currentDir.getChild(path);
            if (target != null && target.isDirectory()) {
                currentDir = target;
                return true;
            }
            return false;
        }
    }

    /**
     * 显示目录内容
     */
    public List<String> listDirectory() {
        List<String> result = new ArrayList<>();
        
        if (!isFormatted) {
            return result;
        }

        // 添加当前目录和父目录
        result.add(".");
        if (currentDir.getParent() != null) {
            result.add("..");
        }

        // 添加子目录和文件
        for (FileNode child : currentDir.getChildren().values()) {
            String item = child.getName();
            if (child.isDirectory()) {
                item += "/";
            } else {
                item += " (" + child.getSize() + " bytes)";
            }
            result.add(item);
        }

        Collections.sort(result.subList(currentDir.getParent() != null ? 2 : 1, result.size()));
        return result;
    }

    /**
     * 创建文件
     */
    public boolean createFile(String name) {
        if (!isFormatted || name == null || name.trim().isEmpty()) {
            return false;
        }
        
        name = name.trim();
        
        // 检查名称是否合法
        if (name.contains("/") || name.equals(".") || name.equals("..")) {
            return false;
        }

        // 检查是否已存在
        if (currentDir.getChild(name) != null) {
            return false;
        }

        FileNode newFile = new FileNode(name, false, currentDir);
        return currentDir.addChild(newFile);
    }

    /**
     * 打开文件
     */
    public boolean openFile(String name) {
        if (!isFormatted || name == null || name.trim().isEmpty()) {
            return false;
        }

        FileNode file = currentDir.getChild(name.trim());
        if (file == null || file.isDirectory()) {
            return false;
        }

        openFiles.add(file);
        return true;
    }

    /**
     * 关闭文件
     */
    public boolean closeFile(String name) {
        if (!isFormatted || name == null || name.trim().isEmpty()) {
            return false;
        }

        FileNode file = currentDir.getChild(name.trim());
        if (file == null || file.isDirectory()) {
            return false;
        }

        return openFiles.remove(file);
    }

    /**
     * 读取文件内容
     */
    public String readFile(String name) {
        if (!isFormatted || name == null || name.trim().isEmpty()) {
            return null;
        }

        FileNode file = currentDir.getChild(name.trim());
        if (file == null || file.isDirectory()) {
            return null;
        }

        // 检查文件是否已打开
        if (!openFiles.contains(file)) {
            return null;
        }

        return file.getContent();
    }

    /**
     * 写入文件内容
     */
    public boolean writeFile(String name, String content) {
        if (!isFormatted || name == null || name.trim().isEmpty() || content == null) {
            return false;
        }

        FileNode file = currentDir.getChild(name.trim());
        if (file == null || file.isDirectory()) {
            return false;
        }

        // 检查文件是否已打开
        if (!openFiles.contains(file)) {
            return false;
        }

        file.setContent(content);
        return true;
    }

    /**
     * 删除文件
     */
    public boolean deleteFile(String name) {
        if (!isFormatted || name == null || name.trim().isEmpty()) {
            return false;
        }

        FileNode file = currentDir.getChild(name.trim());
        if (file == null || file.isDirectory()) {
            return false;
        }

        // 如果文件已打开，先关闭
        openFiles.remove(file);
        
        return currentDir.removeChild(name.trim());
    }

    /**
     * 获取打开的文件列表
     */
    public List<String> getOpenFiles() {
        List<String> result = new ArrayList<>();
        for (FileNode file : openFiles) {
            result.add(file.getName());
        }
        Collections.sort(result);
        return result;
    }

    /**
     * 获取文件信息
     */
    public String getFileInfo(String name) {
        if (!isFormatted || name == null || name.trim().isEmpty()) {
            return null;
        }

        FileNode file = currentDir.getChild(name.trim());
        if (file == null) {
            return null;
        }

        StringBuilder info = new StringBuilder();
        info.append("名称: ").append(file.getName()).append("\n");
        info.append("类型: ").append(file.isDirectory() ? "目录" : "文件").append("\n");
        info.append("大小: ").append(file.getSize()).append(" bytes\n");
        info.append("创建时间: ").append(file.getCreateTime()).append("\n");
        info.append("修改时间: ").append(file.getModifyTime()).append("\n");
        info.append("完整路径: ").append(file.getFullPath());

        return info.toString();
    }
} 