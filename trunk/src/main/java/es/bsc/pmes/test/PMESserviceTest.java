package es.bsc.pmes.test;

import es.bsc.pmes.api.PMESclient;
import es.bsc.pmes.service.PMESservice;
import es.bsc.pmes.types.*;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.ws.Endpoint;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by scorella on 8/22/16.
 */
public class PMESserviceTest {
    /*public static PMESclient client;

    public static void testCreateActivity(){
        // java -classpath /home/user/compss-connectors-commons.jar:/home/user/compss-connectors-rocci.jar:/home/user/pmes.jar es.bsc.pmes.test.PMESserviceTest
        ArrayList<JobDefinition> jobDefinitions = new ArrayList<>();

        App app = new App("sleep");
        //Image img = new Image("uuid_pmestestingocci_68", "small");
        Image img = new Image("uuid_testcompss14_82", "small");
        User usr = new User("scorella");
        HashMap<String, String> credentials = new HashMap<>();
        credentials.put("key", "~/certs/test/scorella_test.key");
        credentials.put("pem", "~/certs/test/scorella_test.pem");
        usr.setCredentials(credentials);

        JobDefinition job = new JobDefinition();
        job.setApp(app);
        job.setJobName("test");
        job.setImg(img);
        job.setUser(usr);
        job.setInputPath("");
        job.setOutputPath("");
        job.setWallTime(10);
        job.setNumNodes(1);
        job.setCores(16);
        job.setMemory(new Float(1.0));
        job.setCompss_flags(null);

        jobDefinitions.add(job);
        ArrayList<String> jobids = client.createActivity(jobDefinitions);
        for (String id:jobids
             ) {
            System.out.println("Job submitted "+id);
        }
    }

    public static void testGenerateProjects() {
        COMPSsJob jobTest = new COMPSsJob();
        jobTest.generateProjects();
    }

    public static void  testGenerateResources() {
        COMPSsJob jobTest = new COMPSsJob();
        jobTest.generateResources();
    }*/

    public static void main(String[] args){
        System.out.println("Testing app");

        /*client = new PMESclient("http://localhost:9998/");
        SystemStatus systemStatus = client.getSystemStatus();
        ArrayList<Host> cluster = systemStatus.getCluster();
        System.out.println(cluster.size());
        System.out.println(cluster.get(0).getName());

        testCreateActivity();

        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        testCreateActivity();*/


    }

}
