
package uy.gub.imm.sae.common.factories.ws.client.recursos;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.Logger;
import javax.xml.namespace.QName;
import javax.xml.ws.Service;
import javax.xml.ws.WebEndpoint;
import javax.xml.ws.WebServiceClient;

import uy.gub.imm.sae.common.SAEProfile;


/**
 * This class was generated by the JAX-WS RI.
 * JAX-WS RI 2.1.3-b02-
 * Generated source version: 2.0
 * 
 */
@WebServiceClient(name = "ManejoRecursosService", targetNamespace = "http://montevideo.gub.uy/schema/sae/1.0/", wsdlLocation = "/SAE-WS/ManejoRecursos?wsdl")
public class ManejoRecursosService
    extends Service
{

    private final static URL MANEJORECURSOSSERVICE_WSDL_LOCATION;
    private final static Logger logger = Logger.getLogger(uy.gub.imm.sae.common.factories.ws.client.recursos.ManejoRecursosService.class.getName());

    static {
        URL url = null;
        try {
            URL baseUrl;
            baseUrl = uy.gub.imm.sae.common.factories.ws.client.recursos.ManejoRecursosService.class.getResource(".");
            
            url = new URL(
            		baseUrl, 
            		SAEProfile.getInstance().getProperties().getProperty(SAEProfile.ENVIRONMENT_PROFILE_WS_WSDL_HOST) +
            		"/" + SAEProfile.getInstance().getProperties().getProperty(SAEProfile.ENVIRONMENT_PROFILE_WS_WSDL_CONTEXT_ROOT) + 
            		"/ManejoRecursos?wsdl");
            
        } catch (MalformedURLException e) {
            logger.warning("Failed to create URL for the wsdl Location: '" + 
            		SAEProfile.getInstance().getProperties().getProperty(SAEProfile.ENVIRONMENT_PROFILE_WS_WSDL_HOST) + 
            		"/" + SAEProfile.getInstance().getProperties().getProperty(SAEProfile.ENVIRONMENT_PROFILE_WS_WSDL_CONTEXT_ROOT) + 
            		"/ManejoRecursos?wsdl', retrying as a local file");
            logger.warning(e.getMessage());
        }
        MANEJORECURSOSSERVICE_WSDL_LOCATION = url;
    }

    public ManejoRecursosService(URL wsdlLocation, QName serviceName) {
        super(wsdlLocation, serviceName);
    }

    public ManejoRecursosService() {
        super(MANEJORECURSOSSERVICE_WSDL_LOCATION, new QName("http://montevideo.gub.uy/schema/sae/1.0/", "ManejoRecursosService"));
    }

    /**
     * 
     * @return
     *     returns RecursosWS
     */
    @WebEndpoint(name = "ManejoRecursosPort")
    public RecursosWS getManejoRecursosPort() {
        return super.getPort(new QName("http://montevideo.gub.uy/schema/sae/1.0/", "ManejoRecursosPort"), RecursosWS.class);
    }

}