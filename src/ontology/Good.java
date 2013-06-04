package ontology;
public class Good {
	private String name;
	private int price;
	private int reserve_price;
	private int qty;
	
	public Good(String name, int price, int reserve, int qty){
		this.name = name;
		this.price = price;
		this.reserve_price = reserve;
		this.qty = qty;
	}
	public int getQty()
	{
		return qty;
	}
	public void setQty(int newqty){
		this.qty = newqty;
	}
	public int getPrice(){
		return price;
	}
	
	public String getName(){
		return name;
	}
	
	public void setName(String name){
		this.name = name;
	}
	
	public void setPrice(int price){
		this.price = price;
	}

	public int getReserve_price() {
		return reserve_price;
	}

	public void setReserve_price(int reserve_price) {
		this.reserve_price = reserve_price;
	}
}