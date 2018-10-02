package de.ust.skill.manipulator;

import java.io.PrintStream;

/**
 * This class replaces the standard system output.
 * Result is the possibility to disable console output.
 * 
 * @author olibroe
 *
 */
public class OutputPrinter {
	// default printstream is System.out
	private static PrintStream outstream = System.out;
	
	public static void println(String x) {
		if(outstream != null) outstream.println(x);
	}
	
	public static void disableOutput() {
		outstream = null;
	}
	
}
