package ch.rakudave.jnetmap.view.properties;

import ch.rakudave.jnetmap.controller.Controller;
import ch.rakudave.jnetmap.controller.command.Command;
import ch.rakudave.jnetmap.model.IF.NetworkIF;
import ch.rakudave.jnetmap.model.Layer;
import ch.rakudave.jnetmap.util.Icons;
import ch.rakudave.jnetmap.util.Lang;
import ch.rakudave.jnetmap.util.SwingHelper;
import ch.rakudave.jnetmap.util.logging.Logger;
import ch.rakudave.jnetmap.view.components.EscapableDialog;
import ch.rakudave.jnetmap.view.components.TabPanel;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;

//Ez itt még komoly átdolgozásra szorul

@SuppressWarnings("serial")
public class LayerProperties extends EscapableDialog {
    private Layer l;

    @SuppressWarnings({"unchecked", "rawtypes"})
    public LayerProperties(final Frame owner, Layer layer, final boolean isNew) {
        super(owner, Lang.getNoHTML("layer.properties"));
        l = layer;
        setLayout(new BorderLayout(5, 5));
        setPreferredSize(new Dimension(300, 440));
        setMinimumSize(new Dimension(230, 400));
        final String oldType = (l.getType() != null) ? l.getType() : l.fallbackType; 
        
        
        JPanel centerWrapper = new JPanel(new BorderLayout());
        centerWrapper.setBorder(BorderFactory.createEmptyBorder(2, 5, 2, 5));

        JPanel propPanel = new JPanel(new GridLayout(0, 2, 5, 5)); // az utolsó két számot kell módosítani ha tobb/kevess funkció lesz
        
        final JComboBox typeCombo = new JComboBox(l.defaultTypes);
        typeCombo.setSelectedItem(oldType);

        final JTextField layerName = new JTextField(l.getName());
        final JTextField layerDesc = new JTextField(l.getDescription());

        propPanel.add(new JLabel(Lang.get("layer.type")));
        propPanel.add(typeCombo);
        propPanel.add(new JLabel(Lang.get("layer.name")));
        propPanel.add(layerName);
        propPanel.add(new JLabel(Lang.get("layer.description")));
        propPanel.add(layerDesc);

        centerWrapper.add(propPanel, BorderLayout.NORTH);


        JPanel bottomRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 5));
        final JDialog _this = this;
        JButton cancel = new JButton(Lang.get("action.cancel"), Icons.get("cancel"));
        cancel.addActionListener(e -> _this.dispose());
        JButton ok = new JButton(Lang.get("action.ok"), Icons.get("ok"));
        ok.setPreferredSize(cancel.getPreferredSize());
        ok.addActionListener(e -> {
            final String newType = (String) typeCombo.getSelectedItem();
            final String oldName = l.getName(), newName = layerName.getText(),
                oldDesc = l.getDescription(), newDesc = layerDesc.getText();
            
            /* Controller.getCurrentMap().getHistory().execute(new Command(){
                @Override
                public Object undo() {
                    //ehhez settereket meg kell írni
                    return null;
                }

                @Override
                public Object redo() {
                    //majd az előzővel megcsinálom
                    return null;
                }
            }); */
            _this.dispose();
            if (TabPanel.getCurrentTab() != null) TabPanel.getCurrentTab().repaint();
        });
        bottomRow.add(cancel);
        bottomRow.add(ok);
        add(centerWrapper, BorderLayout.CENTER);
        add(bottomRow, BorderLayout.SOUTH);
        pack();
        SwingHelper.centerTo(owner, this);
        layerName.requestFocus();
        setVisible(true);
    }
}
