import java.util.ArrayList;
import java.util.Vector;

import ontology.DutchOntology;
import ontology.Good;
import ontology.IBuy;
import ontology.NewGood;
import ontology.NewPrice;
import ontology.Sold;
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
import jade.core.behaviours.Behaviour;
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
	ArrayList<AMSAgentDescription> agents = null;
	private AID winner = null;  
	boolean finnish = false;
	private Good good; 
	private int dif = 1;
	boolean stop = false;

	protected void setup() {
		manager.registerLanguage(codec);
		manager.registerOntology(ontology);
		System.out.println("Lançou o Auctioneer");
		findBidders(); 
		//sendStartAuctionMessage(); 
		ACLMessage msg = new ACLMessage(ACLMessage.CFP);
		addBehaviour(new DutchAuctionBehaviour(this));

		//Item para o leilão
		good = new Good("airplane-ticket",10);



	} 

	private int findBidders() {
		//Procura todos os agentes
		//http://jade.17737.x6.nabble.com/newbie-question-how-to-get-all-agents-td4705860.html

		try {
			agents = new ArrayList<AMSAgentDescription>();
			SearchConstraints c = new SearchConstraints();
			c.setMaxResults (new Long(-1));
			AMSAgentDescription [] allagents = null;
			allagents = AMSService.search( this, new AMSAgentDescription(), c );

			System.out.println("Agentes encontrados inicialmente: ");
			for (AMSAgentDescription a: allagents){
				System.out.println(a.getName());
			}

			for (AMSAgentDescription a: allagents){
				if(a.getName().getLocalName().equals("ams") 
						|| a.getName().getLocalName().equals("df")
						|| a.getName().getLocalName().equals("rma") 
						|| a.getName().getLocalName().equals(getLocalName()))
				{//Não faz nada
				}
				else{
					agents.add(a);
				}
			}

			System.out.println("Agentes guardados: ");
			for (AMSAgentDescription a: agents)
				System.out.println(a.getName());
		}
		catch (Exception e) {
			System.out.println( "Problem searching AMS: " + e );
			e.printStackTrace();
		}
		return agents.size();
	}

	class DutchAuctionBehaviour extends Behaviour{
		public DutchAuctionBehaviour(Agent a){
			super(a);
		}
		public void action(){
			//a cada iteração procura se há mais bidders
			//Se não houver, faz um sleep de 5 segundos e volta a procurar
			
//			if (findBidders() == 0)
//				try {
//					Thread.currentThread().sleep(5000);
//				} catch (InterruptedException e1) {
//					// TODO Auto-generated catch block
//					e1.printStackTrace();
//				}
			switch (state) {
			case 0: { //Inicia um novo leilão
				
				//Só envia a mensagem se tiver encontrado agentes
				if(agents.size() > 0){
					for(int i = 0; i< agents.size(); i++)
						sendMessage(agents.get(i).getName(), 0);
				}
				ACLMessage bid = blockingReceive(5000);
				if(bid == null) {
					//actualiza o preço
					good.setPrice(good.getPrice()-dif);
					System.out.println("Auctioneer a actualizar o preço para "+good.getPrice());					
					//e passa para o próximo estado (que vai ser enviar um NewPrice para os bidders)
					state = 1;

				} else {
					try {
						ContentElement content = getContentManager().extractContent(bid);
						Concept action = ((Action)content).getAction();
						if(action instanceof IBuy){
							//recebeu mensagem informando que alguém comprou o item
							if(((IBuy)action).getGoodName().equals(good.getName())){
								//envia mensagem para o comprador a informar que ganhou
								sendMessage(bid.getSender(), 1);

								if(agents.size() >0)
									//envia para todos os outros a avisar que o item foi vendido
									for(int i=0; i < agents.size(); i++){
										if(!agents.get(i).getName().getLocalName().equals(bid.getSender())){
											sendMessage(agents.get(i).getName(), 2);
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
			case 1:{ //Novo Preço
				System.out.println("Novo preço do item: " + good.getName() + "\n Alterado para: "+good.getPrice());
				if(agents.size() >0)
					for(int i=0; i<agents.size();i++)
						//envia com o New Price
						sendMessage(agents.get(i).getName(), 3);

				ACLMessage bid = blockingReceive(5000);
				if(bid != null) {
					/*
					 * Se recebe resposta, é porque alguém quer comprar 
					 */
					sendMessage(bid.getSender(), 1);
					//Envia para os outros a avisar que o Item foi comprado
					for(int i=0; i < agents.size(); i++){
						if(!agents.get(i).getName().getLocalName().equals(bid.getSender())){
							sendMessage(agents.get(i).getName(), 2);
						}
						
					}
					/*se houver mais items
					state = 0; //Switching to new state
					*/
					stop = true;
				}
				else
					good.setPrice(good.getPrice()-dif);

				break;
			}

			}

		}
		public boolean done(){
				return stop;
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
				//  Envia mensagem a informar o início de um novo leilão
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
				System.out.println(getLocalName() + ": MESSAGEM 'Parabéns! Comprou: "+ good.getName() +" por "
						+ good.getPrice() + "' para " + agent.getLocalName());
				break;
			}
			case 2:{ //Envia mensagem a informar que o item foi vendido
				Sold sold = new Sold();
				sold.setGoodName(good.getName());
				sold.setGoodPrice(good.getPrice());
				Action a = new Action(agent, sold); 
				cm.fillContent(msg, a);
				System.out.println(getLocalName() + ": MESSAGEM 'Vendido: "+ good.getName() +" por "
						+ good.getPrice() + "' para " + agent.getLocalName());
				break;
			}
			case 3:{ // Envia mensagem a informar que o há um novo preço
				NewPrice novo = new NewPrice();
				novo.setGoodName(good.getName());
				novo.setGoodPrice(good.getPrice());
				Action a = new Action(agent, novo); 
				cm.fillContent(msg, a);
				System.out.println(getLocalName() + ": MESSAGEM 'Novo Preço: "+ good.getName() +" por "
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
