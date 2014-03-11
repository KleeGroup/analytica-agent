package io.analytica.mock;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

/**
 * Plugin gérant l'api reseau en REST avec jersey.
 * @author npiedeloup
 * @version $Id: RestNetApiPlugin.java,v 1.3 2012/10/16 12:39:27 npiedeloup Exp $
 */
@Path("/process")
public final class RestMockServerPlugin {
	/**
	 * Constructeur simple pour instanciation par jersey.
	 */
	public RestMockServerPlugin() {

	}

	/**
	 * @param json json du process recu
	 */
	@POST
	@Consumes(MediaType.TEXT_PLAIN)
	public void push(final String json) {
		MockServerManager.getInstance().push(json);
	}

	@GET
	@Path("/version")
	@Produces(MediaType.TEXT_PLAIN)
	public String getVersion() {
		return "1.0.0";
	}
}
