import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.security.KeyPair;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.concurrent.BlockingQueue;

public class Bob implements Runnable {

    private String nom;
    private KeyPair maPaire;
    private PublicKey publicDeAlice;
    private BlockingQueue<Object> pipeline;

    public Bob(String nom, KeyPair maPaire, PublicKey publicDuDestinataire, BlockingQueue<Object> pipeline) {
        this.nom = nom;
        this.maPaire = maPaire;
        this.publicDeAlice = publicDuDestinataire;
        this.pipeline = pipeline;
    }

    @Override
    public void run() {



        // Chargement de la base de données XML de Bob
        Document bd_bob = XMLXPathHandler.loadDocument("src/database/bd_bob.xml");

        // Récupération des documents XML contenant les requêtes de l'envoyeur depuis le canal
        ArrayList<Document> queriesDoc= Communication.receiveDoc(pipeline, new ArrayList<>());
        ArrayList<Document> responsesDoc = new ArrayList<>();

        // Pour chaque document de requête reçu
        for (int i = 0; i < queriesDoc.size(); i++) {

            // Validation de la signature du document reçu par Alice
            Security.validateSignature(publicDeAlice, queriesDoc.get(i));

            // Extraction de la requête XPath du document
            String req = (XMLXPathHandler.extractQuery(queriesDoc.get(i)));

            // Exécution de la requête dans la base de données de Bob
            NodeList nodeList = XMLXPathHandler.executeXPath("/" + req, bd_bob);

            // Construction de la réponse à la requête
            StringBuilder reponses = new StringBuilder();
            for (int j = 0; j < nodeList.getLength(); j++) {
                Node nNode = nodeList.item(j);
                if (nNode.getNodeType() == Node.ELEMENT_NODE) {
                    reponses.append("\n").append(j).append("\t");
                    reponses.append(nNode.getTextContent());
                }
            }

            // Création du document contenant la réponse à la requête
            Document response = XMLXPathHandler.createXmlResponse(req, reponses.toString());
            responsesDoc.add(response);

            // Signature du document de réponse par Bob
            Security.signXML(maPaire, response);
        }

        // Envoi des réponses dans le canal de communication
        Communication.sendDoc(pipeline, responsesDoc, responsesDoc.size());

    }
}

