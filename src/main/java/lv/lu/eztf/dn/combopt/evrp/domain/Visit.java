package lv.lu.eztf.dn.combopt.evrp.domain;

import java.util.Objects;

import ai.timefold.solver.core.api.domain.entity.PlanningEntity;
import ai.timefold.solver.core.api.domain.entity.PlanningPin;
import ai.timefold.solver.core.api.domain.lookup.PlanningId;
import ai.timefold.solver.core.api.domain.variable.PlanningVariable;

import com.fasterxml.jackson.annotation.JsonIgnore;

@PlanningEntity
public class Visit {


    @PlanningId
    private String id;
    private Plane plane;
    @PlanningPin
    private boolean pinned;
    private String visitType = "ARRIVAL";

    // Planning variables: changes during planning, between score calculations.
    @PlanningVariable
    private TimeGrain startingTimeGrain;
    @PlanningVariable
    private Gate gate;


    public Visit() {
    }

    public Visit(String id) {
        this.id = id;
    }

    public Visit(String id, Plane plane) {
        this(id);
        this.plane = plane;
    }

    public Visit(String id, Plane plane, String visitType) {
        this(id, plane);
        this.visitType = visitType;
    }

    public Visit(String id, Plane plane, TimeGrain startingTimeGrain, Gate gate) {
        this(id, plane);
        this.startingTimeGrain = startingTimeGrain;
        this.gate = gate;
    }

    public Visit(String id, Plane plane, TimeGrain startingTimeGrain, Gate gate, String visitType) {
        this(id, plane, startingTimeGrain, gate);
        this.visitType = visitType;
    }
    public String getVisitType() {
        return visitType;
    }

    public void setVisitType(String visitType) {
        this.visitType = visitType;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Plane getPlane() {
        return plane;
    }

    public void setPlane(Plane plane) {
        this.plane = plane;
    }

    public boolean isPinned() {
        return pinned;
    }

    public void setPinned(boolean pinned) {
        this.pinned = pinned;
    }

    public TimeGrain getStartingTimeGrain() {
        return startingTimeGrain;
    }

    public void setStartingTimeGrain(TimeGrain startingTimeGrain) {
        this.startingTimeGrain = startingTimeGrain;
    }

    public Gate getGate() {
        return gate;
    }

    public void setGate(Gate gate) {
        this.gate = gate;
    }

    // ************************************************************************
    // Complex methods
    // ************************************************************************

    @JsonIgnore
    public int getGrainIndex() {
        return getStartingTimeGrain().getGrainIndex();
    }

    @JsonIgnore
    public int calculateOverlap(Visit other) {
        if (startingTimeGrain == null || other.getStartingTimeGrain() == null) {
            return 0;
        }
        // start is inclusive, end is exclusive
        int start = startingTimeGrain.getGrainIndex();
        int end = getLastTimeGrainIndex() + 1;
        int otherStart = other.startingTimeGrain.getGrainIndex();
        int otherEnd = other.getLastTimeGrainIndex() + 1;
        if (otherEnd < start) {
            return 0;
        }
        if (end < otherStart) {
            return 0;
        }
        return Math.min(end, otherEnd) - Math.max(start, otherStart);
    }

    @JsonIgnore
    public Integer getLastTimeGrainIndex() {
        if (startingTimeGrain == null) {
            return null;
        }
        return startingTimeGrain.getGrainIndex() + (visitType.equals("ARRIVAL") ? plane.getArrivalDurationInGrains() : plane.getDepartureDurationInGrains()) - 1;
    }

    @JsonIgnore
    public int getGateCapacity() {
        if (gate == null) {
            return 0;
        }
        return gate.getCapacity();
    }

    @JsonIgnore
    public int getRequiredCapacity() {
        return plane.getRequiredCapacity();
    }

    @Override
    public String toString() {
        return plane.toString() + " [" + visitType + "]";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof Visit that))
            return false;
        return Objects.equals(getId(), that.getId());
    }

    @Override
    public int hashCode() {
        return getId().hashCode();
    }
}
