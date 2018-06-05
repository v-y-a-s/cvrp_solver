package org.opts.sols.domain;


import java.util.List;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class JsonVehicleRoute {

    protected String depotLocationName;
    protected double depotLatitude;
    protected double depotLongitude;

    protected String hexColor;
    protected int capacity;
    protected int demandTotal;

    protected List<JsonCustomer> customerList;

    public String getDepotLocationName() {
        return depotLocationName;
    }

    public void setDepotLocationName(String depotLocationName) {
        this.depotLocationName = depotLocationName;
    }

    public double getDepotLatitude() {
        return depotLatitude;
    }

    public void setDepotLatitude(double depotLatitude) {
        this.depotLatitude = depotLatitude;
    }

    public double getDepotLongitude() {
        return depotLongitude;
    }

    public void setDepotLongitude(double depotLongitude) {
        this.depotLongitude = depotLongitude;
    }

    public String getHexColor() {
        return hexColor;
    }

    public void setHexColor(String hexColor) {
        this.hexColor = hexColor;
    }

    public int getCapacity() {
        return capacity;
    }

    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }

    public int getDemandTotal() {
        return demandTotal;
    }

    public void setDemandTotal(int demandTotal) {
        this.demandTotal = demandTotal;
    }

    public List<JsonCustomer> getCustomerList() {
        return customerList;
    }

    public void setCustomerList(List<JsonCustomer> customerList) {
        this.customerList = customerList;
    }

}
