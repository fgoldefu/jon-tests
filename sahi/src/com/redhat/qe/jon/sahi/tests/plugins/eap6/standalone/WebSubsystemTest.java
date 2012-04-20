package com.redhat.qe.jon.sahi.tests.plugins.eap6.standalone;

import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.redhat.qe.jon.sahi.base.inventory.Configuration;
import com.redhat.qe.jon.sahi.base.inventory.Configuration.ConfigEntry;
import com.redhat.qe.jon.sahi.base.inventory.Configuration.ConfigHistory;
import com.redhat.qe.jon.sahi.base.inventory.Configuration.CurrentConfig;
import com.redhat.qe.jon.sahi.base.inventory.Inventory;
import com.redhat.qe.jon.sahi.base.inventory.Inventory.NewChildWizard;
import com.redhat.qe.jon.sahi.base.inventory.Resource;
import com.redhat.qe.jon.sahi.tasks.Timing;

/**
 * 
 * @author lzoubek
 *
 */
public class WebSubsystemTest extends AS7StandaloneTest {
	private Resource web;
	private Resource defaultConnector;
	private Resource myConnector;
	private Resource myVHost;
	private Resource defaultVhost;
	private String virtualServer = "tld.example.org";
	
	@BeforeClass(groups = "setup")
	protected void setupAS7Plugin() {
		as7SahiTasks.importResource(server);
        web = server.child("web");
        defaultConnector = web.child("http");
        myConnector = web.child("myconnector");
        myVHost = web.child("my-host");
        defaultVhost = web.child("default-host");
    }
	@Test(groups={"vhost"})
	public void createVHost() {
		Inventory inventory = web.inventory();
		NewChildWizard nc = inventory.childResources().newChild("VHost");
		nc.getEditor().setText("resourceName", myVHost.getName());
		nc.next();		
		ConfigEntry ce = nc.getEditor().newEntry(0);
		ce.setField("alias", virtualServer);
		ce.OK();
		nc.finish();
		inventory.childHistory().assertLastResourceChange(true);
		mgmtClient.assertResourcePresence("/subsystem=web", "virtual-server", myVHost.getName(), true);
		Assert.assertTrue(mgmtClient.readAttribute("/subsystem=web/virtual-server="+myVHost.getName(), "alias").get("result").asList().get(0).asString().equals(virtualServer),"New VHost has correctly set aliases");
		myVHost.assertExists(true);
	}
	
	@Test(dependsOnMethods="createVHost",groups={"vhost"})
	public void removeVHost() {
		myVHost.delete();
		web.inventory().childHistory().assertLastResourceChange(true);
		mgmtClient.assertResourcePresence("/subsystem=web", "virtual-server", myVHost.getName(), false);
		myVHost.assertExists(false);
	}
	
	@Test(groups={"vhost"})
	public void configureVHost() {
		Configuration configuration = defaultVhost.configuration();
		CurrentConfig config = configuration.current();
		ConfigEntry ce = config.getEditor().newEntry(0);
		ce.setField("alias", virtualServer);
		ce.OK();
		config.save();
		ConfigHistory history = configuration.history();
		history.failOnPending();
		history.failOnFailure();
		Assert.assertTrue(mgmtClient.readAttribute("/subsystem=web/virtual-server="+defaultVhost.getName(), "alias").get("result").asList().get(0).asString().equals(virtualServer),"VHost configuration change was successfull");
	}
	
	
	@Test(groups={"connector"})
	public void configureConnector() {
		Configuration configuration = defaultConnector.configuration();
		CurrentConfig config = configuration.current();
		config.getEditor().setText("max-save-post-size", "8192");
		config.save();
		ConfigHistory history = configuration.history();
		history.failOnPending();
		history.failOnFailure();
		Assert.assertTrue(mgmtClient.readAttribute("/subsystem=web/connector="+defaultConnector.getName(), "max-save-post-size").get("result").asString().equals("8192"),"Connector configuration change was successfull");
	}
	
	@Test(groups={"connector","blockedByBug-811149"})
	public void createConnector() {
		Inventory inventory = web.inventory();
		NewChildWizard nc = inventory.childResources().newChild("Connector");
		nc.getEditor().setText("resourceName", myConnector.getName());
		nc.next();
		// wait a little bit longer - it takes time to load connector configuration
		sahiTasks.waitFor(Timing.WAIT_TIME);
		nc.getEditor().checkRadio("http");
		nc.getEditor().selectCombo(1,"remoting");
		nc.finish();
		sahiTasks.waitFor(Timing.WAIT_TIME);
		
		inventory.childHistory().assertLastResourceChange(true);
		mgmtClient.assertResourcePresence("/subsytem=web", "connector", myConnector.getName(), true);
		myConnector.assertExists(true);

	}
	@Test(dependsOnMethods="createConnector",groups={"connector"})
	public void removeConnector() {
		myConnector.delete();
		web.inventory().childHistory().assertLastResourceChange(true);
		mgmtClient.assertResourcePresence("/subsytem=web", "connector", myConnector.getName(), false);
		myConnector.assertExists(false);
	}
	
}