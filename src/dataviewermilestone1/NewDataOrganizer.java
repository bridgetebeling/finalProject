package dataviewermilestone1;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

import edu.du.dudraw.Draw;

public class NewDataOrganizer {
	private final static double 	DATA_WINDOW_BORDER = 50.0;
	private final static String 	DEFAULT_COUNTRY = "United States";
	private final static boolean	DO_DEBUG = true;
	private final static boolean	DO_TRACE = false;
	private final static double 	EXTREMA_PCT = 0.1;
	private final static int 		FILE_COUNTRY_IDX = 4;
	private final static int 		FILE_DATE_IDX = 0;
	private final static int 		FILE_NUM_COLUMNS = 5;
	private final static int 		FILE_STATE_IDX = 3;
	private final static int 		FILE_TEMPERATURE_IDX = 1;
    private final static int 		GUI_MODE_MAIN_MENU = 0;
    private final static int 		GUI_MODE_DATA = 1;
    private final static double		MENU_STARTING_X = 40.0;
	private final static double 	MENU_STARTING_Y = 90.0;
	private final static double 	MENU_ITEM_SPACING = 5.0;
	private final static String[] 	MONTH_NAMES = { "", // 1-based
			"Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec" };
	private final static int		RECORD_MONTH_IDX = 1;
	private final static int		RECORD_STATE_IDX = 3;
	private final static int		RECORD_TEMPERATURE_IDX = 2;
	private final static int		RECORD_YEAR_IDX = 0;
	private final static double		TEMPERATURE_MAX_C = 30.0;
	private final static double		TEMPERATURE_MIN_C = -10.0;
	private final static double		TEMPERATURE_RANGE = TEMPERATURE_MAX_C - TEMPERATURE_MIN_C;
	private final static String[] 	VISUALIZATION_MODES = { "Raw", "Extrema (within 10% of min/max)" };
	private final static int		VISUALIZATION_EXTREMA_IDX = 1;
	private final static int 		WINDOW_HEIGHT = 720;
	private final static String 	WINDOW_TITLE = "DataViewer Application";
	private final static int 		WINDOW_WIDTH = 1320; // should be a multiple of 12
		
	// Instance variables (alphabetized)
	
    // data storage
    private final String m_dataFile;
    private List<List<Object>> m_dataRaw;
    private SortedSet<String> m_dataStates;
    private SortedSet<String> m_dataCountries;
    private SortedSet<Integer> m_dataYears;
    
    // GUI-related settings    
    private int m_guiMode = GUI_MODE_MAIN_MENU; // Menu by default
    
    // user selections
    private String m_selectedCountry = DEFAULT_COUNTRY;
    private Integer m_selectedEndYear;
    private String m_selectedState;
    private Integer m_selectedStartYear;
    private String m_selectedVisualization = VISUALIZATION_MODES[0];
    
    // plot-related data
	private TreeMap<Integer, SortedMap<Integer,Double>> m_plotData = null;
	private TreeMap<Integer,Double> m_plotMonthlyMaxValue = null;
	private TreeMap<Integer,Double> m_plotMonthlyMinValue = null;
	
	// Window-variables
    private Draw m_window;
    
    public NewDataOrganizer(String dataFile) throws FileNotFoundException {
    	// save the data file name for later use if user switches country
    	m_dataFile = dataFile;
    	
        // Setup the DuDraw board
        
        
        // Load data
        loadData();
        
        
    }
    
    public SortedSet getCountries() {
    	return m_dataCountries;
    }
    
    public SortedSet getYears() {
    	return m_dataYears;
    }
    
    public SortedSet getStates() {
    	return m_dataStates;
    }
    
    public Integer getEndYear() {
    	return m_selectedEndYear;
    }
    
    public Integer getStartYear() {
    	return m_selectedStartYear;
    }
    
    public TreeMap<Integer,Double> getMonthyMax() {
    	return m_plotMonthlyMaxValue;
    }
    
    public TreeMap<Integer,Double> getMonthyMin() {
    	return m_plotMonthlyMinValue;
    }
    
    public TreeMap<Integer, SortedMap<Integer,Double>> getPlotData() {
    	return m_plotData;
    }
    
    public void callLoadData() throws FileNotFoundException {
    	loadData();
    }
    
    public void callUpdatePlotData() {
    	updatePlotData();
    }
	
