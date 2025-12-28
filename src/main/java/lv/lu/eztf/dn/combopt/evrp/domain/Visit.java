package lv.lu.eztf.dn.combopt.evrp.domain;

import ai.timefold.solver.core.api.domain.entity.PlanningEntity;
import ai.timefold.solver.core.api.domain.lookup.PlanningId;
import ai.timefold.solver.core.api.domain.variable.*;
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

    @JsonIdentityReference(alwaysAsId = true)
    Gate gate;
    Long startTime; // second of a day
    Long endTime; // second of a day
    String name;

    @InverseRelationShadowVariable(sourceVariableName = "visits")
    @JsonIdentityReference(alwaysAsId = true)
    Plane plane;
    @PreviousElementShadowVariable(sourceVariableName = "visits")
    @JsonIdentityReference(alwaysAsId = true)
    Visit previous;
    @NextElementShadowVariable(sourceVariableName = "visits")
    @JsonIdentityReference(alwaysAsId = true)
    Visit next;
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    // public abstract Long getVisitTime();
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

        // No movement time between gates:
        // earliest arrival is plane arrival for first visit, otherwise previous end.
        final Long computedArrival = (this.getPrevious() == null)
                ? this.getPlane().getScheduledArrivalTime()
                : this.getPrevious().getEndTime();

        this.setArrivalTime(computedArrival);

        if (computedArrival == null) {
            this.setStartTime(null);
            this.setEndTime(null);
            return;
        }

        // If you have additional time windows, apply them here via Math.max(...).
        this.setStartTime(computedArrival);
        this.setEndTime(computedArrival+ this.getPlane().getServiceTimeArrival());

        // Long dur = this.getVisitTime();
        // this.setEndTime(dur == null ? null : computedArrival + dur);
    }

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    public Long getDepartureTime() {
        // In this model, leaving the gate == endTime.
        return this.getEndTime();
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
