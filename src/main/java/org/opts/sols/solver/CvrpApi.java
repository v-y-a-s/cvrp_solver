package org.opts.sols.solver;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import java.awt.Color;
import java.io.File;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Context;

import org.optaplanner.examples.vehiclerouting.domain.VehicleRoutingSolution;
import org.optaplanner.core.api.score.buildin.hardsoftlong.HardSoftLongScore;
import org.optaplanner.examples.vehiclerouting.domain.Customer;
import org.optaplanner.examples.vehiclerouting.domain.Vehicle;
import org.optaplanner.examples.vehiclerouting.domain.location.Location;
import org.optaplanner.swing.impl.TangoColorFactory;
import org.opts.sols.domain.JsonCustomer;
import org.opts.sols.domain.JsonMessage;
import org.opts.sols.domain.JsonVehicleRoute;
import org.opts.sols.domain.JsonVehicleRoutingSolution;

/**
 * Root resource (exposed at "CvrpApi" path)
 */
@Path("/vehiclerouting")
public class CvrpApi {
	
    private static final NumberFormat NUMBER_FORMAT = new DecimalFormat("#,##0.00");
    private String fileName;
    
    
    @Inject
    private VehicleRoutingSolverManager solverManager;
    
    @Context
    private HttpServletRequest request;
    
    /**
     * Method handling HTTP GET requests. The returned object will be sent
     * to the client as "text/plain" media type.
     *
     * @return String that will be returned as a text/plain response.
     */
   
   

    @GET
    @Path("/setsFileName")
    @Produces("application/json")
	public JsonMessage setsFileName() {
    	
    	if(request.getHeader("fileName") != null)
    	{
    	request.getServletContext().setAttribute("fileName",request.getHeader("fileName"));
    	
        return new JsonMessage("Successfully Set Name : ");
    }
    	else {
    		return new JsonMessage("Mention File Name in header in the header parameter fileName");
    	}
    }
   



	@GET
    @Path("/getsFileName")
    @Produces("application/json")
	public JsonMessage getsFileName() {
    	
    		return new JsonMessage("FN : " + request.getServletContext().getAttribute("FILES_DIR") + File.separator + request.getServletContext().getAttribute("fileName"));
    	
    }
    
    @GET
    @Path("/solution")
    @Produces("application/json")
	public JsonVehicleRoutingSolution getSolution() {
    	// TODO : check if solution file string is empty
    	String DSURL = request.getServletContext().getAttribute("FILES_DIR") + File.separator + request.getServletContext().getAttribute("fileName");
        VehicleRoutingSolution solution = solverManager.retrieveOrCreateSolution(request.getSession().getId(),DSURL);
        return convertToJsonVehicleRoutingSolution(solution);
    }
	   
    protected JsonVehicleRoutingSolution convertToJsonVehicleRoutingSolution(VehicleRoutingSolution solution) {
        JsonVehicleRoutingSolution jsonSolution = new JsonVehicleRoutingSolution();
        jsonSolution.setName(solution.getName());
        List<JsonCustomer> jsonCustomerList = new ArrayList<>(solution.getCustomerList().size());
        for (Customer customer : solution.getCustomerList()) {
            Location customerLocation = customer.getLocation();
            jsonCustomerList.add(new JsonCustomer(customerLocation.getName(),
                    customerLocation.getLatitude(), customerLocation.getLongitude(), customer.getDemand()));
        }
        jsonSolution.setCustomerList(jsonCustomerList);
        List<JsonVehicleRoute> jsonVehicleRouteList = new ArrayList<>(solution.getVehicleList().size());
        TangoColorFactory tangoColorFactory = new TangoColorFactory();
        for (Vehicle vehicle : solution.getVehicleList()) {
            JsonVehicleRoute jsonVehicleRoute = new JsonVehicleRoute();
            Location depotLocation = vehicle.getDepot().getLocation();
            jsonVehicleRoute.setDepotLocationName(depotLocation.getName());
            jsonVehicleRoute.setDepotLatitude(depotLocation.getLatitude());
            jsonVehicleRoute.setDepotLongitude(depotLocation.getLongitude());
            jsonVehicleRoute.setCapacity(vehicle.getCapacity());
            Color color = tangoColorFactory.pickColor(vehicle);
            jsonVehicleRoute.setHexColor(
                    String.format("#%02x%02x%02x", color.getRed(), color.getGreen(), color.getBlue()));
            Customer customer = vehicle.getNextCustomer();
            int demandTotal = 0;
            List<JsonCustomer> jsonVehicleCustomerList = new ArrayList<>();
            while (customer != null) {
                Location customerLocation = customer.getLocation();
                demandTotal += customer.getDemand();
                jsonVehicleCustomerList.add(new JsonCustomer(customerLocation.getName(),
                        customerLocation.getLatitude(), customerLocation.getLongitude(), customer.getDemand()));
                customer = customer.getNextCustomer();
            }
            jsonVehicleRoute.setDemandTotal(demandTotal);
            jsonVehicleRoute.setCustomerList(jsonVehicleCustomerList);
            jsonVehicleRouteList.add(jsonVehicleRoute);
        }
        jsonSolution.setVehicleRouteList(jsonVehicleRouteList);
        HardSoftLongScore score = solution.getScore();
        jsonSolution.setFeasible(score != null && score.isFeasible());
        jsonSolution.setDistance(solution.getDistanceString(NUMBER_FORMAT));
        return jsonSolution;
    }

    @POST
    @Path("/solution/solve")
    @Produces("application/json")
    public JsonMessage solve() {
    	// TODO : check if solution file string is empty
    	String DSURL = request.getServletContext().getAttribute("FILES_DIR") + File.separator + request.getServletContext().getAttribute("fileName");
        boolean success = solverManager.solve(request.getSession().getId(),DSURL);
        return new JsonMessage(success ? "Solving started." : "Solver was already running.");
    }

    @POST
    @Path("/solution/terminateEarly")
    @Produces("application/json")
    public JsonMessage terminateEarly() {
        boolean success = solverManager.terminateEarly(request.getSession().getId());
        return new JsonMessage(success ? "Solver terminating early." : "Solver was already terminated.");
    }
}
