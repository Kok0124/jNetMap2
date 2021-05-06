package ch.rakudave.jnetmap.model;

import ch.rakudave.jnetmap.model.device.Device;
import com.thoughtworks.xstream.annotations.XStreamOmitField;

import javax.swing.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.LinkedHashSet;
import java.util.Set;

public class Layer {
    private String name;
    private boolean isVisible;
    private Set<Device> devices;
    @XStreamOmitField
    private static String[] LayerTypes = {
        "Layer1", "Layer2", "Layer3", "Layer4", "Layer5", "Layer6", "Layer7"
    };
    private static String fallbackLayer="Layer1";

    public static String[] getLayerTypes() {
        return LayerTypes;
    }

   /*  public static String getFallbackLayer() {
        return fallbackLayer;
    } */

    public Layer() {
        devices = new LinkedHashSet<>();
        isVisible = true;
        
    }

    public Layer(String name) {
        this();
        setName(name);
    }

    

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isVisible() {
        return isVisible;
    }

    public void setVisible(boolean isVisible) {
        this.isVisible = isVisible;
    }

    public Set<Device> getDevices() {
        return devices;
    }

    public boolean containsDevice(Device device) {
        return devices.contains(device);
    }

    public void addDevice(Device device) {
        devices.add(device);
    }

    public void removeDevice(Device device) {
        devices.remove(device);
    }

    public void addDevices(Set<Device> devices) {
        this.devices.addAll(devices);
    }

}