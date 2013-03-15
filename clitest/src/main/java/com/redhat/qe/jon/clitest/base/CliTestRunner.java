package com.redhat.qe.jon.clitest.base;

import java.util.Arrays;
import java.util.logging.Logger;

import org.testng.Assert;

public class CliTestRunner {

    private static Logger log = Logger.getLogger(CliTestRunner.class.getName());
    private final CliEngine engine;
    private String username;
    private String password;
    private String jsFile;
    private String jsSnippet;
    private String cliArgs;
    private String expectedResult;
    private String makeFailure;
    private String[] jsDepends;
    private String[] resSrc;
    private String[] resDst;
    private CliTestRunListener runListener;
    
    public CliTestRunner(CliEngine engine) {
	this.engine = engine;
	setDefaults();
    }
    /**
     * sets default values
     */
    private void setDefaults() {
	this.username="rhqadmin";
	this.password="rhqadmin";
	this.expectedResult="Login successful";
	this.makeFailure="Login failed:,No such file or directory";	
    }
    /**
     * validates if all required parameter values are defined and parameters are correct
     */
    private void validate() {
	if (username==null) {
	    throw new RuntimeException("user cannot be null");
	}
	if (password==null) {
	    throw new RuntimeException("password cannot be null");
	}
	if (jsFile==null && jsSnippet==null) {
	    throw new RuntimeException("both jsFile and jsSnippet cannot be null");
	}
	if (jsFile!=null && jsSnippet!=null) {
	    throw new RuntimeException("both jsFile and jsSnippet cannot be defined");
	}
	if (resSrc!=null) {
	    if (resDst==null) {
		throw new RuntimeException("Resource destinations cannot be null, when resource sources are defined");
	    }
	    if (resSrc.length!=resDst.length) {
		throw new RuntimeException("Resource destinations and sources must be same size");
	    }
	}
    }
    /**
     * sets {@link CliTestRunListener} for this run
     * @param listener
     * @return
     */
    public CliTestRunner withRunListener(CliTestRunListener listener) {
	this.runListener = listener;
	return this;
    }
    /**
     * JS file that is going to run
     * @param jsFile
     */
    public CliTestRunner jsFile(String jsFile) {
	this.jsFile = jsFile;
	return this;
    }
    /**
     * JS file that is going to run
     * @param jsSnippet
     */
    public CliTestRunner jsSnippet(String jsSnippet) {
	this.jsSnippet = jsSnippet;
	return this;
    }
    /**
     * add jsFiles that your script depends on. Those will be merged with your {@link #jsFile(String)}
     * together. Note that order of jsFiles is preserved, {@link #jsFile(String)} is always going to be last piece
     */
    public CliTestRunner dependsOn(String... jsFiles) {
	this.jsDepends = jsFiles;
	return this;
    }
    /**
     * set additional resource source paths (looked up as java resource)
     * @param resSrc
     */
    public CliTestRunner resourceSrcs(String... resSrc) {
	this.resSrc = resSrc;
	return this;
    }
    /**
     * set destinations for additional {@link #resourceSrcs(String...)}
     * @param resDst
     */
    public CliTestRunner resourceDests(String... resDst) {
	this.resDst = resDst;
	return this;
    }
    /**
     * adds jsFile dependency
     * @param jsFile
     */
    public CliTestRunner addDepends(String jsFile) {
	if (jsFile != null) {
	    if (this.jsDepends == null) {
		this.jsDepends = new String[] {};
	    }
	    this.jsDepends = Arrays.copyOf(this.jsDepends, this.jsDepends.length + 1);
	    this.jsDepends[this.jsDepends.length - 1] = jsFile;
	}
	return this;
    }
    /**
     * specify message that makes test fail, if produced as output of test
     */
    public CliTestRunner addFailOn(String failOn) {
	if (failOn!=null) {
	    this.makeFailure+=","+failOn;
	}
	return this;
    }
    /**
     * specify message - if such message is missing in output produced by CLI, test will fail
     * @param expect
     */
    public CliTestRunner addExpect(String expect) {
	if (expect!=null) {
	    this.expectedResult+=","+expect;
	}
	return this;
    }
    /**
     * specify username (subject)
     * @param user
     */
    public CliTestRunner asUser(String user) {
	this.username = user;
	return this;
    }
    public CliTestRunner withArg(String name, String value) {
	if (name != null && value != null) {
	    if (this.cliArgs == null) {
		this.cliArgs = "";
	    }
	    this.cliArgs += " " + name + "=" + value;
	}
	return this;
    }
    /**
     * prepares array args for CLI execution (just puts ',' separator between items)
     * @param args
     */
    private String prepareArrayArgs(String[] args) {
	if (args==null) {
	    return null;
	}
	StringBuilder sb = new StringBuilder();
	for (String arg : args) {
	    sb.append(arg+",");
	}
	sb.deleteCharAt(sb.length()-1);
	return sb.toString();
    }
    /**
     * runs this CLI test
     * @return consoleOutput 
     */
    public String run() {
	validate();
	if (this.cliArgs!=null) { // we support named arguments only at this time
	    this.cliArgs = "--args-style=named"+this.cliArgs;
	}
	CliEngine.runListener = this.runListener;
	String jsDepends = prepareArrayArgs(this.jsDepends);
	String resSrc = prepareArrayArgs(this.resSrc);
	String resDst = prepareArrayArgs(this.resDst);
	String result = null;
	if (jsSnippet==null) {
	    try {
		engine.runJSfile(null, this.username, this.password, this.jsFile, this.cliArgs, this.expectedResult, this.makeFailure, jsDepends, resSrc, resDst);
		result = engine.consoleOutput;
	    } catch (Exception e) {
		Assert.fail("Test failed : "+e.getMessage(), e);
	    } 
	}
	else {
	    try {
		engine.runJSSnippet(this.jsSnippet, null, this.username, this.password, cliArgs, expectedResult, this.makeFailure, jsDepends, resSrc, resDst);
		result = engine.consoleOutput;
	    } catch (Exception e) {
		Assert.fail("Test failed : "+e.getMessage(), e);
	    } 
	}
	return result;	
    }
}
