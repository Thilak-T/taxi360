/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package sg.edu.astar.ihpc.resources;

import java.util.ArrayList;
import javax.ejb.EJB;
import javax.enterprise.context.RequestScoped;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;
import sg.edu.astar.taxi360.ejb.DriverEJB;
import sg.edu.astar.taxi360.entity.Driver;
import sg.edu.astar.taxi360.entity.DriverDestination;

/**
 * REST Web Service
 *
 * @author mido
 */
@Path("driverDestionations")
@RequestScoped
public class DriverDestinationResource {

    @Context
    private UriInfo context;

    /**
     * Creates a new instance of DriverResource
     */
    public DriverDestinationResource() {
    }

    @EJB
    private DriverEJB driverEJB;

    @POST
    public void addDestination(DriverDestination driverDestination) {
        driverEJB.addDestination(driverDestination);
    }

    
    @GET
    @Path("{id}")
    public DriverDestination getDriver(@PathParam("id") Long id) {
        return driverEJB.getDriverDestiination(id);
    }
    
    
    
    @GET
    public ArrayList<DriverDestination> getDriver(Driver d) {
        return driverEJB.getDriverDestinations(d);
    }
    
}

