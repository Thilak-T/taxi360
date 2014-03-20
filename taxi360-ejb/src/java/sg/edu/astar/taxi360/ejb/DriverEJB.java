/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package sg.edu.astar.taxi360.ejb;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import sg.edu.astar.taxi360.bo.AvailableDriverFacade;
import sg.edu.astar.taxi360.bo.DriverDestinationFacade;
import sg.edu.astar.taxi360.bo.DriverFacade;
import sg.edu.astar.taxi360.bo.RequestFacade;
import sg.edu.astar.taxi360.bo.RideFacade;
import sg.edu.astar.taxi360.entity.AvailableDriver;
import sg.edu.astar.taxi360.entity.Driver;
import sg.edu.astar.taxi360.entity.DriverDestination;
import sg.edu.astar.taxi360.entity.Location;
import sg.edu.astar.taxi360.entity.Request;
import sg.edu.astar.taxi360.entity.Ride;
import sg.edu.astar.taxi360.util.GCMMessenger;
import sg.edu.astar.taxi360.util.Resources;

/**
 *
 * @author mido
 */
@Stateless
public class DriverEJB {

    @EJB
    private DriverFacade driverFacade;
    @EJB
    private RequestFacade requestFacade;
    @EJB
    private AvailableDriverFacade availableDriverFacade;
    @EJB
    private RideFacade rideFacade;
    @EJB
    private DriverDestinationFacade driverDestinationFacade;

    public void changeStatus(AvailableDriver availableDriver, boolean status) {
        if (status) {
            if (availableDriverFacade.find(availableDriver.getDriver().getId()) == null) {
                availableDriverFacade.create(availableDriver);
            } else {
                availableDriverFacade.edit(availableDriver);
            }
        } else if (availableDriverFacade.find(availableDriver.getDriver().getId()) != null) {
            availableDriverFacade.remove(availableDriver);
        }
    }

    public Ride startRide(Ride ride) throws IOException {
        ride.setCreatetime(new Date());
        Request r;
        AvailableDriver ad;
        ride.setEndStatus("-1"); // to inform that this is active ride.
        if ((r = requestFacade.find(ride.getPassenger().getId())) != null) {
            ride.setPassengerStartLocation(r.getLocation());
            ride.setPassengerKey(r.getPassengerKey());
            requestFacade.remove(r);
        }
        if ((ad = availableDriverFacade.find(ride.getDriver().getId())) != null) {
            ride.setDriverStartLocation(ad.getLocation());
            availableDriverFacade.remove(ad);
        }

        if (ride.getId() == null) {
            rideFacade.create(ride);
        } else if (rideFacade.find(ride.getId()) != null) {
            rideFacade.edit(ride);
        }
        GCMMessenger.sendMessage(ride.getId(), Resources.get("gcm_ride_collapse_key"), ride.getPassengerKey());
        return ride;
    }

    public Ride endRide(Ride ride) throws IOException {
        AvailableDriver ad;
        Ride r;
        r = rideFacade.find(ride.getId());
        if (r != null) {
            //rideFacade.edit(ride);
            r.setEndtime(new Date());
            r.setEndStatus(ride.getEndStatus());
            r.setPlace(ride.getPlace());
            r.setDistance(ride.getDistance());
            rideFacade.edit(r);

            GCMMessenger.sendMessage(Resources.get("gcm_ride_message"), Resources.get("gcm_ride_collapse_key"), ride.getPassengerKey());
            ad = new AvailableDriver();
            ad.setDriver(ride.getDriver());
            changeStatus(ad, true);
        }
        System.out.println("ride2=" + r);
        return r;
    }

    public Driver getDriver(Long id) {
        return driverFacade.find(id);
    }

    public AvailableDriver getAvailableDriver(long id) {
        return availableDriverFacade.findById(id);
    }

    public void updateDriverLocation(AvailableDriver avDriver) {
        availableDriverFacade.edit(avDriver);
    }

    public void updateRating(Ride r) {
        
            Logger.getLogger(DriverEJB.class.getName()).log(Level.INFO,null,"ride="+r );
        Ride ride;
        Driver driver;
        ride = rideFacade.find(r.getId());
            Logger.getLogger(DriverEJB.class.getName()).log(Level.INFO,null,"ride2="+ride );
        if (ride != null) {
            ride.setDriverRating(r.getDriverRating());
            driver = getDriver(ride.getDriver().getId());

            if ((driver.getNumberofratings() == null) || (driver.getNumberofratings() == 0)) {
                driver.setNumberofratings(Long.valueOf(1));

                //gfchgcffg
                driver.setRating(r.getDriverRating());
                driverFacade.edit(driver);
                return;
            }

            driver.setNumberofratings(driver.getNumberofratings() + 1);
            driver.setRating((driver.getNumberofratings() * driver.getRating() + ride.getDriverRating()) / (driver.getNumberofratings() + 1));
            driverFacade.edit(driver);
            rideFacade.edit(ride);
        }

    }

    public List<AvailableDriver> findAvailableDriverWithin(Location location, Double radius) {
        return availableDriverFacade.findWithin(location, radius);
    }

    public long createDriver(Driver driver) {
        return driverFacade.createDriver(driver);
    }

    public void updateDriverLocation(Ride ride) {
        try {
            Ride r = rideFacade.find(ride.getId());
            if (r == null)
                throw new Exception("Error: Ride not Found");            
            r.setDriverStartLocation(ride.getDriverStartLocation());
            rideFacade.edit(r);
        } catch (Exception ex) {
            Logger.getLogger(DriverEJB.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void reachedNear(Ride ride) {
            Ride r = rideFacade.find(ride.getId());
        try {
            if (r == null) 
                throw new Exception("Error: Ride not Found");
            
            GCMMessenger.sendMessage(Resources.get("gcm_driver_close_message"), Resources.get("gcm_ride_collapse_key"), r.getPassengerKey());
        } catch (Exception ex) {
            Logger.getLogger(DriverEJB.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void addDestination(DriverDestination driverDestination) {
        Driver d= driverFacade.find(driverDestination.getDriverid().getId());
        if (d==null)
                return;
        driverDestination.setCreatetime(new Date());
        driverDestination.setDriverid(d);
        driverDestinationFacade.create(driverDestination);
        
    }

    public DriverDestination getDriverDestiination(Long id) {
        return driverDestinationFacade.find(id);
    }

    public ArrayList<DriverDestination> getDriverDestinations(Driver d) {
        return driverFacade.find(d.getId()).getDriverDestinations();
    }
    
    
    

}
