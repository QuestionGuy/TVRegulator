package com.keddy.tvregulator;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {

    private TvController createTvController(){
        return new TvController("10.0.0.23");
    }

    @Test
    public void tvcontroller_getVolume() throws  Exception {
        TvController controller = createTvController();
        assertEquals(0,controller.getVolume());
    }

    @Test
    public void tvcontroller_setVolume() throws Exception {
        TvController controller = createTvController();
        boolean res = controller.setVolume(0);

        assertEquals(res, true);
    }
}