package de.rwthaachen.openlap.analyticsengine;

import javax.ws.rs.GET;
import javax.ws.rs.Produces;
import javax.ws.rs.Path;

/**
 * Created by Arham Muslim
 * on 21-Dec-15.
 */
// The Java class will be hosted at the URI path "/helloworld"
@Path("/helloworld")
public class OpenLAP {
    // The Java method will process HTTP GET requests
    @GET
    // The Java method will produce content identified by the MIME Media type "text/plain"
    @Produces("text/plain")
    public String getClichedMessage() {
        // Return some cliched textual content
        return "Hello World";
    }
}
