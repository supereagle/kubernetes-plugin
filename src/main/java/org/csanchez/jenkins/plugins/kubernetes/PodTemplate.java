package org.csanchez.jenkins.plugins.kubernetes;

import hudson.Extension;
import hudson.model.AbstractDescribableImpl;
import hudson.model.Descriptor;
import hudson.model.Label;
import hudson.model.labels.LabelAtom;

import org.apache.commons.lang.StringUtils;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;

import com.google.common.base.Preconditions;

import java.util.List;
import java.util.Set;

/**
 * @author <a href="mailto:nicolas.deloof@gmail.com">Nicolas De Loof</a>
 */
public class PodTemplate extends AbstractDescribableImpl<PodTemplate> {

    private String name;

    private final String image;

    private boolean privileged;

    private String command;

    private String args;

    private String remoteFs;

    private int instanceCap;

    private String label;

    private String volumesString;

    private int poolSize;

    private String cpuLimit;

    private String memoryLimit;

    private String cpuRequest;

    private String memoryRequest;

    @DataBoundConstructor
    public PodTemplate(String image) {
        Preconditions.checkArgument(!StringUtils.isBlank(image));
        this.image = image;
    }

    @DataBoundSetter
    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public String getImage() {
        return image;
    }

    @DataBoundSetter
    public void setCommand(String command) {
        this.command = command;
    }

    public String getCommand() {
        return command;
    }

    @DataBoundSetter
    public void setArgs(String args) {
        this.args = args;
    }

    public String getArgs() {
        return args;
    }

    public String getDisplayName() {
        return "Kubernetes Pod Template";
    }

    @DataBoundSetter
    public void setRemoteFs(String remoteFs) {
        this.remoteFs = StringUtils.isBlank(remoteFs) ? "/home/jenkins" : remoteFs;
    }

    public String getRemoteFs() {
        return remoteFs;
    }

    public void setInstanceCap(int instanceCap) {
        this.instanceCap = instanceCap;
    }

    public int getInstanceCap() {
        return instanceCap;
    }

    @DataBoundSetter
    public void setInstanceCapStr(String instanceCapStr) {
        if ("".equals(instanceCapStr)) {
            setInstanceCap(Integer.MAX_VALUE);
        } else {
            setInstanceCap(Integer.parseInt(instanceCapStr));
        }
    }

    public String getInstanceCapStr() {
        if (getInstanceCap() == Integer.MAX_VALUE) {
            return "";
        } else {
            return String.valueOf(instanceCap);
        }
    }

    public Set<LabelAtom> getLabelSet() {
        return Label.parse(label);
    }

    @DataBoundSetter
    public void setLabel(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    @DataBoundSetter
    public void setVolumesString(String volumesString) {
        this.volumesString = volumesString;
    }

    public String getVolumesString() {
        return volumesString;
    }

    @DataBoundSetter
    public void setPoolSize(int poolSize) {
        this.poolSize = poolSize;
    }

    public int getPoolSize() {
        return poolSize;
    }

    @DataBoundSetter
    public void setCpuRequest(String cpuRequest) {
        this.cpuRequest = cpuRequest;
    }

    public String getCpuRequest() {
        return cpuRequest;
    }

    @DataBoundSetter
    public void setMemoryRequest(String memoryRequest) {
        this.memoryRequest = memoryRequest;
    }

    public String getMemoryRequest() {
        return memoryRequest;
    }

    @DataBoundSetter
    public void setCpuLimit(String cpuLimit) {
        this.cpuLimit = cpuLimit;
    }

    public String getCpuLimit() {
        return cpuLimit;
    }

    @DataBoundSetter
    public void setMemoryLimit(String memoryLimit) {
        this.memoryLimit = memoryLimit;
    }

    public String getMemoryLimit() {
        return memoryLimit;
    }

    @DataBoundSetter
    public void setPrivileged(boolean privileged) {
        this.privileged = privileged;
    }

    public boolean isPrivileged() {
        return privileged;
    }

    @Extension
    public static class DescriptorImpl extends Descriptor<PodTemplate> {

        @Override
        public String getDisplayName() {
            return "Kubernetes Pod Template";
        }
    }
}
