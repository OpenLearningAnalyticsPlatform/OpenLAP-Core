package de.rwthaachen.openlap.analyticsengine.datamodel;

import javax.persistence.*;

/**
 * Created by Arham Muslim
 * on 24-Feb-16.
 */
@Entity
@Table(name = "Entity", schema = "dbo", catalog = "LADB")
public class OpenLAPEntity {
    private int eId;
    private Integer eventFk;
    private String entityKey;
    private String value;

    @Id
    @Column(name = "E_ID", nullable = false)
    public int geteId() {
        return eId;
    }

    public void seteId(int eId) {
        this.eId = eId;
    }

    @Basic
    @Column(name = "Event_fk", nullable = true)
    public Integer getEventFk() {
        return eventFk;
    }

    public void setEventFk(Integer eventFk) {
        this.eventFk = eventFk;
    }

    @Basic
    @Column(name = "EntityKey", nullable = true, length = 255)
    public String getEntityKey() {
        return entityKey;
    }

    public void setEntityKey(String entityKey) {
        this.entityKey = entityKey;
    }

    @Basic
    @Column(name = "Value", nullable = true, length = 255)
    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        OpenLAPEntity that = (OpenLAPEntity) o;

        if (eId != that.eId) return false;
        if (eventFk != null ? !eventFk.equals(that.eventFk) : that.eventFk != null) return false;
        if (entityKey != null ? !entityKey.equals(that.entityKey) : that.entityKey != null) return false;
        if (value != null ? !value.equals(that.value) : that.value != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = eId;
        result = 31 * result + (eventFk != null ? eventFk.hashCode() : 0);
        result = 31 * result + (entityKey != null ? entityKey.hashCode() : 0);
        result = 31 * result + (value != null ? value.hashCode() : 0);
        return result;
    }
}
