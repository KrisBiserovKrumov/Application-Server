package bg.codexio;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Enumeration;

public class CustomURLClassLoader extends URLClassLoader {
    public CustomURLClassLoader(URL[] urls) {
        super(urls);
    }

    @Override
    public void addURL(URL url) {
        super.addURL(url);
    }

    @Override
    public Class<?> findClass(String name) throws ClassNotFoundException {
        return super.findClass(name);
    }

    public InputStream getResourceAsStream(String webInfDirectory, String fileName) {
        webInfDirectory = webInfDirectory.replace('\\', '/');
        InputStream inputStream = null;
        Enumeration<URL> resources;
        try {
            resources = super.getResources(fileName);
            URL url = null;
            while (resources.hasMoreElements()) {
                URL currentUrl = resources.nextElement();
                if (currentUrl.toString().contains(webInfDirectory)) {
                    url = currentUrl;
                }
            }
            inputStream = url.openStream();
        } catch (IOException ioException) {
            ioException.printStackTrace();
        }
        return inputStream;
    }
}