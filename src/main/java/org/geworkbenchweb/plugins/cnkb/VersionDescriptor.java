package org.geworkbenchweb.plugins.cnkb;

/**
 * Created by Min You  
 */

/**
 * The Version Descriptor of one Dataset.
 */
public class VersionDescriptor {
    private final String version;    
    private final boolean requiresAuthentication;
    private final String versionDesc;
    

    public VersionDescriptor(final String version, final boolean requiresAuthentication, final String versionDesc) {
        this.version = version;
        this.requiresAuthentication = requiresAuthentication;
        this.versionDesc = versionDesc;
    }

    public String getVersion() {
        return version;
    }

    public String getVersionDesc() {
        return versionDesc;
    }

    public boolean getRequiresAuthentication() {
        return requiresAuthentication;
    }
   
}
