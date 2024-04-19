import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.security.KeyPair;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.Objects;
import java.util.concurrent.BlockingQueue;

public class Alice implements Runnable {

    private String nom;
    private KeyPair maPaire;
    private PublicKey publicDeBob;


    private BlockingQueue<Object> pipeline;

    public Alice(String nom, KeyPair maPaire, PublicKey publicDuDestinataire, BlockingQueue<Object> pipeline) {
        this.nom = nom;
        this.maPaire = maPaire;
        this.publicDeBob = publicDuDestinataire;
        this.pipeline = pipeline;
    }

    @Override
    public void run() {

        /*Phase 1 Alice: Lire ReadMe.txt */


        // Récupération de la liste des fichiers XML dans le dossier "query"
        String[] listFiles = XMLXPathHandler.listFiles("src/query/"+nom.toLowerCase());
        ArrayList<Document> queriesDoc = new ArrayList<>(listFiles.length);

        // Ajout de chaque Requete Xpath dans la liste des docs à envoyer
        for (int i = 0; i < listFiles.length; i++) {
            queriesDoc.add(i, XMLXPathHandler.loadDocument("src/query/"+nom.toLowerCase()+"/" + listFiles[i]));
        }

        // Signature de chaque document contenant une requête Xpath
        for (int i = 0; i < listFiles.length; i++) {
            Security.signXML(maPaire, queriesDoc.get(i));
        }

        // Envoi de chaque document signé dans le canal de communication partagé entre Alice et Bob
        Communication.sendDoc(pipeline, queriesDoc, listFiles.length);


        // Récupération des documents XML contenant les réponses de l'autre depuis le canal
        ArrayList<Document> docResponse = Communication.receiveDoc(pipeline, new ArrayList<>());

        // Pour chaque document de réponse reçu
        for (int i = 0; i < docResponse.size(); i++) {
            // Vérification de la validité de la signature
            boolean valid = Security.validateSignature(publicDeBob, docResponse.get(i));
            if (!valid) {
                throw new RuntimeException("Document n'est pas valide");
            }
            // Sauvegarde du document de réponse dans un fichier
            XMLXPathHandler.saveDOMToFile(docResponse.get(i), "src/response/"+nom.toLowerCase()+"/responses.xml", i);

        }

        /*Phase 2 Alice: Lire ReadMe.txt */
        // Chargement de la base de données XML de Alice
        Document bd_alice = XMLXPathHandler.loadDocument("src/database/bd_alice.xml");
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        // Récupération des documents XML contenant les requêtes de l'envoyeur depuis le canal
        queriesDoc.clear();
        queriesDoc = Communication.receiveDoc(pipeline, new ArrayList<>());

        ArrayList<Document> responsesDoc = new ArrayList<>();

        // Pour chaque document de requête reçu
        for (Document document : queriesDoc) {

            // Validation de la signature du document reçu par Alice
            Security.validateSignature(publicDeBob, document);

            // Extraction de la requête XPath du document
            String req = (XMLXPathHandler.extractQuery(document));

            // Exécution de la requête dans la base de données de Alice
            NodeList nodeList = XMLXPathHandler.executeXPath("/" + req, bd_alice);

            // Construction de la réponse à la requête
            StringBuilder reponses = new StringBuilder();
            for (int j = 0; j < Objects.requireNonNull(nodeList).getLength(); j++) {
                Node nNode = nodeList.item(j);
                if (nNode.getNodeType() == Node.ELEMENT_NODE) {
                    reponses.append("\n").append(j).append("\t");
                    reponses.append(nNode.getTextContent());
                }
            }

            // Création du document contenant la réponse à la requête
            Document response = XMLXPathHandler.createXmlResponse(req, reponses.toString());
            responsesDoc.add(response);

            // Signature du document de réponse par Alice
            Security.signXML(maPaire, response);
        }

        // Envoi des réponses dans le canal de communication
        Communication.sendDoc(pipeline, responsesDoc, responsesDoc.size());

    }
}