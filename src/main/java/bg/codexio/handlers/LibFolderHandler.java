package bg.codexio.handlers;

import bg.codexio.libraries.ApplicationConfiguration;
import bg.codexio.libraries.JarFileLibrary;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.jar.JarFile;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;

import static bg.codexio.global.Fields.customURLClassLoader;
import static bg.codexio.global.Fields.typeMap;
import static bg.codexio.global.Methods.getDocument;
import static bg.codexio.global.Methods.getFileContentFromInputStream;
import static bg.codexio.global.Names.*;
import static bg.codexio.global.Paths.CONF_XML_FILEPATH;

public class LibFolderHandler {
    public static Map<String, JarFileLibrary> handleLibFolderFiles(ApplicationConfiguration applicationConfiguration)
            throws IOException, ParserConfigurationException, SAXException {
        var jarFileLibrariesMap = new HashMap<String, JarFileLibrary>();

        var files = new File(LIB_FOLDER_FILEPATH).listFiles();

        for (var file : files) {
            if (file.getName().endsWith(JAR_FILE_ENDING)) {
                var jarFileName = file.getName().substring(0, file.getName().length() - 4);
                var library = getLibraryForJarFile(file, applicationConfiguration);
                library.ifPresent(lib -> {
                    try {
                        jarFileLibrariesMap.putIfAbsent(jarFileName, lib);
                        customURLClassLoader.addURL(new URL(String.format(JAR_FILE_URL, LIB_FOLDER_FILEPATH,
                                lib.getJarFileName())));
//                        executeJarFile(lib);
                    } catch (MalformedURLException /* | ClassNotFoundException | InvocationTargetException |
                            NoSuchMethodException | InstantiationException | IllegalAccessException*/ e) {
                        e.printStackTrace();
                    }
                });
            }
        }

        return jarFileLibrariesMap;
    }

    private static Optional<JarFileLibrary> getLibraryForJarFile(File file, ApplicationConfiguration applicationConfiguration)
            throws IOException, ParserConfigurationException, SAXException {
        var jarFile = new JarFile(file);

        // Remove the 'lib\' from the file name
        var jarFileName = jarFile.getName().substring(LIB_FOLDER_FILEPATH.length() + 1,
                jarFile.getName().length() - 4);

        var confXmlFile = jarFile.getEntry(CONF_XML_FILEPATH);

//        Optional<JarFileLibrary> jarFileLibraryFromConf = Optional.empty();
        if (confXmlFile != null) {
            var inputStream = jarFile.getInputStream(confXmlFile);
//            jarFileLibraryFromConf = getLibraryFromConfXml(inputStream, jarFileName);
            return getLibraryFromConfXml(inputStream, jarFileName);
        }
        var serverConfigLibraries = applicationConfiguration.getLibraries();
//        var libraryFromServer = getLibraryFromServerXml(serverConfigLibraries, jarFileName);
        return getLibraryFromServerXml(serverConfigLibraries, jarFileName);

//        return Stream.of(jarFileLibraryFromConf, libraryFromServer)
//                .flatMap(Optional::stream)
//                .findFirst();
    }


    private static Optional<JarFileLibrary> getLibraryFromServerXml(Map<String, JarFileLibrary> serverConfigLibraries, String jarFileName) {
        return Optional.ofNullable(serverConfigLibraries.get(jarFileName));
    }

    private static Optional<JarFileLibrary> getLibraryFromConfXml(InputStream streamOfXmlFile, String jarFileName)
            throws ParserConfigurationException, SAXException, IOException {
        var confXmlFileContent = getFileContentFromInputStream(streamOfXmlFile);
        var doc = getDocument(confXmlFileContent);
        doc.getDocumentElement().normalize();
        var nodeList = doc.getElementsByTagName(LIBRARY_TAG);

        // Get through all nodes and check if their attribute "name" is equal to the given jar file name
        for (var i = 0; i < nodeList.getLength(); i++) {
            var tag = nodeList.item(i);
            var nameAttribute = tag.getAttributes().getNamedItem(NAME_PROPERTY);
            if (nameAttribute.getNodeValue().equals(jarFileName)) {
                var mainClassName = tag.getAttributes().getNamedItem(MAINCLASS_PROPERTY).getNodeValue();
                var methodName = tag.getAttributes().getNamedItem(METHOD_PROPERTY).getNodeValue();

                return Optional.of(new JarFileLibrary(jarFileName, mainClassName, methodName));
            }
        }

        return Optional.empty();
    }

    public static Object executeJarFile(JarFileLibrary jarFileInfo)
            throws NoSuchMethodException, ClassNotFoundException, InstantiationException,
            IllegalAccessException, InvocationTargetException {
        var fullMethodDeclaration = jarFileInfo.getMethodName();
        var methodName = fullMethodDeclaration.substring(0, fullMethodDeclaration.indexOf('('));
        var parameters = getMethodParameters(fullMethodDeclaration);

        var classToLoad = Class.forName(jarFileInfo.getMainClassName(), true, customURLClassLoader);
        var method = classToLoad.getDeclaredMethod(methodName, parameters);
        var instance = classToLoad.getDeclaredConstructor().newInstance();

        var argumentTypes = getArgumentTypes(fullMethodDeclaration, parameters);

        var methodArguments = new ArrayList<>();
        for (var currentArg : argumentTypes) {
            methodArguments.add(typeMap.get(currentArg));
        }

        method.invoke(instance, methodArguments.toArray(new Object[0]));
        return instance;
    }

    private static List<String> getArgumentTypes(String fullMethodDeclaration, Class<?>[] parameters) {
        var methodParameters = fullMethodDeclaration.substring(fullMethodDeclaration.indexOf('(') + 1, fullMethodDeclaration.indexOf(')'));
        var argumentTypePattern = Pattern.compile("[\\w+.]+(?<values>(<[? \\w+.]+(,\\s*[\\w+.]+)?>)?)\\s+\\w+");
        var i = new AtomicInteger(0);
        return argumentTypePattern
                .matcher(methodParameters)
                .results().map(result -> parameters[i.getAndIncrement()].getName() + result.group(1))
                .collect(Collectors.toList());
    }

    private static Class<?>[] getMethodParameters(String methodName) {
        var methodParameters = methodName.substring(methodName.indexOf('(') + 1, methodName.indexOf(')'));
        var plainArgTypePattern = Pattern.compile("(?<name>[\\w+.]+)(<[?\\w+ .]+(,\\s+[\\w+.]+)?>)?\\s+\\w+");
        var matcher = plainArgTypePattern.matcher(methodParameters);

        var foundParameters = new ArrayList<Class<?>>();
        while (matcher.find()) {
            try {
                var parameterClass = Class.forName(matcher.group(NAME_CAPTURING_GROUP));
                foundParameters.add(parameterClass);
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }

        return foundParameters.toArray(new Class<?>[0]);
    }
}