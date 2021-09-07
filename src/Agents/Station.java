package Agents;

import Classes.InformStationState;
import Classes.InformUserState;
import Classes.Position;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.core.behaviours.TickerBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.UnreadableException;

import static java.lang.Math.sqrt;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Station extends Agent {

    private int maxCapacity;
    private int currentCapacity;
    private int totalUsers;

    private InformStationState info;

    /* 1 -> baixo; 2 -> medio; 3 -> alto */
    private int fill_level;

    /* raio da area de proximidade */
    private double proximityRadius;

    /* coordenadas da estacao */
    private Position pos;

    /* descontos aceites/recusados */
    private int acceptedDiscounts;
    private int deniedDiscounts;
    private List<AID> deniedUsers;

    private int[] discounts = new int[2];
    private String LocalName = "";

    @Override
    protected void setup() {
        deniedUsers = new ArrayList<>();
        maxCapacity = 12;
        currentCapacity = 8;
        updateLevel(currentCapacity, maxCapacity);
        proximityRadius = 2500;
        LocalName = this.getLocalName();

        pos = new Position();

        // diferentes tipos de desconto:
        discounts[0] = 80;
        discounts[1] = 50;

        setPosition();

        DFAgentDescription dfd = new DFAgentDescription();
        dfd.setName(getAID());
        ServiceDescription sd = new ServiceDescription();
        sd.setType("Station");
        sd.setName("JADE-Station");
        dfd.addServices(sd);

        try {
            DFService.register(this, dfd);
        } catch (FIPAException e) {
            e.printStackTrace();
        }

        /* Recebe mensagens (do utilizador ou do Manager) e atualiza os seus dados */
        this.addBehaviour(new receiveMsgs());
        /* de 500ms em 500ms, envia os seus dados para o Agente Interface */
        this.addBehaviour(new sendStatusInterface(this, 500));
    }

    /* atualizar variavel fill_level conforme a capacidade max e atual */
    public void updateLevel(int s, int c) {
        double pct = ((double) s / c) * 100;

        if (pct <= 34)
            fill_level = 1;
        else if (pct >= 75)
            fill_level = 3;
        else
            fill_level = 2;
    }


    /* definir posicao conforme o nome do agente, este nome é definido no maincontainer */
    private void setPosition() {
        switch (this.getLocalName()) {
            case "Station1":
                pos.setX(5500);
                pos.setY(2000);
                break;

            case "Station2":
                pos.setX(6000);
                pos.setY(3500);
                break;

            case "Station3":
                pos.setX(4000);
                pos.setY(4000);
                break;

            case "Station4":
                pos.setX(2500);
                pos.setY(3500);
                break;

            case "Station5":
                pos.setX(3000);
                pos.setY(2000);
                break;
        }
    }

    private class sendStatusInterface extends TickerBehaviour {

        public sendStatusInterface(Agent t, long time) {
            super(t, time);
        }

        @Override
        protected void onTick() {
            AID dest = new AID("InterfaceAgent", AID.ISLOCALNAME);
            ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
            msg.addReceiver(dest);
            String cd = pos.getX() + ":" + pos.getY();
            info = new InformStationState(LocalName, cd, currentCapacity, maxCapacity, totalUsers, acceptedDiscounts,deniedDiscounts);
            try {
                msg.setContentObject(info);
            } catch (IOException e) {
                e.printStackTrace();
            }
            send(msg);
        }
    }


    /* Recebe mensagens (do utilizador ou do gestor) e atualiza os seus dados */
    private class receiveMsgs extends CyclicBehaviour {
        @Override
        public void action() {

            ACLMessage msg = receive();
            String content;

            if (msg != null) {
                //16 -> REQUEST -> para saber se pode levantar bike da station
                if(msg.getPerformative() == 16){
                    if (currentCapacity == 0) {
                        ACLMessage msgR = new ACLMessage(ACLMessage.INFORM);
                        msgR.addReceiver(msg.getSender());
                        msgR.setContent("NoBikesAvailable");
                        send(msgR);
                    } else {
                        currentCapacity--;
                        totalUsers++;
                    }
                    updateLevel(currentCapacity, maxCapacity);
                }
                // 7-> INFORM
                if (msg.getPerformative() == 7) {

                    content = msg.getOntology();

                    if (content.equals("UpdatingPos")) {
                        // Se tem um UpdatingPos no inicio é pq é msg do utilizador com atualizacao da posicao

                        try {
                            InformUserState message = (InformUserState) msg.getContentObject();
                            int posX = message.getActual().getX();
                            int posY = message.getActual().getY();
                            double h = (posX - pos.getX()) * (posX - pos.getX()) + (posY - pos.getY()) * (posY - pos.getY());
                            double dist = sqrt(h);

                            if (dist < proximityRadius && message.getFinalStation()!=LocalName && message.getInitStation()!=LocalName) {
                                ACLMessage proposta = new ACLMessage(ACLMessage.CFP);
                                proposta.addReceiver(msg.getSender());
                                int desconto = calcula(dist); // x;y
                                proposta.setContent(desconto + ";" + pos.getX() + ";" + pos.getY());
                                send(proposta);
                            }
                        } catch (UnreadableException e) {
                            e.printStackTrace();
                        }
                    }
                    if (content.equals("ReturnBike")){
                        currentCapacity++;
                        updateLevel(currentCapacity, maxCapacity);
                    }
                } else {
                    /* Aceitou proposta */
                    if (msg.getPerformative() == 0) {
                        acceptedDiscounts++;
                    }
                    /* Recusou proposta */
                    if (msg.getPerformative() == 14) {
                        if (!deniedUsers.contains(msg.getSender())) {
                            deniedDiscounts++;
                            deniedUsers.add(msg.getSender());
                        }
                    }
                }
            }
        }
    }

    public int calcula(double dist) {
        /* nesta funçao deve-se decidir as diferentes descontos a dar*/
        if (fill_level == 1)
            return discounts[0];
        if (fill_level == 2)
            return discounts[1];
        return 0;
    }

    @Override
    protected void takeDown() {

        super.takeDown();
    }

}
