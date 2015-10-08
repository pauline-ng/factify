package utility;

import java.util.HashMap;

public class Debug {
	public  enum  DEBUG_CONFIG { 
		debug_textpieces,
		debug_getDocInfo,
		debug_merge_textPiece,
		debug_clustering,
		debug_header,
		debug_footer,
		debug_fig_caption,
		debug_title,
		debug_bodytext,
		debug_section_title,
		debug_temp,
		debug_highlight,
		////
		debug_detectorpipeline,
		////
		debug_C_Facts,
		debug_S_Facts,
		debug_pattern,
		debug_acronym,
		///
		debug_error,//for alerting errors 
		debug_warning,
		debug_timeline
	};
	public static void init() {
		configs.put(DEBUG_CONFIG.debug_textpieces, false);
		configs.put(DEBUG_CONFIG.debug_getDocInfo, false);
		configs.put(DEBUG_CONFIG.debug_merge_textPiece, false);
		configs.put(DEBUG_CONFIG.debug_clustering, false);
		configs.put(DEBUG_CONFIG.debug_header, false);
		configs.put(DEBUG_CONFIG.debug_footer, false);
		configs.put(DEBUG_CONFIG.debug_fig_caption, false);
		configs.put(DEBUG_CONFIG.debug_title, false);
		configs.put(DEBUG_CONFIG.debug_bodytext, false);
		configs.put(DEBUG_CONFIG.debug_section_title, false);
		configs.put(DEBUG_CONFIG.debug_temp, false);
		configs.put(DEBUG_CONFIG.debug_highlight, false);
		
		configs.put(DEBUG_CONFIG.debug_C_Facts, false);
		configs.put(DEBUG_CONFIG.debug_S_Facts, false);
		configs.put(DEBUG_CONFIG.debug_pattern, false);
		configs.put(DEBUG_CONFIG.debug_acronym, false);
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
			if(configs.get(debug)) System.err.println(s);
		}else
			if(configs.get(debug)) System.out.println(s);
	}
	public static void println(DEBUG_CONFIG debug) {
		if(!set) init();
		if(debug == DEBUG_CONFIG.debug_highlight) {
			if(configs.get(debug)) System.err.println();
		}else
			if(configs.get(debug)) System.out.println();
	}
	public static void print(String s, DEBUG_CONFIG debug) {
		if(!set) init();
		if(debug == DEBUG_CONFIG.debug_highlight) {
			if(configs.get(debug)) System.err.print(s);
		}else
			if(configs.get(debug)) System.out.print(s);
	}
	public static boolean get(DEBUG_CONFIG con) {
		if(!set) init();
		return configs.get(con);
	}

	public static void reset() {
		configs.put(DEBUG_CONFIG.debug_textpieces, false);
		configs.put(DEBUG_CONFIG.debug_getDocInfo, false);
		configs.put(DEBUG_CONFIG.debug_merge_textPiece, false);
		configs.put(DEBUG_CONFIG.debug_clustering, false);
		configs.put(DEBUG_CONFIG.debug_header, false);
		configs.put(DEBUG_CONFIG.debug_footer, false);
		configs.put(DEBUG_CONFIG.debug_fig_caption, false);
		configs.put(DEBUG_CONFIG.debug_title, false);
		configs.put(DEBUG_CONFIG.debug_bodytext, false);
		configs.put(DEBUG_CONFIG.debug_section_title, false);
		configs.put(DEBUG_CONFIG.debug_temp, false);
		configs.put(DEBUG_CONFIG.debug_highlight, false);
		
		configs.put(DEBUG_CONFIG.debug_detectorpipeline, false);
		
		configs.put(DEBUG_CONFIG.debug_C_Facts, false);
		configs.put(DEBUG_CONFIG.debug_S_Facts, false);
		configs.put(DEBUG_CONFIG.debug_pattern, false);
		configs.put(DEBUG_CONFIG.debug_acronym, false);

		
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
