package bg.codexio;

import bg.codexio.exceptions.PortAlreadyInUseException;
import bg.codexio.handlers.ConfFolderHandler;
import bg.codexio.handlers.LibFolderHandler;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.List;

import static bg.codexio.global.Fields.typeMap;

public class CrossLoader {

    public static void main(String[] args) throws IOException,
            ParserConfigurationException, SAXException,
            ClassNotFoundException, PortAlreadyInUseException,
            InvocationTargetException, NoSuchMethodException,
            InstantiationException, IllegalAccessException {
        var serverConfig = ConfFolderHandler.getServerConfig();
        var jarFilesInfo = LibFolderHandler.handleLibFolderFiles(serverConfig);
        var instance = LibFolderHandler.executeJarFile(jarFilesInfo.get("catalina"));
        var instance1 = LibFolderHandler.executeJarFile(jarFilesInfo.get("static-file-handler"));
        typeMap.put("java.util.List<bg.codexio.handler.Handler>", List.of(instance, instance1));
        LibFolderHandler.executeJarFile(jarFilesInfo.get("http-connector"));
    }
}