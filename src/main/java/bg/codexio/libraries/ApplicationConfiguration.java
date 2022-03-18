package bg.codexio.libraries;

import bg.codexio.serverConfig.ServerConfig;

import java.util.List;
import java.util.Map;

public class ApplicationConfiguration {
    private Map<String, JarFileLibrary> libraries;
    private List<ServerConfig> serverConfigs;
    private List<Object> handlers;

    public ApplicationConfiguration() {
    }

    public ApplicationConfiguration(Map<String, JarFileLibrary> libraries, List<ServerConfig> serverConfigs, List<Object> handlers) {
        this.libraries = libraries;
        this.serverConfigs = serverConfigs;
        this.handlers = handlers;
    }

    public Map<String, JarFileLibrary> getLibraries() {
        return libraries;
    }

    public String getJarFileMainClass(String jarFileName) {
        return this.libraries
                .keySet()
                .stream()
                .filter(key -> key.equals(jarFileName))
                .findFirst()
                .orElse("");
    }
}
