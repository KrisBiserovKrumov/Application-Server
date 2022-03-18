package bg.codexio.parsers;

import bg.codexio.exceptions.PortAlreadyInUseException;
import bg.codexio.serverConfig.ServerConfig;
import bg.codexio.serverConfig.ServerSettings;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.util.ArrayList;
import java.util.List;

import static bg.codexio.exceptions.ExceptionMessages.PORT_ALREADY_IN_USE_EXCEPTION;
import static bg.codexio.global.Names.*;

public class ServerConfigParser {
    private static int currentPort = 8080;
    public static final int DEFAULT_MAX_PARAMETERS_COUNT = 10_000;
    public static final int DEFAULT_ASYNC_TIME_OUT = 3_000;
    public static final int DEFAULT_MAX_HEADERS_COUNT = 100;
    public static final int DEFAULT_MAX_POST_SIZE = 2;
    public static final int DEFAULT_THREAD_POOL_SIZE = 300;
    private static final List<Integer> usedPorts = new ArrayList<>();

    public static List<ServerConfig> getAllConnectorConfigs(Document document)
            throws PortAlreadyInUseException {
        var serverConfigs = new ArrayList<ServerConfig>();
        var nodeList = document.getElementsByTagName(CONNECTOR_TAG);

        for (var i = 0; i < nodeList.getLength(); i++) {
            var tag = nodeList.item(i);

            var port = tag.getAttributes().getNamedItem(PORT_PROPERTY).getNodeValue();
            var asyncTimeout = tag.getAttributes().getNamedItem(ASYNC_TIMEOUT_PROPERTY).getNodeValue();
            var maxHeaderCount = tag.getAttributes().getNamedItem(MAX_HEADER_COUNT_PROPERTY).getNodeValue();
            var maxParametersCount = tag.getAttributes().getNamedItem(MAX_PARAMETERS_COUNT_PROPERTY).getNodeValue();
            var maxPostSize = tag.getAttributes().getNamedItem(MAX_POST_SIZE_PROPERTY).getNodeValue();
            var threadPoolSize = tag.getAttributes().getNamedItem(THREADPOOL_SIZE_PROPERTY).getNodeValue();

            // Validate each field
            port = validatePort(port);
            maxHeaderCount = validateMaxHeadersCount(maxHeaderCount);
            maxParametersCount = validateMaxParametersCount(maxParametersCount);
            maxPostSize = validateMaxPostSize(maxPostSize);
            asyncTimeout = validateAsyncTimeout(asyncTimeout);
            threadPoolSize = validateThreadPoolSize(threadPoolSize);

            var contextPath = "";
            NodeList contextPathList = ((Element) tag)
                    .getElementsByTagName(CONTEXT_PATH_TAG);
            if (contextPathList.getLength() != 0) {
                contextPath = contextPathList
                        .item(0)
                        .getTextContent()
                        .trim();
            }

            serverConfigs.add(new ServerConfig(
                    new ServerSettings(Integer.parseInt(port), Integer.parseInt(asyncTimeout),
                            Integer.parseInt(maxHeaderCount), Integer.parseInt(maxParametersCount),
                            Double.parseDouble(maxPostSize), Integer.parseInt(threadPoolSize)), contextPath));
        }

        return serverConfigs;
    }

    private static String validateAsyncTimeout(String asyncTimeout) {
        return stringIsNotNumeric(asyncTimeout) ||
               Integer.parseInt(asyncTimeout) < DEFAULT_ASYNC_TIME_OUT
                ? String.valueOf(DEFAULT_ASYNC_TIME_OUT) : asyncTimeout;
    }

    private static String validateMaxHeadersCount(String maxHeaderCount) {
        return stringIsNotNumeric(maxHeaderCount) ? String.valueOf(DEFAULT_MAX_HEADERS_COUNT)
                : maxHeaderCount;
    }

    private static String validateMaxPostSize(String maxPostSize) {
        return stringIsNotNumeric(maxPostSize) ||
               Integer.parseInt(maxPostSize) > 2 ? String.valueOf(DEFAULT_MAX_POST_SIZE) : maxPostSize;
    }

    private static String validateMaxParametersCount(String maxParametersCount) {
        return stringIsNotNumeric(maxParametersCount) ? String.valueOf(DEFAULT_MAX_PARAMETERS_COUNT)
                : maxParametersCount;
    }

    private static String validateThreadPoolSize(String threadPoolSize) {
        return stringIsNotNumeric(threadPoolSize) ? String.valueOf(DEFAULT_THREAD_POOL_SIZE)
                : threadPoolSize;
    }

    private static boolean stringIsNotNumeric(String maxParametersCount) {
        if (maxParametersCount == null) {
            return true;
        }
        try {
            Integer.parseInt(maxParametersCount);
        } catch (NumberFormatException numberFormatException) {
            return true;
        }
        return false;
    }

    private static String validatePort(String port) throws PortAlreadyInUseException {
        var portNumber = Integer.parseInt(port);

        if (stringIsNotNumeric(port)) {
            usedPorts.add(currentPort);
            portNumber = currentPort;
            currentPort++;
        } else if (usedPorts.contains(portNumber)) {
            throw new PortAlreadyInUseException(PORT_ALREADY_IN_USE_EXCEPTION);
        } else {
            usedPorts.add(portNumber);
        }
        return String.valueOf(portNumber);
    }
}
