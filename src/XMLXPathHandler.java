import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;


import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Cette classe fournit des méthodes pour la gestion des documents XML et l'exécution de requêtes XPath.
 */
public class XMLXPathHandler {


    /**
     * Charge un document XML depuis un fichier.
     *
     * @param filePath Le chemin du fichier XML.
     * @return Le document XML chargé.
     * @throws RuntimeException Si une erreur survient lors du chargement du document XML.
     */
    public static Document loadDocument(String filePath) {
        try {
            // Création du parseur DOM
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(true);

            // On vérifie si le xml est conforme à la DTD
            factory.setValidating(true);

            DocumentBuilder builder = factory.newDocumentBuilder();

            // Gestion des erreurs liées à la DTD
            builder.setErrorHandler(new ErrorHandler() {
                @Override
                public void warning(SAXParseException e) throws SAXException {
                    throw new SAXException("Xml non conforme à la DTD");
                }

                @Override
                public void error(SAXParseException e) throws SAXException {
                    throw new SAXException("Xml non conforme à la DTD");
                }

                @Override
                public void fatalError(SAXParseException e) throws SAXException {
                    throw new SAXException("Xml non conforme à la DTD");
                }
            });

            // Charger le document XML
            Document document = builder.parse(new File(filePath));
            document.getDocumentElement().normalize();

            return document;

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    /**
     * Exécute une requête XPath sur un document XML et retourne une liste de nœuds correspondants.
     *
     * @param xPathExpression L'expression XPath à exécuter.
     * @param document        Le document XML sur lequel exécuter la requête.
     * @return Une liste de nœuds correspondant à l'expression XPath.
     */
    public static NodeList executeXPath(String xPathExpression, Document document) {
        try {
            // Création d'un objet XPath
            XPath xPath = XPathFactory.newInstance().newXPath();

            // Compilation de l'expression XPath
            XPathExpression expression = xPath.compile(xPathExpression);

            // Exécution de l'expression XPath sur le document XML
            return (NodeList) expression.evaluate(document, XPathConstants.NODESET);
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }

    /**
     * Crée un document DOM vide.
     *
     * @return Le document DOM vide créé.
     * @throws RuntimeException Si une erreur survient lors de la création du document DOM.
     */
    private static Document emptyDoc() {
        try {
            //cree le document
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder;
            builder = factory.newDocumentBuilder();
            return builder.newDocument();
        } catch (ParserConfigurationException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Crée un document XML de réponse avec une requête et une réponse spécifiées.
     *
     * @param request  La requête associée au document XML.
     * @param response La réponse associée au document XML.
     * @return Le document XML de réponse créé.
     */
    public static Document createXmlResponse(String request, String response) {

        Document doc = emptyDoc();

        // Création de l'élément racine
        Element rootElement = doc.createElement("Envelope");
        doc.appendChild(rootElement);

        // Ajout de contenu aux éléments
        Element query = doc.createElement("QUERY");
        rootElement.appendChild(query);
        query.setTextContent(request);

        Element result = doc.createElement("RESULT");
        rootElement.appendChild(result);
        result.setTextContent(response);

        return doc;
    }

    /**
     * Extrait le contenu de l'élément "QUERY" d'un document XML.
     *
     * @param document Le document XML à traiter.
     * @return Le contenu de l'élément "QUERY" s'il existe, sinon un message indiquant que l'élément n'a pas été trouvé.
     */
    public static String extractQuery(Document document) {
        NodeList nList = document.getElementsByTagName("QUERY");

        if (nList.getLength() > 0) {
            Element dataElement = (Element) nList.item(0);
            return dataElement.getTextContent();
        } else {
            return "Élément Data non trouvé.";
        }
    }

    /**
     * Affiche un document XML dans le terminal.
     *
     * @param document Le document XML à afficher.
     */
    public static void displayDoc(Document document) {
        OutputStream os;
        os = System.out;
        TransformerFactory tf = TransformerFactory.newInstance();
        Transformer trans;
        try {
            trans = tf.newTransformer();
        } catch (TransformerConfigurationException e) {
            throw new RuntimeException(e);
        }
        try {
            trans.transform(new DOMSource(document), new StreamResult(os));
        } catch (TransformerException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Récupère le nom de tous les fichiers XML dans un dossier spécifié.
     *
     * @param directoryPath Le chemin du dossier contenant les fichiers XML.
     * @return Un tableau de chaînes contenant les noms des fichiers XML.
     */
    public static String[] listFiles(String directoryPath) {

        File directory = new File(directoryPath);

        if (!directory.isDirectory()) {

            System.err.println("Le chemin spécifié n'est pas un dossier.");
            return new String[0]; // Retourner un tableau vide s'il n'y a pas de dossier
        }

        File[] files = directory.listFiles();

        if (files == null) {
            System.err.println("Impossible de lire le contenu du dossier.");
            return new String[0];
        }

        // Créer une liste pour stocker les noms des fichiers XML
        List<String> xmlFileNames = new ArrayList<>();
        for (File file : files) {
            if (file.isFile() && file.getName().toLowerCase().endsWith(".xml")) {
                xmlFileNames.add(file.getName());
            }
        }
        return xmlFileNames.toArray(new String[0]);
    }

    /**
     * Sauvegarde un document DOM dans un Dossier spécifié.
     *
     * @param doc       Le document DOM à sauvegarder.
     * @param filePath  Le Dossier de destination.
     * @param count     Le compteur utilisé pour générer un nom de fichier unique.
     * @throws RuntimeException Si une erreur survient lors de la sauvegarde du document DOM.
     */
    public static void saveDOMToFile(Document doc, String filePath, int count) {
        // Créer un transformateur
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = null;
        try {
            transformer = transformerFactory.newTransformer();

            // Extraire l'extension du fichier
            String extension = getFileExtension(filePath);

            // Créer le nouveau nom de fichier avec le compteur
            String fileNameWithCounter = filePath.replaceFirst("[.][^.]+$", "") + count + "." + extension;

            File file = new File(fileNameWithCounter);
            if (!file.exists()) {
                file.createNewFile();
            }

            Result output = new StreamResult(fileNameWithCounter);

            Source input = new DOMSource(doc);

            transformer.transform(input, output);

            System.out.println("Document DOM enregistré dans " + fileNameWithCounter);

        } catch (TransformerException | IOException e) {
            throw new RuntimeException(e);
        }

    }

    /**
     * Récupère l'extension d'un fichier à partir de son chemin.
     *
     * @param filePath Le chemin du fichier.
     * @return L'extension du fichier.
     */
    private static String getFileExtension(String filePath) {
        String extension = "";

        int lastDotIndex = filePath.lastIndexOf('.');
        if (lastDotIndex > 0) {
            extension = filePath.substring(lastDotIndex + 1);
        }

        return extension;
    }
}
