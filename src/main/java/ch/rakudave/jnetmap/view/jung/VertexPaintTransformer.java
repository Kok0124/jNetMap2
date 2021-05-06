import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Paint;

import javax.swing.JFrame;

import org.apache.commons.collections15.Transformer;
import org.apache.commons.collections15.functors.ConstantTransformer;

import edu.uci.ics.jung.algorithms.layout.FRLayout;
import edu.uci.ics.jung.algorithms.layout.Layout;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.visualization.VisualizationViewer;
import edu.uci.ics.jung.visualization.control.DefaultModalGraphMouse;
import edu.uci.ics.jung.visualization.decorators.ToStringLabeller;
import edu.uci.ics.jung.visualization.picking.PickedInfo;
import edu.uci.ics.jung.visualization.renderers.DefaultVertexLabelRenderer;
import edu.uci.ics.jung.visualization.renderers.Renderer.VertexLabel.Position;

import ch.rakudave.jnetmap.model.device.*;

public class VertexPaintTransformer implements Transformer<Device,Paint> {

    private final PickedInfo<Device> pi;

    VertexPaintTransformer ( PickedInfo<Device> pi ) { 
        super();
        if (pi == null)
            throw new IllegalArgumentException("PickedInfo instance must be non-null");
        this.pi = pi;
    }

    @Override
    public Paint transform(Device d) {
        Color p = null;
        //Edit here to set the colours as reqired by your solution
        if ( d.isLayer() )
            p = Color.GREEN;
        else
            p =  Color.RED;
        //Remove if a selected colour is not required
        if ( pi.isPicked(d)){
            p = Color.yellow;
        }
        return p;
    }
}