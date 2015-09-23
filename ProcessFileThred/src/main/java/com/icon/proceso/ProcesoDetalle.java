package com.icon.proceso;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.icon.tools.Tools;

public class ProcesoDetalle extends Thread {

	public static Logger log = Logger.getLogger(ProcesoDetalle.class);

	private Thread padre;
	private List list;

	public ProcesoDetalle(Thread padre, List list) {
		this.padre = padre;
		this.list = new ArrayList(list);
	}

	private UnixReporte getPadre() {
		return (UnixReporte) this.padre;
	}

	protected String procesaArchivo(String archivo) throws Exception {
		String llave;
		Map datos = new HashMap();

		int iniU = Integer.parseInt(Tools.getValor("upload-inicio"));
		int finU = Integer.parseInt(Tools.getValor("upload-fin"));
		int iniD = Integer.parseInt(Tools.getValor("donwload-inicio"));
		int finD = Integer.parseInt(Tools.getValor("donwload-fin"));

		FileReader fr = new FileReader(archivo);
		try {
			BufferedReader br = new BufferedReader(fr);
			try {
				log.info("INICIO read " + archivo);

				String linea;
				while ((linea = br.readLine()) != null) {

					llave = linea.substring(getPadre().getInicio(),
							getPadre().getFin()).trim();

					Map detalle = new HashMap();
					detalle.put("upload",
							Long.valueOf(linea.substring(iniU, finU).trim()));
					detalle.put("donwload",
							Long.valueOf(linea.substring(iniD, finD).trim()));
					datos.put(llave, detalle);
				}

				log.info("INICIO addReporte " + archivo);
				getPadre().addReporte(datos);
				log.info("FIN addReporte " + archivo);
				return "OK " + archivo;
			} finally {
				br.close();
			}
		} finally {
			fr.close();
		}
	}

	private void ejecutar() throws Exception {
		String r;

		String extZip = getPadre().getExtZip();
		String rm = Tools.getValor("comando-rm");
		String pathTemp = Tools.getValor("path-temp");
		String cmdCP = Tools.getValor("comando-cp");
		cmdCP = Tools.replaceAll(cmdCP, "{destino}", pathTemp);

		String cmdUnzip = Tools.getValor("comando-unzip");

		for (int x = 0; x < list.size(); x++) {
			File f = (File) list.get(x);

			try {
				log.info("INICIO " + list.get(x));

				if (getPadre().isNotZip()) {
					r = procesaArchivo(f.getAbsolutePath());

				} else {
					// copio
					Tools.procesoExterno(Tools.replaceAll(cmdCP, "{archivo}",
							f.getAbsolutePath()));

					// descromprimo
					Tools.procesoExterno(Tools.replaceAll(cmdUnzip,
							"{archivo}", pathTemp + f.getName()));

					String archivo = pathTemp
							+ Tools.replaceAll(f.getName(), extZip, "");

					r = procesaArchivo(archivo);

					Tools.procesoExterno(Tools.replaceAll(rm, "{archivo}",
							archivo));

					if (extZip.toLowerCase().endsWith(".zip"))
						Tools.procesoExterno(Tools.replaceAll(rm, "{archivo}",
								pathTemp + f.getName()));
				}

				log.info(r);
			} catch (Exception e) {
				log.error("ERROR " + f.getName() + " " + e.getMessage(), e);
			}
		}
	}

	public void run() {
		try {
			log.debug("Inicio hijo");
			ejecutar();
			log.debug("Fin hijo");
		} catch (Exception e) {
			log.error("Error inesperado.", e);
		} finally {
			((UnixReporte) padre).resta();
		}
	}
}
