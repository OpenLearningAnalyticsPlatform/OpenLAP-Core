package de.rwthaachen.openlap.analyticsmodules.model;

/**
 * This class represents a reference to a particular Indicator of the Indicator Engine macro component of the
 * OpenLAP.It is modeled after the corresponding Class on the Indicator Engine and holds metadata necessary to reference
 * on a Triad
 */
public class IndicatorReference {
    long id;
    String shortName;
    String indicatorName;

    /**
     * Empty constructor
     */
    public IndicatorReference() {
    }

    /**
     * Standard Constructor
     *
     * @param id            ID of the Indicator
     * @param shortName     Short Name of the Indicator
     * @param indicatorName Name of the Indicator
     */
    public IndicatorReference(long id, String shortName, String indicatorName) {
        this.id = id;
        this.shortName = shortName;
        this.indicatorName = indicatorName;
    }

    /**
     * @return ID of the IndicatorReference
     */
    public long getId() {
        return id;
    }

    /**
     * @param id ID to be set.
     */
    public void setId(long id) {
        this.id = id;
    }

    /**
     * @return Short Name of the IndicatorReference
     */
    public String getShortName() {
        return shortName;
    }

    /**
     * @param shortName Short Name to be Set
     */
    public void setShortName(String shortName) {
        this.shortName = shortName;
    }

}
