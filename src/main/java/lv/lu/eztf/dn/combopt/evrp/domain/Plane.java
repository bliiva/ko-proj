package lv.lu.eztf.dn.combopt.evrp.domain;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIdentityReference;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter @Getter @AllArgsConstructor @NoArgsConstructor
@JsonIdentityInfo(scope = Plane.class, property = "id",
        generator = ObjectIdGenerators.PropertyGenerator.class)
public class Plane {
    String id;
    Long scheduledArrivalTime;      // minutes since day start (or any consistent time unit)
    Long scheduledDepartureTime;    // minutes since day start (or any consistent time unit)
    int serviceTimeArrival; // minutes
    int serviceTimeDeparture; // minutes
    List<String> necessaryGateTypes;
    int servicePriority; // 1 - low, 5 - high

    @JsonIdentityReference(alwaysAsId = true)
    Company company;
    // public Visit lastSupplier() {
    //     Visit last = null;
    //     if (!this.getVisits().isEmpty()) {
    //         last = this.getVisits().get(this.getVisits().size() - 1);
    //     }
    //     //log.info(String.valueOf(last));
    //     return last;
    // }

    // @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    // public Double getTotalDistance() {
    //     Double totalDistance = 0.0;
    //     Location prevLoc = this.getDepot();
    //     for (Visit visit: this.getVisits()) {
    //         totalDistance = totalDistance +
    //                 prevLoc.distanceTo(visit.getLocation());
    //         prevLoc = visit.getLocation();
    //     }
    //     totalDistance = totalDistance +
    //             prevLoc.distanceTo(this.getDepot());
    //     return totalDistance;
    // }
    // @JsonIgnore
    // public Boolean isBatteryEmpty() {
    //     Boolean batteryEmpty = false;
    //     Double charge = this.getCharge();
    //     Location prevLoc = this.getDepot();
    //     for (Visit visit: this.getVisits()) {
    //         charge = charge - this.getDischargeSpeed() * prevLoc.distanceTo(visit.getLocation());
    //         if (charge < 0) { batteryEmpty = true; }
    //         if (visit instanceof ChargingStation) {charge = this.getMaxCharge(); }
    //         prevLoc = visit.getLocation();
    //     }
    //     charge = charge - this.getDischargeSpeed() * prevLoc.distanceTo(this.getDepot());
    //     if (charge < 0) { batteryEmpty = true; }
    //     return batteryEmpty;

    // }

    @Override
    public String toString() {
        return this.id;
    }
}
