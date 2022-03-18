package bg.codexio.libraries;

public class JarFileLibrary {
    private String jarFileName;
    private String mainClassName;
    private String methodName;

    public JarFileLibrary(String jarFileName, String mainClassName, String methodName) {
        this.jarFileName = jarFileName;
        this.mainClassName = mainClassName;
        this.methodName = methodName;
    }

    public String getJarFileName() {
        return jarFileName;
    }

    public String getMainClassName() {
        return mainClassName;
    }

    public String getMethodName() {
        return methodName;
    }
}
