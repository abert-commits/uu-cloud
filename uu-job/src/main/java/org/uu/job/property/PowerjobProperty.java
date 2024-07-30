package org.uu.job.property;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * @author Admin
 */
@Configuration
@ConfigurationProperties(prefix = "powerjob")
public class PowerjobProperty {

    public String getHosts() {
        return hosts;
    }

    public void setHosts(String hosts) {
        this.hosts = hosts;
    }

    private String hosts;

}
