package bg.codexio.global;

import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;

public class Methods {
    public static String getFileContentFromInputStream(InputStream streamOfXmlFile) throws IOException {
        var bufferedInputStream = new BufferedInputStream(streamOfXmlFile);

        return new String(bufferedInputStream.readAllBytes());
    }

    public static Document getDocument(String xmlString)
            throws ParserConfigurationException, SAXException, IOException {
        var dbf = DocumentBuilderFactory.newInstance();
        var db = dbf.newDocumentBuilder();

        // Parses the content of the given file as an XML document and returns a new DOM object.
        return db.parse(new InputSource(new StringReader(xmlString)));
    }
}
