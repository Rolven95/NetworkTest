package original;

import java.util.ArrayList;

public class History {
		public int id = 0;
    	ArrayList <unicast_packet> sent_history = new ArrayList<unicast_packet>(); 
    	ArrayList <unicast_packet> ACK_history = new ArrayList<unicast_packet>(); 
    	ArrayList <unicast_packet> oneWay_history = new ArrayList<unicast_packet>(); 
    	//ArrayList<Integer> list1=new ArrayList<Integer>()
    	
    	public void incID(int i) {
    		this.id ++ ;
    	}
    	//public 
    	
    	
    	
    	
    	public synchronized void insert_sent(unicast_packet to_insert ) {
    		sent_history.add(to_insert);
    	}
    	
    	public synchronized void insert_ACK(unicast_packet to_insert ) {	
    		ACK_history.add(to_insert);
    	}
    	
    	
    	public synchronized void insert_oneWayHistory(unicast_packet to_insert ) {	
    		oneWay_history.add(to_insert);
    	}
    	
    	public synchronized void clearOneWayHistory() {	
    		oneWay_history.clear();
    		
    	}
    	
    	
    	public synchronized void clearSentHistory() {	
    		sent_history.clear();
    		
    	}
    	public synchronized void clearACKHistory() {	
    		ACK_history.clear();
    		
    	}
    	
    	public float getLossRate () {
    		//float result; 
    		int total = sent_history.size();
    		int received = ACK_history.size(); 
    		//result = (total - received)/total; 
    		//return (double)( (double)sent_history.size() - (double)ACK_history.size()) / (double)sent_history.size(); 
    	 return (float) (total - received)/total;
    	}
    	
    	public float getAverageRTT () {
    		float totalRTT = 0 ; 
    		for(int i = 0 ; i < this.sent_history.size() ; i++) {
    			for (int q = 0 ; q < this.ACK_history.size() ; q++) {
    				if( this.sent_history.get(i).getSeq() == this.ACK_history.get(q).getSeq() ) {
    					
    					totalRTT +=  this.ACK_history.get(q).getArrival() 
    								- this.ACK_history.get(q).getProcessing_cost() 
    								- this.ACK_history.get(q).getdeparture();
    				}
    			}
    		}
    		return (float)totalRTT/this.ACK_history.size(); 
    	}
    	
    	public float getRTTVariance () {
    		float averageRTT = this.getAverageRTT(); 
    		float bigfuck = 0;
    		for(int i = 0 ; i < this.sent_history.size() ; i++) {
    			for (int q = 0 ; q < this.ACK_history.size() ; q++) {
    				if( this.sent_history.get(i).getSeq() == this.ACK_history.get(q).getSeq() ) {
    					
    					bigfuck +=  Math.pow((this.ACK_history.get(q).getArrival() 
    							- this.ACK_history.get(q).getProcessing_cost() 
    							- this.ACK_history.get(q).getdeparture()-averageRTT),2);
    				}
    			}
    		}
    		return bigfuck/(this.ACK_history.size()-1); 
    	}
    
}
