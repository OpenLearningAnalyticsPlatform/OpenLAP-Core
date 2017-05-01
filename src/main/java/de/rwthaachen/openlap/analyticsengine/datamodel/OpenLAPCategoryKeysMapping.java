package de.rwthaachen.openlap.analyticsengine.datamodel;

import javax.persistence.*;

/**
 * Created by Arham Muslim
 * on 21-Sep-16.
 */
@Entity
@Table(name = "Category_Keys_Mapping", schema = "dbo", catalog = "LADB")
public class OpenLAPCategoryKeysMapping {
    private long id;
    private OpenLAPCategory categoryByCId;
    private OpenLAPKeysSpecifications keysSpecificationsByKId;

    @Id
    @Column(name = "ID", nullable = false)
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        OpenLAPCategoryKeysMapping that = (OpenLAPCategoryKeysMapping) o;

        if (id != that.id) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return (int) (id ^ (id >>> 32));
    }

    @ManyToOne
    @JoinColumn(name = "C_ID", referencedColumnName = "C_Id")
    public OpenLAPCategory getCategoryByCId() {
        return categoryByCId;
    }

    public void setCategoryByCId(OpenLAPCategory categoryByCId) {
        this.categoryByCId = categoryByCId;
    }

    @ManyToOne
    @JoinColumn(name = "K_ID", referencedColumnName = "ID")
    public OpenLAPKeysSpecifications getKeysSpecificationsByKId() {
        return keysSpecificationsByKId;
    }

    public void setKeysSpecificationsByKId(OpenLAPKeysSpecifications keysSpecificationsByKId) {
        this.keysSpecificationsByKId = keysSpecificationsByKId;
    }
}
