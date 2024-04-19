import org.w3c.dom.Document;

import java.util.ArrayList;
import java.util.concurrent.BlockingQueue;


public class Communication {

    /**
     * Récupère les documents XML contenant les réponses dans le canal.
     *
     * @param pipeline        Le canal de communication.
     * @param requetsDocument La liste où ajouter les documents XML reçus.
     * @return La liste mise à jour des documents XML reçus.
     */
    public static ArrayList<Document> receiveDoc(BlockingQueue<Object> pipeline, ArrayList<Document> requetsDocument) {
        boolean communicationIsFinished = false;

        try {
            Object request;
            while (!communicationIsFinished) {

                request = pipeline.take();
                if (request instanceof Document) {

                    requetsDocument.add((Document) request);
                } else if (request instanceof String) {
                    if (request.equals("finished")) {
                        //dans ce cas l'envoie des documents xml est terminé
                        communicationIsFinished = true;
                    }
                }
            }
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        return requetsDocument;
    }

    /**
     * Envoie des documents XML via le canal de communication.
     *
     * @param pipeline                  Le canal de communication.
     * @param requetsDocument           La liste des documents XML à envoyer.
     * @param numberOfDocumentsToSend   Le nombre de documents à envoyer.
     */
    public static void sendDoc(BlockingQueue<Object> pipeline, ArrayList<Document> requetsDocument, int numberOfDocumentsToSend) {
        try {
            for (int i = 0; i < numberOfDocumentsToSend; i++) {
                pipeline.put(requetsDocument.get(i));
            }

            //On notifie que la les envoies sont finis
            pipeline.put("finished");
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
