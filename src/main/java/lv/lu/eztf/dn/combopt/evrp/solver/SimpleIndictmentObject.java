package lv.lu.eztf.dn.combopt.evrp.solver;

import ai.timefold.solver.core.api.score.buildin.hardsoft.HardSoftScore;
import ai.timefold.solver.core.api.score.constraint.ConstraintMatch;
import lombok.Getter;
import lombok.Setter;
// import lv.lu.eztf.dn.combopt.evrp.domain.ChargingStation;
import lv.lu.eztf.dn.combopt.evrp.domain.Gate;
import lv.lu.eztf.dn.combopt.evrp.domain.Plane;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Setter @Getter
public class SimpleIndictmentObject {
    private String indictedObjectID;
    private String indictedObjectClass;
    private HardSoftScore score;
    private int matchCount;
    private List<SimpleConstraintMatch> constraintMatches = new ArrayList<>();

    public SimpleIndictmentObject(Object indictedObject, HardSoftScore score, int matchCount, Set<ConstraintMatch<HardSoftScore>> constraintMatches) {
        this.indictedObjectID = indictedObject instanceof Plane ? ((Plane) indictedObject).getId() :
                // indictedObject instanceof Gate ? ((Gate) indictedObject).getId() :
                        // indictedObject instanceof ChargingStation ? ((ChargingStation) indictedObject).getName() :
                                "0";
        this.indictedObjectClass = indictedObject instanceof Plane ? "Plane" :
                // indictedObject instanceof Gate ? "Gate" :
                        // indictedObject instanceof ChargingStation ? "ChargingStation" :
                                "Object";
        this.score = score;
        this.matchCount = matchCount;
        this.constraintMatches = constraintMatches.stream().map(constraintMatch -> {
            return new SimpleConstraintMatch(constraintMatch);
        }).collect(Collectors.toList());
    }
}
