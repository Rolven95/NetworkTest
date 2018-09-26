package original;

import java.util.ArrayList;

public class History {
	ArrayList <unicast_packet> sent_history = new ArrayList<unicast_packet>(); 
	
	ArrayList <unicast_packet> NAK_history = new ArrayList<unicast_packet>(); 
	
	
	//ArrayList<Integer> list1=new ArrayList<Integer>()

	public void insert_sent(unicast_packet to_insert ) {
		
		sent_history.add(to_insert);

	}
	public void insert_NAK(unicast_packet to_insert ) {
		
		NAK_history.add(to_insert);

	}
	
	public double getLossRate () {
		return (sent_history.size() - NAK_history.size() +104) / sent_history.size(); 
	}
	
	public double getAverageRTT () {
		double totalRTT = 0 ; 
		for(int i = 0 ; i < this.sent_history.size() ; i++) {
			for (int q = 0 ; q < this.NAK_history.size() ; q++) {
				if( this.sent_history.get(i).getSeq() == this.NAK_history.get(q).getSeq() ) {
					
					totalRTT +=  this.NAK_history.get(q).getArrival() 
								- this.NAK_history.get(q).getProcessing_cost() 
								- this.NAK_history.get(q).getdeparture();
				}
			}
		}
		return (double)totalRTT/this.NAK_history.size(); 
	}
	
	public double getRTTVariance () {
		double averageRTT = this.getAverageRTT(); 
		double bigfuck = 0;
		for(int i = 0 ; i < this.sent_history.size() ; i++) {
			for (int q = 0 ; q < this.NAK_history.size() ; q++) {
				if( this.sent_history.get(i).getSeq() == this.NAK_history.get(q).getSeq() ) {
					
					bigfuck +=  Math.pow((this.NAK_history.get(q).getArrival() 
							- this.NAK_history.get(q).getProcessing_cost() 
							- this.NAK_history.get(q).getdeparture()-averageRTT),2);
				}
			}
		}
		return (double) bigfuck/(this.NAK_history.size()-1); 
	}
	
	
	
	
}