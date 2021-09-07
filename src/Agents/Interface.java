package Agents;

import Classes.InformStationState;
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
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;
import org.knowm.xchart.*;
import org.knowm.xchart.demo.charts.ExampleChart;
import org.knowm.xchart.demo.charts.pie.PieChart02;
import org.knowm.xchart.style.Styler;

import java.util.*;
import javax.swing.*;
import java.awt.*;
import java.time.LocalTime;
import java.util.Timer;
import java.util.TimerTask;

public class Interface extends Agent implements ExampleChart {

    private PieChart piechart;
    private CategoryChart bar_chart;
    private CategoryChart bar_chart_percentages;
    private int[] numberofUsers=new int[] {9,9,9,9,9};
    private Number[] percentageTotal=new Number[] {66,66,66,66,66};
    private String[] array = {"Repouso", "Andamento","Total"};
    private String[] array_stations = {"S1","S2","S3","S4","S5"};
    private Number[] barchart=new Number[] {40,0,40};

    private AID[] Agents;
    private Map<AID, InformStationState> stations;
    private int nUsers;
    private int UsersTotal;
    StringBuilder sb;
    String paneltext;
    JTextPane textPane;
    @Override
    protected void setup() {
        super.setup();
        this.addBehaviour(new searchStation());
        stations = new HashMap<AID, InformStationState>();
        this.addBehaviour(new StationRequest());

        textPane = new JTextPane();
        sb = new StringBuilder();

        XChartPanel<PieChart> exampleChart = new XChartPanel<PieChart>(getPieChart());
        XChartPanel<CategoryChart> barchart = new XChartPanel<CategoryChart>(getBarChart());
        XChartPanel<CategoryChart> barchartpercentages = new XChartPanel<CategoryChart>(getBarChart02());
        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                JFrame frame = new JFrame("XChart");

                frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
                frame.getContentPane().add(exampleChart, BorderLayout.WEST);
                frame.getContentPane().add(barchart, BorderLayout.EAST);
                frame.getContentPane().add(barchartpercentages, BorderLayout.SOUTH);
                frame.getContentPane().add(textPane, BorderLayout.CENTER);
                frame.pack();
                frame.setVisible(true);
            }
        });

        this.addBehaviour(new TickerBehaviour(this, 500) {
            protected void onTick() {
                showInterface();
            }
        });

        TimerTask chartUpdaterTask = new TimerTask() {
            public void run() {
                updateData();
                textPane.setText(paneltext);
                exampleChart.revalidate();
                barchart.revalidate();
                barchartpercentages.revalidate();
                exampleChart.repaint();
                barchart.repaint();
                barchartpercentages.repaint();
            }
        };
        java.util.Timer timer = new Timer();
        timer.scheduleAtFixedRate(chartUpdaterTask, 0, 500);
    }


    public PieChart getPieChart() {
        // Create Chart
        piechart = new PieChartBuilder().width(800).height(600).title(getClass().getSimpleName()).build();

        // Customize Chart
        Color[] sliceColors = new Color[] {
                new Color(187, 47, 169),
                new Color(56, 118, 29),
                new Color(66, 79, 220),
                new Color(194, 182, 68),
                new Color(213, 6, 6) };
        piechart.getStyler().setSeriesColors(sliceColors);

        // Series
        piechart.addSeries("Station1", numberofUsers[0]);
        piechart.addSeries("Station2", numberofUsers[1]);
        piechart.addSeries("Station3", numberofUsers[2]);
        piechart.addSeries("Station4", numberofUsers[3]);
        piechart.addSeries("Station5", numberofUsers[4]);

        return piechart;
    }

    public CategoryChart getBarChart() {
        // Create Chart
        bar_chart = new CategoryChartBuilder().width(800).height(600).title("Andamento vs. Repouso").xAxisTitle("Users").yAxisTitle("Total").theme(Styler.ChartTheme.XChart).build();
        // Series
        bar_chart.addSeries("Utilizadores", new ArrayList<String>(Arrays.asList(array)), new ArrayList<Number>(Arrays.asList(barchart)));
        return bar_chart;
    }

    public CategoryChart getBarChart02() {
        // Create Chart
        bar_chart_percentages = new CategoryChartBuilder().width(800).height(600).title("percentagens").xAxisTitle("Estaçoes").yAxisTitle("Percentagem").theme(Styler.ChartTheme.XChart).build();

        // Series
        bar_chart_percentages.addSeries("Estaçoes", new ArrayList<String>(Arrays.asList(array_stations)), new ArrayList<Number>(Arrays.asList(percentageTotal)));

        return bar_chart_percentages;
    }

    public void updateData() {
        piechart.updatePieSeries("Station1", numberofUsers[0]);
        piechart.updatePieSeries("Station2", numberofUsers[1]);
        piechart.updatePieSeries("Station3", numberofUsers[2]);
        piechart.updatePieSeries("Station4", numberofUsers[3]);
        piechart.updatePieSeries("Station5", numberofUsers[4]);
        bar_chart.updateCategorySeries("Utilizadores", new ArrayList<String>(Arrays.asList(array)), new ArrayList<Number>(Arrays.asList(barchart)), null);
        bar_chart_percentages.updateCategorySeries("Estaçoes", new ArrayList<String>(Arrays.asList(array_stations)), new ArrayList<Number>(Arrays.asList(percentageTotal)), null);
    }

    @Override
    public PieChart getChart() {
        return null;
    }

    @Override
    public String getExampleChartName() {
        return null;
    }

    protected void showInterface(){
        addBehaviour(new searchUsers());
        double count = 0;
        sb.setLength(0);
        UsersTotal = 0;
        for(Map.Entry<AID, InformStationState> c : stations.entrySet()) {
            if(c.getValue().getName().equals("Station1")){
                numberofUsers[0] = c.getValue().getnBikes();
                count = (double) numberofUsers[0]/12*100;
                percentageTotal[0] = (Number) count;
            }
            if(c.getValue().getName().equals("Station2")){
                numberofUsers[1] = c.getValue().getnBikes();
                count = (double) numberofUsers[1]/12*100;
                percentageTotal[1] = (Number) count;
            }
            if(c.getValue().getName().equals("Station3")){
                numberofUsers[2] = c.getValue().getnBikes();
                count = (double) numberofUsers[2]/12*100;
                percentageTotal[2] = (Number) count;
            }
            if(c.getValue().getName().equals("Station4")){
                numberofUsers[3] = c.getValue().getnBikes();
                count = (double) numberofUsers[3]/12*100;
                percentageTotal[3] = (Number) count;
            }
            if(c.getValue().getName().equals("Station5")){
                numberofUsers[4] = c.getValue().getnBikes();
                count = (double) numberofUsers[4]/12*100;
                percentageTotal[4] = (Number) count;
            }

            paneltext = sb.append(c.getValue().getName() +" | Cap: "+ c.getValue().getnBikes() +
                    "/"+c.getValue().getCap() + " | Coords: " +
                    c.getValue().getCoords() + " | Desc A: "+c.getValue().getAcceptedDisc()+
                    " |  Desc R: " + c.getValue().getDeniedDisc() + " | Total Users: " +
                    c.getValue().getTotalUsers() + "\n\n").toString();
            UsersTotal += c.getValue().getTotalUsers();
        }
        paneltext = sb.append("Numero de Bicicletas em circulação: "+ nUsers + "\n\n" +
                "Numero de Total de Users: "+ UsersTotal + "\n").toString();
        barchart[1] = nUsers;
        barchart[0] = 40-nUsers;
    }


    /* Procura utilizadores e conta-os */
    private class searchUsers extends OneShotBehaviour {

        @Override
        public void action() {
            DFAgentDescription template = new DFAgentDescription();
            ServiceDescription sd = new ServiceDescription();
            sd.setType("User");
            template.addServices(sd);
            try {
                DFAgentDescription[] result = DFService.search(myAgent, template);
                nUsers = result.length;
            } catch (FIPAException fe) {
                fe.printStackTrace();
            }
        }
    }

    /* Procura e guarda todas as stations registadas */
    private class searchStation extends OneShotBehaviour {
        @Override
        public void action() {
            DFAgentDescription template = new DFAgentDescription();
            ServiceDescription sd = new ServiceDescription();
            sd.setType("Station");
            template.addServices(sd);

            try {
                DFAgentDescription[] result = DFService.search(myAgent, template);
                Agents = new AID[result.length];

                for (int i = 0; i < result.length; i++) {
                    Agents[i] = result[i].getName();
                }

            } catch (FIPAException fe) {
                fe.printStackTrace();
            }

            if (Agents.length != 0) {
                for (int i = 0; i < Agents.length; i++) {
                    stations.put(Agents[i], new InformStationState());
                }
            }
        }
    }

    /* serve para receber todas as mensagens do agente Station, contendo todos os dados importantes.*/
    private class StationRequest extends CyclicBehaviour {

        AID estacao;
        InformStationState est;

        @Override
        public void action() {
            MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.INFORM);
            ACLMessage msg = myAgent.receive(mt);
            if (msg != null) {
                try {
                    InformStationState content = (InformStationState) msg.getContentObject();
                    estacao = msg.getSender();
                    if (!stations.containsKey(estacao)){
                        stations.put(estacao, content);
                    } else {
                        stations.get(estacao).setName(content.getName());
                        stations.get(estacao).setCoords(content.getCoords());
                        stations.get(estacao).setnBikes(content.getnBikes());
                        stations.get(estacao).setCap(content.getCap());
                        stations.get(estacao).setTotalUsers(content.getTotalUsers());
                        stations.get(estacao).setAcceptedDisc(content.getAcceptedDisc());
                        stations.get(estacao).setDeniedDisc(content.getDeniedDisc());
                    }
                }catch (UnreadableException e){ }
            } else {
                block();
            }
        }
    }

    @Override
    protected void takeDown() {
        super.takeDown();
    }
}

