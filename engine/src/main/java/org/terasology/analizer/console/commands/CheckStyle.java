package org.terasology.analizer.console.commands;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;

import org.terasology.entitySystem.systems.BaseComponentSystem;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.logic.console.commandSystem.annotations.Command;
import org.terasology.logic.console.commandSystem.annotations.CommandParam;

@RegisterSystem
public class CheckStyle extends BaseComponentSystem{
	
	@Command(shortDescription = "Utiliza checkstyle para analizar un archivo", 
			 helpText = "Utiliza checkStyle para para analizar algun programa segun la metrica dada \n"
			 		+ "cstyle archivo metrica maxValue \n"
			 		+ "Donde:\n"
			 		+ "    archivo: es el archivo a analizar \n"
			 		+ "    metrica: b para booleana, ciclomatica por defecto \n"
			 		+ "    maxValue: maximo valor de comparadores en caso de metrica booleana")
	
	public String cstyle(@CommandParam("Archivo") String path, 
						 @CommandParam("Metrica") String metric,
						 @CommandParam("Maximo valor booleano") Integer max) throws IOException {
		
		String pathDefault = "engine/PruebasCheckStyle";
		String pathMetric = pathDefault;
		
		if (metric.equals("-b")) pathMetric += "/booleanRule.xml";
		else if (metric.equals("-c")) pathMetric += "/cyclomaticRule.xml";
		else return "No existe esa metrica, prueba con -b o -c";
		
		setMetricValue(pathMetric, max, getTextFromFile(pathMetric));
		
		String commandJar = "-jar " + pathDefault  + "/checkstyle-6.6-all.jar ";
		String commandMetric = " -c " + pathMetric + " ";
		String commandOut = " -f xml -o " + pathDefault + "/out.xml ";
		String commandFile = pathDefault + "/" + path;
		
		Runtime.getRuntime().exec("java " + commandJar
						          + commandMetric
						          + commandOut
						          + commandFile);
		return "Analisis realizado";
	}
		
	private void setMetricValue(String path, Integer max, ArrayList<String> lines) {
		FileWriter bw = null; 
		PrintWriter pw = null;
		try {
			bw = new FileWriter(path); 
			pw = new PrintWriter(bw);
			for (String actualLine : lines) {
				String regExp = " *<property name=\"max\" value=\".*\"/>";
				if (actualLine.matches(regExp)){
					actualLine = "      <property name=\"max\" value=\"" + max + "\"/>";
				}
				pw.write(actualLine + "\n");
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			pw.close();
			try {
				bw.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	private ArrayList<String> getTextFromFile(String path) {
		FileReader fr = null;
		BufferedReader br = null;
		ArrayList<String> lines = new ArrayList<String>();
		try {
			fr = new FileReader(path);
			br = new BufferedReader(fr);
			String line = "";
			while ((line = br.readLine()) != null) {
				lines.add(line);
			}
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		} finally {
			try {
				br.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			try {
				fr.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return lines;
	}
	
}
