package de.rwthaachen.openlap.analyticsmodules.model;

/**
 * Created by Arham Muslim
 * on 23-Jan-17.
 */
public class OpenLAPDataSetMergeMapping {
    private String indRefKey1;
    private String indRefKey2;
    private String indRefField1;
    private String indRefField2;

    public OpenLAPDataSetMergeMapping(String indRefKey1, String indRefKey2, String indRefField1, String indRefField2) {
        this.indRefKey1 = indRefKey1;
        this.indRefKey2 = indRefKey2;
        this.indRefField1 = indRefField1;
        this.indRefField2 = indRefField2;
    }

    public OpenLAPDataSetMergeMapping() {
    }

    public String getIndRefKey1() {
        return indRefKey1;
    }

    public void setIndRefKey1(String indRefKey1) {
        this.indRefKey1 = indRefKey1;
    }

    public String getIndRefKey2() {
        return indRefKey2;
    }

    public void setIndRefKey2(String indRefKey2) {
        this.indRefKey2 = indRefKey2;
    }

    public String getIndRefField1() {
        return indRefField1;
    }

    public void setIndRefField1(String indRefField1) {
        this.indRefField1 = indRefField1;
    }

    public String getIndRefField2() {
        return indRefField2;
    }

    public void setIndRefField2(String indRefField2) {
        this.indRefField2 = indRefField2;
    }
}
