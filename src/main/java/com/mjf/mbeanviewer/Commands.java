package com.mjf.mbeanviewer;

/**
 * for command executing
 *
 * @author David.W
 */
public class Commands {

    public static void main(String[] args){
        String result = null;
        if(args == null || args.length == 0){
            result = "you can use cmds below: \n" +
                    "ps -- get all local java process id, return as json format\n" +
                    "mbean-onames -- get all ObjectNames of MBeans for a given pid, return as json format\n" +
                    "mbean-values -- get all Attribute Value of A MBean for a given pid and ObjectName, return as json format";
        }else{
            try{
                String cmd = args[0];
                if("ps".equals(cmd)){
                    result = JMXTools.getPids();
                }else if("mbean-onames".equals(cmd)){
                    if(args.length != 2){
                        result = "mbean-onames : usage : mbean-onames pid \ne.g: mbean-onames 12345 \nyou can use cmd 'ps' for all local pid list first!";
                    }else{
                        String pid = args[1];
                        result = JMXTools.getAllMBeanObjectNames(pid);
                    }
                }else if("mbean-values".equals(cmd)){
                    if(args.length != 3){
                        result = "mbean-values : usage : mbean-values pid objectname \ne.g: mbean-values 12345 \"java.lang:type=Threading\" \nyou can use cmd 'mbean-onames' for all mbean objectname list first!";
                    }else{
                        String pid = args[1];
                        String objectName = args[2];
                        result = JMXTools.getMBeanValue(pid, objectName);
                    }
                }
            } catch (Exception e){
                result = e.getMessage();
            }
        }
        System.out.println(result);
    }

}
