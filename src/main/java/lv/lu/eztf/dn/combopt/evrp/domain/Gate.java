package lv.lu.eztf.dn.combopt.evrp.domain;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIdentityReference;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter @Getter @AllArgsConstructor @NoArgsConstructor
@JsonIdentityInfo(scope = Gate.class,
    property = "id", generator = ObjectIdGenerators.PropertyGenerator.class)
public class Gate {
    String id;
    String type;
    Double serviceSpeedCoefficient;
    @JsonIdentityReference(alwaysAsId = true)
    Terminal terminal;
}
