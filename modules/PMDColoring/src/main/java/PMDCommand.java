import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.terasology.entitySystem.systems.BaseComponentSystem;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.logic.console.Console;
import org.terasology.logic.console.commandSystem.annotations.Command;
import org.terasology.logic.console.commandSystem.annotations.CommandParam;
import org.terasology.logic.permission.PermissionManager;
import org.terasology.registry.In;


@RegisterSystem
public class PMDCommand extends BaseComponentSystem{
	@In
    private Console console;
	@Command(shortDescription = "PMD coloring.",
            requiredPermission = PermissionManager.NO_PERMISSION)
    public String pmdColoring(@CommandParam(value = "sourcePath",required = true) String sourcePath,@CommandParam(value="rules",required=false) String rules,@CommandParam(value="outPutType",required=false) String outPutType) throws IOException
    {
    	if (rules == null) rules = "basic";
    	if (outPutType == null) outPutType = "text";
    	String inputString = buildInputString(sourcePath, outPutType, rules);
    	
    	Thread t = new Thread(new ThreadPMDExecution(inputString,console));
		t.start();
		
		return "Esperando por resultados del analisis...";
    }
	private String buildInputString(String sourcePath, String outPutType, String rules) {
		return "java -classpath .\\modules\\PMDColoring\\libs\\pmd\\lib\\* net.sourceforge.pmd.PMD -d "+ sourcePath+" -f "+outPutType+" -R rulesets/java/"+ rules+".xml";
	}
}

class ThreadPMDExecution implements Runnable
{
	String inputString;
	Console console;
	public ThreadPMDExecution(String inputString, Console console) {
		// TODO Auto-generated constructor stub
		this.inputString = inputString;
		this.console = console;
	}

	@Override
	public void run() 
	{
		// TODO Auto-generated method stub
		
		try {
			Process process;
			process = Runtime.getRuntime().exec(inputString);
			InputStream is = process.getInputStream();
			 InputStreamReader isr = new InputStreamReader(is);
			 BufferedReader br = new BufferedReader(isr);
				
			 StringBuilder sb = new StringBuilder();
			 String line;
			 while ((line = br.readLine()) != null) 
			 {
				 console.addMessage(line);	
			 }
			 console.addMessage("Fin del Analisis");
		}
		catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

} 
