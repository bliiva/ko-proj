package lv.lu.eztf.dn.combopt.evrp.domain;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIdentityReference;

import ai.timefold.solver.core.api.domain.solution.PlanningEntityCollectionProperty;
import ai.timefold.solver.core.api.domain.solution.PlanningScore;
import ai.timefold.solver.core.api.domain.solution.PlanningSolution;
import ai.timefold.solver.core.api.domain.solution.ProblemFactCollectionProperty;
import ai.timefold.solver.core.api.domain.valuerange.ValueRangeProvider;
import ai.timefold.solver.core.api.score.buildin.hardsoft.HardSoftScore;
import ai.timefold.solver.core.api.solver.SolverStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@PlanningSolution
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
@Slf4j
public class EVRPsolution {
    SolverStatus solverStatus;
    @PlanningScore
    HardSoftScore score;
    String name;

    @ProblemFactCollectionProperty
    @JsonIdentityReference(alwaysAsId = true)
    List<Terminal> terminalList = new ArrayList<>();

    @ProblemFactCollectionProperty
    @JsonIdentityReference(alwaysAsId = true)
    List<Company> companyList = new ArrayList<>();

    // Flights are problem facts; assignments are Visits.
    @ProblemFactCollectionProperty
    //@JsonIdentityReference(alwaysAsId = true)
    List<Plane> planeList = new ArrayList<>();

    @ValueRangeProvider
    @PlanningEntityCollectionProperty
    //@JsonIdentityReference(alwaysAsId = true)
    List<Visit> visitList = new ArrayList<>();

    @PlanningEntityCollectionProperty
    //@JsonIdentityReference(alwaysAsId = true)
    List<Gate> gateList = new ArrayList<>();
}
