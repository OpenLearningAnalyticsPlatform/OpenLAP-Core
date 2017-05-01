package de.rwthaachen.openlap.analyticsengine.datamodel;

import javax.persistence.*;
import java.util.Collection;

/**
 * Created by Arham Muslim
 * on 21-Sep-16.
 */
@Entity
@Table(name = "Users", schema = "dbo", catalog = "LADB")
public class OpenLAPUsers {
    private int uId;
    private String name;
    private Collection<OpenLAPEvent> eventsByUId;

    @Id
    @Column(name = "U_Id", nullable = false)
    public int getuId() {
        return uId;
    }

    public void setuId(int uId) {
        this.uId = uId;
    }

    @Basic
    @Column(name = "Name", nullable = true, length = 255)
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        OpenLAPUsers that = (OpenLAPUsers) o;

        if (uId != that.uId) return false;
        if (name != null ? !name.equals(that.name) : that.name != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = uId;
        result = 31 * result + (name != null ? name.hashCode() : 0);
        return result;
    }

    @OneToMany(mappedBy = "usersByUId")
    public Collection<OpenLAPEvent> getEventsByUId() {
        return eventsByUId;
    }

    public void setEventsByUId(Collection<OpenLAPEvent> eventsByUId) {
        this.eventsByUId = eventsByUId;
    }
}
