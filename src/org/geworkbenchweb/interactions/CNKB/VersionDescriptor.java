package org.geworkbenchweb.interactions.CNKB;

/**
 * Created by Min You  
 */

/**
 * The  Version Descriptorof one Dataset.
 */
public class VersionDescriptor {
    private String version;    
    private Boolean requiresAuthentication;
    private String versionDesc;
    

    public VersionDescriptor(String version, boolean requiresAuthentication, String versionDesc) {
        this.version = version;
        this.requiresAuthentication = requiresAuthentication;
        this.versionDesc = versionDesc;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getVersionDesc() {
        return versionDesc;
    }

    public void setVersionDesc(String versionDesc) {
        this.versionDesc = versionDesc;
    }


    public void setRequiresAuthentication(Boolean requiresAuthentication) {
        this.requiresAuthentication = requiresAuthentication;
    }

    public Boolean  getRequiresAuthentication() {
        return requiresAuthentication;
    }

   
}
