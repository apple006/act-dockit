package act.dockit;

import org.osgl.$;
import org.osgl.util.*;

import java.io.File;
import java.io.FileFilter;
import java.util.List;

/**
 * Implement a {@link DocRepo} based on file system
 */
public class FsDocRepo implements DocRepo {

    private File root;
    private String rootPath;

    public FsDocRepo(File root) {
        E.illegalArgumentIf(!root.isDirectory());
        this.root = root.getAbsoluteFile();
        this.rootPath = this.calibaratePath(this.root.getPath());
    }

    @Override
    public DocFolder root() {
        RepoElement element = fromFile(root);
        try {
            return $.cast(element);
        } catch (ClassCastException e) {
            System.out.println(root.getAbsolutePath());
            throw e;
        }
    }

    @Override
    public RepoElement fetch(String path) {
        File file = toFile(path);
        if (!file.exists()) {
            return null;
        }
        if (file.isDirectory()) {
            return new DocFolder(path);
        } else {
            return new DocSource(path);
        }
    }

    @Override
    public List<RepoElement> list(String path) {
        return list(path, 0);
    }

    @Override
    public List<RepoElement> list(String path, final int coolingTimeInSecond) {
        File dir = toFile(path);
        File[] fa = dir.listFiles(new FileFilter() {
            @Override
            public boolean accept(File file) {
                return ($.ms() - file.lastModified()) > coolingTimeInSecond * 1000;
            }
        });
        if (null == fa) {
            return C.list();
        }
        List<RepoElement> list = C.newList();
        for (File file : fa) {
            list.add(fromFile(file));
        }
        return list;
    }

    @Override
    public String read(String path) {
        File file = toFile(path);
        return IO.readContentAsString(file);
    }

    @Override
    public void persist(String path, String content) {
        File file = toFile(path);
        file.getParentFile().mkdirs();
        IO.writeContent(content, file);
    }

    @Override
    public void delete(String path) {
        File file = toFile(path);
        if (file.isFile()) {
            file.delete();
        }
    }

    private RepoElement fromFile(File file) {
        if (file.isDirectory()) {
            return new DocFolder(path(file));
        } else {
            return new DocSource(path(file));
        }
    }

    private File toFile(RepoElement element) {
        StringBuilder sb = S.builder(rootPath).append(element.path());
        return new File(sb.toString());
    }

    private File toFile(String path) {
        StringBuilder sb = S.builder(rootPath).append(path);
        return new File(sb.toString());
    }

    private String path(File file) {
        String path = calibaratePath(file.getAbsolutePath());
        return S.after(path, rootPath);
    }

    private String calibaratePath(String path) {
        if (OS.get() == OS.WINDOWS) {
            return path.replace('\\', '/');
        }
        return path;
    }

    @Override
    public String toString() {
        return S.fmt("DocRepo[%s]", root.getAbsolutePath());
    }
}
