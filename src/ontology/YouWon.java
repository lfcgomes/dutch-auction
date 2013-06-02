package ontology;

import jade.content.AgentAction;

public class YouWon implements AgentAction {
	private String goodname;
	private int goodprice;
	private int goodreserveprice;
	
	public String getGoodName(){
		return goodname;
	}
	
	public void setGoodName(String goodname){
		this.goodname = goodname;
	}
	
	public int getGoodPrice() {
		return this.goodprice;
	}
	public void setGoodPrice(int goodprice) {
		this.goodprice = goodprice;
	}

	public int getGoodreserveprice() {
		return goodreserveprice;
	}

	public void setGoodreserveprice(int goodreserveprice) {
		this.goodreserveprice = goodreserveprice;
	}
}
