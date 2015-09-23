package com.icon.proceso;

import java.io.File;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.log4j.Logger;

import com.icon.tools.Tools;

public class UnixReporte extends Thread {

	public static Logger log = Logger.getLogger(UnixReporte.class);

	private int maxProcesos = 5;
	private int maxArchivos = 5;
	private int procesos = 0;

	private String prefijo;
	private String mascara;
	private String extencion;
	private String extZip;
	private boolean isNotZip = false;

	private String llave = Tools.getValor("llave");
	private int inicio = Integer.parseInt(Tools.getValor(llave + "-inicio"));
	private int fin = Integer.parseInt(Tools.getValor(llave + "-fin"));

	private String nombreReporte = Tools.getValor("nombre-report");
	private String pathInput = Tools.getValor("path-input");
	private String pathReport = Tools.getValor("path-report");

	Map reporte = new HashMap();
  
	UnixReporte() {
           
		this.prefijo = "elcapa_CTCMD_";//strings[0];
		this.mascara = "20130920";//strings[1];
		this.extencion = ".cdr.PROC"; //strings[2];
		this.extZip = ".cdr.PROC.zip";//strings[3];
		this.isNotZip =false;// (strings[4].toLowerCase().equals("true") ? true : false);
	}

	/**
	 * @return the aa 
	 */
	public boolean isNotZip() {
		return isNotZip;
	}

	/**
	 * @param isNotZip
	 *            the isNotZip to set
	 */
	public void setNotZip(boolean isNotZip) {
		this.isNotZip = isNotZip;
	}

	/**
	 * @return the extZip
	 */
	public String getExtZip() {
		return extZip;
	}

	/**
	 * @return the prefijo
	 */
	public String getPrefijo() {
		return prefijo;
	}

	/**
	 * @param prefijo
	 *            the prefijo to set
	 */
	public void setPrefijo(String prefijo) {
		this.prefijo = prefijo;
	}

	/**
	 * @return the mascara
	 */
	public String getMascara() {
		return mascara;
	}

	/**
	 * @param mascara
	 *            the mascara to set
	 */
	public void setMascara(String mascara) {
		this.mascara = mascara;
	}

	/**
	 * @return the extencion
	 */
	public String getExtencion() {
		return extencion;
	}

	/**
	 * @param extencion
	 *            the extencion to set
	 */
	public void setExtencion(String extencion) {
		this.extencion = extencion;
	}

	/**
	 * @param extZip
	 *            the extZip to set
	 */
	public void setExtZip(String extZip) {
		this.extZip = extZip;
	}

	/**
	 * @return the llave
	 */
	public String getLlave() {
		return llave;
	}

	/**
	 * @param llave
	 *            the llave to set
	 */
	public void setLlave(String llave) {
		this.llave = llave;
	}

	/**
	 * @return the inicio
	 */
	public int getInicio() {
		return inicio;
	}

	/**
	 * @param inicio
	 *            the inicio to set
	 */
	public void setInicio(int inicio) {
		this.inicio = inicio;
	}

	/**
	 * @return the fin
	 */
	public int getFin() {
		return fin;
	}

	/**
	 * @param fin
	 *            the fin to set
	 */
	public void setFin(int fin) {
		this.fin = fin;
	}

	public synchronized void addReporte(Map datos) {
		Iterator i = datos.entrySet().iterator();
		while (i.hasNext()) {
			Entry e = (Entry) i.next();

			String campos = Tools.getValor("campos");

			if (reporte.get(e.getKey()) == null) {
				Map m = new HashMap();

				for (int x = 0; x < campos.split(",").length; x++)
					m.put(campos.split(",")[x], new Long(0));

				reporte.put(e.getKey(), m);

			}

			for (int x = 0; x < campos.split(",").length; x++) {
				String campo = campos.split(",")[x];

				long l = ((Long) ((Map) reporte.get(e.getKey())).get(campo))
						.longValue();
				long l1 = ((Long) ((Map) e.getValue()).get(campo)).longValue();
				((Map) reporte.get(e.getKey())).put(campo, new Long(l + l1));
			}
		}
	}

	private void generarReporte() throws IOException {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
		FileWriter f = new FileWriter(pathReport + nombreReporte + "_"
				+ prefijo + "_" + mascara + "_" + sdf.format(new Date())
				+ ".txt");
		try {

			f.write("Reporte para," + prefijo + ",Mascara," + mascara
					+ ",Extension," + extencion + "\n");

			f.write(llave + ",");

			String campos = Tools.getValor("campos");
			for (int x = 0; x < campos.split(",").length; x++)
				f.write(campos.split(",")[x] + ",");

			f.write("\n");

			String linea = "";
			String campo;

			Iterator i = reporte.entrySet().iterator();
			while (i.hasNext()) {
				Entry e = (Entry) i.next();

				linea = e.getKey().toString() + ",";

				for (int x = 0; x < campos.split(",").length; x++) {
					campo = campos.split(",")[x];
					linea = linea
							+ String.valueOf((Long) ((Map) e.getValue())
									.get(campo)) + ",";
				}

				f.write(linea + "\n");
				linea = "";
			}
		} finally {
			f.close();
		}
	}

	private List getLista() throws Exception {
		List list = new ArrayList();

		File dir = new File(pathInput);

		File[] files1 = dir.listFiles(new FilenameFilter() {
			public boolean accept(File dir, String name) {
				return name.startsWith(prefijo)
						&& (name.indexOf(mascara) > 0 ? true : false)
						&& name.endsWith(extencion);
			}
		});

		for (int i = 0; i < files1.length; i++)
			list.add(files1[i]);

		return list;
	}

	public synchronized void resta() {
		this.procesos--;
	}

	void ejecutar() throws Exception {
		log.info("ProcesoAdmin Inicio ejecutar");
		maxProcesos = Integer.parseInt(Tools.getValor("max-procesos"));
		maxArchivos = Integer.parseInt(Tools.getValor("max-archivos"));

		List list = getLista();

		log.debug("lista size:" + list.size());

		List listTmp = new ArrayList();
		for (int x = 0; x < list.size(); x++) {
			listTmp.add(list.get(x));

			if (listTmp.size() >= maxArchivos) {
				while (procesos >= maxProcesos)
					sleep(1000);

				log.debug("leveanto hijo");
				procesos++;
				new ProcesoDetalle(this, listTmp).start();

				listTmp.clear();
			}
		}

		while (procesos >= maxProcesos)
			sleep(1000);

		if (listTmp.size() > 0) {
			log.debug("leveanto hijo");
			procesos++;
			new ProcesoDetalle(this, listTmp).start();
		}

		while (procesos > 0)
			sleep(1000);

		log.info("ProcesoAdmin Fin ejecutar");
	}

	public void run() {
		try {
			ejecutar();
			generarReporte();
		} catch (Exception e) {
			log.error("Error inesperado ", e);
		}
	}

	public static void main(String[] strings) throws IOException {
		new UnixReporte().start();
	}
}
