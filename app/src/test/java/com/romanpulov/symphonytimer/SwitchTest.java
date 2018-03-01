package com.romanpulov.symphonytimer;

import org.junit.Test;

/**
 * Created by romanpulov on 28.02.2018.
 */

public class SwitchTest {
    @Test
    public void simpleTest() {
        System.out.println("Simple switch test");
    }

    @Test
    public void switchTest() {
        for (int i = 1; i < 6; i ++) {
            System.out.println("Testing " + i);
            switchStatement(i);
            System.out.println("===");
        }
    }

    public void switchStatement(int value) {
        switch(value) {
            case 2:
                System.out.println("Case 2");
            case 3:
                System.out.println("Case 3");
            case 4:
                System.out.println("Case 4");
            case 100:
                break;
            default:
                System.out.println("Case default");
        }
    }
}
