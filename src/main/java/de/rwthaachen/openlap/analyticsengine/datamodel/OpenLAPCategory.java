package de.rwthaachen.openlap.analyticsengine.datamodel;

import javax.persistence.*;

/**
 * Created by Arham Muslim
 * on 24-Feb-16.
 */
@Entity
@Table(name = "Category", schema = "dbo", catalog = "LADB")
public class OpenLAPCategory {
    private int cId;
    private String type;
    private String major;
    private String minor;

    @Id
    @Column(name = "C_Id", nullable = false)
    public int getcId() {
        return cId;
    }

    public void setcId(int cId) {
        this.cId = cId;
    }

    @Basic
    @Column(name = "Type", nullable = true, length = 255)
    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    @Basic
    @Column(name = "Major", nullable = true, length = 255)
    public String getMajor() {
        return major;
    }

    public void setMajor(String major) {
        this.major = major;
    }

    @Basic
    @Column(name = "Minor", nullable = true, length = 255)
    public String getMinor() {
        return minor;
    }

    public void setMinor(String minor) {
        this.minor = minor;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        OpenLAPCategory that = (OpenLAPCategory) o;

        if (cId != that.cId) return false;
        if (type != null ? !type.equals(that.type) : that.type != null) return false;
        if (major != null ? !major.equals(that.major) : that.major != null) return false;
        if (minor != null ? !minor.equals(that.minor) : that.minor != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = cId;
        result = 31 * result + (type != null ? type.hashCode() : 0);
        result = 31 * result + (major != null ? major.hashCode() : 0);
        result = 31 * result + (minor != null ? minor.hashCode() : 0);
        return result;
    }
}
