import jade.core.Agent;
import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.core.Runtime;
import jade.wrapper.AgentController;
import jade.wrapper.ContainerController;
import jade.wrapper.StaleProxyException;

public class JADELauncher {

	public static void main(String[] args) {
		Runtime rt = Runtime.instance();

		Profile p1 = new ProfileImpl();
		p1.setParameter("host","127.0.0.1");
		ContainerController mainContainer = rt.createMainContainer(p1);
		
		Profile p2 = new ProfileImpl();
		//p2.setParameter(...);
		ContainerController container = rt.createAgentContainer(p2);

		Object[] agentArgs = new Object[0];
		AgentController ac2;
		try {
			ac2 = container.createNewAgent("p1", "PatientBidder", agentArgs);
			ac2.start();
		} catch (StaleProxyException e) {
			e.printStackTrace();
		}
		
		Object[] agentArgs1 = new Object[0];
		AgentController ac4;
		try {
			ac4 = container.createNewAgent("r1", "RapidBidder", agentArgs1);
			ac4.start();
		} catch (StaleProxyException e) {
			e.printStackTrace();
		}
		
		Object[] agentArgs2 = new Object[0];
		AgentController ac5;
		try {
			ac5 = container.createNewAgent("a1", "Auctioneer", agentArgs2);
			ac5.start();
		} catch (StaleProxyException e) {
			e.printStackTrace();
		}
		
//		Object[] agentArgs3 = new Object[0];
//		AgentController ac6;
//		try {
//			ac6 = container.createNewAgent("p2", "PatientBidder", agentArgs3);
//			ac6.start();
//		} catch (StaleProxyException e) {
//			e.printStackTrace();
//		}
	}

}
