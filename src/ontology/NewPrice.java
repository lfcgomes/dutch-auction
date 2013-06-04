package ontology;

import jade.content.AgentAction;

public class NewPrice implements AgentAction {
	private String goodname;
	private int goodprice;
	private int goodreserveprice;
	private int qty;
	
	public int getQty()
	{
		return qty;
	}
	public void setQty(int newqty){
		this.qty = newqty;
	}
	
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

	public int getGoodReservePrice() {
		return goodreserveprice;
	}

	public void setGoodReservePrice(int goodreserveprice) {
		this.goodreserveprice = goodreserveprice;
	}
}