package org.springframework.ide.eclipse.boot.dash.test;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;


@RunWith(Suite.class)
@SuiteClasses({
	BootDashModelStateSaverTest.class,
	BootDashModelTest.class,
	BootProjectDashElementTest.class,
	BootDashElementTagsTests.class
})
public class AllBootDashTests {

}
