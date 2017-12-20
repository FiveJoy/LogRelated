package com.ifeng.liulu.check;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class CheckType {

	public static void main(String[] args) {
		try {
			File file = new File("D:\\data\\clientapplog\\2017-11-23\\0000.sta");
			// File file=new File("D:\\data\\vapp\\2017-11-23\\0000.sta");
			BufferedReader br = new BufferedReader(new FileReader(file));

			File page_file = new File("D:\\data\\check\\newsapp\\store.log");
			BufferedWriter bw_page = new BufferedWriter(new FileWriter(page_file));

			File action_file = new File("D:\\data\\check\\newsapp\\jump.log");
			BufferedWriter bw_action = new BufferedWriter(new FileWriter(action_file));

			String line = "";
			int validLine = 0;
			int invalidLine = 0;
			int pageLine = 0;
			int actionLine = 0;
			Set<String> actionTypeSet = new HashSet<>();
			while ((line = br.readLine()) != null) {
				String[] details = line.split("\t");
				if (details.length != 13)
					invalidLine++;
				else {
					validLine++;
					String type = details[11];
					actionTypeSet.add(type);

					if (type.equals("action") && details[12].contains("type=store")) {
						pageLine++;
						bw_page.write(details[12]);
						bw_page.write("\r\n");
					} /*
						 * else if(type.equals("jump")){ actionLine++; bw_action.write(details[12]);
						 * bw_action.write("\r\n"); }
						 */
				}
			}
			br.close();
			bw_page.close();
			bw_action.close();
			System.out.println("valid numbers=" + validLine);
			System.out.println("invalid numbers=" + invalidLine);
			System.out.println("page lines=" + pageLine);
			System.out.println("action lines=" + actionLine);
			Iterator<String> it = actionTypeSet.iterator();
			System.out.println("actiontype number=" + actionTypeSet.size());
			/*
			 * while(it.hasNext()) { System.out.println(it.next()); }
			 */
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
}
