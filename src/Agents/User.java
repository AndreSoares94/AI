package Agents;

import Classes.InformUserState;
import Classes.Position;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.*;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;

import java.io.IOException;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

import static java.lang.Math.sqrt;

public class User extends Agent {

    /* Posicao */
    private Position pos;
    private Position curr;

    /* Estacao inicial/Estacao final */
    private String initStation;
    private String finalStation;

    private InformUserState infoUpdate;

    /* Coordenadas da estacao final */
    private Position finalStationPos;

    /* usado para "andar"/ver se ja andou 3/4 do caminho */
    private double three_quarters = 0;
    private double distInit = 0;

    /*String usada para tomar decisoes, toma valor de "SIM" ou "NAO" para informar a estaçao se aceitamos ou nao o desconto*/
    private String decision;

    /* flag que diz se user ja aceitou um desconto ou nao */
    private int flag = 0;

    /* Array com todas as estacoes -  usado para decidir uma aleatoria inicial e final */
    private AID[] Stations;

    @Override
    protected void setup() {

        /* Registo nas paginas amarelas: */
        DFAgentDescription dfd = new DFAgentDescription();
        dfd.setName(getAID());
        ServiceDescription sd = new ServiceDescription();
        sd.setType("User");
        sd.setName("JADE-Utilizador");
        dfd.addServices(sd);

        try {
            DFService.register(this, dfd);
        } catch (FIPAException e) {
            e.printStackTrace();
        }

        pos = new Position();
        curr = new Position();

        finalStationPos = new Position();

        finalStation = null;

        /* Pesquisa as Stations que existem no sistema */
        this.addBehaviour(new searchStation());

        startStation();
        endStation();

        /* recebe informacao geral && propostas */
        this.addBehaviour(new receiveMsgs());

        /* muda de posicao e informa as estacoes de 0.5 em 0.5s */
        this.addBehaviour(new newPosition(this, 500));
        this.addBehaviour(new informPosition(this, 500));

    }


    /* procura stations e coloca na variavel Stations para depois inicializar o Utilizador numas delas
     *  na funçao startStation e definir uma de destino na funçao endStation*/
    private class searchStation extends OneShotBehaviour {
        @Override
        public void action() {
            DFAgentDescription template = new DFAgentDescription();
            ServiceDescription sd = new ServiceDescription();
            sd.setType("Station");
            template.addServices(sd);

            try {
                DFAgentDescription[] result = DFService.search(myAgent, template);
                Stations = new AID[result.length];
                for (int i = 0; i < result.length; i++) {
                    Stations[i] = result[i].getName();
                }
            } catch (FIPAException fe) {
                fe.printStackTrace();
            }
        }
    }

    /* funçao que coloca o utilizador numa estaçao aleatoria */
    private void startStation() {
        int randomNum = ThreadLocalRandom.current().nextInt(0, 5);
        //randomNum = 4;
        switch (randomNum) {
            case 0:
                pos.setX(5500);
                pos.setY(2000);
                curr.setX(5000);
                curr.setY(2000);
                initStation = "Station1";
                break;

            case 1:
                pos.setX(6000);
                pos.setY(3500);
                curr.setX(6000);
                curr.setY(3500);
                initStation = "Station2";
                break;

            case 2:
                pos.setX(4000);
                pos.setY(4000);
                curr.setX(4000);
                curr.setY(4000);
                initStation = "Station3";
                break;

            case 3:
                pos.setX(2500);
                pos.setY(3500);
                curr.setX(2500);
                curr.setY(3500);
                initStation = "Station4";
                break;

            case 4:
                pos.setX(3000);
                pos.setY(2000);
                curr.setX(3000);
                curr.setY(2000);
                initStation = "Station5";
                break;
        }
        AID dest = new AID(initStation, AID.ISLOCALNAME);
        ACLMessage msg = new ACLMessage(ACLMessage.REQUEST);
        msg.addReceiver(dest);
        msg.setContent("RequestingBike");
        send(msg);
    }

    /* funçao que define a estacao final aleatoriamente */
    private void endStation() {
        while (finalStation == null) {
            int randomNum = ThreadLocalRandom.current().nextInt(0, 5);
            int aux = randomNum + 1;
            //randomNum = 3;
            if (!initStation.equals("Station" + aux)) {
                switch (randomNum ) {
                    case 0:
                        finalStationPos.setX(5500);
                        finalStationPos.setY(2000);
                        finalStation = "Station1";
                        break;

                    case 1:
                        finalStationPos.setX(6000);
                        finalStationPos.setY(3500);
                        finalStation = "Station2";
                        break;

                    case 2:
                        finalStationPos.setX(4000);
                        finalStationPos.setY(4000);
                        finalStation = "Station3";
                        break;

                    case 3:
                        finalStationPos.setX(2500);
                        finalStationPos.setY(3500);
                        finalStation = "Station4";
                        break;

                    case 4:
                        finalStationPos.setX(2500);
                        finalStationPos.setY(2000);
                        finalStation = "Station5";
                        break;
                }
            }
        }
        /*
        AID dest = new AID(finalStation, AID.ISLOCALNAME);
        ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
        msg.addReceiver(dest);
        msg.setContent("RequestingDestination");
        send(msg);*/
    }

