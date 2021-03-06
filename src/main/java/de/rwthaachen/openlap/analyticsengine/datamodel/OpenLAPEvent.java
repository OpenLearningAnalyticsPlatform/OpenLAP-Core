package de.rwthaachen.openlap.analyticsengine.datamodel;

import javax.persistence.*;
import java.util.Collection;

/**
 * Created by Arham Muslim
 * on 21-Sep-16.
 */
@Entity
@Table(name = "Event", schema = "dbo", catalog = "LADB")
public class OpenLAPEvent {
    private int eventId;
    private Integer timestamp;
    private String session;
    private String action;
    private String platform;
    private String source;
    private Collection<OpenLAPEntity> entitiesByEventId;
    private OpenLAPUsers usersByUId;
    private OpenLAPCategory categoryByCId;

    @Id
    @Column(name = "Event_Id", nullable = false)
    public int getEventId() {
        return eventId;
    }

    public void setEventId(int eventId) {
        this.eventId = eventId;
    }

    @Basic
    @Column(name = "Timestamp", nullable = true)
    public Integer getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Integer timestamp) {
        this.timestamp = timestamp;
    }

    @Basic
    @Column(name = "Session", nullable = true, length = 50)
    public String getSession() {
        return session;
    }

    public void setSession(String session) {
        this.session = session;
    }

    @Basic
    @Column(name = "Action", nullable = true, length = 255)
    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    @Basic
    @Column(name = "Platform", nullable = true, length = 255)
    public String getPlatform() {
        return platform;
    }

    public void setPlatform(String platform) {
        this.platform = platform;
    }

    @Basic
    @Column(name = "Source", nullable = true, length = 255)
    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        OpenLAPEvent that = (OpenLAPEvent) o;

        if (eventId != that.eventId) return false;
        if (timestamp != null ? !timestamp.equals(that.timestamp) : that.timestamp != null) return false;
        if (session != null ? !session.equals(that.session) : that.session != null) return false;
        if (action != null ? !action.equals(that.action) : that.action != null) return false;
        if (platform != null ? !platform.equals(that.platform) : that.platform != null) return false;
        if (source != null ? !source.equals(that.source) : that.source != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = eventId;
        result = 31 * result + (timestamp != null ? timestamp.hashCode() : 0);
        result = 31 * result + (session != null ? session.hashCode() : 0);
        result = 31 * result + (action != null ? action.hashCode() : 0);
        result = 31 * result + (platform != null ? platform.hashCode() : 0);
        result = 31 * result + (source != null ? source.hashCode() : 0);
        return result;
    }

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "eventByEventFk")
    public Collection<OpenLAPEntity> getEntitiesByEventId() {
        return entitiesByEventId;
    }

    public void setEntitiesByEventId(Collection<OpenLAPEntity> entitiesByEventId) {
        this.entitiesByEventId = entitiesByEventId;
    }

    @ManyToOne
    @JoinColumn(name = "U_Id", referencedColumnName = "U_Id")
    public OpenLAPUsers getUsersByUId() {
        return usersByUId;
    }

    public void setUsersByUId(OpenLAPUsers usersByUId) {
        this.usersByUId = usersByUId;
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "C_ID", referencedColumnName = "C_Id")
    public OpenLAPCategory getCategoryByCId() {
        return categoryByCId;
    }

    public void setCategoryByCId(OpenLAPCategory categoryByCId) {
        this.categoryByCId = categoryByCId;
    }
}
