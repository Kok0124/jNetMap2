package ch.rakudave.jnetmap.view.jung;

import ch.rakudave.jnetmap.controller.Controller;
import ch.rakudave.jnetmap.controller.Scheduler;
import ch.rakudave.jnetmap.model.Connection;
import ch.rakudave.jnetmap.model.device.Device;
import ch.rakudave.jnetmap.model.factories.ConnectionFactory;
import ch.rakudave.jnetmap.plugins.extensions.RightClickAction;
import ch.rakudave.jnetmap.util.Icons;
import ch.rakudave.jnetmap.util.Lang;
import ch.rakudave.jnetmap.util.logging.Logger;
import ch.rakudave.jnetmap.view.components.StatusBar;
import ch.rakudave.jnetmap.view.components.TabPanel;
import ch.rakudave.jnetmap.view.preferences.ScriptsPanel;
import ch.rakudave.jnetmap.view.properties.ConnectionProperties;
import ch.rakudave.jnetmap.view.properties.DeviceProperties;
import ch.rakudave.jnetmap.view.properties.InterfaceProperties;
import edu.uci.ics.jung.algorithms.layout.AbstractLayout;
import edu.uci.ics.jung.algorithms.layout.GraphElementAccessor;
import edu.uci.ics.jung.algorithms.layout.Layout;
import edu.uci.ics.jung.algorithms.layout.StaticLayout;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.util.EdgeType;
import edu.uci.ics.jung.graph.util.Pair;
import edu.uci.ics.jung.visualization.VisualizationViewer;
import edu.uci.ics.jung.visualization.control.AbstractPopupGraphMousePlugin;
import edu.uci.ics.jung.visualization.picking.PickedState;
import org.apache.commons.collections15.Factory;

import ch.rakudave.jnetmap.model.Layer;
import ch.rakudave.jnetmap.model.Connection.Type;
import ch.rakudave.jnetmap.view.properties.LayerProperties;

import edu.uci.ics.jung.visualization.RenderContext;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;

import ch.rakudave.jnetmap.view.jung.VertexTransformers;

//import ch.rakudave.jnetmap.model.Device;


/**
 * a plugin that uses popup menus to create vertices, undirected edges, and
 * directed edges.
 *
 * @author Tom Nelson, rakudave
 */
public class PopupGraphMousePlugin extends AbstractPopupGraphMousePlugin {
    private Frame owner;
    private Factory<Device> vertexFactory;
    private Factory<Connection> edgeFactory;
   
   

    private JMenu plugins;
    private Device vertex; 


    public PopupGraphMousePlugin(Frame owner, Factory<Device> vertexFactory, Factory<Connection> edgeFactory) {
        this.owner = owner;
        this.vertexFactory = vertexFactory;
        this.edgeFactory = edgeFactory;
        buildPluginsMenu();
    }


    @SuppressWarnings("serial")
    private void buildPluginsMenu() {
        plugins = new JMenu(Lang.get("preferences.plugins"));
        plugins.setIcon(Icons.get("plugin"));
        List<RightClickAction> rightClickPlugins = Controller.getPluginManager().getExtensions(RightClickAction.class);
        rightClickPlugins.addAll(ScriptsPanel.getScriptPlugins());
        rightClickPlugins.sort(Comparator.comparing(RightClickAction::getName));
        for (final RightClickAction p : rightClickPlugins) {
            plugins.add(new AbstractAction(p.getName(), p.getIcon()) {
                @Override
                public void actionPerformed(ActionEvent e) {
                    try {
                        Scheduler.execute(() -> p.execute(vertex));
                    } catch (Exception ex) {
                        Logger.error("An error occured in plugin '" + p.getName() + "'", ex);
                    }
                }
            });
        }
    }

