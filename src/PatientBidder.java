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
import ontology.YouWon;

public class PatientBidder extends Agent {
	
	AID auctioneer; //Agent-leading
	Good initial_good;
	Good actual_good;
	int available_price = 7;
	int patience = 2;
	boolean stop = false;
	
	
	private Codec codec = new SLCodec();
	private Ontology ontology = DutchOntology.getInstance();
	
	protected void setup(){
		
		//Finding auctioneer
		auctioneer = new AID("a1", AID.ISLOCALNAME);
		
		this.getContentManager().registerLanguage(codec);
		this.getContentManager().registerOntology(ontology);
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
		  							((NewGood)action).getGoodReservePrice());
		  					actual_good = new Good(((NewGood)action).getGoodName(), ((NewGood)action).getGoodPrice(),
		  							((NewGood)action).getGoodReservePrice());
		  					System.out.println(getLocalName() + ": Message 'Recebei novo leilão. "+ actual_good.getName() +" por "+ 
		  							actual_good.getPrice());
		  				} else if ( action instanceof NewPrice) {
		  					actual_good = new Good(((NewPrice)action).getGoodName(), ((NewPrice)action).getGoodPrice(), 
		  							((NewPrice)action).getGoodReservePrice());
		  					System.out.println(getLocalName() + ": Message 'Recebei novo preço. "+ actual_good.getName() +" por "+ 
		  							actual_good.getPrice());
		  					if(actual_good.getPrice() < available_price){
		  						patience= patience-1;
		  						System.out.println(getLocalName() + ": Message 'Vou esperar mais "+ patience +" turnos ");
		  					}
		  				}
		  				else if ( action instanceof YouWon) {
		  					System.out.println(getLocalName() + ": Message 'Ganhei! "+ actual_good.getName() +" por "+ 
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
		  				//fazer para o caso de alguém o ter comprado

		  				if(actual_good.getPrice() <= available_price &&
		  						patience == 0){
		  					ACLMessage answermsg = new ACLMessage(ACLMessage.INFORM);
		  					answermsg.setLanguage(codec.getName());
		  					answermsg.setOntology(ontology.getName());
		  					ContentManager cm = getContentManager();
		  					IBuy buy = new IBuy();
		  					buy.setGoodName(actual_good.getName());
		  					buy.setGoodPrice(actual_good.getPrice());
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