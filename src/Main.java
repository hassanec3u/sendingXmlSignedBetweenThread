import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.SynchronousQueue;

/**
 * Cette classe contient la méthode principale pour exécuter le programme.
 */
public class Main {
    public static void main(String[] args) {

        // Génération de la paire de clés pour chaque agent
        KeyPair AliceKeyPair = generateKeyPair();
        KeyPair BobKeyPair = generateKeyPair();

        //creation d'un moyen de communication entre les agents
        BlockingQueue<Object> pipeline = new SynchronousQueue<>();

        // Création des threads représentant les agents
        Thread agent1 = new Thread(new Alice("Alice", AliceKeyPair, BobKeyPair.getPublic(), pipeline));
        Thread agent2 = new Thread(new Bob("Bob", BobKeyPair, AliceKeyPair.getPublic(), pipeline));


        // Démarrage des threads
        agent1.start();
        agent2.start();
    }

    /**
     * Méthode pour générer une paire de clés RSA.
     *
     * @return Une paire de clés générée.
     */
    private static KeyPair generateKeyPair() {
        try {
            KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
            keyPairGenerator.initialize(2048); // Taille de la clé, peut être ajustée selon les besoins
            return keyPairGenerator.generateKeyPair();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null;
        }
    }
}

