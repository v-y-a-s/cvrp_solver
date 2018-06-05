package org.opts.sols.domain;

import java.util.List;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class JsonVehicleRoutingSolution {

    protected String name;

    protected List<JsonCustomer> customerList;
    protected List<JsonVehicleRoute> vehicleRouteList;

    protected Boolean feasible;
    protected String distance;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<JsonCustomer> getCustomerList() {
        return customerList;
    }

    public void setCustomerList(List<JsonCustomer> customerList) {
        this.customerList = customerList;
    }

    public List<JsonVehicleRoute> getVehicleRouteList() {
        return vehicleRouteList;
    }

    public void setVehicleRouteList(List<JsonVehicleRoute> vehicleRouteList) {
        this.vehicleRouteList = vehicleRouteList;
    }

    public Boolean getFeasible() {
        return feasible;
    }

    public void setFeasible(Boolean feasible) {
        this.feasible = feasible;
    }

    public String getDistance() {
        return distance;
    }

    public void setDistance(String distance) {
        this.distance = distance;
    }

}
