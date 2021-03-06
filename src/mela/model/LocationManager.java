/**
 * 
 */
package mela.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

/**
 * @author ludovicaluisavissat
 *
 */

public class LocationManager {

	private ArrayList<Location> locations = new ArrayList<Location>();			
	private HashMap<String,Location> directory = new HashMap<>();
	//directory used to verify there are not duplicates in the location names
	private HashMap<Integer,HashMap<Integer,Integer>> adjacencyMap = new HashMap<>();	
	private HashMap<Integer,HashMap<Integer,Integer>> distanceMap = new HashMap<>();
	
	
	/********
	 * methods related to Location names and index
	 *********/
	
	public ArrayList<Location> getLocations() {
		return locations;
	}
	
	public Location createLocation( String name ) {
		if (directory.containsKey(name)) {
			throw new IllegalArgumentException("Duplicated location name!");
		}
		Location location = new Location( name , locations.size() );
		locations.add(location);
		directory.put(name, location);
		adjacencyMap.put(location.getIndex(), new HashMap<>());
		return location;
	}
	
	public Location getLocation( String name ) {
		return directory.get(name);
	}
	
	public int getLocationIndex( String name ) {
		Location l = getLocation(name);
		if (l != null) {
			return l.getIndex();
		}
		return -1;
	}
	
	//not used for the moment (but probably used for the action info)
	public String getLocationName( int idx ) {
		return locations.get(idx).getName();
	}	

	
	/**
	 * @param args: coordinates
	 * use in the creation of 2D and 3D grid
	 * @return name of the location
	 */
	public static String locationName(int ... args) {
		return Arrays.toString(args);
	}

	public int size() {
		return locations.size();
	}
	
	/********
	 * methods related to adjecencyMap	
	 *********/

	/**
	 * @param i1: index
	 * @param d: weight
	 * @param i2: index
	 * add an edge in the matrix
	 */
	public void addEdge(int i1, int d, int i2) {
		adjacencyMap.get(i1).put(i2,d);
		adjacencyMap.get(i2).put(i1, d);
	}
	
	//same method, but with names and weights instead of index
	public void addEdge( Location l1 , Location l2 ) {
		addEdge(l1,1,l2);
	}
	public void addEdge(Location l1, int d, Location l2) {
		addEdge(l1.getIndex(),d,l2.getIndex());
	}

	/**
	 * @param src: source location 
	 * @param trg: target location
	 * @return distance between the two locations
	 */
	public int getEdge( int src , int trg ) {
		return LocationManager.getValue(adjacencyMap,src,trg);
	}

	/**
	 * @param src: source location 
	 * @param trg: target location
	 * @return is there an edge between the two locations?
	 */
	public boolean edge( int src , int trg ) {
		return adjacencyMap.get(src).get(trg) != null;
	}
	
	//same method, with string and Location instead of index
	public boolean edge(String src, String trg) {
		return edge(getLocation(src),getLocation(trg));
	}
	public boolean edge(Location src, Location trg) {
		return edge(src.getIndex(),trg.getIndex());
	}
	
	/**
	 * @param location index
	 * @return the set (key set) of index of the adjacent locations
	 */
	public Set<Integer> nextTo(int l) {
		return adjacencyMap.get(l).keySet();
	}
	
	
	/**
	 * @param map: hashmap
	 * @param src: source location
	 * @param trg: target location
	 * @return weight/distance between the two locations, according to the map
	 */
	public static int getValue(HashMap<Integer, HashMap<Integer, Integer>> map, int src, int trg) {
		HashMap<Integer, Integer> next = map.getOrDefault(src, new HashMap<>());
		int weight = next.getOrDefault(trg, Integer.MAX_VALUE);
		//TODO: check arythmetic of Double.POSITIVE_INFINITY
		return weight;
	}
	
	/**
	 * given the adjecency matrix, it builds the distance matrix
	 */
	public void buildDistanceMatrix() {
		distanceMap = new HashMap<>();
		for (Entry<Integer, HashMap<Integer, Integer>> e1 : adjacencyMap.entrySet() ) {
			HashMap<Integer, Integer> row = new HashMap<>();
			distanceMap.put(e1.getKey(), row);
			row.put(e1.getKey(), 0);
			for( Entry<Integer,Integer> e2: e1.getValue().entrySet() ) {
				row.put(e2.getKey(), e2.getValue());
			}
		}		
		for ( int k=0 ; k<locations.size() ; k++ ) {
	         // Pick all vertices as source one by one
			HashMap<Integer,Integer> kMap = distanceMap.get(k);
			for ( int i = 0; i < locations.size() ; i++ ) {
	             // Pick all vertices as destination for the above picked source
				HashMap<Integer,Integer> iMap = distanceMap.get(i);
	             for (int j = 0; j < locations.size() ; j++ ) {
	            	 int newDistance = LocationManager.sum( iMap.getOrDefault(k, Integer.MAX_VALUE) , kMap.getOrDefault(j, Integer.MAX_VALUE) );
	            	 int oldDistance = iMap.getOrDefault(j, Integer.MAX_VALUE);
	            	 if (newDistance<oldDistance) {
		            	 iMap.put(j, newDistance);
	            	 }
	             }
	         }
	     }
		
	}

