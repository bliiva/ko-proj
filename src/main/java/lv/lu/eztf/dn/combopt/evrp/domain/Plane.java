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

    @Override
    public String toString() {
        return this.id;
    }
}
