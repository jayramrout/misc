package com.dbquery.main;


public class DBToolSplasher {
    /**
     * Shows the splash screen, launches the application and then disposes
     * the splash screen.
     * @param args the command line arguments
     */
    public static void main(String[] args) {
    	
        SplashWindow.splash(DBToolSplasher.class.getResource("/RunImage.png"));
        SplashWindow.invokeMain("com.dbquery.main.Main", args);
        SplashWindow.disposeSplash();
    }
}