package com.icon.tools;

import java.beans.XMLEncoder;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Properties;

import org.apache.log4j.Logger;

public class Tools {

	public static Logger log = Logger.getLogger(Tools.class);

	protected static Properties propiedades;

	public static final String ADMIN = "tol";

	public static String getValor(String llave) {
		if (propiedades == null) {
			propiedades = new Properties();
			try {
				log.debug("Path;"
						+ new File("configuracion.properties")
								.getAbsolutePath());
				propiedades.load(new FileInputStream(new File(
						"configuracion.properties").getAbsolutePath()));
			} catch (FileNotFoundException e) {
				log.error(
						"Error el archivo de configuracion (conf.properties) no existe.",
						e);
			} catch (IOException e) {
				log.error(
						"Error al intentar leer el archivo de configuracion (conf.properties) no existe.",
						e);
			}
		}

		return propiedades.getProperty(llave);
	}

	public static String lPad(String cadena, String caracter, int longitud) {
		while (cadena.length() < longitud)
			cadena = caracter + cadena;

		return cadena;
	}

	public static String procesoExterno(String comando) throws IOException,
			InterruptedException {
		Runtime rt = Runtime.getRuntime();
		try {
			log.debug(comando);

			Process proc = rt.exec(comando);

			BufferedReader is = new BufferedReader(new InputStreamReader(
					proc.getInputStream()));

			String line;
			StringBuffer sb = new StringBuffer();
			while ((line = is.readLine()) != null) {
				sb.append(line);
				log.debug(line);
			}
			proc.waitFor();

			return sb.toString();
		} finally {
			rt.runFinalization();
		}
	}

	public static long getLineas(String archivo, long longitud)
			throws Exception {
		long tamanio = 0;
		File file = new File(archivo);

		if (!file.exists())
			throw new Exception("Error el archivo .TXT no existe.");

		tamanio = file.length();
		long lineas = tamanio / longitud;

		log.debug("Archivo:" + archivo + ", bytes: " + tamanio + ", lineas:"
				+ lineas);
		return lineas;
	}

	public static boolean exists(String archivo) {
		File file = new File(archivo);
		return file.exists();
	}

	public static void move(String origen, String destino, String archivo)
			throws Exception {
		File file = new File(origen + archivo);

		if (!file.renameTo(new File(destino + archivo)))
			throw new Exception("Archivo no pudeo ser movido:" + origen
					+ archivo + " a " + destino + archivo);
	}

	public static long getLinesLST(String archivo) throws IOException {
		String linea;

		FileReader fr = new FileReader(archivo);
		try {
			BufferedReader br = new BufferedReader(fr);
			try {
				linea = br.readLine();

				if (linea != null) {
					/* SE MACHEA SI LST ES ESTILO MAT */
					if (linea.matches(".+TXT.+"))
						return Long.parseLong(linea.substring(
								linea.lastIndexOf(".TXT") + 4, linea.length()));
					else if (linea.matches(".+_.+"))
						return Long.parseLong(linea.substring(
								linea.lastIndexOf("_") + 1, linea.length()));
					else
						return Long.parseLong(linea);
				} else
					return -1;

			} finally {
				br.close();
			}
		} finally {
			fr.close();
		}
	}

	public static String replaceAll(String cadena, String viejo, String neuvo) {
		int vPos = cadena.indexOf(viejo);
		int vLen = viejo.length();

		while (vPos >= 0) {
			cadena = cadena.substring(0, vPos) + neuvo
					+ cadena.substring(vPos + vLen);
			vPos = cadena.indexOf(viejo, vPos + neuvo.length() - 1);
		}

		return cadena;
	}

	public static String replace(String cadena, String viejo, String nuevo) {
		if (cadena.indexOf(viejo) >= 0)
			cadena = cadena.substring(0, cadena.indexOf(viejo)) + nuevo
					+ cadena.substring(cadena.indexOf(viejo) + viejo.length());

		return cadena;
	}

	public static String objectToXml(Object resultado) {
		String salida = null;
		try {

			ByteArrayOutputStream out = new java.io.ByteArrayOutputStream();

			XMLEncoder enc = new XMLEncoder(out);

			enc.writeObject(resultado);
			enc.close();
			salida = "\n" + out.toString();

		} catch (Exception e) {
		}

		return salida;
	}

	public static void borraArchivo(String file, Logger log) throws Exception {
		log.debug("Eliminando archivo: " + file);

		File f1 = new File(file);
		boolean success = f1.delete();

		if (success)
			log.debug("Archivo eliminado con exito");
		else
			log.debug("Archivo no pudo ser eliminado");
	}

	public static void main(String[] strings) {
		try {
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
