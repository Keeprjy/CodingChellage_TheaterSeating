package MovieTheaterSeating;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.PriorityQueue;


public class MainSeating {
	// represent seats in the theater. 
	// e means the seat is empty; f means the seat is filled; u means the seat is unsafe
	private char[][] seats; 						
	// represent continuous safe & available seats in array [rowId, colId, seats number]. 
	// seats number largest first, then min rowId first, then min colId first.
	private PriorityQueue<int[]> continuousSeats; 
	// represent num of remaining seats
	private int remainingSeats; 	
	// represent how many seats in a row we should keep between 2 orders
	private int rowUnsafeRange = 3; 				
	
	public MainSeating(int rows, int cols) {
		seats = new char[rows][cols];
		continuousSeats = new PriorityQueue<>((o1, o2) -> {
			if (o2[2] - o1[2] != 0) {
				return o2[2] - o1[2];
			} else if (o1[0] - o2[0] != 0) {
				return o1[0] - o2[0];
			} else {
				return o1[1] - o2[1];
			}
		});
		for(int i = 0; i < rows; i++) {
			for(int j = 0; j < cols; j++) {
				seats[i][j] = 'e';
			}
			int[] seat = {i, 0, cols};
			continuousSeats.add(seat);
		}
		remainingSeats = rows * cols;
	}
	
	/**
	 * fill taken seats to 'f', not unsafe seats. if there's empty seats left, add to pq.
	 * return seat numbers
	 * @param rowId
	 * @param colId
	 * @param num
	 * @return
	 */
	private String[] fillSeats(int rowId, int colId, int num) {
		String[] res = new String[num];
		// update filled seats
		for(int i = 0; i < num; i++) {
			seats[rowId][colId + i] = 'f';
			res[i] = "" + (char)('A' + rowId) + (colId + i + 1);
			remainingSeats--;
		}
		
		// update priority queue based on remaining seats
		int start = colId + num, end = start;
		for(int i = colId + num; i < seats[0].length; i++) {
			if (seats[rowId][i] == 'e') {
				end = i;
			}
		}
		if (end > start) {
			int[] newConSeats = {rowId, start, end - start + 1};
			continuousSeats.add(newConSeats);
		}
		
		// return filled seats
		return res;
	}
	
	/**
	 * fill unsafe seats to 'u'. We don't update pq here. it would be very complicated. pq will be updated when we get continuous seats from pq
	 * @param seatNums
	 */
	private void fillUnsafeSeats(String[] seatNums) {
		for(int i = 0; i < seatNums.length; i++) {
			String seat = seatNums[i];
			int rowId = seat.charAt(0) - 'A';
			int colId = Integer.parseInt(seat.substring(1)) - 1;
			if (rowId - 1 >= 0 && seats[rowId-1][colId] == 'e') { 			// previous row
				seats[rowId-1][colId] = 'u';
				remainingSeats--;
			}
			if (rowId + 1 < seats.length && seats[rowId+1][colId] == 'e') {	// next row
				seats[rowId+1][colId] = 'u';
				remainingSeats--;
			}
			for(int j = 1; j <= rowUnsafeRange; j++) {
				if (colId - j >= 0 && seats[rowId][colId-j] == 'e') {		// left 3 seats
					seats[rowId][colId-j] = 'u';
					remainingSeats--;
				}
				if (colId + j < seats[0].length && seats[rowId][colId+j] == 'e') {// right 3 seats
					seats[rowId][colId+j] = 'u';
					remainingSeats--;
				}				
			}
		}
	}
	
	/**
	 * get the largest available seats.
	 * if find, return the largest available continuous seats and pop it from priority queue; else return null
	 * @param seatsRequested
	 * @return
	 */
	private int[] getLargestAvailableSeats() {
		while(!continuousSeats.isEmpty()) {
			int[] ss = continuousSeats.poll();
			int rowId = ss[0], colId = ss[1], num = ss[2];
			int start = colId;
			for(int i = 0; i < num; i++) {
				if (seats[rowId][colId+i] != 'e') { // need to update continuous seats 
					if (colId + i - start > 0) {
						int[] newConSeats = {rowId, start, colId + i - start};
						continuousSeats.add(newConSeats);
					}
					start = colId + i + 1;
				}
			}
			if (start == colId) { // this continuous seats are still available and safe
				return ss;
			} else if (colId + num - start > 0) {	// continue updating continuous seats
				int[] newConSeats = {rowId, start, colId + num - start};
				continuousSeats.add(newConSeats);				
			}
		}
		return null;
	}
	