     /**
	 * @param i1: integer
	 * @param i2: integer
	 * @return sum between two integers, considering also the MAX_VALUE option
	 */
	private static int sum(int i1, int i2) {
		if (i1 == Integer.MAX_VALUE) {
			return i1;
		}
		if (i2 == Integer.MAX_VALUE) {
			return i2;
		}
		return i1+i2;
	}

	/**
	 * @param src: source location
	 * @param trg: target location
	 * @return distance between to locations
	 */
	public int getDistance(int src, int trg) {
		return LocationManager.getValue(distanceMap, src, trg);
	}
	
	
	//build Distance Matrix for different spatial structure choices:

	/**
	 * @param length: dimension
	 * @param isPeriodic: boundary
	 * @return: build OneD
	 */
	public static LocationManager buildGridOne(int length, boolean isPeriodic) {
		LocationManager lm = new LocationManager();
		Location[] locations = new Location[length];
		for( int i=0 ; i<length ; i++ ) {
			locations[i] = lm.createLocation(""+i);
		}
		for( int i=0 ; i<length-1 ; i++ ) {
			lm.addEdge(locations[i], 1, locations[i+1]);
		}
		if (isPeriodic) {
			lm.addEdge(locations[length-1], locations[0]);
		}
		lm.buildDistanceMatrix();
		return lm;
	}
	
	public static LocationManager buildGridOne(int length) {
		return buildGridOne( length , false );
	}
	
	/**
	 * @param lengthX: dimension
	 * @param lengthY: dimension
	 * @param isPeriodic: boundary
	 * @return build TwoD
	 */
	public static LocationManager buildGridTwo(int lengthX, int lengthY, boolean isPeriodic) {
		LocationManager lm = new LocationManager();
		Location[][] locations = new Location[lengthX][lengthY];
		
		for (int i=0 ; i<lengthX ; i++) {
			for (int j=0 ; j<lengthY ; j++ ) {
				locations[i][j] = lm.createLocation(locationName(i,j));
			}
		}

		for (int i=0 ; i<lengthX ; i++) {
			for (int j=0 ; j<lengthY ; j++ ) {
				if (!isPeriodic || (i<lengthX-1)) {
					lm.addEdge(locations[i][j], 1, locations[(i+1)%lengthX][j]);
				}
				if (!isPeriodic || (j<lengthY-1)) {
					lm.addEdge(locations[i][j], 1, locations[i][(j+1)%lengthY]);
				}
			}
		}
		lm.buildDistanceMatrix();
		return lm;
	}
	
	
	/**
	 * @param lengthX: dimension
	 * @param lengthY: dimension
	 * @param lengthZ: dimension
	 * @param isPeriodic: boundary
	 * @return build ThreeD
	 */
	public static LocationManager buildGridThree(int lengthX, int lengthY, int lengthZ, boolean isPeriodic) {
		LocationManager lm = new LocationManager();
		Location[][][] locations = new Location[lengthX][lengthY][lengthZ];
		
		for (int i=0 ; i<lengthX ; i++) {
			for (int j=0 ; j<lengthY ; j++ ) {
				for (int k=0 ; k<lengthZ ; k++ ) {
					locations[i][j][k] = lm.createLocation(locationName(i,j,k));
				}
			}
		}

		for (int i=0 ; i<lengthX ; i++) {
			for (int j=0 ; j<lengthY ; j++ ) {
				for (int k=0 ; k<lengthZ ; k++ ) {
					if (!isPeriodic || (i<lengthX-1)) {
						lm.addEdge(locations[i][j][k], 1, locations[(i+1)%lengthX][j][k]);
					}
					if (!isPeriodic || (j<lengthY-1)) {
						lm.addEdge(locations[i][j][k], 1, locations[i][(j+1)%lengthY][k]);
					}
					if (!isPeriodic || (k<lengthZ-1)) {
						lm.addEdge(locations[i][j][k], 1, locations[i][j][(k+1)%lengthZ]);
					}
				}
			}
		}
		lm.buildDistanceMatrix();
		return lm;
	}
		
	/**
	 * @return build the graph
	 */
	public static LocationManager buildGraph(){
		LocationManager lm = new LocationManager();
		//TODO build here!
		//while parsing add location names and edges
		return lm;
	}
	   
	
	/**
	 * not used at the moment
	 * @param currentLocIndex: location index
	 * @param d: distance
	 * @return indexes of the locations at distance d
	 */
	public LinkedList<Integer> indexNeigh (int currentLocIndex, int d ) {
		//TODO check
		LinkedList<Integer> indexList = new LinkedList<>();
		for (int i=0; i< distanceMap.get(currentLocIndex).size(); i++){
			if (distanceMap.get(currentLocIndex).get(i) == d){
				indexList.add(i);
			}
		}
		return indexList;
	}

}
