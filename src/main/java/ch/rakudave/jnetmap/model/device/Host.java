package ch.rakudave.jnetmap.model.device;

import ch.rakudave.jnetmap.model.String;
import ch.rakudave.jnetmap.model.Connection;
import ch.rakudave.jnetmap.model.Layer;
import ch.rakudave.jnetmap.model.IF.LogicalIF;
import ch.rakudave.jnetmap.model.IF.NetworkIF;
import ch.rakudave.jnetmap.net.status.Status;
import ch.rakudave.jnetmap.util.Settings;
import ch.rakudave.jnetmap.util.Tuple;
import ch.rakudave.jnetmap.util.logging.Logger;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamOmitField;

import ch.rakudave.jnetmap.model.SuperInterface;


import java.util.*;
import java.util.stream.Collectors;


/**
 * @author rakudave
 */
@XStreamAlias("Device")
public class Host implements Device {
    private String name = "", vendor = "", model = "", location = "", otherID = "", description = "";
    private Status status;
    private int nrOfPorts = 1;
    private boolean ignore;
    @XStreamOmitField
    private Date lastStatusChange;
    private String type;
    private List<NetworkIF> interfaces;
    private List<DeviceListener> listeners;
    private Map<String, String> metadata;
    private LinkedList<Tuple<Date, Status>> statusHistory;
    private boolean isLayer=false;
    private String layer=null;
    private String belongsTo=null;

    public Host() {
        this(Host.fallbackType);
    }

    public Host(String type) {
        this.type = type;
        interfaces = new ArrayList<>();
        listeners = new ArrayList<>();
        initMetadata();
        status = Status.UNKNOWN;
    }

    @Override
    public void addInterface(NetworkIF i) {
        if (i != null) interfaces.add(i);
    }

    @Override
    public void addDeviceListener(DeviceListener listener) {
        if (listener != null && !listeners.contains(listener)) listeners.add(listener);
    }

    @Override
    public List<NetworkIF> getInterfaces() {
        return Collections.unmodifiableList(interfaces.stream()
                .sorted(Comparator.comparing(NetworkIF::getName))
                .collect(Collectors.toList()));
    }

    @Override
    public Date getLastSeen() {
        return lastStatusChange;
    }

    @Override
    public String getLocation() {
        return location;
    }

