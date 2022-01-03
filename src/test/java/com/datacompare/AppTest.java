package com.datacompare;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Unit test for datacompare App.
 */
public class AppTest extends TestCase
{
    /**
     * Create the test case
     *
     * @param testName name of the test case
     */
    public AppTest( String testName )
    {
        super( testName );
    }

    /**
     * @return
     */
    public static Test suite()
    {
        return new TestSuite( AppTest.class );
    }

    /**
     *
     */
    public void testApp()
    {
        assertTrue( true );
    }
}
