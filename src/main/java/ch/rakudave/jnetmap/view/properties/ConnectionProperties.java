package ch.rakudave.jnetmap.view.properties;

import ch.rakudave.jnetmap.controller.Controller;
import ch.rakudave.jnetmap.controller.command.Command;
import ch.rakudave.jnetmap.model.Connection;
import ch.rakudave.jnetmap.model.IF.NetworkIF;
import ch.rakudave.jnetmap.model.device.Device;
import ch.rakudave.jnetmap.model.device.Host;
import ch.rakudave.jnetmap.util.Icons;
import ch.rakudave.jnetmap.util.Lang;
import ch.rakudave.jnetmap.util.SwingHelper;
import ch.rakudave.jnetmap.util.logging.Logger;
import ch.rakudave.jnetmap.view.components.EscapableDialog;
import ch.rakudave.jnetmap.view.components.TabPanel;
import ch.rakudave.jnetmap.view.jung.EdgeTransformers;
import ch.rakudave.jnetmap.view.preferences.PreferencePanel;
import edu.uci.ics.jung.graph.util.Pair;

import javax.swing.*;

import org.pf4j.util.StringUtils;

import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.geom.Line2D;



@SuppressWarnings("serial")
public class ConnectionProperties extends EscapableDialog {
    private Stroke stroke;

