package lv.lu.eztf.dn.combopt.evrp.domain;

import ai.timefold.solver.core.api.domain.entity.PlanningEntity;
import ai.timefold.solver.core.api.domain.lookup.PlanningId;
import ai.timefold.solver.core.api.domain.variable.CascadingUpdateShadowVariable;
import ai.timefold.solver.core.api.domain.variable.InverseRelationShadowVariable;
import ai.timefold.solver.core.api.domain.variable.NextElementShadowVariable;
import ai.timefold.solver.core.api.domain.variable.PreviousElementShadowVariable;
import com.fasterxml.jackson.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@PlanningEntity
@Getter @Setter @AllArgsConstructor @NoArgsConstructor
@JsonIdentityInfo(scope = Visit.class, property = "id",
        generator = ObjectIdGenerators.PropertyGenerator.class)
public class  Visit {
    @PlanningId
    private String id;

    // The flight data (problem fact).
    @JsonIdentityReference(alwaysAsId = true)
    Plane plane;

    String name;

    // Gate is determined by which Gate.visits list contains this visit.
    @InverseRelationShadowVariable(sourceVariableName = "visits")
    @JsonIdentityReference(alwaysAsId = true)
    Gate gate;

    Long startTime; // same unit as Plane scheduled times (minutes)
    Long endTime;   // same unit as Plane scheduled times (minutes)
    @PreviousElementShadowVariable(sourceVariableName = "visits")
    @JsonIdentityReference(alwaysAsId = true)
    Visit previous;
    @NextElementShadowVariable(sourceVariableName = "visits")
    @JsonIdentityReference(alwaysAsId = true)
    Visit next;
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    @CascadingUpdateShadowVariable(targetMethodName = "updateShadows")
    public Long arrivalTime = null;
    public void updateShadows() {
        // Unassigned -> no times.
        if (this.getPlane() == null || this.getGate() == null) {
            this.setArrivalTime(null);
            this.setStartTime(null);
            this.setEndTime(null);
            return;
        }

        // Sequencing on a gate: earliest gate-available time is previous visit's end.
        final Long gateAvailableTime = (this.getPrevious() == null) ? null : this.getPrevious().getEndTime();
        final Long scheduledArrival = this.getPlane().getScheduledArrivalTime();

        Long computedArrival = scheduledArrival;
        if (computedArrival != null && gateAvailableTime != null) {
            computedArrival = Math.max(computedArrival, gateAvailableTime);
        } else if (computedArrival == null) {
            computedArrival = gateAvailableTime;
        }

        this.setArrivalTime(computedArrival);
        if (computedArrival == null) {
            this.setStartTime(null);
            this.setEndTime(null);
            return;
        }

        // Service times can be adjusted by gate service speed (default 1.0).
        final Double coeffObj = this.getGate().getServiceSpeedCoefficient();
        final double coeff = (coeffObj == null) ? 1.0 : coeffObj.doubleValue();
        final long arrivalService = Math.round(this.getPlane().getServiceTimeArrival() * coeff);
        final long departureService = Math.round(this.getPlane().getServiceTimeDeparture() * coeff);

        // Model: can't depart before scheduledDepartureTime, and must spend both services.
        final long earliestPossibleDeparture = computedArrival + arrivalService + departureService;
        final Long scheduledDeparture = this.getPlane().getScheduledDepartureTime();
        final long actualDeparture = (scheduledDeparture == null)
                ? earliestPossibleDeparture
                : Math.max(scheduledDeparture, earliestPossibleDeparture);

        this.setStartTime(computedArrival);
        this.setEndTime(actualDeparture);

        // Long dur = this.getVisitTime();
        // this.setEndTime(dur == null ? null : computedArrival + dur);
    }

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    public Long getDepartureTime() {
        // In this model, leaving the gate == endTime.
        return this.getEndTime();
    }

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    public Long getDelay() {
        if (this.getPlane() == null || this.getDepartureTime() == null || this.getPlane().getScheduledDepartureTime() == null) {
            return null;
        }
        return Math.max(0L, this.getDepartureTime() - this.getPlane().getScheduledDepartureTime());
    }

    @Override
    public String toString() {
        return this.getName();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
