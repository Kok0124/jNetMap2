package ch.rakudave.jnetmap.model.device;

import ch.rakudave.jnetmap.model.Connection;
import ch.rakudave.jnetmap.model.Connection.Type;
import ch.rakudave.jnetmap.model.IF.LogicalIF;
import ch.rakudave.jnetmap.model.IF.NetworkIF;
import ch.rakudave.jnetmap.model.IF.PhysicalIF;
import ch.rakudave.jnetmap.net.status.Status;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

/**
 * 
 * @author sebehuber
 * 
 */

public class HostTest {
	Host h;
	NetworkIF i;
	NetworkIF ii;
	NetworkIF iii;
	List<NetworkIF> interfaces;
	List<NetworkIF> ifs;
	Connection c;

	@Before
	public void setUp() {
		c = new Connection();
		c.setBandwidth(100);
		c.setType(Type.Ethernet);
		c.setStatus(i, Status.UP);
		h = new Host();
		i = new PhysicalIF(h, c, "192.168.0.35");
		ii = new LogicalIF(h, new Connection(), "127.0.0.1");
		iii = null;
		interfaces = new ArrayList<>();
		ifs = new ArrayList<>();
		interfaces.add(0, i);
		interfaces.add(1, ii);
		h.addInterface(i);
		h.addInterface(ii);
		h.addInterface(iii);
	}

	@Test(expected = IndexOutOfBoundsException.class)
	public void addInterfaceTest() {
		assertSame(interfaces.get(0), h.getInterfaces().get(0));
		assertSame(interfaces.get(1), h.getInterfaces().get(1));
		h.getInterfaces().get(2);// throws IndexOutOfBoundsException because
									// Element 2 shouldn't exist

	}

	@Test
	public void getInterfacesTest() {
		h.addInterface(i);
		h.addInterface(ii);
		h.getInterfaces().containsAll(interfaces);
	}

	@Test
	public void getInterfaceForTest() {
		ifs.add(i);
		assertEquals(h.getInterfaceFor(null), null);
		assertEquals(ifs.get(0), h.getInterfaceFor(c));
	}

	@Test
	public void setGetMetaDataTest() {
		h.setMetadata("BS", "Linux");
		assertEquals("Linux", h.getMetadata("BS"));

	}

	@Test
	public void RemovetMetaDataTest() {
		h.removeMetadata("BS");
		assertNull(h.getMetadata("BS"));

	}

	@After
	public void tearDown() {
		ifs = null;
		h = null;
		i = null;
		ii = null;
		interfaces = null;

	}
}



public class Layer {
    private String name;
    private boolean isVisible;
    private Set<Device> devices;
    @XStreamOmitField
    private JCheckBox checkBox;
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
        checkBox = new JCheckBox(name,true);
        checkBox.addMouseListener(new MouseListener() {

            @Override
            public void mouseReleased(MouseEvent e) {
                // TODO Auto-generated method stub
                System.out.println("mousereleasaed event happened");

            }

            @Override
            public void mousePressed(MouseEvent e) {
                // TODO Auto-generated method stub
                setVisible(!isVisible);
                System.out.println("mousepressed event happened");
            }

            @Override
            public void mouseExited(MouseEvent e) {
                // TODO Auto-generated method stub
                System.out.println("mousep exited event happened");

            }

            @Override
            public void mouseEntered(MouseEvent e) {
                // TODO Auto-generated method stub
                System.out.println("mouse entered event happened");

            }

            @Override
            public void mouseClicked(MouseEvent e) {
                // TODO Auto-generated method stub
                System.out.println("mouse clicked event happened");

            }
        });
    }

    public Layer(String name) {
        this();
        setName(name);
    }

    public JCheckBox getComponent() { // nemhasználom
        return checkBox;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
        checkBox.setText(name);
    }

    public boolean isVisible() {
        return isVisible;
    }

    public void setVisible(boolean isVisible) {
        this.isVisible = isVisible;
        checkBox.setSelected(isVisible);
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