    @Override
    public String getModel() {
        return model;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getOtherID() {
        return otherID;
    }

    @Override
    public Status getStatus() {
        return status;
    }

    @Override
    public String getType() {
        return type;
    }

    @Override
    public String getVendor() {
        return vendor;
    }

    @Override
    public String getLayer() {
        return layer;
    }

    @Override
    public void notifyListeners(DeviceEvent e) {
        if (ignore && e.getType() == DeviceEvent.Type.STATUS_CHANGED) return;
        for (DeviceListener l : listeners) {
            try {
                l.deviceChanged(e);
            } catch (Exception ex) {
                Logger.error("Unable to notify DeviceListener", ex);
            }
        }
    }

    @Override
    public void removeInterface(NetworkIF i) {
        interfaces.remove(i);
    }

    @Override
    public void removeDeviceListener(DeviceListener listener) {
        listeners.remove(listener);
    }

    @Override
    public void setLocation(String location) {
        if (this.location.equals(location) || location == null) return;
        this.location = location;
        notifyListeners(new DeviceEvent(this, DeviceEvent.Type.PROPERTY_CHANGED, location));
    }

    @Override
    public void setModel(String modelNr) {
        if (model.equals(modelNr) || modelNr == null) return;
        model = modelNr;
        notifyListeners(new DeviceEvent(this, DeviceEvent.Type.PROPERTY_CHANGED, modelNr));
    }

    @Override
    public void setName(String name) {
        if (this.name.equals(name) || name == null) return;
        this.name = name;
        notifyListeners(new DeviceEvent(this, DeviceEvent.Type.PROPERTY_CHANGED, name));
    }

    @Override
    public void setOtherID(String other) {
        if (otherID.equals(other) || other == null) return;
        if (other != null) otherID = other;
        notifyListeners(new DeviceEvent(this, DeviceEvent.Type.PROPERTY_CHANGED, other));
    }

    @Override
    public void setType(String type) {
        if (type == null || type.endsWith(this.type)) return;
        this.type = type;
        notifyListeners(new DeviceEvent(this, DeviceEvent.Type.PROPERTY_CHANGED, type));
    }

    @Override
    public void setVendor(String vendor) {
        if (this.vendor.equals(vendor) || vendor == null) return;
        this.vendor = vendor;
        notifyListeners(new DeviceEvent(this, DeviceEvent.Type.PROPERTY_CHANGED, vendor));
    }

    @Override
    public void setLayer(String layer) {
        this.layer = layer;
        notifyListeners(new DeviceEvent(this, DeviceEvent.Type.PROPERTY_CHANGED, layer));
    }

    @Override
    public void updateStatus() {
        Status newStatus = Status.UNKNOWN;
        for (NetworkIF i : interfaces) {
            i.updateStatus();
            Status intStatus = i.getStatus();
            if (newStatus.compareTo(intStatus) < 0 && !(i instanceof LogicalIF)) newStatus = intStatus;
        }
        if (!status.equals(newStatus)) {
            Logger.debug(name + " has changes status from " + status + " to " + newStatus);
            status = newStatus;
            Date now = new Date(System.currentTimeMillis());
            lastStatusChange = now;
            putInStatusHistory(now, newStatus);
            notifyListeners(new DeviceEvent(this, DeviceEvent.Type.STATUS_CHANGED, null));
        }
    }

    @Override
    public String toString() {
        return name;
    }

    @Override
    public String toHtmlString() {
        return "<span style=\"color: " + getStatus().getHtmlValue() + "\">" + toString() + "</span>";
    }

    @Override
    public NetworkIF getInterfaceFor(Connection c) {
        if (c == null) return null;
        for (NetworkIF nif : getInterfaces()) {
            if (c == nif.getConnection()) return nif;
        }
        Logger.debug("getInterfaceFor: nothing found");
        return null;
    }

    @Override
    public String getDesctription() {
        return description;
    }

    @Override
    public void setDescription(String description) {
        if (description != null) this.description = description;
    }

    @Override
    public void setMetadata(String id, String data) {
        initMetadata();
        if (metadata.put(id, data) != null) {
            notifyListeners(new DeviceEvent(this, DeviceEvent.Type.PROPERTY_CHANGED, id + "=" + data));
        }
    }

    @Override
    public String getMetadata(String id) {
        initMetadata();
        return metadata.get(id);
    }

    @Override
    public void removeMetadata(String id) {
        initMetadata();
        metadata.remove(id);
    }

    @Override
    public LinkedList<Tuple<Date, Status>> getStatusHistory() {
        if (statusHistory == null) statusHistory = new LinkedList<>();
        return statusHistory;
    }

    private void putInStatusHistory(Date date, Status status) {
        if (statusHistory == null) statusHistory = new LinkedList<>();
        if (status != null && (statusHistory.size() == 0 || !status.equals(statusHistory.getLast().getSecond()))) {
            statusHistory.add(new Tuple<>(date, status));
            if (statusHistory.size() > Settings.getInt("device.history.maxsize", 20)) statusHistory.removeFirst();
        }
    }

    @Override
    public void addStatusUnknownToHistory() {
        putInStatusHistory(new Date(System.currentTimeMillis()), Status.UNKNOWN);
        for (NetworkIF nif : getInterfaces()) nif.addStatusUnknownToHistory();
    }

    private void initMetadata() {
        if (metadata == null) metadata = new HashMap<>();
    }

    @Override
    public boolean isIgnore() {
        return ignore;
    }

    @Override
    public void setIgnore(boolean ignore) {
        this.ignore = ignore;
    }

    @Override
    public boolean isLayer() {
        return isLayer;
    }

    @Override
    public void setisLayer(boolean isLayer) {
        this.isLayer = isLayer;
    }

    @Override
    public int compareTo(Device o) {
        return name.compareTo(o.getName());
    }

    @Override
    public int getNrOfPorts() {
        return Math.max(nrOfPorts, interfaces.size());
    }

    @Override
    public void setNrOfPorts(int nrOfPorts) {
        this.nrOfPorts = nrOfPorts;
    }

    @Override
    public void setBelongsTo(String d) {
        this.belongsTo = d;
    }

    @Override
    public String getBelongsTo() {
        return belongsTo;
    }
}