    @Override
    @SuppressWarnings({"unchecked", "serial"})
    protected void handlePopup(MouseEvent e) {
        JPopupMenu popup = new JPopupMenu();
        final VisualizationViewer<Device, Connection> vv = (VisualizationViewer<Device, Connection>) e
                .getSource();
        final Layout<Device, Connection> layout = vv.getGraphLayout();
        final Graph<Device, Connection> graph = layout.getGraph();
        final Point2D p = e.getPoint();
        final Point2D ivp = p;
        GraphElementAccessor<Device, Connection> pickSupport = vv.getPickSupport();
        if (pickSupport != null) {
            vertex = pickSupport.getVertex(layout, ivp.getX(), ivp.getY());
            final Connection edge = pickSupport.getEdge(layout, ivp.getX(), ivp.getY());
            final PickedState<Device> pickedVertexState = vv.getPickedVertexState();
            final PickedState<Connection> pickedEdgeState = vv.getPickedEdgeState();
            if (vertex != null) { // Ha egy eszközre lett klikkelve
                pickedVertexState.pick(vertex, false);
                popup.add(new AbstractAction(Lang.get("device.properties"), Icons.get("properties")) {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        new DeviceProperties(owner, vertex, false);
                    }
                });
                if (ScriptsPanel.isDirty()) buildPluginsMenu();
                if (plugins.getMenuComponentCount() > 0) popup.add(plugins);
                popup.add(new AbstractAction(Lang.get("menu.view.refresh"), Icons.get("refresh")) {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        // TODO should be a triggerable action instead of being hard-coded here
                        Scheduler.execute(() -> {
                            StatusBar.getInstance().setBusy(true);
                            StatusBar.getInstance().setMessage(Lang.getNoHTML("message.status.update").replaceAll("%name%", vertex.getName()));
                            vertex.updateStatus();
                            try {
                                Collection<Device> neighbors = Controller.getCurrentMap().getNeighbors(vertex);
                                if (!neighbors.isEmpty()) for (Device d : neighbors) d.updateStatus();
                            } catch (Exception e1) {
                                Logger.error("Failed to update the neighbors of " + vertex, e1);
                            }
                            StatusBar.getInstance().setBusy(false);
                            StatusBar.getInstance().clearMessage();
                            TabPanel.getCurrentTab().repaint();
                        });
                    }
                });
                //popup.add(TabPanel.getZoomTo(vertex));
                popup.add(new AbstractAction(Lang.get("action.delete"), Icons.get("remove")) {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        graph.removeVertex(vertex);
                        vv.repaint();
                    }
                });
                popup.add(new AbstractAction(Lang.get("layer.add"), Icons.get("add")) {
                    @Override
                    public void actionPerformed(ActionEvent arg0) {
                        Device newVertex = vertexFactory.create();
                        newVertex.setisLayer(true);
                    
                        
                        newVertex.setBelongsTo(vertex.toString());

                       
                               
                                final String layerType = (String)JOptionPane.showInputDialog(owner, "Please select a layer:", 
                                "Please select a layer", JOptionPane.PLAIN_MESSAGE, null, Layer.getLayerTypes(), "Layer1");
                                newVertex.setLayer(layerType);
                                newVertex.setType(layerType);
                                newVertex.setName(vertex.getName() + " " + newVertex.getLayer());
                   

                        

                        //Device tmp_vertex = vertex;

                        // Hova tegyük az új layert?
                        // Ha van azonos layer letéve, (ami device-hoz tartozik, nem layerhez) akkor egy magasságban van (y)
                        //  ebben az esetben az x-et pedig a "szülő" device határozza meg.

                        Double x = layout.transform(vertex).getX();
                        Double y = layout.transform(vertex).getY() + 150.0;
                        Point2D location = new Point2D.Double(x, y);



                        if (graph.getVertexCount() > 0) { // akkor vizsgálom, ha van
                            for (Device d : graph.getVertices()) {
                                if (d.isLayer() && layout.transform(d).equals(location)){ // ha már van ott valami, ahova tenni akarjuk, akkor tolni kell
                                  
                                    
                                    for (Device dev :graph.getVertices()) { // minden layert
                                        if (dev.isLayer()) {
                                            Point2D coordinates = layout.transform(dev);
                                            coordinates.setLocation(coordinates.getX(), coordinates.getY()+150);
                                            layout.setLocation(dev, coordinates);
                                        }
                                    }
                                    break;
                                    
                                }
                                else if (d.isLayer() && d.getType().equals(newVertex.getType())) { // Ha már van a mapon ilyen layer akkor legyenek egyvonalban
                                    y = layout.transform(d).getY();
                                }
                                
                                /* if (d.isLayer() && d.getBelongsTo().equals(newVertex.getBelongsTo())) { // ha van azonos számú layer
                                    y = layout.transform(d).getY(); // az meghatározza a y-t
                                    x = layout.transform(d).getX(); // a device pedig az x-t
                                    if (d.isLayer() && d.getBelongsTo().equals(newVertex.getBelongsTo())) { 
                                        // Ha olyant layert találtunk, ami ahhoz a devicehoz tartozik, amihhez most is adunk.
                                        // Itt az összeset lejjebb kéne vinni
                                        if(d.getType().equals(newVertex.getType())) {
                                            x = layout.transform(d).getX() + 100.0;
                                            break;
                                        }
                                        Point2D cooridnates = layout.transform(d);
                                        cooridnates.setLocation(layout.transform(d).getX(), layout.transform(d).getY()+150);
                                        layout.setLocation(d, cooridnates);
                                    }
                                    
                                }
                                
                                else {
                                    y = layout.transform(vertex).getY() + 150;
                                    x = layout.transform(vertex).getX();
                                } */
                            }
                        }



                        
                        
                        graph.addVertex(newVertex);

                        /* RenderContext<Device, Connection> rc = vv.getRenderContext();

                        rc.setEdgeStrokeTransformer(EdgeTransformers.strokeTransformer());
                        rc.setEdgeDrawPaintTransformer(EdgeTransformers.paintTransformer(vv.getPickedEdgeState()));
                        rc.setEdgeLabelTransformer(new EdgeLabeller());
                        EdgeTransformers.setEdgeShape(rc); */
                        
                        

                        //Point2D location = layout.transform(tmp_vertex);
                        //location.setLocation(location.getX(), location.getY()+250.0);

                        //System.out.println(location.toString());
                        //System.out.print(e.getPoint().toString());

                        location.setLocation(x, y);
                        layout.setLocation(newVertex, location);

                        for (Device d : graph.getVertices()) {
                            if (d.isLayer() && d.getBelongsTo().equals(newVertex.getBelongsTo()) && layout.transform(d).getY() == y + 150.0) {
                                Connection newConnection = edgeFactory.create();
                                    newConnection.setType(Type.Layer_Vertical);
                                    graph.addEdge(newConnection, newVertex, d);
                                    break;
                            }
                        }
                        
                        vv.repaint();

                    }
                });
            } else if (edge != null) { // ha egy connectionre lett klikkelve
                pickedEdgeState.pick(edge, false);
                popup.add(new AbstractAction(Lang.get("connection.properties"), Icons.get("properties")) {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        Pair<Device> p = Controller.getCurrentMap().getEndpoints(edge);
                        if (p.getFirst().equals(p.getSecond())) {
                            new InterfaceProperties(owner, p.getFirst().getInterfaceFor(edge));
                        } else {
                            new ConnectionProperties(owner, edge);
                        }
                    }
                });
                popup.add(new AbstractAction(Lang.get("menu.view.refresh"), Icons.get("refresh")) {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        edge.updateStatus();
                    }
                });
                popup.add(new AbstractAction(Lang.get("action.delete"), Icons.get("remove")) {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        graph.removeEdge(edge);
                        vv.repaint();
                    }
                });
            } else { // Ha üres részre lett klikkelve
                popup.add(new AbstractAction(Lang.get("action.add"), Icons.get("add")) {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        Device newVertex = vertexFactory.create();
                        new DeviceProperties(owner, newVertex, true);
                        VertexTransformers.iconTransformer(pickedVertexState);
                        graph.addVertex(newVertex);

                        layout.setLocation(newVertex, vv.getRenderContext()
                                .getMultiLayerTransformer().inverseTransform(p));
                        vv.repaint();

                    }
                });
            }
            if (popup.getComponentCount() > 0) {
                popup.show(vv, e.getX(), e.getY());
            }
        }
    }
}