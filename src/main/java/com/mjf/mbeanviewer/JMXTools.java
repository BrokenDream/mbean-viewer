package com.mjf.mbeanviewer;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.sun.tools.attach.VirtualMachine;
import com.sun.tools.attach.VirtualMachineDescriptor;

import javax.management.MBeanAttributeInfo;
import javax.management.MBeanInfo;
import javax.management.MBeanServerConnection;
import javax.management.ObjectName;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;
import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * Some tool methods for JMX
 *
 * @author David.W
 */
public class JMXTools {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();

    /**
     * get local vm info list.
     *
     * @return
     *      vminfo list in json format.
     */
    public static String getPids(){
        List<VirtualMachineDescriptor> virtualMachineDescriptors = VirtualMachine.list();
        List<VMInfo> vmInfos = new ArrayList<VMInfo>();
        if(virtualMachineDescriptors != null && virtualMachineDescriptors.size() > 0){
            for(VirtualMachineDescriptor descriptor : virtualMachineDescriptors){
                VMInfo vmInfo = new VMInfo();
                vmInfo.setId(descriptor.id());
                vmInfo.setName(descriptor.displayName());
                vmInfos.add(vmInfo);
            }
        }
        return GSON.toJson(vmInfos);
    }

    /**
     * get all mbean object names according to a pid.
     *
     * @param pid a java process id
     * @return all mbean object names as json format
     * @throws Exception
     */
    public static String getAllMBeanObjectNames(String pid) throws Exception{
        JMXConnector jmxConnector = null;
        try {
            String address = getJMXConnectionAddress(pid);
            JMXServiceURL jmxServiceURL = new JMXServiceURL(address);
            Map<String, Object> env = new HashMap<String, Object>();
            jmxConnector = JMXConnectorFactory.connect(jmxServiceURL, env);
            MBeanServerConnection mBeanServerConnection = jmxConnector.getMBeanServerConnection();
            Set<ObjectName> objectNameSet = mBeanServerConnection.queryNames(null, null);
            List<String> objectNameList = new ArrayList<String>();
            for(ObjectName objectName : objectNameSet){
                objectNameList.add(objectName.getCanonicalName());
            }
            return GSON.toJson(objectNameList);
        } finally{
            try {
                jmxConnector.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * get all mbean attribute values according to a pid and a object name.
     *
     * @param pid a java process name
     * @param objectName @see {@link ObjectName}
     * @return all mbean attribute values as json format
     * @throws Exception
     */
    public static String getMBeanValue(String pid, String objectName) throws Exception {
        JMXConnector jmxConnector = null;
        try {
            String address = getJMXConnectionAddress(pid);
            JMXServiceURL jmxServiceURL = new JMXServiceURL(address);
            Map<String, Object> env = new HashMap<String, Object>();
            jmxConnector = JMXConnectorFactory.connect(jmxServiceURL, env);
            MBeanServerConnection mBeanServerConnection = jmxConnector.getMBeanServerConnection();
            ObjectName oName =  new ObjectName(objectName);
            MBeanInfo mBeanInfo = mBeanServerConnection.getMBeanInfo(oName);
            MBeanAttributeInfo[] attributes = mBeanInfo.getAttributes();
            Map<String, Object> attrMap = new HashMap<String, Object>();
            for(MBeanAttributeInfo attribute : attributes){
                try{
                    String attributeName = attribute.getName();
                    attrMap.put(attributeName, mBeanServerConnection.getAttribute(oName, attributeName));
                } catch (Exception ignore){
                }
            }
            return GSON.toJson(attrMap);
        } finally{
            try {
                jmxConnector.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private static String getJMXConnectionAddress(String pid) throws Exception{
        VirtualMachine vm = null;
        try{
            vm = VirtualMachine.attach(pid);
            String home = vm.getSystemProperties().getProperty("java.home");
            String agent = home + File.separator + "jre" + File.separator +
                    "lib" + File.separator + "management-agent.jar";
            File f = new File(agent);
            if (!f.exists()) {
                agent = home + File.separator +  "lib" + File.separator +
                        "management-agent.jar";
                f = new File(agent);
                if (!f.exists()) {
                    throw new IOException("load management agent error, can't find management-agent.jar!");
                }
            }
            agent = f.getCanonicalPath();
            vm.loadAgent(agent, "com.sun.management.jmxremote");
            Properties agentProps = vm.getAgentProperties();
            return (String) agentProps.get("com.sun.management.jmxremote.localConnectorAddress");
        } finally {
            if(vm != null){
                vm.detach();
            }
        }
    }

    private JMXTools(){}

}
