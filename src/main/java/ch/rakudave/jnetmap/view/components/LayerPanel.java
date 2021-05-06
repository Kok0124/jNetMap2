package ch.rakudave.jnetmap.view.components;

import ch.rakudave.jnetmap.controller.Controller;

import ch.rakudave.jnetmap.model.Map;

import ch.rakudave.jnetmap.model.device.Device;

import edu.uci.ics.jung.algorithms.layout.Layout;
import edu.uci.ics.jung.graph.Graph;

import edu.uci.ics.jung.visualization.VisualizationViewer;




import javax.swing.*;

import java.awt.*;
import java.awt.event.ActionEvent;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import java.awt.event.ActionListener;
import java.io.Serializable;

import ch.rakudave.jnetmap.model.Connection;

import java.util.ArrayList;
import edu.uci.ics.jung.graph.util.Pair;





@SuppressWarnings("serial")
public class LayerPanel extends JPanel implements ActionListener {

    protected JCheckBox Layer1, Layer2, Layer3, Layer4, Layer5, Layer6, Layer7;
    protected static Graph<Device, Connection> subGraph = new Map();
    protected static Graph<Device, Connection> verticalGraph = new Map();
    



    public LayerPanel(final Frame owner) {

        setLayout(new BorderLayout());
        JPanel panel = new JPanel(new GridBagLayout());

        Layer1 = new JCheckBox("Layer1", true);
        Layer2 = new JCheckBox("Layer2", true);
        Layer3 = new JCheckBox("Layer3", true);
        Layer4 = new JCheckBox("Layer4", true);
        Layer5 = new JCheckBox("Layer5", true);
        Layer6 = new JCheckBox("Layer6", true);
        Layer7 = new JCheckBox("Layer7", true);

        Layer1.addActionListener(this);
        Layer2.addActionListener(this);
        Layer3.addActionListener(this);
        Layer4.addActionListener(this);
        Layer5.addActionListener(this);
        Layer6.addActionListener(this);
        Layer7.addActionListener(this);

        


        GridBagConstraints c = new GridBagConstraints();

        c.weightx = 1;
        c.weighty = 1;
        c.anchor=GridBagConstraints.WEST;

        c.gridx = 0;//set the x location of the grid for the next component
        c.gridy = 0;//set the y location of the grid for the next component
        panel.add(Layer1,c);

        c.gridy = 1;//change the y location
        panel.add(Layer2,c);

        c.gridy = 2;//change the y location
        panel.add(Layer3,c);

        c.gridy = 3;//change the y location
        panel.add(Layer4,c);

        c.gridy = 4;//change the y location
        panel.add(Layer5,c);

        c.gridy = 5;//change the y location
        panel.add(Layer6,c);

        c.gridy =6;//change the y location
        panel.add(Layer7,c);

        add(panel);


        
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        
            Layout<Device, Connection> layout = Controller.getCurrentMap().getGraphLayout();
            VisualizationViewer vv = new VisualizationViewer<>(layout);
        // új gráf, érintett verticle és connection bele, eredtiből töröl
        //vissza: simán hozzáadjuk az eredetihez, nullázuk

        if (e.getSource() == Layer1) {
            if (Layer1.isSelected()) { // Mikor "bepipálom"
                add("Layer1");
            }
            else { // Mikor kiveszem a pipát
                remove("Layer1");
            }
            vv.repaint();
        }
        else if (e.getSource() == Layer2) {
            if (Layer2.isSelected()) { // Mikor "bepipálom"
                add("Layer2");
            }
            else { // Mikor kiveszem a pipát
                remove("Layer2");
            }
            vv.repaint();
        }
        else if (e.getSource() == Layer3) {
            if (Layer3.isSelected()) { // Mikor "bepipálom"
                add("Layer3");
            }
            else { // Mikor kiveszem a pipát
                remove("Layer3");
            }
            vv.repaint();
        }
        else if (e.getSource() == Layer4) {
            if (Layer4.isSelected()) { // Mikor "bepipálom"
                add("Layer4");
            }
            else { // Mikor kiveszem a pipát
                remove("Layer4");
            }
            vv.repaint();
        }
        else if (e.getSource() == Layer5) {
            if (Layer5.isSelected()) { // Mikor "bepipálom"
                add("Layer5");
            }
            else { // Mikor kiveszem a pipát
                remove("Layer5");
            }
            vv.repaint();
        }
        else if (e.getSource() == Layer6) {
            if (Layer6.isSelected()) { // Mikor "bepipálom"
                add("Layer6");
            }
            else { // Mikor kiveszem a pipát
                remove("Layer6");
            }
            vv.repaint();
        }
        else if (e.getSource() == Layer7) {
            if (Layer7.isSelected()) { // Mikor "bepipálom"
                add("Layer7");
            }
            else { // Mikor kiveszem a pipát
                remove("Layer7");  
            }
            vv.repaint();
        }
    }

