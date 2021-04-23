import org.neo4j.driver.AuthTokens;
import org.neo4j.driver.Driver;
import org.neo4j.driver.GraphDatabase;
import org.neo4j.driver.Result;
import org.neo4j.driver.Session;
import org.neo4j.driver.Transaction;
import org.neo4j.driver.TransactionWork;

import io.javalin.Javalin;


import static org.neo4j.driver.Values.parameters;

import java.nio.file.*;

public class Principal {
	private static Driver driver;
	public static void main(String[] args) {
		Javalin servidor = Javalin.create().start(7000);
		conectarBanco("bolt://localhost:7687", "neo4j", "ursopanda");
		servidor.get("/importaEmpresas", requisicao -> {
		       importaEmpresas("file:///socios.csv");
		});
		servidor.get("/importaSocios", requisicao -> {
		       importaSocios("file:///socios.csv");
		});
		servidor.get("/importaCursos", requisicao -> {
		       importaCursos("file:///socios.csv");
		});
		servidor.get("/importarArestasSociosEmpresas", requisicao -> {
				importarArestasSociosEmpresas("file:///socios.csv");
		});
		servidor.get("/importaArestasSociosCursos", requisicao -> {
			importaArestasSociosCursos("file:///socios.csv");
		});
	}
	public static void conectarBanco(String uri, String user, String password) {
		driver = GraphDatabase.driver( uri, AuthTokens.basic( user, password ) );
		
	}
	public static void desconectaBanco() {
		driver.close();
	}
	public static void importaEmpresas(final String arquivo) {
		try ( Session session = driver.session() )
        {
            String greeting = session.writeTransaction( new TransactionWork<String>()
            {
                @Override
                public String execute( Transaction tx )
                {
                    Result result = tx.run( "LOAD CSV WITH HEADERS FROM $arquivo AS row\r\n"
                    		+ "MERGE (e:Empresa {Razao_Social: row.RAZAOSOCIAL})\r\n"
                    		+ "ON CREATE \r\n"
                    		+ "SET e.CNPJ = row.CNPJ\r\n"
                    		+ "RETURN count(e);",
                            parameters( "arquivo", arquivo ) );
                    return result.single().get( 0 )+"";
                }
            } );
            System.out.println( greeting );
        }
    }
	public static void importaSocios(final String arquivo) {
		try ( Session session = driver.session() )
        {
            String greeting = session.writeTransaction( new TransactionWork<String>()
            {
                @Override
                public String execute( Transaction tx )
                {
                    Result result = tx.run( "LOAD CSV WITH HEADERS FROM $arquivo AS row\r\n"
                    		+ "MERGE (s:Socio {CPF: row.CPF})\r\n"
                    		+ "ON CREATE \r\n"
                    		+ "SET s.NOME = row.NOME\r\n"
                    		+ "RETURN count(s);",
                            parameters( "arquivo", arquivo ) );
                    return result.single().get( 0 )+"";
                }
            } );
            System.out.println( greeting );
        }
    }
	public static void importaCursos(final String arquivo) {
		try ( Session session = driver.session() )
        {
            String greeting = session.writeTransaction( new TransactionWork<String>()
            {
                @Override
                public String execute( Transaction tx )
                {
                    Result result = tx.run( "LOAD CSV WITH HEADERS FROM $arquivo AS row\r\n"
                    		+ "MERGE (c:Curso {Nome: row.NOMECURSO})\r\n"
                    		+ "RETURN count(c);\r\n",
                            parameters( "arquivo", arquivo ) );
                    return result.single().get( 0 )+"";
                }
            } );
            System.out.println( greeting );
        }
    }
	public static void importarArestasSociosEmpresas(final String arquivo) {
		try ( Session session = driver.session() )
        {
            String greeting = session.writeTransaction( new TransactionWork<String>()
            {
                @Override
                public String execute( Transaction tx )
                {
                    Result result = tx.run( "LOAD CSV WITH HEADERS FROM $arquivo' AS row\r\n"
                    		+ "MATCH (e:Empresa {Razao_Social: row.RAZAOSOCIAL})\r\n"
                    		+ "MATCH (s:Socio {CPF: row.CPF})\r\n"
                    		+ "MERGE (s)-[:É_sócio_de]->(e)\r\n"
                    		+ "RETURN count(e);",
                            parameters( "arquivo", arquivo ) );
                    return result.single().get( 0 )+"";
                }
            } );
            System.out.println( greeting );
        }
    }
	public static void importaArestasSociosCursos(final String arquivo) {
		try ( Session session = driver.session() )
        {
            String greeting = session.writeTransaction( new TransactionWork<String>()
            {
                @Override
                public String execute( Transaction tx )
                {
                    Result result = tx.run( "LOAD CSV WITH HEADERS FROM $arquivo AS row\r\n"
                    		+ "MATCH (c:Curso {Nome: row.NOMECURSO})\r\n"
                    		+ "MATCH (s:Socio {CPF: row.CPF})\r\n"
                    		+ "MERGE (s)-[:estudou_em]->(c)\r\n"
                    		+ "RETURN count(c);",
                            parameters( "arquivo", arquivo ) );
                    return result.single().get( 0 )+"";
                }
            } );
            System.out.println( greeting );
        }
    }	

}
