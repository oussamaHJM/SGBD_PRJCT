import java.io.File;
import java.io.FileOutputStream;

import java.sql.Blob;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

import org.json.JSONArray;
import org.json.JSONObject;

import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Chunk;
import com.itextpdf.text.Document;
import com.itextpdf.text.Image;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;

public class Pdfgenerator {

	// URL de connexion
	private String url = "jdbc:oracle:thin:@oracle.fil.univ-lille1.fr:1521:filora";
	// Nom du user
	private String user = "elhajjam";
	// Mot de passe de l'utilisateur
	private String passwd = "ozamax1997";
	// Objet Connection
	private static Connection connect;
	
	public static JSONObject obj;
	public static String station_depart;
	public static String station_fin;
	public static JSONArray arr;
	public static List<Object> trajet;
	public static List<String> trajet_str;
	
	private Pdfgenerator() {
		try {
			connect = DriverManager.getConnection(url, user, passwd);
			connect.setAutoCommit(false);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public static Connection getInstance() {
		if (connect == null) {
			new Pdfgenerator();
		}
		return connect;
	}

	public static String callFun(String arg, Connection conn) throws SQLException {
		// Call a function with an IN
		CallableStatement funcin = conn.prepareCall("begin ? := nv_itineraire (?); end;");
		funcin.registerOutParameter(1, Types.CHAR);
		funcin.setString(2, arg);
		funcin.execute();
		return funcin.getString(1);
	}

	private static void addTableHeader(PdfPTable table) {
		Stream.of("Année 1", "Année 2", "Année 3").forEach(columnTitle -> {
			PdfPCell header = new PdfPCell();
			header.setBackgroundColor(BaseColor.LIGHT_GRAY);
			header.setBorderWidth(2);
			header.setPhrase(new Phrase(columnTitle));
			table.addCell(header);
		});
	}

	private static void addRows(String s1, String s2, String s3,PdfPTable table) {
		table.addCell(s1);
		table.addCell(s2);
		table.addCell(s3);
	}


	public static void main(String[] args) throws Exception {
		// TODO Auto-generated method stub
		Document document = new Document();
		PdfWriter.getInstance(document, new FileOutputStream("StudentsList.pdf"));

		ArrayList<String> Resultat_nom = new ArrayList<String>();
		ArrayList<String> Resultat_prenom = new ArrayList<String>();
		ArrayList<Blob> Resultat_photos = new ArrayList<Blob>();
		ArrayList<String> Resultat_adresse = new ArrayList<String>();
		ArrayList<String> Resultat_numero = new ArrayList<String>();
		ArrayList<String> Resultat_email = new ArrayList<String>();
		ArrayList<String> Resultat_cursus = new ArrayList<String>();
		ArrayList<String> Resultat_techno = new ArrayList<String>();
		ArrayList<String> List_start = new ArrayList<String>();
		ArrayList<String> List_end = new ArrayList<String>();

		document.open();

		Connection c = getInstance();
		Statement stmt = c.createStatement();

		
		//System.out.println(station_depart);

		

		String query = "select nom, prenom, extractvalue(cv,'/cv/donnees-personnelles/adresse/text()') as adresse, "
				+ "extractvalue(cv,'/cv/donnees-personnelles/telephone[@type=\"mobile\"]/text()') as numero,"
				+ "extractvalue(cv,'/cv/donnees-personnelles/e-mail/text()') as email, "
				+ " extractvalue(cv,'//formation[1][@date-debut]/titre/text()') as annee1,\n"
				+ " extractvalue(cv,'//formation[2][@date-debut]/titre/text()') as annee2,\n"
				+ " extractvalue(cv,'//formation[3][@date-debut]/titre/text()') as annee3,\n"
				+ "nvl(extractvalue(cv,'//experience[@nom]/environnement-technique/technologie[1][@nom]/@nom'),' ') as techno1,\n"
				+ "nvl(extractvalue(cv,'//experience[@nom]/environnement-technique/technologie[2][@nom]/@nom'),' ') as techno2,\n"
				+ "nvl(extractvalue(cv,'//experience[@nom]/environnement-technique/technologie[3][@nom]/@nom'),' ') as techno3,\n"
				+ "nvl(extractvalue(cv,'//experience[@nom]/environnement-technique/technologie[4][@nom]/@nom'),' ') as techno4,\n"
				+ "nvl(extractvalue(cv,'//experience[@nom]/environnement-technique/technologie[5][@nom]/@nom'),' ') as techno5,\n"
				+ "photo from BOSSUT.vue_etu";
		ResultSet result = stmt.executeQuery(query);
		
		
		while (result.next()) {			
			Resultat_nom.add(result.getString("Nom"));
			Resultat_prenom.add(result.getString("prenom"));
			Resultat_adresse.add(result.getString("adresse"));
			Resultat_numero.add(result.getString("numero"));
			Resultat_email.add(result.getString("email"));
			Resultat_photos.add(result.getBlob("photo"));
			Resultat_cursus.add(result.getString("annee1") + "-" + result.getString("annee2") + "-"
					+ result.getString("annee3"));
			Resultat_techno.add(result.getString("techno1") + "-" + result.getString("techno2") + "-"
					+ result.getString("techno3") + "-" + result.getString("techno4") + "-"
					+ result.getString("techno5"));
			
			
		}
		for (int i = 0; i < Resultat_photos.size(); i++) {
			Paragraph paragraph = new Paragraph("Nom" + ": " + Resultat_nom.get(i));
			Paragraph paragraph1 = new Paragraph("Prénom" + ": " + Resultat_prenom.get(i));
			Paragraph paragraph2 = new Paragraph("adresse" + ": " + Resultat_adresse.get(i));
			Paragraph paragraph3 = new Paragraph("numero" + ": " + Resultat_numero.get(i));
			Paragraph paragraph4 = new Paragraph("email" + ": " + Resultat_email.get(i));
			Paragraph paragraph5 = new Paragraph("Station départ"+": ");
			Paragraph trajet_pr = new Paragraph("");
			
			try {
				File fileOut = new File("img.jpg");
				FileOutputStream fos = new FileOutputStream(fileOut);
				fos.write(Resultat_photos.get(i).getBytes(1, (int) Resultat_photos.get(i).length()));
				fos.close();
				Image image = Image.getInstance("img.jpg");
				image.scaleAbsolute(64, 64);
				document.add(image);
			} catch (Exception e) {
				e.printStackTrace();
			}
			PdfPTable table = new PdfPTable(3);
			addTableHeader(table);
			Paragraph paragraph6 = new Paragraph("technologie" + ":" + "  " + Resultat_techno.get(i));
			String string = Resultat_cursus.get(i);
			String[] parts = string.split("-");
			addRows(parts[0],parts[1], parts[2], table);
			document.add(paragraph);
			document.add(paragraph1);
			document.add(Chunk.NEWLINE);
			document.add(paragraph2);
			document.add(paragraph3);
			document.add(paragraph4);
			document.add(Chunk.NEWLINE);
			document.add(Chunk.NEWLINE);
			document.add(table);
			document.add(paragraph6);
			try {
				obj = new JSONObject(callFun(Resultat_nom.get(i), c));
				//System.out.println(obj.get("station_depart").toString());
				/*List_start.add(obj.getString("Station départ"));
				List_end.add(obj.getString("Station fin"));*/
				arr = obj.getJSONArray("trajet");
				trajet = arr.toList();
				trajet_str = new ArrayList<>(trajet.size());
				/*Chunk start = new Chunk(obj.getString("Station départ"));
				Chunk end = new Chunk(obj.getString("Station fin"));*/
				Phrase trjt = new Phrase();
				/*trjt.add(start);
				trjt.add(Chunk.NEWLINE);
				trjt.add(end);
				trjt.add(Chunk.NEWLINE);*/
				for (Object object : trajet) {
					trajet_str.add(Objects.toString(object, null));
					trjt.add(Objects.toString(object, null));
					trjt.add(Chunk.NEWLINE);
					//System.out.println(Objects.toString(object, null));
				}
				Paragraph p = new Paragraph();
				p.add(trjt);
				System.out.println(trjt.toString());
				document.add(p);
				
			}
			catch(Exception e) {
				
			}
			
			document.add(Chunk.NEXTPAGE);
		}
		document.close();
		c.commit();
		c.close();

		System.out.println("PDF generated successfully");
	}
}
