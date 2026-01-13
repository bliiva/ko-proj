package lv.lu.eztf.dn.combopt.evrp.domain;

import java.util.Objects;

import ai.timefold.solver.core.api.domain.lookup.PlanningId;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;

@JsonIdentityInfo(scope = Gate.class, generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
public class Gate {

    @PlanningId
    private String id;
    private String name;
    private int capacity;

    public Gate() {
    }

    public Gate(String id) {
        this.id = id;
    }

    public Gate(String id, String name) {
        this(id);
        this.name = name;
    }

    public Gate(String id, String name, int capacity) {
        this(id, name);
        this.capacity = capacity;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public int getCapacity() {
        return capacity;
    }

    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof Gate gate))
            return false;
        return Objects.equals(getId(), gate.getId());
    }

    @Override
    public int hashCode() {
        return getId().hashCode();
    }
}

