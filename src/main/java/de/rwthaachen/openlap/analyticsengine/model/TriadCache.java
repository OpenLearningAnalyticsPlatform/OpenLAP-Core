package de.rwthaachen.openlap.analyticsengine.model;

import javax.persistence.*;

/**
 * Created by Arham Muslim
 * on 03-Nov-16.
 */
@Entity
@Table(name = "TriadCache")
public class TriadCache {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "cache_id")
    private long id;

    @Column(name = "triad_id")
    private long triadId;

    @Column(name = "user_hash")
    private String userHash;

    @Column(name = "code", columnDefinition = "TEXT")
    private String code;

    public TriadCache() {
        this.triadId = 0;
        this.code = "";
    }

    public TriadCache(long triadId, String code) {
        this.triadId = triadId;
        this.userHash = "";
        this.code = code;
    }

    public TriadCache(long triadId, String userHash, String code) {
        this.triadId = triadId;
        this.userHash = userHash;
        this.code = code;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getTriadId() {
        return triadId;
    }

    public void setTriadId(long triadId) {
        this.triadId = triadId;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getUserHash() {
        return userHash;
    }

    public void setUserHash(String userHash) {
        this.userHash = userHash;
    }
}
