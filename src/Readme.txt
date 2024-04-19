-------------------------------------------------------------------------Agent Alice-------------------------------------------------------------------------
La classe Alice représente le comportement et les actions réalisées par Alice dans un système de communication sécurisée avec Bob. Voici un résumé des principales fonctionnalités et du fonctionnement de cette classe :

Phase 1 :
  Envoi de Requêtes XPath à Bob :
- Alice lit les fichiers XML contenant des requêtes XPath depuis "src/query/alice".
- Chaque requête est signée avec la clé privée d'Alice 
- Les requêtes signées sont envoyées à Bob via le canal de communication partagé.
- Alice attend ensuite les réponses de Bob.

Réception et Traitement des Réponses de Bob :
- Alice reçoit les documents XML contenant les réponses de Bob depuis le canal de communication.
- Pour chaque réponse reçue, Alice vérifie la validité de la signature pour s'assurer qu'elle provient bien de Bob.
- Les réponses validées sont sauvegardées dans le dossier "src/response/alice

Phase 2 :
 Traitement des Requêtes XPath recu de Bob :
- Alice reçoit les documents XML contenant les requêtes Xpath de Bob depuis le canal de communication.
- Chaque requête est validée et exécutée dans la base de données XML d'Alice.
- Les réponses aux requêtes sont construites et signées par Alice.
- Les réponses signées sont envoyées à Bob via le canal de communication partagé.
- Gestion des Exceptions :
Des exceptions sont gérées pour assurer la robustesse du système, telles que les erreurs de signature ou les interruptions de processus.

-------------------------------------------------------------------------Agent Bob-------------------------------------------------------------------------

"Même procédé que Alice, sauf qu'ici, nous inverserons les phases 1 et 2."


-------------------------------------------------------------------------Main-------------------------------------------------------------------------


La classe Main sert de point d'entrée pour l'exécution du programme. Voici un résumé des fonctionnalités de cette classe :

Génération de Paires de Clés RSA :
- La méthode generateKeyPair() est utilisée pour générer une paire de clés RSA pour chaque agent (Alice et Bob).
- La taille des clés est fixée à 2048 bits, mais peut être ajustée selon les besoins.

Création du Canal de Communication :
- Un BlockingQueue de type SynchronousQueue est utilisé comme moyen de communication entre les agents Alice et Bob.
- Ce canal permet un échange sécurisé et synchronisé des informations entre les agents.

Création et Démarrage des Threads des Agents :
- Des threads sont créés pour représenter les agents Alice et Bob.
- Chaque agent est initialisé avec son nom, sa paire de clés, la clé publique de son interlocuteur et le canal de communication.
Les threads des agents sont démarrés, ce qui lance l'exécution de leurs comportements respectifs.