	 private void trace(String format, Object...args) {
	    	if(DO_TRACE) {
	    		System.out.print("TRACE: ");
	    		System.out.println(String.format(format, args));
	    	}
	    }
	    
	    /**
	     * For informational output.
	     * @param format
	     * @param args
	     */
	    private void info(String format, Object... args) {
	    	System.out.print("INFO: ");
	    	System.out.println(String.format(format, args));
	    }
	    
	    /**
	     * For error output.
	     * @param format
	     * @param args
	     */
	    private void error(String format, Object... args) {
	    	System.out.print("ERROR: ");
	    	System.out.println(String.format(format, args));
	    }
	    
	    /**
	     * For debugging output.  Output is controlled by the DO_DEBUG constant.
	     * @param format
	     * @param args
	     */
	    private void debug(String format, Object... args) {
	    	if(DO_DEBUG) {
	    		System.out.print("DEBUG: ");
	    		System.out.println(String.format(format, args));
	    	}
	    }
	    
	    /**
	     * Utility function to pull a year integer out of a date string.  Supports M/D/Y and Y-M-D formats only.
	     * 
	     * @param dateString
	     * @return
	     */
	    private Integer parseYear(String dateString) {
	    	Integer ret = null;
	    	if(dateString.indexOf("/") != -1) {
	    		// Assuming something like 1/20/1823
	    		String[] parts = dateString.split("/");
	    		if(parts.length == 3) {
		    		ret = Integer.parseInt(parts[2]);
	    		}
	    	}
	    	else if(dateString.indexOf("-") != -1) {
	    		// Assuming something like 1823-01-20
	    		String[] parts = dateString.split("-");
	    		if(parts.length == 3) {
	    			ret = Integer.parseInt(parts[0]);
	    		}
	    	}
	    	else {
	    		throw new RuntimeException(String.format("Unexpected date delimiter: '%s'", dateString));
	    	}
	    	if(ret == null) {
	    		trace("Unable to parse year from date: '%s'", dateString);
	    	}
	    	return ret;
	    }
	    
	    private List<Object> getRecordFromLine(String line) {
	        List<String> rawValues = new ArrayList<String>();
	        try (Scanner rowScanner = new Scanner(line)) {
	            rowScanner.useDelimiter(",");
	            while (rowScanner.hasNext()) {
	                rawValues.add(rowScanner.next());
	            }
	        }
	        m_dataCountries.add(rawValues.get(FILE_COUNTRY_IDX));
	        if(rawValues.size() != FILE_NUM_COLUMNS) {
	        	trace("malformed line '%s'...skipping", line);
	        	return null;
	        }
	        else if(!rawValues.get(FILE_COUNTRY_IDX).equals(m_selectedCountry)) {
	        	trace("skipping non-USA record: %s", rawValues);
	        	return null;
	        }
	        else {
	        	trace("processing raw data: %s", rawValues.toString());
	        }
	        try {
	        	// Parse these into more useful objects than String
	        	List<Object> values = new ArrayList<Object>(4);
	        	
	        	Integer year = parseYear(rawValues.get(FILE_DATE_IDX));
	        	if(year == null) {
	        		return null;
	        	}
	        	values.add(year);
	        	
	        	Integer month = parseMonth(rawValues.get(FILE_DATE_IDX));
	        	if(month == null) {
	        		return null;
	        	}
	        	values.add(month);
	        	values.add(Double.parseDouble(rawValues.get(FILE_TEMPERATURE_IDX)));
	        	//not going to use UNCERTAINTY yet
	        	//values.add(Double.parseDouble(rawValues.get(FILE_UNCERTAINTY_IDX)));
	        	values.add(rawValues.get(FILE_STATE_IDX));
	        	// since all are the same country
	        	//values.add(rawValues.get(FILE_COUNTRY_IDX));
	        	
	        	// if we got here, add the state to the list of states
	        	m_dataStates.add(rawValues.get(FILE_STATE_IDX));
	        	m_dataYears.add(year);
	        	return values;
	        }
	        catch(NumberFormatException e) {
	        	trace("unable to parse data line, skipping...'%s'", line);
	        	return null;
	        }
	    }
	    
