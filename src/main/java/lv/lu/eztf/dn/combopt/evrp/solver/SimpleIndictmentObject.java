package lv.lu.eztf.dn.combopt.evrp.solver;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import ai.timefold.solver.core.api.score.buildin.hardsoft.HardSoftScore;
import ai.timefold.solver.core.api.score.constraint.ConstraintMatch;
import lombok.Getter;
import lombok.Setter;
import lv.lu.eztf.dn.combopt.evrp.domain.Gate;
import lv.lu.eztf.dn.combopt.evrp.domain.Plane;
import lv.lu.eztf.dn.combopt.evrp.domain.Visit;

@Setter @Getter
public class SimpleIndictmentObject {
    private String indictedObjectID;
    private String indictedObjectClass;
    private String indictedObjectLabel;
    private String planeId;
    private String gateId;
    private String visitType;
    private HardSoftScore score;
    private int matchCount;
    private List<SimpleConstraintMatch> constraintMatches = new ArrayList<>();

    public SimpleIndictmentObject(Object indictedObject, HardSoftScore score, int matchCount, Set<ConstraintMatch<HardSoftScore>> constraintMatches) {
        if (indictedObject instanceof Visit) {
            Visit visit = (Visit) indictedObject;
            this.indictedObjectID = visit.getId();
            this.indictedObjectClass = "Visit";
            this.indictedObjectLabel = visit.getName();
            this.planeId = visit.getPlane() == null ? null : visit.getPlane().getId();
            this.gateId = visit.getGate() == null ? null : visit.getGate().getId();
            this.visitType = visit.getType() == null ? null : visit.getType().name();
        } else if (indictedObject instanceof Plane) {
            Plane plane = (Plane) indictedObject;
            this.indictedObjectID = plane.getId();
            this.indictedObjectClass = "Plane";
            this.indictedObjectLabel = plane.getId();
            this.planeId = plane.getId();
        } else if (indictedObject instanceof Gate) {
            Gate gate = (Gate) indictedObject;
            this.indictedObjectID = gate.getId();
            this.indictedObjectClass = "Gate";
            this.indictedObjectLabel = gate.getId();
            this.gateId = gate.getId();
        } else {
            this.indictedObjectID = indictedObject == null ? null : String.valueOf(indictedObject);
            this.indictedObjectClass = indictedObject == null ? "null" : indictedObject.getClass().getSimpleName();
            this.indictedObjectLabel = this.indictedObjectID;
        }
        this.score = score;
        this.matchCount = matchCount;
        this.constraintMatches = constraintMatches.stream().map(constraintMatch -> {
            return new SimpleConstraintMatch(constraintMatch);
        }).collect(Collectors.toList());
    }
}
