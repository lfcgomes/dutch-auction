package ontology;
public class Good {
	private String name;
	private int price;
	
	public Good(String name, int price){
		this.name = name;
		this.price = price;
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
	
	public void setResPrice(int price){
		this.price = this.price - price;
	}
}