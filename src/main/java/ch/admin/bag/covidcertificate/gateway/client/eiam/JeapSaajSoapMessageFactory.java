package ch.admin.bag.covidcertificate.gateway.client.eiam;

import org.springframework.ws.soap.saaj.SaajSoapMessageFactory;

import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;


/**
 * Diese Klasse stellt sicher, dass im Header der SoapMessage nur Accept: text/xml steht. Auf Grund eines Fehlers im Nevis-Webservice ist es
 * so, dass ein Fault-Response des Webservice, wenn im Header "text/html" enthalten ist, dieser Umgeleitet wird auf eine Web-URL. Somit kann
 * dies verhindert werden.
 *
 * @author A80765555
 */
public class JeapSaajSoapMessageFactory extends SaajSoapMessageFactory {

    @Override
    protected void postProcess(SOAPMessage soapMessage) throws SOAPException {
        super.postProcess(soapMessage);
        soapMessage.getMimeHeaders().setHeader("Accept", "text/xml");
    }

}
