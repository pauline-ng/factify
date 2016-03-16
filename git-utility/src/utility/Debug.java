/**
    Copyright (C) 2016, Genome Institute of Singapore, A*STAR  

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package utility;

import java.util.HashMap;

public class Debug {
	public static String debugFile;
	public  enum  DEBUG_CONFIG { 
		debug_textpieces,
		debug_temp,
		debug_highlight,
		////
		debug_detectorpipeline,
		////
		debug_C_Facts,
		debug_S_Facts,
		debug_pattern,
		///
		debug_error,//for alerting errors 
		debug_warning,
		debug_timeline
	};
	public static void init() {
		configs.put(DEBUG_CONFIG.debug_textpieces, false);
		configs.put(DEBUG_CONFIG.debug_temp, false);
		configs.put(DEBUG_CONFIG.debug_highlight, false);
		
		configs.put(DEBUG_CONFIG.debug_C_Facts, false);
		configs.put(DEBUG_CONFIG.debug_S_Facts, false);
		configs.put(DEBUG_CONFIG.debug_pattern, false);
		configs.put(DEBUG_CONFIG.debug_detectorpipeline, false);
		
		
		configs.put(DEBUG_CONFIG.debug_error, true);
		configs.put(DEBUG_CONFIG.debug_warning, true);
		configs.put(DEBUG_CONFIG.debug_timeline, true);
		set = true;
	}

	private static HashMap<DEBUG_CONFIG, Boolean> configs = new HashMap<DEBUG_CONFIG, Boolean>(); 
	private static Boolean set = false;
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
//		DEBUG_CONFIG.
	}
	public static void println(Object s, DEBUG_CONFIG debug) {
		if(!set) init();
		if(debug == DEBUG_CONFIG.debug_highlight) {
			if(configs.get(debug)) {
				if(debugFile == null) System.err.println(s);
				else {
					utility util = new utility();
					util.writeFile(debugFile, "HIGHLIGHT: " + s + "\r\n", true);
				}
			}
		}else
			if(configs.get(debug)) {
				if(debugFile == null) System.out.println(s);
				else {
					utility util = new utility();
					util.writeFile(debugFile, s + "\r\n", true);
				}
			}
	}
	public static void println(DEBUG_CONFIG debug) {
		if(!set) init();
		if(debug == DEBUG_CONFIG.debug_highlight) {
			if(configs.get(debug)) {
				if(debugFile == null) System.err.println();
				else {
					utility util = new utility();
					util.writeFile(debugFile, "HIGHLIGHT: \r\n", true);
				}
			}
		}else
			if(configs.get(debug)) {
				if(debugFile == null) System.out.println();
				else {
					utility util = new utility();
					util.writeFile(debugFile, "\r\n", true);
				}
			}
	}
	public static void print(String s, DEBUG_CONFIG debug) {
		if(!set) init();
		if(debug == DEBUG_CONFIG.debug_highlight) {
			if(configs.get(debug)) {
				if(debugFile == null) System.err.print(s);
				else {
					utility util = new utility();
					util.writeFile(debugFile, "HIGHLIGHT: " + s, true);
				}
			}
		}else
			if(configs.get(debug)) {
				if(debugFile == null) System.out.print(s);
				else {
					utility util = new utility();
					util.writeFile(debugFile, s, true);
				}
			}
	}
	public static boolean get(DEBUG_CONFIG con) {
		if(!set) init();
		return configs.get(con);
	}

	public static void reset() {
		configs.put(DEBUG_CONFIG.debug_textpieces, false);
		configs.put(DEBUG_CONFIG.debug_temp, false);
		configs.put(DEBUG_CONFIG.debug_highlight, false);
		
		configs.put(DEBUG_CONFIG.debug_detectorpipeline, false);
		
		configs.put(DEBUG_CONFIG.debug_C_Facts, false);
		configs.put(DEBUG_CONFIG.debug_S_Facts, false);
		configs.put(DEBUG_CONFIG.debug_pattern, false);

		
		configs.put(DEBUG_CONFIG.debug_error, false);
		configs.put(DEBUG_CONFIG.debug_warning, false);
		configs.put(DEBUG_CONFIG.debug_timeline, false);
		set = true;
	}

	public static void set(DEBUG_CONFIG conf, boolean value) {
		if(!set) init();
		configs.put(conf, value);
		set = true;
	}
}
