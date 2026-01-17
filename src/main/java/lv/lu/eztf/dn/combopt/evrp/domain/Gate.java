package lv.lu.eztf.dn.combopt.evrp.domain;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIdentityReference;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;

import ai.timefold.solver.core.api.domain.entity.PlanningEntity;
import ai.timefold.solver.core.api.domain.lookup.PlanningId;
import ai.timefold.solver.core.api.domain.variable.PlanningListVariable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter @Getter @AllArgsConstructor @NoArgsConstructor
@JsonIdentityInfo(scope = Gate.class,
    property = "id", generator = ObjectIdGenerators.PropertyGenerator.class)
@PlanningEntity
public class Gate {
    @PlanningId
    String id;
    String type;
    Double serviceSpeedCoefficient;
    @JsonIdentityReference(alwaysAsId = true)
    Terminal terminal;

    @PlanningListVariable
    List<Visit> visits = new ArrayList<>();
}