    private void add(String lyr) {
        Graph<Device, Connection> graph = Controller.getCurrentMap();

        Iterable<Device> toRecover = new ArrayList<>(subGraph.getVertices());
        Iterable<Connection> toRecoverConn = new ArrayList<>(subGraph.getEdges());

        for (Connection c : toRecoverConn) {
            if (c.getType().toString().equals(lyr)) {
                graph.addVertex(subGraph.getEndpoints(c).getFirst());
                graph.addVertex(subGraph.getEndpoints(c).getSecond());
                graph.addEdge(c, subGraph.getEndpoints(c));
            }
        }
        for (Device d : toRecover) {
            if(d.isLayer() && d.getLayer().equals(lyr)) {
                graph.addVertex(d);
                subGraph.removeVertex(d);
            }
            
        }

        addVertical();
    }

    private void remove(String lyr) {
        Graph<Device, Connection> graph = Controller.getCurrentMap();

        Iterable<Device> toRemove = new ArrayList<>(graph.getVertices());
        Iterable<Connection> toRemoveConn = new ArrayList<>(graph.getEdges());

        for (Connection c : toRemoveConn) {
            if (c.getType().toString().equals(lyr)) {
                subGraph.addVertex(graph.getEndpoints(c).getFirst());
                subGraph.addVertex(graph.getEndpoints(c).getSecond());
                subGraph.addEdge(c, graph.getEndpoints(c));
            }
        }
        
        for (Device d : toRemove) {
            if (d.isLayer() && d.getLayer().equals(lyr)) {
                for (Connection c : graph.getEdges()) {
                    if (c.getType().toString().equals("Layer_Vertical")) {
                        if (graph.getEndpoints(c).getFirst().equals(d) || graph.getEndpoints(c).getSecond().equals(d)) {
                            verticalGraph.addVertex(graph.getEndpoints(c).getFirst());
                            verticalGraph.addVertex(graph.getEndpoints(c).getSecond());
                            verticalGraph.addEdge(c, graph.getEndpoints(c).getFirst(), graph.getEndpoints(c).getSecond());
                        }
                    }
                }
                graph.removeVertex(d);
                subGraph.addVertex(d);
            }
        }
    }

    private void addVertical() {
        Graph<Device, Connection> graph = Controller.getCurrentMap();

        for(Connection c : verticalGraph.getEdges()) {
            Pair<Device> pair = verticalGraph.getEndpoints(c);
            if (graph.containsVertex(pair.getFirst()) && graph.containsVertex(pair.getSecond())) {
                graph.addEdge(c, verticalGraph.getEndpoints(c));
                if(graph.degree(pair.getFirst()) == 0) verticalGraph.removeVertex(pair.getFirst());
                if(graph.degree(pair.getSecond()) == 0) verticalGraph.removeVertex(pair.getSecond());
            }
        }
    }
}