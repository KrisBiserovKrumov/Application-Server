package bg.codexio.handlers;

import bg.codexio.exceptions.PortAlreadyInUseException;
import bg.codexio.libraries.ApplicationConfiguration;
import bg.codexio.libraries.JarFileLibrary;
import bg.codexio.parsers.ServerConfigParser;
import bg.codexio.serverConfig.ServerConfig;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

import static bg.codexio.global.Fields.typeMap;
import static bg.codexio.global.Methods.getDocument;
import static bg.codexio.global.Names.*;
import static bg.codexio.global.Paths.SERVER_XML_FILEPATH;

public class ConfFolderHandler {
    public static ApplicationConfiguration getServerConfig()
            throws ParserConfigurationException, IOException, SAXException,
            PortAlreadyInUseException {
        var xmlFile = new File(SERVER_XML_FILEPATH);
        var fileLines = Files.readAllLines(Paths.get(String.valueOf(xmlFile)),
                Charset.defaultCharset());

        var fileContent = fileLines
                .stream()
                .collect(Collectors.joining(System.lineSeparator()));

        var document = getDocument(fileContent);

        var libraries = getAllLibraryTagsInServerXml(document);
        var serverConfigs = getAllConnectorTagsInServerXml(document);

        typeMap.put("java.util.Map<String, bg.codexio.config.Library>", libraries);
        typeMap.put("java.util.List<bg.codexio.serverConfig.ServerConfig>", serverConfigs);
        return new ApplicationConfiguration(libraries, serverConfigs, new ArrayList<>());
    }

    private static Map<String, JarFileLibrary> getAllLibraryTagsInServerXml(Document document) {
        var libraries = new HashMap<String, JarFileLibrary>();
        var libraryTags = document.getElementsByTagName(LIBRARY_TAG);

        for (var i = 0; i < libraryTags.getLength(); i++) {
            var libraryTag = libraryTags.item(i);
            var jarFileName = libraryTag.getAttributes().getNamedItem(NAME_PROPERTY).getNodeValue();
            var mainClassName = libraryTag.getAttributes().getNamedItem(MAINCLASS_PROPERTY).getNodeValue();
            var methodName = libraryTag.getAttributes().getNamedItem(METHOD_PROPERTY).getNodeValue();

            libraries.putIfAbsent(jarFileName, new JarFileLibrary(jarFileName, mainClassName, methodName));
        }

        return libraries;
    }

    private static List<ServerConfig> getAllConnectorTagsInServerXml(Document document)
            throws PortAlreadyInUseException {
        return ServerConfigParser.getAllConnectorConfigs(document);
    }

    private static Set<String> getAllServletHandlersInServerXml(Document document) {
        var servletHandlerClassNames = new LinkedHashSet<String>();
        var servletHandlers = document.getElementsByTagName(SERVLET_HANDLER_TAG);

        for (var i = 0; i < servletHandlers.getLength(); i++) {
            var servletHandlerTag = servletHandlers.item(i);
            var servletHandlerClassName = servletHandlerTag.getAttributes().getNamedItem(MAINCLASS_PROPERTY).getNodeValue();
            servletHandlerClassNames.add(servletHandlerClassName);
        }

        return servletHandlerClassNames;
    }
}
