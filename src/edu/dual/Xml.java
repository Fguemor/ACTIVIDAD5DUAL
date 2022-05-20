package edu.dual;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.sql.*;
import java.util.ArrayList;
import java.util.StringTokenizer;
/**
 * Clase para conectar con una base de datos
 * envia logs en formato xml a la base de datos
 * recibe logs de la base de datos y crea un xml
 * @author Fátima Guerrero
 *@version 1.0
 */
public class Xml {
	//Declaración de variables que utilizaremos 
	private Connection conector;
	private Statement statment;
	private ResultSet resultSet;
	private DocumentBuilderFactory documentBF;
	private DocumentBuilder documentB;
	private Document document;
	private TransformerFactory transformerFactory;
	private Transformer transformer;
	private DOMSource source;
	private StreamResult streamResult;

	/**
	 * Constructor que configura la conexión con la base de datos
	 * @param path ruta del archivo de configuración, es un xml con los datos de configuración 
	 * para conectarnos con la base de datos
	 */
	public Xml(String path) {
		try {
			// Se obtiene el documento de configuracion y se carga en memoria, creando una nueva instancia 
			DocumentBuilderFactory documentBF = DocumentBuilderFactory.newInstance();
			DocumentBuilder documentB = documentBF.newDocumentBuilder();
			Document documento = documentB.parse(new File(path));
			String url="",usuario="",contraseña="";
			// Se crea una lista de nodos con los nodos hijos del elemento raiz
			NodeList nodoLista = documento.getDocumentElement().getChildNodes();
			for (int i = 0; i < nodoLista.getLength(); i++) {
				//Por cada uno de los elementos se crea un nodo
				Node nodo = nodoLista.item(i);
				if (nodo.getNodeType() == Node.ELEMENT_NODE) {
					Element elemento = (Element) nodo;
					//Utilizo un switch para buscar en cada elemento el nombre para mandar las configuraciones
					switch (elemento.getTagName()){
					case "url":
						url = elemento.getTextContent();
						break;
					case "usr":
						usuario = elemento.getTextContent();
						break;
					case "passwd":
						contraseña =  elemento.getTextContent();
						break;
					}
				}
			}

			//Conectamos con la base de datos
			this.conector = DriverManager.getConnection(url,usuario,contraseña);
		}catch (Exception e){
			e.printStackTrace();
		}
	}
	/**
	 * Metodo que pasa de xml a base de datos, recoge todos los registros de log del xml y los guarda en la base de datos
	 * @param f Archivo XML con los log para guardar en la bd
	 * @return True si el archivo se ha cargado correctamente false si el archivo no se ha cargado correctamente
	 */
	public boolean XmlBd(File f){
		boolean resultado;
		StringBuffer stringBuffer = new StringBuffer();
		StringTokenizer logsregistro;
		Log log;
		try {
			//Prepara el árbol con el documento xml
			this.documentBF = DocumentBuilderFactory.newInstance();
			this.documentB = documentBF.newDocumentBuilder();
			this.document = this.documentB.parse(f);

			if (this.conector!=null){
				this.statment = this.conector.createStatement();
				NodeList nl = this.document.getElementsByTagName("record");
				for (int i=0;i<nl.getLength();i++){
					if (nl.item(i).hasChildNodes()){
						NodeList nodelist = nl.item(i).getChildNodes();

						//Recorre los hijos del nodo y crea un elemento por cada uno de los elementos
						//de la lista y los añade al StringBuffer

						for (int j=0;j<nodelist.getLength();j++){
							if (nodelist.item(j).getNodeType() == Node.ELEMENT_NODE) {
								Element ele = (Element) nodelist.item(j);
								stringBuffer.append(ele.getTextContent()+",");
							}
						}

						//creo un StringTokenizer para separar
						//los datos, con los datos separados se crea un objeto de la clase Log con el cual
						// crearemos un nuevo registro en la base de datos en la tabla registros

						stringBuffer.deleteCharAt(stringBuffer.lastIndexOf(","));
						logsregistro = new StringTokenizer(stringBuffer.toString(),",");
						log = new Log(logsregistro.nextToken(),logsregistro.nextToken(),logsregistro.nextToken(), logsregistro.nextToken());
						this.statment.execute("Insert into logs (date,logger,level,message) values ("
								+"'"+log.getDate()+"'"+","+"'"+log.getLogger()+"'"+","+"'"+log.getLevel()+"'"+","+"'"+log.getMensaje());
						// Se vacia el StringBuffer a la espera del siguiente Registro
						stringBuffer.replace(0,stringBuffer.length(),"");
					}
				}

			}
			resultado = true;
		}catch (Exception e) {
			resultado = false;
			e.printStackTrace();
		}
		return resultado;
	}

	/**
	 * Metodo que pasa de base de datos a xml, recoge los registros de una base de datos en un arraylist y los carga en un archivo XML
	 * @return true si no hay error, false si hay algun error
	 */
	public boolean BdXml(){
		boolean resultado;
		ArrayList<Log> logs = new ArrayList<Log>();
		try {
			// Se obtiene el documento de configuracion y se carga en memoria, creando una nueva instancia 
			this.documentBF = DocumentBuilderFactory.newInstance();
			this.documentB= documentBF.newDocumentBuilder();
			this.document = this.documentB.newDocument();

			// Si la conexion no es nula  creamos el select en la tabla registro y añadimos 
			// los registros a un array de Log

			if (this.conector!=null){
				this.statment = this.conector.createStatement();
				this.resultSet= this.statment.executeQuery("select * from logs");
				while (this.resultSet.next()){
					logs.add(new Log(resultSet.getString("date"),
							resultSet.getString("logger"),resultSet.getString("level"),resultSet.getString("message")));
				}
				// Se crea la raíz del xml y se añade a documento
				Element raiz = this.document.createElement("log");
				this.document.appendChild(raiz);
				//Se crea un registro por cada elemento en el arraylist Log y se añaden a documento
				for (int i=0;i<logs.size();i++){
					Element registro = this.document.createElement("registro");
					Element date = this.document.createElement("date");
					date.setTextContent(logs.get(i).getDate());
					registro.appendChild(date);
					Element logger = this.document.createElement("logger");
					logger.setTextContent(logs.get(i).getLogger());
					registro.appendChild(logger);
					Element level = this.document.createElement("level");
					level.setTextContent(logs.get(i).getLevel());
					registro.appendChild(level);
					Element mensaje = this.document.createElement("message");
					mensaje.setTextContent(logs.get(i).getLevel());
					registro.appendChild(mensaje);
					raiz.appendChild(registro);
				}
				//lo transformamos para que lo pueda leer un editor de texto
				this.transformerFactory = TransformerFactory.newInstance();
				this.transformer = transformerFactory.newTransformer();
				this.transformer.setOutputProperty(OutputKeys.INDENT,"yes");
				source = new DOMSource(document);
				streamResult = new StreamResult(new File("Logs.xml"));
				transformer.transform(source,streamResult);
			}
			resultado = true;
		}catch (Exception e){
			resultado = false;
			e.printStackTrace();
		}
		return resultado;
	}
}




