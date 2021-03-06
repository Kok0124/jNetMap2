package ch.rakudave.jnetmap.view.jung;

import ch.rakudave.jnetmap.model.IF.NetworkIF;
import ch.rakudave.jnetmap.model.IF.PhysicalIF;
import ch.rakudave.jnetmap.model.device.Device;
import ch.rakudave.jnetmap.util.Settings;
import org.apache.commons.collections15.Transformer;

public class VertexLabeller implements Transformer<Device, String> {

    @Override
    public String transform(Device device) {
        StringBuffer sb = new StringBuffer();
        sb.append("<html><center>");
        if (device.isIgnore()) sb.append("<i>");
        if (Settings.getBoolean("device.label.name", true)) appendLine(sb, device.getName());
        if (Settings.getBoolean("device.label.description", false)) appendLine(sb, device.getDesctription());
        if (Settings.getBoolean("device.label.location", false)) appendLine(sb, device.getLocation());
        if (Settings.getBoolean("device.label.vendor", false)) appendLine(sb, device.getVendor());
        if (Settings.getBoolean("device.label.model", false)) appendLine(sb, device.getModel());
        if (Settings.getBoolean("device.label.ip", false)) {
            for (NetworkIF nif : device.getInterfaces()) {
                if (nif instanceof PhysicalIF && nif.getAddress() != null) {
                    appendLine(sb, nif.getAddress().getHostAddress());
                }
            }
        }
        if (Settings.getBoolean("device.label.mac", false)) {
            for (NetworkIF nif : device.getInterfaces()) {
                if (nif instanceof PhysicalIF) {
                    appendLine(sb, ((PhysicalIF) nif).getMacAddress());
                }
            }
        }
        if (device.isIgnore()) sb.append("</i>");
        sb.append("</center></html>");
        return sb.toString();
    }
    

    private void appendLine(StringBuffer sb, String input) {
        if (input != null && !"".equals(input)) {
            sb.append(input).append("<br>");
        }
    }

}
