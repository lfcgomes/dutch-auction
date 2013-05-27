import java.util.Vector;

import ontology.DutchOntology;
import ontology.Good;
import ontology.IBuy;
import ontology.NewGood;
import ontology.YouWon;

import jade.content.Concept;
import jade.content.ContentElement;
import jade.content.ContentManager;
import jade.content.lang.Codec;
import jade.content.lang.Codec.CodecException;
import jade.content.lang.leap.LEAPCodec;
import jade.content.lang.sl.SLCodec;
import jade.content.onto.Ontology;
import jade.content.onto.OntologyException;
import jade.content.onto.basic.Action;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.domain.AMSService;
import jade.domain.FIPAAgentManagement.AMSAgentDescription;
import jade.domain.FIPAAgentManagement.SearchConstraints;
import jade.lang.acl.ACLMessage;


public class Auctioneer extends Agent{

	//private static final long serialVersionUID = -1702763678567916722L;
	private ContentManager manager = (ContentManager) getContentManager();
	private Codec codec = new SLCodec();
	private Ontology ontology = DutchOntology.getInstance();
	//private Integer currentPrice = 10; 
	private int state = 0;
	AMSAgentDescription [] agents = null; //All Bidders
	private AID winner = null;  
	boolean finnish = false;
	private Good good; 
	private int dif = 1;

	protected void setup() {
		manager.registerLanguage(codec);
		manager.registerOntology(ontology);
		
		init(); 
		//ou findBidders();
		//sendStartAuctionMessage(); 
		ACLMessage msg = new ACLMessage(ACLMessage.CFP);
		//addBehaviour(new InitiatorBehaviour(this, msg));
		
		//Item para o leilão
		good = new Good("airplane-ticket",10);
		
		
		
	} 

	private void init() {
		//Procura todos os agentes
		//http://jade.17737.x6.nabble.com/newbie-question-how-to-get-all-agents-td4705860.html
		
		try {
			SearchConstraints c = new SearchConstraints();
			c.setMaxResults (new Long(-1));
			agents = AMSService.search( this, new AMSAgentDescription(), c );
		}
		catch (Exception e) {
			System.out.println( "Problem searching AMS: " + e );
			e.printStackTrace();
		}
	}

	class DutchAuctionBehaviour extends CyclicBehaviour{
		public DutchAuctionBehaviour(Agent a){
			super(a);
		}
		public void action(){
			//findBidders();
			switch (state) {
				case 0: { //Starting new thing 
						 for(int i=0; i < agents.length; i++){
						 	if(agents[i].getName().getLocalName().equals("ams") || agents[i].getName().getLocalName().equals("df") || agents[i].getName().getLocalName().equals(getLocalName())){
						 		//Not sending info;
						 	} else {
						 		sendMessage(agents[i].getName(), 0);
						 	}
						 }
						 ACLMessage bid = blockingReceive(5000);
						 if(bid == null) {
							//actualiza o preço
							good.setResPrice(dif);
							//e passa para o próximo estado (que vai ser enviar um NewPrice para os bidders
						 	state = 1;

						 } else {
							 // Checking for preposition:
							 try {
								 ContentElement content = getContentManager().extractContent(bid);
								 Concept action = ((Action)content).getAction();
								 if(action instanceof IBuy){
									 if(((IBuy)action).getGoodName().equals(good.getName())){
										 //recebeu mensagem informando que alguém comprou o item

										 //envia mensagem para o comprador
										 sendMessage(bid.getSender(), 1);
										 
										 //envia para todos os outros a avisar que o item foi vendido
										 for(int i=0; i < agents.length; i++){
											 if(agents[i].getName().getLocalName().equals("ams") || agents[i].getName().getLocalName().equals("df") || agents[i].getName().getLocalName().equals(getLocalName()) || agents[i].getName().getLocalName().equals(bid.getSender())){
												 //Not sending loose-message;
											 } else {
												 sendMessage(agents[i].getName(), 2);

											 }
										 }
									 }
								 }
							 } catch (Exception e) {
								 e.printStackTrace();
						 	}

						 }

					break;
				}
				case 1://TODO estado para enviar um NewPrice a todos os Bidders 
					break;
				
				}
							
			}
		}	

	public void sendMessage(AID agent, int state) {
		ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
		msg.setLanguage(codec.getName());
		msg.setOntology(ontology.getName());

		try {
			ContentManager cm = getContentManager();
			// 0 - Inicia um novo leilão.
			switch (state) {
			case 0: {
				//  SEND THE MESSAGE, THA THE NEW AUCTION IS STARTED
				NewGood newgood = new NewGood();
				newgood.setGoodName(good.getName());
				newgood.setGoodPrice(good.getPrice());
				Action a = new Action(agent, newgood);
				cm.fillContent(msg, a);
				System.out.println(getLocalName() + ": MENSAGEM 'Novo Item "+ good.getName() +" por "+
											good.getPrice()+ "' enviado para " + agent.getLocalName());
				break;
			}
			case 1: { // Envia mensagem a dizer que o agente ganhou o leilão
				YouWon win = new YouWon();
				win.setGoodName(good.getName());
				win.setGoodPrice(good.getPrice());
				Action a = new Action(agent, win);
				cm.fillContent(msg, a);
				System.out.println(getLocalName() + ": MESSAGEM 'Compra: "+ good.getName() +" por "
				+ good.getPrice() + "' para " + agent.getLocalName());
				break;
			}
			
			}
			msg.addReceiver(agent);
			send(msg);
		} catch (OntologyException ex) {
			ex.printStackTrace();

		} catch (CodecException ce) {
			ce.printStackTrace();
		}
	}
}
