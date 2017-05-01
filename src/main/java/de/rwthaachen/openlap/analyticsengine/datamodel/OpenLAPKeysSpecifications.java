package de.rwthaachen.openlap.analyticsengine.datamodel;

import javax.persistence.*;
import java.util.Collection;

/**
 * Created by Arham Muslim
 * on 21-Sep-16.
 */
@Entity
@Table(name = "Keys_Specifications", schema = "dbo", catalog = "LADB")
public class OpenLAPKeysSpecifications {
    private long id;
    private String key;
    private String description;
    private String title;
    private String type;
    private Collection<OpenLAPCategoryKeysMapping> categoryKeysMappingsById;

    @Id
    @Column(name = "ID", nullable = false)
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    @Basic
    @Column(name = "Key", nullable = true, length = 50)
    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    @Basic
    @Column(name = "Description", nullable = true, length = 255)
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Basic
    @Column(name = "Title", nullable = true, length = 50)
    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    @Basic
    @Column(name = "Type", nullable = true, length = 50)
    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        OpenLAPKeysSpecifications that = (OpenLAPKeysSpecifications) o;

        if (id != that.id) return false;
        if (key != null ? !key.equals(that.key) : that.key != null) return false;
        if (description != null ? !description.equals(that.description) : that.description != null) return false;
        if (title != null ? !title.equals(that.title) : that.title != null) return false;
        if (type != null ? !type.equals(that.type) : that.type != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = (int) (id ^ (id >>> 32));
        result = 31 * result + (key != null ? key.hashCode() : 0);
        result = 31 * result + (description != null ? description.hashCode() : 0);
        result = 31 * result + (title != null ? title.hashCode() : 0);
        result = 31 * result + (type != null ? type.hashCode() : 0);
        return result;
    }

    @OneToMany(mappedBy = "keysSpecificationsByKId")
    public Collection<OpenLAPCategoryKeysMapping> getCategoryKeysMappingsById() {
        return categoryKeysMappingsById;
    }

    public void setCategoryKeysMappingsById(Collection<OpenLAPCategoryKeysMapping> categoryKeysMappingsById) {
        this.categoryKeysMappingsById = categoryKeysMappingsById;
    }
}
