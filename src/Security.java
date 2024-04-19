import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import javax.xml.crypto.dsig.*;
import javax.xml.crypto.dsig.dom.DOMSignContext;
import javax.xml.crypto.dsig.dom.DOMValidateContext;
import javax.xml.crypto.dsig.keyinfo.KeyInfo;
import javax.xml.crypto.dsig.keyinfo.KeyInfoFactory;
import javax.xml.crypto.dsig.keyinfo.KeyValue;
import javax.xml.crypto.dsig.spec.C14NMethodParameterSpec;
import javax.xml.crypto.dsig.spec.TransformParameterSpec;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.security.KeyPair;
import java.security.PublicKey;
import java.util.Collections;

/**
 * Cette classe fournit des méthodes pour la sécurité des documents XML.
 */
public class Security {

    /**
     * Valide la signature d'un document XML à l'aide d'une clé publique.
     *
     * @param publicKey La clé publique utilisée pour la validation de la signature.
     * @param document  Le document XML à valider.
     * @return true si la signature est valide, sinon false.
     */
    public static boolean validateSignature(PublicKey publicKey, Document document) {
        try {
            // Charger le document XML
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            dbf.setNamespaceAware(true);
            DocumentBuilder builder = dbf.newDocumentBuilder();


            // Trouver la signature XML dans le document
            NodeList nl = document.getElementsByTagNameNS(XMLSignature.XMLNS, "Signature");
            if (nl.getLength() == 0) {
                throw new Exception("Impossible de trouver la signature dans le document.");
            }

            // Créer un contexte de validation DOM pour la signature
            DOMValidateContext valContext = new DOMValidateContext(publicKey, nl.item(0));

            // Créer une instance de XMLSignatureFactory
            XMLSignatureFactory factory = XMLSignatureFactory.getInstance("DOM");

            // Charger la signature XML à partir du document et du contexte
            XMLSignature signature = factory.unmarshalXMLSignature(valContext);

            // Valider la signature XML
            boolean isValid = signature.validate(valContext);

            if (!isValid) {
                throw new RuntimeException("Un des Document n'est pas valide");
            }

            return isValid;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Signe un document XML à l'aide d'une paire de clés.
     *
     * @param kp  La paire de clés utilisée pour la signature.
     * @param doc Le document XML à signer.
     */
    public static void signXML(KeyPair kp, Document doc) {
        try {
            // Création d'un objet XMLSignatureFactory
            XMLSignatureFactory sigFactory = XMLSignatureFactory.getInstance("DOM");


            // Création des éléments de la signature
            Reference ref = sigFactory.newReference("", sigFactory.newDigestMethod(DigestMethod.SHA1, null),
                    Collections.singletonList(sigFactory.newTransform(Transform.ENVELOPED, (TransformParameterSpec) null)),
                    null, null);
            SignedInfo signedInfo = sigFactory.newSignedInfo(sigFactory.newCanonicalizationMethod(CanonicalizationMethod.INCLUSIVE,
                            (C14NMethodParameterSpec) null), sigFactory.newSignatureMethod(SignatureMethod.RSA_SHA1, null),
                    Collections.singletonList(ref));

            // Création de la clé de signature
            KeyInfoFactory keyInfoFactory = sigFactory.getKeyInfoFactory();
            KeyValue keyValue = keyInfoFactory.newKeyValue(kp.getPublic());
            KeyInfo keyInfo = keyInfoFactory.newKeyInfo(Collections.singletonList(keyValue));

            // Création du contexte de signature
            DOMSignContext signContext = new DOMSignContext(kp.getPrivate(), doc.getDocumentElement());

            // Création de l'objet XMLSignature
            XMLSignature signature = sigFactory.newXMLSignature(signedInfo, keyInfo);

            // Signature du document
            signature.sign(signContext);


        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

}
