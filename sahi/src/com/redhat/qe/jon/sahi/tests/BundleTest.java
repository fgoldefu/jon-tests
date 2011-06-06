package com.redhat.qe.jon.sahi.tests;

import com.redhat.qe.jon.sahi.base.SahiTestScript;

import org.testng.annotations.Test;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;

import com.redhat.qe.auto.testng.TestNGUtils;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;

public class BundleTest extends SahiTestScript{
	@Test (groups="bundleTest", dataProvider="bundleData")
	public void createBundleViaURL(String bundleFileName, String bundleDesc) {
		sahiTasks.createBundleURL(bundleHostURL + bundleFileName);
	}	

	@Test (groups="bundleTest", dataProvider="bundleData", dependsOnMethods={"createBundleViaURL"})
	public void deleteBundleViaURL(String bundleFileName, String bundleDesc) {
		sahiTasks.deleteBundle(bundleDesc);
	}	

	@DataProvider(name="bundleData")
	public Object[][] bundleData() {
		return TestNGUtils.convertListOfListsTo2dArray(getBundleData());
	}
	public List<List<Object>> getBundleData() {
		ArrayList<List<Object>> data = new ArrayList<List<Object>>();
		data.add(Arrays.asList(new Object[]{"bundle1.zip", "My Bundle 1297957137903"}));
		data.add(Arrays.asList(new Object[]{"bundle2.zip", "Small Bundle 1297957144497"}));
		return data;
	}
}