	    private Integer parseMonth(String dateString) {
	    	Integer ret = null;
	    	if(dateString.indexOf("/") != -1) {
	    		// Assuming something like 1/20/1823
	    		String[] parts = dateString.split("/");
	    		if(parts.length == 3) {
		    		ret = Integer.parseInt(parts[0]);
	    		}
	    	}
	    	else if(dateString.indexOf("-") != -1) {
	    		// Assuming something like 1823-01-20
	    		String[] parts = dateString.split("-");
	    		if(parts.length == 3) {
	    			ret = Integer.parseInt(parts[1]);
	    		}
	    	}
	    	else {
	    		throw new RuntimeException(String.format("Unexpected date delimiter: '%s'", dateString));
	    	}
	    	if(ret == null || ret.intValue() < 1 || ret.intValue() > 12) {
	    		trace("Unable to parse month from date: '%s'", dateString);
	    		return null;
	    	}
	    	return ret;
		}

		private void loadData() throws FileNotFoundException {
			// reset the data storage in case this is a re-load
			m_dataRaw = new ArrayList<List<Object>>();
		    m_dataStates = new TreeSet<String>();
		    m_dataCountries = new TreeSet<String>();
		    m_dataYears = new TreeSet<Integer>();
		    m_plotData = null;
		    
	    	try (Scanner scanner = new Scanner(new File(m_dataFile))) {
	    	    boolean skipFirst = true;
	    	    while (scanner.hasNextLine()) {
	    	    	String line = scanner.nextLine();
	    	    	
	    	    	if(!skipFirst) {
	    	    		List<Object> record = getRecordFromLine(line);
	    	    		if(record != null) {
	    	    			m_dataRaw.add(record);
	    	    		}
	    	    	}
	    	    	else {
	    	    		skipFirst = false;
	    	    	}
	    	    }
	    	    // update selections (not including country) for the newly loaded data
	            m_selectedState = m_dataStates.first();
	            m_selectedStartYear = m_dataYears.first();
	            m_selectedEndYear = m_dataYears.last();

	            info("loaded %d data records", m_dataRaw.size());
	            info("loaded data for %d states", m_dataStates.size());
	            info("loaded data for %d years [%d, %d]", m_dataYears.size(), m_selectedStartYear, m_selectedEndYear);
	    	}
	    }
		
		private void updatePlotData() {
			//debug("raw data: %s", m_rawData.toString());
			// plot data is a map where the key is the Month, and the value is a sorted map where the key
			// is the year. 
			m_plotData = new TreeMap<Integer,SortedMap<Integer,Double>>();
			for(int month = 1; month <= 12; month++) {
				// any year/months not filled in will be null
				m_plotData.put(month, new TreeMap<Integer,Double>());
			}
			// now run through the raw data and if it is related to the current state and within the current
			// years, put it in a sorted data structure, so that we 
			// find min/max year based on data 
			m_plotMonthlyMaxValue = new TreeMap<Integer,Double>();
			m_plotMonthlyMinValue = new TreeMap<Integer,Double>();
			// initialize
			for(int i = 1; i <= 12; i++) {
				m_plotMonthlyMaxValue.put(i, Double.MIN_VALUE);
				m_plotMonthlyMinValue.put(i, Double.MAX_VALUE);
			}
			for(List<Object> rec : m_dataRaw) {
				String state = (String)rec.get(RECORD_STATE_IDX);
				Integer year = (Integer)rec.get(RECORD_YEAR_IDX);
				
				// Check to see if they are the state and year range we care about
				if (state.equals(m_selectedState) && 
				   ((year.compareTo(m_selectedStartYear) >= 0 && year.compareTo(m_selectedEndYear) <= 0))) {
							
					// Ok, we need to add this to the list of values for the month
					Integer month = (Integer)rec.get(RECORD_MONTH_IDX);
					Double value = (Double)rec.get(RECORD_TEMPERATURE_IDX);
					
					if(!m_plotMonthlyMinValue.containsKey(month) || value.compareTo(m_plotMonthlyMinValue.get(month)) < 0) {
						m_plotMonthlyMinValue.put(month, value);
					}
					if(!m_plotMonthlyMaxValue.containsKey(month) || value.compareTo(m_plotMonthlyMaxValue.get(month)) > 0) {
						m_plotMonthlyMaxValue.put(month, value);
					}
		
					m_plotData.get(month).put(year, value);
				}
			}
			//debug("plot data: %s", m_plotData.toString());
		}
}
