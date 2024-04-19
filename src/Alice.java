import org.w3c.dom.Document;

import java.security.KeyPair;
import java.security.PublicKey;
import java.util.ArrayList;
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

        // Récupération de la liste des fichiers XML dans le dossier "query"
        String[] listFiles = XMLXPathHandler.listFiles("src/query/"+nom.toLowerCase());
        ArrayList<Document> queriesDoc = new ArrayList<>(listFiles.length);

        // Chargement de chaque document XML de requête dans la liste
        for (int i = 0; i < listFiles.length; i++) {
            queriesDoc.add(i, XMLXPathHandler.loadDocument("src/query/"+nom.toLowerCase()+"/" + listFiles[i]));
        }

        // Signature de chaque document de requête
        for (int i = 0; i < listFiles.length; i++) {
            Security.signXML(maPaire, queriesDoc.get(i));
        }

        // Envoi de chaque document signé dans le canal de communication partagé entre Alice et Bob
        Communication.sendDoc(pipeline, queriesDoc, listFiles.length);


        // Récupération des documents XML contenant les réponses de l'autre depuis le canal
        ArrayList<Document> docResponse = Communication.receiveDoc(pipeline, new ArrayList<Document>());

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
    }
}