    @SuppressWarnings({"unchecked", "rawtypes"})
    public ConnectionProperties(final Frame owner, final Connection c) {
        super(owner, Lang.getNoHTML("connection.properties"));
        setLayout(new BorderLayout(5, 5));
        setPreferredSize(new Dimension(800, 460));
        setMinimumSize(new Dimension(800, 460));
        Pair<Device> pair = Controller.getCurrentMap().getEndpoints(c);
        String lefLayer = pair.getFirst().getLayer(); // Az egyik layer
        String rightLayer = pair.getSecond().getLayer(); // a másik layer
        NetworkIF nif1 = pair.getFirst().getInterfaceFor(c), nif2 = pair.getSecond().getInterfaceFor(c);
        JPanel deviceNames = new JPanel(new GridLayout(1, 3));
        JLabel leftDevice = new JLabel(pair.getFirst().getName(), (Host.otherType.equals(pair.getFirst().getType())) ?
                Icons.fromBase64(pair.getFirst().getOtherID()) : Icons.getCisco(pair.getFirst().getType().toString().toLowerCase()), JLabel.CENTER);
        leftDevice.setHorizontalTextPosition(JLabel.CENTER);
        leftDevice.setVerticalTextPosition(JLabel.BOTTOM);
        stroke = EdgeTransformers.strokeTransformer().transform(c);
        final JPanel centerLine = new JPanel() {
            @Override
            public void paint(Graphics g) {
                super.paint(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setStroke(stroke);
                g2d.setPaint(c.getStatus().getColor());
                g2d.draw(new Line2D.Double(new Point(0, 32), new Point(super.getWidth(), 32)));
            }
        };
        JLabel rightDevice = new JLabel(pair.getSecond().getName(), (Host.otherType.equals(pair.getSecond().getType())) ?
                Icons.fromBase64(pair.getSecond().getOtherID()) : Icons.getCisco(pair.getSecond().getType().toString().toLowerCase()), JLabel.CENTER);
        rightDevice.setHorizontalTextPosition(JLabel.CENTER);
        rightDevice.setVerticalTextPosition(JLabel.BOTTOM);
        deviceNames.add(leftDevice);
        deviceNames.add(centerLine);
        deviceNames.add(rightDevice);
        JPanel centerWrapper = new JPanel(new GridLayout(1, 3));
        final PreferencePanel leftInterface = InterfaceProperties.getInnerPanel(owner, nif1, false);
        JPanel connectionPanel = new JPanel(new GridLayout(0, 1, 5, 5));
        connectionPanel.setBorder(BorderFactory.createEmptyBorder(0, 50, 0, 50));
        final JComboBox connectionType = new JComboBox();
        final JComboBox protocolType = new JComboBox();
        protocolType.setVisible(false);
        // A layer típusú kapcsolatokat nem kell tudnia kiválasztania a usernek. Így "elrejtve" maradnak.
        for (Connection.Type type : Connection.Type.values()) {
            if (!type.toString().startsWith("Layer")) connectionType.addItem(type);
        }
        connectionType.setSelectedItem(c.getType());
        
        final JTextField bandwidth = new JTextField(String.valueOf(c.getBandwidth()));
        ActionListener updateLine = e -> SwingUtilities.invokeLater(() -> {
            try {
                double aBandwidth = Double.valueOf(bandwidth.getText());
                stroke = EdgeTransformers.getStroke((Connection.Type) connectionType.getSelectedItem(), aBandwidth);
                centerLine.repaint();
            } catch (NumberFormatException e1) {
                Logger.warn(bandwidth.getText() + " is not a number!", e1);
            }
        });
        final JTextField name = new JTextField(c.getName());
        connectionType.addActionListener(updateLine);
        bandwidth.addActionListener(updateLine);

        

        connectionPanel.add(new JLabel(Lang.get("connection.name") + ":"));
        connectionPanel.add(name);
        connectionPanel.add(new JLabel(Lang.get("connection.type") + ":"));
        connectionPanel.add(connectionType);
        connectionPanel.add(protocolType);
        connectionPanel.add(new JLabel(Lang.get("connection.bandwidth") + ":"));
        connectionPanel.add(bandwidth);
        connectionPanel.add(new JLabel(""));
        connectionPanel.add(new JLabel(""));
        connectionPanel.add(new JLabel(""));
        final PreferencePanel rightInterface = InterfaceProperties.getInnerPanel(owner, nif2, false);
        centerWrapper.add(leftInterface);
        centerWrapper.add(connectionPanel);
        centerWrapper.add(rightInterface);
        JPanel bottomRow = new JPanel(new FlowLayout(FlowLayout.TRAILING, 5, 5));
        final JDialog _this = this;
        JButton cancel = new JButton(Lang.get("action.cancel"), Icons.get("cancel"));
        cancel.addActionListener(e -> _this.dispose());
        JButton ok = new JButton(Lang.get("action.ok"), Icons.get("ok"));
        ok.setPreferredSize(cancel.getPreferredSize());
        // Megnézi, hogy mekkora különbég vn a layerek között. Layerx, Layery -> x-y
        int LayerDiff = (lefLayer != null && rightLayer != null) ? Integer.parseInt(lefLayer.substring(5))-Integer.parseInt(rightLayer.substring(5)) : 0;
        
        if (pair.getFirst().isLayer() && pair.getSecond().isLayer()) {
            leftInterface.setEnable(false);
            rightInterface.setEnable(false);
            bandwidth.setEnabled(false);
            if (Math.abs(LayerDiff) != 0 && pair.getFirst().getBelongsTo().equals(pair.getSecond().getBelongsTo())) {
                connectionType.setEnabled(false); // nincs nyúlkapiszka
                connectionType.addItem(Connection.Type.Layer_Vertical); // mivel alapból "rejtve van" hozzáadjuk
                connectionType.setSelectedItem(Connection.Type.Layer_Vertical); // és ki is választjuk
                
            }
            else if (pair.getFirst().getLayer().equals(pair.getSecond().getLayer())) {
                connectionType.setEnabled(false);
                //connectionType.setVisible(false);
                connectionType.addItem(Connection.Type.valueOf(pair.getFirst().getLayer()));
                connectionType.setSelectedItem(Connection.Type.valueOf(pair.getFirst().getLayer()));
                protocolType.setVisible(true); // látszódjék
                protocolType.setModel(new DefaultComboBoxModel(Connection.Protocoll.Layer1_protocoll.values())); // beletöltjük a megfelelő dolokat
                // A betöltéshez majd if... wagy swithc-case lesz ami a megfelelő layerhez tartozó protokolokat tölt be
                if (c.getProtocoll() == null) { // ha most hozzuk létre, ne legyen üres
                    protocolType.setSelectedIndex(1);
                }
                else { // amúgy meg a szokásos
                    protocolType.setSelectedItem(c.getProtocoll());
                }
                ActionListener updateName = e -> SwingUtilities.invokeLater(() -> { // elnevezés miatt
                    name.setText(connectionType.getSelectedItem().toString() + " " + protocolType.getSelectedItem().toString());

                });
                protocolType.addActionListener(updateName);

            }
            
        }
        

        
        
        ok.addActionListener(e -> {
            // Két device VAGY két layer:
            if (!pair.getFirst().isLayer() && !pair.getSecond().isLayer() ||
                pair.getFirst().isLayer() && pair.getSecond().isLayer()) {
            
            leftInterface.save(); // TODO integrate this in one step
            rightInterface.save(); // TODO integrate this in one step
            //TODO skip new command if nothing has changed
            Controller.getCurrentMap().getHistory().execute(new Command() {
                Connection.Type oldType = c.getType(), newType = (Connection.Type) connectionType.getSelectedItem();
                double oldBandwidth = c.getBandwidth(), newBandwidth;
                String oldName = c.getName(), newName = name.getText();
                Connection.Protocoll oldProtocol = c.getProtocoll(), newProtocol = (Connection.Protocoll) protocolType.getSelectedItem();

                

                @Override
                public Object undo() {
                    c.setType(oldType);
                    c.setBandwidth(oldBandwidth);
                    c.setName(oldName);
                    c.setProtocoll(oldProtocol);
                    return null;
                }

                @Override
                public Object redo() {
                    try {
                        newBandwidth = Double.valueOf(bandwidth.getText());
                    } catch (NumberFormatException e) {
                        Logger.warn(bandwidth.getText() + " is not a number!", e);
                    }
                    c.setType(newType);
                    c.setBandwidth(newBandwidth);
                    c.setName(newName);
                    c.setProtocoll(newProtocol);
                    return null;
                }

                @Override
                public String toString() {
                    return Lang.getNoHTML("command.update.connection")+": "+pair.getFirst().getName()+" ↔ "+pair.getSecond().getName();
                }
            });

            _this.dispose();
            if (TabPanel.getCurrentTab() != null) TabPanel.getCurrentTab().repaint();
        } // nagy if vége

        _this.dispose();
        });
    
        bottomRow.add(cancel);
        bottomRow.add(ok);
        add(deviceNames, BorderLayout.NORTH);
        add(centerWrapper, BorderLayout.CENTER);
        add(bottomRow, BorderLayout.SOUTH);
        pack();
        SwingHelper.centerTo(owner, this);
        bandwidth.requestFocus();
        setVisible(true);
    }
}