    /* recebe informacao geral && propostas */
    private class receiveMsgs extends CyclicBehaviour {
        @Override
        public void action() {
            // TODO Auto-generated method stub
            ACLMessage msg = myAgent.receive();

            if (msg != null) {
                /* quando recebe um INFORM basicamente é pq requisitou bike mas nao ha disponivel */
                if (msg.getPerformative() == 7) {
                    String msg_res = msg.getContent();
                    /* Quando nao ha bicicletas mata-se o user*/
                    if (msg_res.equals("NoBikesAvailable"))
                        myAgent.doDelete();
                }

                /* quando recebe CFP -> call for proposals -> inicia negociacoes entre agentes, isto acontece qd vai andando
                 * se aceitar manda um ACCEPT_PROPOSAL, caso contrario REFUSE, só pode acontecer quando estamos a 6/8 do caminho*/
                if (msg.getPerformative() == 3 && three_quarters >= (0.75*distInit)) {
                    /* se ainda n aceitou nenhum desconto aka flag!=1 */
                    if (flag != 1) {
                        String msg_res = msg.getContent();
                        String[] tokens = msg_res.split(";");

                        /* Tomar decisao conforme as coords da estacao e o desconto sugerido */
                        decision = chooseDecision(Integer.parseInt(tokens[1]), Integer.parseInt(tokens[2]), Integer.parseInt(tokens[0]));

                        if (decision.equals("SIM")) {
                            /* se escolhermos que de facto queremos mudar de posiçao atualizamos a nossa estacao final: */
                            updateFinalStation(msg.getSender().getLocalName(), Integer.parseInt(tokens[1]),
                                    Integer.parseInt(tokens[2]));
                            ACLMessage msg1 = new ACLMessage(ACLMessage.ACCEPT_PROPOSAL);
                            //System.out.println(getAID().getName() + " ACEITEI A PROPOSTA");
                            msg1.addReceiver(msg.getSender());
                            msg1.setContent(decision);
                            send(msg1);
                            flag = 1;
                        }

                        if (decision.equals("NAO")) {
                            ACLMessage msg1 = new ACLMessage(ACLMessage.REFUSE);
                            //System.out.println(getAID().getName() + " RECUSEI A PROPOSTA");
                            msg1.addReceiver(msg.getSender());
                            msg1.setContent(decision);
                            send(msg1);
                        }
                    }

                }
            } else {
                block();
            }
        }
    }



    private class newPosition extends TickerBehaviour {

        public newPosition(Agent t, long time) {
            super(t, time);
        }
        @Override
        protected void onTick() {
            // TODO Auto-generated method stub
            System.out.println("Sou o utilizador " + myAgent.getName() + " e estou na posicao " + curr.getX() + " : " + curr.getY() +
                " e vou para " + finalStationPos.getX() + " : " + finalStationPos.getY());

            if (finalStationPos.getY() > curr.getY()){
                int currY = curr.getY();
                curr.setY(currY+250);
            }
            if (finalStationPos.getY() < curr.getY()){
                int currY = curr.getY();
                curr.setY(currY-250);
            }
            if (finalStationPos.getX() > curr.getX()){
                int currX = curr.getX();
                curr.setX(currX+250);
            }
            if (finalStationPos.getX() < curr.getX()){
                int currX = curr.getX();
                curr.setX(currX-250);
            }

            if ((finalStationPos.getX() == curr.getX()) && (finalStationPos.getY() == curr.getY())) {
                returnBike();
                try {
                    DFService.deregister(myAgent);
                } catch (FIPAException e) {
                    e.printStackTrace();
                }
                myAgent.doDelete();
            }
            distInit = sqrt((finalStationPos.getX() - pos.getX())*(finalStationPos.getX() - pos.getX()) +
                    (finalStationPos.getY() - pos.getY())*(finalStationPos.getY() - pos.getY()));
            three_quarters = distInit - sqrt((finalStationPos.getX() - curr.getX())*(finalStationPos.getX() - curr.getX()) +
                    (finalStationPos.getY() - curr.getY())*(finalStationPos.getY() - curr.getY()));
            }
    }

    public void returnBike() {
        AID dest = new AID(finalStation, AID.ISLOCALNAME);
        ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
        System.out.println(getAID().getName() + " DEVOLVEU A BICICLETA");
        msg.addReceiver(dest);
        msg.setOntology("ReturnBike");
        send(msg);
    }

    private class informPosition extends TickerBehaviour {
        public informPosition(Agent t, long time) {
            super(t, time);
        }
        protected void onTick() {
            int j = 0;
            for (j = 0; j < Stations.length; j++) {
                ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
                msg.addReceiver(Stations[j]);
                // msg do tipo: UpdatingPos
                msg.setOntology("UpdatingPos");
                infoUpdate = new InformUserState(curr,initStation,finalStation);
                try {
                    msg.setContentObject(infoUpdate);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                send(msg);
            }
        }
    }


    public String chooseDecision(int destino_novo_X, int destino_novo_Y, int desconto) {
        // calcular distancia entre local atual e estacao que está a propor o desconto:
        /*
        double new1 = destino_novo_X - curr.getX();
        double new2 = destino_novo_Y - curr.getY();
        double new_distance = sqrt(new1 * new1 + new2 * new2);

        double old1 = finalStationX - curr.getX();
        double old2 = finalStationY - curr.getY();
        double old_distance = sqrt(old1 * old1 + old2 * old2);*/

        if(desconto >=75) return "SIM";
        if(desconto >=50){
            int randomNum = ThreadLocalRandom.current().nextInt(0, 3);
            if (randomNum == 1 || randomNum == 2) return "SIM";
            else return "NAO";
        }
        return "NAO";
    }


    public void updateFinalStation(String nome, int x, int y) {
        finalStation = nome;
        finalStationPos.setX(x);
        finalStationPos.setY(y);
    }
}