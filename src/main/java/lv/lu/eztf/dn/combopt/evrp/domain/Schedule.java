package lv.lu.eztf.dn.combopt.evrp.domain;

import java.util.List;
import java.util.stream.Stream;

import ai.timefold.solver.core.api.domain.solution.PlanningEntityCollectionProperty;
import ai.timefold.solver.core.api.domain.solution.PlanningScore;
import ai.timefold.solver.core.api.domain.solution.PlanningSolution;
import ai.timefold.solver.core.api.domain.solution.ProblemFactCollectionProperty;
import ai.timefold.solver.core.api.domain.valuerange.ValueRangeProvider;
import ai.timefold.solver.core.api.score.buildin.hardmediumsoft.HardMediumSoftScore;
import ai.timefold.solver.core.api.solver.SolverStatus;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

@PlanningSolution
public class Schedule {

    @ProblemFactCollectionProperty
    @ValueRangeProvider
    private List<TimeGrain> timeGrains;
    @ProblemFactCollectionProperty
    @ValueRangeProvider
    private List<Gate> gates;
    @ProblemFactCollectionProperty
    private List<Plane> planes;
    @PlanningEntityCollectionProperty
    private List<Visit> visits;

    @PlanningScore
    private HardMediumSoftScore score;

    private SolverStatus solverStatus;

    public Schedule() {
    }

    @JsonCreator
    public Schedule(@JsonProperty("planes") List<Plane> planes, @JsonProperty("timeGrains") List<TimeGrain> timeGrains,
            @JsonProperty("gates") List<Gate> gates, @JsonProperty("visits") List<Visit> visits) {
        this.planes = planes;
        this.timeGrains = timeGrains;
        this.gates = gates;
        this.visits = visits;
    }

    public Schedule(HardMediumSoftScore score, SolverStatus solverStatus) {
        this.score = score;
        this.solverStatus = solverStatus;
    }

    public List<Plane> getPlanes() {
        return planes;
    }

    public void setPlanes(List<Plane> planes) {
        this.planes = planes;
    }

    public List<TimeGrain> getTimeGrains() {
        return timeGrains;
    }

    public void setTimeGrains(List<TimeGrain> timeGrains) {
        this.timeGrains = timeGrains;
    }

    public List<Gate> getGates() {
        return gates;
    }

    public void setGates(List<Gate> gates) {
        this.gates = gates;
    }

    public List<Visit> getVisits() {
        return visits;
    }

    public void setVisits(List<Visit> visits) {
        this.visits = visits;
    }

    public HardMediumSoftScore getScore() {
        return score;
    }

    public void setScore(HardMediumSoftScore score) {
        this.score = score;
    }

    public SolverStatus getSolverStatus() {
        return solverStatus;
    }

    public void setSolverStatus(SolverStatus solverStatus) {
        this.solverStatus = solverStatus;
    }
}
