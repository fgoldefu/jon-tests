// this example shows how to create a child resource
// an EAP6 or AS7 server is required to exist on any agent

// this script required named parameter called deployment which is a path to sample deployment file
var content = deployment;

// bind input parameters
var platform = agent;

var eap = getEAP(platform)

// increase timeout for following operations
timeout = 660;

println("Create [Network Interface] child - new resource without backed content");
var netiface = eap.createChild({name:"testinterface",type:"Network Interface"});
assertTrue(netiface!=null,"New resource was returned by createChild method = > successfull creation");
assertTrue(netiface.exists(),"New resource exists in inventory");
netiface.waitForAvailable();
assertTrue(netiface.isAvailable(),"New interface is available");
netiface.remove();
assertFalse(netiface.exists(),"Network interface exists in inventory");
delete netiface;

println("Create [Network Interface] child with extra configuration - new resource without backed content");
var netiface = eap.createChild({name:"testinterface2",type:"Network Interface",config:{"inet-address":"127.0.0.1","any-address":false}});
assertTrue(netiface!=null,"New resource was returned by createChild method = > successfull creation");
assertTrue(netiface.exists(),"New resource exists in inventory");
netiface.waitForAvailable();
assertTrue(netiface.isAvailable(),"New interface is available");
netiface.remove();
assertFalse(netiface.exists(),"Network interface exists in inventory");


println("Create Deployment without specifying name - name is derived from content parameter (file name)");
var deployed = eap.createChild({content:deployment,type:"Deployment"});
assertTrue(deployed!=null,"Deployment resource was returned by createChild method = > successfull creation");
assertTrue(deployed.exists(),"Deployment resource exists in inventory");
deployed.waitForAvailable();
assertTrue(deployed.isAvailable(),"New deployment is available");
deployed.remove();
assertFalse(deployed.exists(),"Deployment exists in inventory");
delete deployed;

println("Create Deployment with name");
var deployed = eap.createChild({content:deployment,name:"hellodeployment",type:"Deployment"});
assertTrue(deployed!=null,"Deployment resource was returned by createChild method = > successfull creation");
assertTrue(deployed.exists(),"Deployment resource exists in inventory");
deployed.waitForAvailable();
assertTrue(deployed.isAvailable(),"New deployment is available");
deployed.remove();
assertFalse(deployed.exists(),"Deployment exists in inventory");
delete deployed;

//timeout back to normal
timeout = 121;

println("Create child resource without passing required parameters");
println(expectException(eap.createChild));

println("Create child resource without passing required parameters");
println(expectException(eap.createChild,[{name:"test"}]));

println("Create child resource without passing required parameters");
println(expectException(eap.createChild,[{type:"test"}]));

println("Create child resource without passing non-existing content param");
println(expectException(eap.createChild,[{content:"/tmp/1/2/3/4/5",type:"Deployment"}]));

println("Create child resource without passing invalid type");
println(expectException(eap.createChild,[{name:"test",type:"DeploymentX"}]));

