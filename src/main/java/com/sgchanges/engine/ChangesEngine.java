package com.sgchanges.engine;

public class ChangesEngine {
	
	public static void main(String[] args) {
        Thread listener = new Thread(new ChangesListener());
        listener.start();
        System.out.println("ChangesListener started");
	}

}
