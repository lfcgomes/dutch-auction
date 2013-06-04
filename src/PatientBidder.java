import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;

import jade.content.Concept;
import jade.content.ContentElement;
import jade.content.ContentManager;
import jade.content.lang.Codec;
import jade.content.lang.sl.SLCodec;
import jade.content.onto.Ontology;
import jade.content.onto.basic.Action;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.lang.acl.ACLMessage;

import ontology.DutchOntology;
import ontology.Good;
import ontology.IBuy;
import ontology.LowPrice;
import ontology.NewGood;
import ontology.NewPrice;
import ontology.PartialSell;
import ontology.YouBuy;

public class PatientBidder extends Agent {
	
	AID auctioneer; //Agent-leading
	Good initial_good;
	Good actual_good;
	int max_price = 7;
	int patience = 2;
	boolean stop = false;
	private int qty;
	
	
	private Codec codec = new SLCodec();
	private Ontology ontology = DutchOntology.getInstance();
	
	protected void setup(){
		
		//Finding auctioneer
		auctioneer = new AID("a1", AID.ISLOCALNAME);
		
		this.getContentManager().registerLanguage(codec);
		this.getContentManager().registerOntology(ontology);
		
		String filename = new String(getLocalName()+".txt");
		File f = new File(filename);
		try {
			FileInputStream fis = new FileInputStream(f);
			BufferedInputStream bis = new BufferedInputStream(fis);
			DataInputStream dis = new DataInputStream(bis);
			String record;
			int i=0;
			while ((record=dis.readLine()) != null ) {
				switch(i){
					case 0:{max_price = Integer.parseInt(record.substring(record.indexOf('=')+1, record.length()));
						break;
					}
					case 1:{qty = Integer.parseInt(record.substring(record.indexOf('=')+1, record.length()));
						break;
					}
					case 2:{patience = Integer.parseInt(record.substring(record.indexOf('=')+1, record.length()));
						break;
					}
				}
				i++;
			}
		} catch (Exception e) { 
				e.printStackTrace();
				System.exit(1);
		}
		
		/*
		for(int i=0; i <= goodsnumber; i++){
			System.out.println(getName() + ":" + goods[i].getName() + "=" + goods[i].getPrice());
		}
		*/
		addBehaviour(new BidderBehaviour(this));
		
	}
	
	class BidderBehaviour extends Behaviour{
		public BidderBehaviour(Agent a){
			super(a);
		}
		public void action(){
				ACLMessage msg = receive();
				if(msg != null){
					try {
         				ContentElement content = getContentManager().extractContent(msg);
         				Concept action = ((Action)content).getAction();
         				
		  				if (action instanceof NewGood){
		  					initial_good = new Good(((NewGood)action).getGoodName(), ((NewGood)action).getGoodPrice(),
		  							((NewGood)action).getGoodReservePrice(),((NewGood)action).getQty());
		  					actual_good = new Good(((NewGood)action).getGoodName(), ((NewGood)action).getGoodPrice(),
		  							((NewGood)action).getGoodReservePrice(),((NewGood)action).getQty());
		  					System.out.println(getLocalName() + ": Message 'Recebi novo leilão. "+ actual_good.getName() +" por "+ 
		  							actual_good.getPrice());
		  					
		  				} else if ( action instanceof NewPrice) {
		  					actual_good = new Good(((NewPrice)action).getGoodName(), ((NewPrice)action).getGoodPrice(), 
		  							((NewPrice)action).getGoodReservePrice(),((NewPrice)action).getQty());
		  					System.out.println(getLocalName() + ": Message 'Recebi novo preço. "+ actual_good.getName() +" por "+ 
		  							actual_good.getPrice());
		  					if(actual_good.getPrice() < max_price){
		  						patience= patience-1;
		  						System.out.println(getLocalName() + ": Message 'Vou esperar mais "+ patience +" turnos ");
		  					}
		  				}
		  				
		  				else if ( action instanceof YouBuy) {
		  					actual_good = new Good(((YouBuy)action).getGoodName(), ((YouBuy)action).getGoodPrice(),
		  							((YouBuy)action).getGoodReservePrice(),((YouBuy)action).getQty());
		  					System.out.println(getLocalName() + ": Message 'Comprei! "+((YouBuy)action).getQty()
			  							+" unidade(s) de "+ actual_good.getName() +" por "+ 
			  							actual_good.getPrice() + "' Enviado para " + msg.getSender().getLocalName());
		  					stop = true;
		  					return;
		  				}
		  				
		  				else if ( action instanceof LowPrice) {
		  					System.out.println(getLocalName() + ": Message 'Recebi o LowPrice "+ actual_good.getName() +" por "+ 
		  							actual_good.getPrice() + "' Enviado para " + msg.getSender().getLocalName());
		  					stop = true;
		  					return;
		  				}
		  				else if ( action instanceof PartialSell) {
		  					actual_good = new Good(((PartialSell)action).getGoodName(), ((PartialSell)action).getGoodPrice(), 
		  							((PartialSell)action).getGoodReservePrice(),((PartialSell)action).getQty());
		  					System.out.println(getLocalName() + ": Message 'Recebi o PartialSell: Só há " + 
		  				actual_good.getQty() + " unidade(s) de "+actual_good.getName() +" por "+ 
		  							actual_good.getPrice() + "' Enviado para " + msg.getSender().getLocalName());
		  				}
		  				//fazer para o caso de alguém o ter comprado

		  				if(actual_good.getPrice() <= max_price &&
		  						patience == 0){
		  					ACLMessage answermsg = new ACLMessage(ACLMessage.INFORM);
		  					answermsg.setLanguage(codec.getName());
		  					answermsg.setOntology(ontology.getName());
		  					ContentManager cm = getContentManager();
		  					IBuy buy = new IBuy();
		  					buy.setGoodName(actual_good.getName());
		  					buy.setGoodPrice(actual_good.getPrice());
		  					
		  					if(qty <= actual_good.getQty())
		  						buy.setQty(qty);
		  					else
		  						buy.setQty(actual_good.getQty());
		  					Action a = new Action(msg.getSender(), buy);
		  					cm.fillContent(answermsg, a);
		  					answermsg.addReceiver(msg.getSender());
		  					send(answermsg);
		  				}
		  				
		  				

        }
        catch(Exception ex) { ex.printStackTrace(); }
				}
		}
		
		@Override
		public boolean done(){
				return stop;
			}
		
	}	
	
}