	/**
	 * try to purchase, return seats. 
	 * @param seatsRequested
	 * @return String[]
	 */
	private String[] tryToPurchase(int seatsRequested) {
//		System.out.println("tryToPurchase " + seatsRequested);

		// corner case
		String[] res = new String[seatsRequested];
		if (seatsRequested <= 0) {
			return res;
		}
		
		int[] ss = getLargestAvailableSeats();
		if (ss == null) { // double check
			return null;
		}
		if (seatsRequested >= ss[2]) { // if seatsRequested >= largest available continuous seats, fill seats, recursively call this function
			String[] res1 = fillSeats(ss[0], ss[1], ss[2]);
			String[] res2 = tryToPurchase(seatsRequested - ss[2]);
			for(int i = 0; i < res1.length; i++) {
				res[i] = res1[i];
			}
			for(int i = 0; i < res2.length; i++) {
				res[res1.length + i] = res2[i];
			}
			return res;
		} else { 						// if seatsRequested < largest available continuous seats, find the least available continuous seats
			// put continuous seats >= seatsRequested to largeSeats
			List<int[]> largeSeats = new ArrayList<>();
			// find the least available continuous seats
			largeSeats.add(ss);
			while(!continuousSeats.isEmpty()) {
				ss = getLargestAvailableSeats();
				if (ss != null && ss[2] < seatsRequested) {
					continuousSeats.add(ss);
					break;
				}
				if (ss != null) {
					largeSeats.add(ss);
				}
			}
			// if several continuous seats have the same least seats number, use the one with the smallest rowId
			ss = largeSeats.remove(largeSeats.size()-1);
			while(!largeSeats.isEmpty()) {
				int[] prevSs = largeSeats.get(largeSeats.size()-1);
				if (ss[2] == prevSs[2]) {
					largeSeats.remove(largeSeats.size()-1);
					continuousSeats.add(ss);
					ss = prevSs;
				} else {
					break;
				}
			}
			res = fillSeats(ss[0], ss[1], seatsRequested);
			
			// put large seats back to pq
			for(int i = largeSeats.size()-1; i >= 0; i--) {
				continuousSeats.add(largeSeats.get(i));
			}
		}

		return res;
	}
	
	/**
	 * purchase the order.
	 * return order number and seats if we can sell; return null if we cannot sell
	 * @param order in this format "R001 2"
	 * @return String | null 
	 */
	public String purchase(String order) {
		// parse order
		String[] orderParts = order.split(" ");
		if (orderParts.length < 2) {
			System.out.println("invalid order [" + order + "]");
			return null;
		}
		String orderNum = orderParts[0];
		int seatsRequested = Integer.parseInt(orderParts[1]);
		
		// purchase
		if (seatsRequested > remainingSeats) {
			return null;
		}
		String[] res = tryToPurchase(seatsRequested);
		fillUnsafeSeats(res);
		printSeats();
		return orderNum + " " + String.join(",", res);
	}
	
	/**
	 * helper function for debug
	 */
	private void printSeats() {
		System.out.println("======== theater =========");
		for(int i = 0; i < seats.length; i++) {
			for(int j = 0; j < seats[0].length; j++) {
				System.out.print(seats[i][j]);
			}
			System.out.println();
		}
		System.out.println("======== continuousSeats =========");
		List<int[]> list = new ArrayList<>(continuousSeats);
		for(int i = 0; i < list.size(); i++) {
			int[] array = list.get(i);
			for(int j = 0; j < array.length; j++) {
				System.out.print(array[j] + ",");
			}
			System.out.println();
		}
		System.out.println("remaining safe seats are " + remainingSeats);
	}
	
	public static void main(String[] args) {
		MainSeating seat = new MainSeating(10, 20);
		
		try {
			// read file
			String file = "/Users/mengjiakong/eclipse-workspace/MovieTheaterSeatingChallenge2020/src/MovieTheaterSeating/input.txt";
			if (args.length > 0) {
		        file = args[0];
			}
			System.out.println("input file is " + file);
	        BufferedReader bufferedReader = new BufferedReader(new FileReader(file));
	        
	        // get path name. put output file to the same path
	        int lastSlash = file.lastIndexOf('/');
	        String folder = file.substring(0, lastSlash+1);
	        
	        // get output file
	        String outputFileName = folder + "output.txt";
	        System.out.println("output file is " + file);
	        BufferedWriter writer = new BufferedWriter(new FileWriter(outputFileName));

	        // read input file
	        String data = null;
	        while((data = bufferedReader.readLine()) != null) {
	    		System.out.println("=====================================");

	        	System.out.println("order is: " + data);
	        	String resSeats = seat.purchase(data);
	        	
		        // write to output file
	        	if (resSeats != null) {
	        		writer.write(resSeats + '\n');
	        	} else {
	        		System.out.println("Attention! no seats available for " + data);
	        	}
        		System.out.println(resSeats);
	    		System.out.println("=====================================");
	        }
	        
	        bufferedReader.close();	        
	        writer.close();
		}
		catch (Exception ex) {
			ex.printStackTrace();
		}

	}
}



