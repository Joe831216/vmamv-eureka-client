package com.example.scsaeureka.mgp.registry.client;

import com.example.scsaeureka.mgp.registry.client.bean.Application;
import com.example.scsaeureka.mgp.registry.client.bean.MgpInstance;
import com.example.scsaeureka.mgp.registry.client.bean.RegisterInfo;
import com.google.gson.Gson;
import com.netflix.appinfo.InstanceInfo;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.netflix.eureka.server.event.EurekaInstanceCanceledEvent;
import org.springframework.cloud.netflix.eureka.server.event.EurekaInstanceRegisteredEvent;
import org.springframework.cloud.netflix.eureka.server.event.EurekaServerStartedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.env.Environment;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;

public class Register {

    private static final Logger logger = LoggerFactory.getLogger(Register.class);
    private static final String registerPath = "/registry/register/eureka";
    private static final String unregisterPath = "/registry/unregister/";

    @Autowired
    private Environment env;
    @Value("${mgp.server.url}")
    private String mgpServerUrl;
    @Value("${mgp.scs.name}")
    private String scsName;
    @Value("${spring.application.name}")
    private String appName;
    @Value("${info.version}")
    private String version;
    @Value("${spring.cloud.client.hostname}")
    private String hostName;
    @Value("${spring.cloud.client.ipAddress}")
    private String ipAddr;
    @Value("${server.port}")
    private int port;
    //@Value("${mgp.scs.name}:${spring.application.name}:${info.version}")
    private String appId;
    //@Value("${spring.cloud.client.hostname}:${spring.application.name}:${server.port}")
    private String instanceId;

    private Gson gson = new Gson();
    private RestTemplate restTemplate = new RestTemplate();

    @PostConstruct
    public void init() {
        ArrayList<String> properties = new ArrayList<>();
        properties.add(mgpServerUrl);
        properties.add(scsName);
        properties.add(appName);
        properties.add(version);
        properties.add(port + "");
        properties.add(appId);
        properties.add(instanceId);
        for (String property: properties) {
            if (property == null || property.length() == 0) {
                logger.info("Property miss, can't register to MGP system");
                break;
            }
        }

        scsName = scsName.toUpperCase();
        appName = appName.toUpperCase();
        appId = scsName + ":" + appName + ":" + version;
        instanceId = hostName + ":" + appName + ":" + port;
    }

    @EventListener
    // Listen to ContextRefreshedEvent is another approach
    public void registerOnEurekaServerStarted(EurekaServerStartedEvent event) {
        // Post service info to MGP registry
        try {
            CloseableHttpClient client = HttpClients.createDefault();
            HttpPost httpPost = new HttpPost(mgpServerUrl + registerPath);
            ArrayList<MgpInstance> instances = new ArrayList<>();
            MgpInstance instance = new MgpInstance(hostName, appName, ipAddr, port);
            instances.add(instance);
            ArrayList<Application> applications = new ArrayList<>();
            Application application = new Application(scsName, appName, version, instances);
            applications.add(application);
            RegisterInfo registerInfo = new RegisterInfo(applications);
            String json = gson.toJson(registerInfo);
            logger.info(json);
            StringEntity entity = new StringEntity(json);
            httpPost.setEntity(entity);
            httpPost.setHeader("Accept", "application/json");
            httpPost.setHeader("Content-type", "application/json");
            CloseableHttpResponse response = client.execute(httpPost);
            // Handle response
            // The handling of the failure has not yet been implemented
            client.close();
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        }
        //refreshAppList();
    }

    @EventListener
    public void refreshAppListOnRegisterApp(EurekaInstanceRegisteredEvent event) {
        if (!event.isReplication()) {
            //refreshAppList();
            InstanceInfo info = event.getInstanceInfo();
            logger.info("register(instance id): " + info.getInstanceId());
            logger.info("register(app name): " + info.getAppName());
            logger.info("register(host name): " + info.getHostName());
        }
    }

    @EventListener
    public void refreshAppListOnUnregisterApp(EurekaInstanceCanceledEvent event) {
        if (!event.isReplication()) {
            //refreshAppList();
            logger.info("unregister(server id): " + event.getServerId());
            logger.info("unregister(app name): " + event.getAppName());
        }
    }

    /*
    public void refreshAppList() {
        String scsName = env.getProperty("mgp.scs.name");
        String appName = env.getProperty("spring.application.name");
        String serviceRegistryId = scsName + ":" + appName;
        String url = env.getProperty("mgp.registry.url") + "/registry/refreshAppList/" + serviceRegistryId;
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> entity = new HttpEntity<>(headers);
        ResponseEntity<String> responseEntity = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
    }
*/

    @PreDestroy
    public void unregisterOnAppShutdown() {
        restTemplate.delete(mgpServerUrl + unregisterPath + instanceId);
    }

}
